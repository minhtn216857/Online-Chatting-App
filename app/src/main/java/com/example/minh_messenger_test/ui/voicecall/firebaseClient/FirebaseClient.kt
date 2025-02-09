package com.example.minh_messenger_test.ui.voicecall.firebaseClient

import android.util.Log
import com.example.minh_messenger_test.data.model.AccountStatus
import com.example.minh_messenger_test.ui.voicecall.firebaseClient.FirebaseFieldNames.LATEST_EVENT
import com.example.minh_messenger_test.ui.login.LoginViewModel
import com.example.minh_messenger_test.ui.voicecall.firebaseClient.FirebaseFieldNames.STATUS
import com.example.minh_messenger_test.utils.DataModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseClient @Inject constructor(
    private val databaseRef: DatabaseReference,
    private val gson: Gson
){

    fun subscribeForLatestEvent(username: String, listener: Listener) {
        try {
            Log.d("FirebaseClient", "Subscribing for latest events for user: $username") // Debug log

            databaseRef.child(username).child(LATEST_EVENT).addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val eventJson = snapshot.value?.toString()
                        val event: DataModel? = try {
                            gson.fromJson(eventJson, DataModel::class.java)
                        } catch (e: Exception) {
                            null
                        }

                        event?.let {
                            listener.onLatestEventReceived(it)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("FirebaseClient", "🔥 Error in subscribeForLatestEvent: ${e.message}")
        }
    }


    fun sendMessageToOtherClient(username: String,message: DataModel, success: (Boolean) -> Unit) {
        val convertedMessage = gson.toJson(message.copy(sender = username))
        databaseRef.child(message.target).child(LATEST_EVENT).setValue(convertedMessage)
            .addOnCompleteListener {
                success(true)
            }
            .addOnFailureListener { e ->
                success(false)
            }

    }

    fun changeMyStatus(username: String, status: AccountStatus) {
        databaseRef.child(username).child(STATUS).setValue(status.name)
    }

    fun clearLatestEvent(username: String) {
        databaseRef.child(username).child(LATEST_EVENT).setValue(null)
    }


    interface Listener{
        fun onLatestEventReceived(event: DataModel)
    }
}