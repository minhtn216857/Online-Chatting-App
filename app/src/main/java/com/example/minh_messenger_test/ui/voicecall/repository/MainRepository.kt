package com.example.minh_messenger_test.ui.voicecall.repository

import android.util.Log
import android.view.SurfaceView
import com.example.minh_messenger_test.data.model.Account
import com.example.minh_messenger_test.data.model.AccountStatus
import com.example.minh_messenger_test.ui.voicecall.firebaseClient.FirebaseClient
import com.example.minh_messenger_test.utils.DataModel
import com.example.minh_messenger_test.utils.DataModelType
import com.example.minh_messenger_test.webrtc.MyPeerObserver
import com.example.minh_messenger_test.webrtc.WebRTCClient
import com.google.gson.Gson
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepository @Inject constructor(
    private val gson: Gson,
    private val webRTCClient: WebRTCClient,
    private val firebaseClient: FirebaseClient
):WebRTCClient.Listener {
    var listener: Listener? = null
    private var target: String? = null
    private var username: String? = null
    private var remoteView: SurfaceViewRenderer? = null

    fun initFirebase(username: String){
        Log.d("MainRepository", "initFirebase() called") // 🔥 Debug log
        firebaseClient.subscribeForLatestEvent(username, object : FirebaseClient.Listener {
            override fun onLatestEventReceived(event: DataModel) {
                listener?.onLatestEventReceived(event)
                when (event.type) {
                    DataModelType.Offer->{
                        webRTCClient.onRemoteSessionReceived(
                            SessionDescription(
                                SessionDescription.Type.OFFER,
                                event.data.toString()
                            )
                        )
                        webRTCClient.answer(target!!)
                    }
                    DataModelType.Answer->{
                        webRTCClient.onRemoteSessionReceived(
                            SessionDescription(
                                SessionDescription.Type.ANSWER,
                                event.data.toString()
                            )
                        )
                    }
                    DataModelType.IceCandidates->{
                        val candidate: IceCandidate? = try {
                            gson.fromJson(event.data.toString(),IceCandidate::class.java)
                        }catch (e:Exception){
                            null
                        }
                        candidate?.let {
                            webRTCClient.addIceCandidateToPeer(it)
                        }
                    }
                    DataModelType.EndCall->{
                        listener?.endCall()
                    }
                    else -> Unit
                }

            }
        })
    }

    fun sendConnectionRequest(sender: String, target: String, isVideoCall: Boolean, success: (Boolean) ->Unit) {
        firebaseClient.sendMessageToOtherClient(
            sender,
            DataModel(
                type = if(isVideoCall) DataModelType.StartVideoCall else DataModelType.StartAudioCall,
                target = target
            ),success
        )
    }

    fun setTarget(target: String?) {
        this.target = target
    }

    fun setUsername(username: String){
        this.username = username
    }

    fun initWebRTCClient(username: String){
        webRTCClient.listener = this
        webRTCClient.initializeWebrtcClient(username, object : MyPeerObserver() {
            override fun onAddStream(p0: MediaStream?) {
                super.onAddStream(p0)
                try {
                    p0?.videoTracks?.get(0)?.addSink(remoteView)
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }

            override fun onIceCandidate(p0: IceCandidate?) {
                super.onIceCandidate(p0)
                p0?.let{
                    webRTCClient.sendIceCandidate(target!!, it)
                }
            }

            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                super.onConnectionChange(newState)
                if(newState == PeerConnection.PeerConnectionState.CONNECTED){
                    //1. change my status to IN_CALL
                    changeMyStatus(username, AccountStatus.IN_CALL)

                    //2. clear latest event inside my user section in firebase database
                    firebaseClient.clearLatestEvent(username)
                }
            }
        })
    }

    private fun changeMyStatus(username: String, status: AccountStatus) {
        firebaseClient.changeMyStatus(username, status)
    }

    fun initLocalSurfaceView(view: SurfaceViewRenderer, isVideoCall: Boolean){
        webRTCClient.initLocalSurfaceView(view, isVideoCall)
    }

    fun initRemoteSurfaceView(view: SurfaceViewRenderer){
        webRTCClient.initRemoteSurfaceView(view)
        this.remoteView = view
    }

    fun startCall(){
        webRTCClient.call(target!!)
    }

    fun endCall(){
        webRTCClient.closeConnection()
        firebaseClient.changeMyStatus(username!!, AccountStatus.ONLINE)
    }

    fun sendEndCall(){
        onTransferEventToSocket(DataModel(
            type = DataModelType.EndCall,
            target = target!!
        ))
    }

    fun switchCamera(){
        webRTCClient.switchCamera()
    }

    fun toggleAudio(shouldBeMuted: Boolean){
        webRTCClient.toggleAudio(shouldBeMuted)
    }

    fun toggleVideo(shouldBeMuted: Boolean){
        webRTCClient.toggleVideo(shouldBeMuted)
    }

    interface Listener{
        fun onLatestEventReceived(data: DataModel)
        fun endCall()
    }

    override fun onTransferEventToSocket(data: DataModel) {
        firebaseClient.sendMessageToOtherClient(username!!, data){}
    }
}