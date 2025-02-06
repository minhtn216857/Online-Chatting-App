package com.example.minh_messenger_test.ui.voicecall.repository

import android.util.Log
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
): WebRTCClient.Listener {
    var listener: Listener? = null
    private var target: String? = null
    private var username: String? = null
    private var remoteView: SurfaceViewRenderer? = null

    private fun setUsername(username: String){
        this.username = username
    }

    fun initFirebase(username: String){
        setUsername(username)
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

    fun initWebRTCClient(username: String){
        webRTCClient.listener = this
        webRTCClient.initializeWebrtcClient(username, object : MyPeerObserver() {
            override fun onAddStream(mediaStream: MediaStream?) {
                super.onAddStream(mediaStream)
                try {
                    if (mediaStream?.videoTracks?.isNotEmpty() == true) {
                        Log.d("WebRTC1", "✅ Nhận Video Stream từ Remote")
                        mediaStream.videoTracks.get(0)?.addSink(remoteView)
                    } else {
                        Log.e("WebRTC1", "❌ Không có Video Stream từ Remote!")
                    }
                } catch (e: Exception) {
                    Log.e("WebRTC1", "Lỗi trong onAddStream: ${e.message}")
                }
            }

            override fun onIceCandidate(p0: IceCandidate?) {
                super.onIceCandidate(p0)
                if (p0 != null) {
                    Log.d("WebRTC1", "📡 Gửi ICE Candidate: $p0")
                    webRTCClient.sendIceCandidate(target!!, p0)
                } else {
                    Log.e("WebRTC1", "❌ ICE Candidate bị null!")
                }
            }


            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                Log.d("WebRTC1", "📶 ICE Connection State: $newState")
                if (newState == PeerConnection.PeerConnectionState.CONNECTED) {
                    Log.d("WebRTC1", "✅ Kết nối thành công!")
                    changeMyStatus(username, AccountStatus.IN_CALL)
                    firebaseClient.clearLatestEvent(username)
                } else if (newState == PeerConnection.PeerConnectionState.FAILED) {
                    Log.e("WebRTC1", "❌ Kết nối thất bại!")
                }
            }

        })
    }

    fun changeMyStatus(username: String?, status: AccountStatus) {
        if (username == null) {
            Log.e("MainRepository", "❌ Lỗi: Username chưa được gán!")
            return
        }
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