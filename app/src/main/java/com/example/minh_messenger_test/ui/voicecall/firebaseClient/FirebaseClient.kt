package com.example.minh_messenger_test.ui.voicecall.firebaseClient

import android.util.Log
import com.example.minh_messenger_test.ui.voicecall.firebaseClient.FirebaseFieldNames.LATEST_EVENT
import com.example.minh_messenger_test.ui.login.LoginViewModel
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

    private val currentAccount: String? = null

    fun subscribeForLatestEvent(username: String, listener: Listener){
        try {
//            val currentAccount = LoginViewModel.currentAccount.value?.username
            val currentAccount = username

            Log.d("FirebaseClient", "Subscribing for latest events for user: $currentAccount") // 🔥 Debug log

            databaseRef.child(currentAccount).child(LATEST_EVENT).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d("FirebaseClient", "DataSnapshot received: $snapshot") // 🔥 Debug log

                        val event: DataModel? = try {
                            gson.fromJson(snapshot.value.toString(), DataModel::class.java)
                        } catch (e: Exception) {
                            Log.e("FirebaseClient", "Error parsing DataModel: ${e.message}")
                            null
                        }

                        event?.let {
                            Log.d("FirebaseClient", "Parsed event: $it") // 🔥 Debug log
                            listener.onLatestEventReceived(it)
                        } ?: Log.e("FirebaseClient", "Event is null")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FirebaseClient", "Firebase subscription cancelled: ${error.message}")
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("FirebaseClient", "Error in subscribeForLatestEvent: ${e.message}")
        }
    }


    fun sendMessageToOtherClient(message: DataModel, success: (Boolean) ->Unit){
        val convertedMessage = gson.toJson(message.copy(sender = currentAccount))
        databaseRef.child(message.target).child(LATEST_EVENT).setValue(convertedMessage)
            .addOnSuccessListener {
                success(true)
            }
            .addOnFailureListener{
                success(false)
            }
    }



    interface Listener{
        fun onLatestEventReceived(event: DataModel)
    }
}