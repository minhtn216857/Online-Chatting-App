package com.example.minh_messenger_test.ui.voicecall.repository

import android.util.Log
import com.example.minh_messenger_test.ui.voicecall.firebaseClient.FirebaseClient
import com.example.minh_messenger_test.utils.DataModel
import com.example.minh_messenger_test.utils.DataModelType
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepository @Inject constructor(
    private val gson: Gson,
    private val firebaseClient: FirebaseClient
) {
    var listener: Listener? = null
    private var target: String? = null

    fun initFirebase(username: String){
        Log.d("MainRepository", "initFirebase() called") // 🔥 Debug log

        firebaseClient.subscribeForLatestEvent(username, object : FirebaseClient.Listener {
            override fun onLatestEventReceived(event: DataModel) {
                Log.d("MainRepository", "Event received: $event") // 🔥 Debug log

                listener?.onLatestEventReceived(event) ?: Log.e("MainRepository", "Listener is NULL")
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

    interface Listener{
        fun onLatestEventReceived(data: DataModel)
    }
}