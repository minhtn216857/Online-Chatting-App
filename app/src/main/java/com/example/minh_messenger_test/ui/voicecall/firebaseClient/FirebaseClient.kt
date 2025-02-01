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

//    fun subscribeForLatestEvent(username: String, listener: Listener){
//        try {
////            val currentAccount = LoginViewModel.currentAccount.value?.username
//            val currentAccount = username
//            Log.d("FirebaseClient", "Subscribing for latest events for user: $currentAccount") // 🔥 Debug log
//
//            databaseRef.child(currentAccount).child(LATEST_EVENT).addValueEventListener(
//                object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        Log.d("FirebaseClient", "DataSnapshot received: $snapshot") // 🔥 Debug log
//                        val event: DataModel? = try {
//                            gson.fromJson(snapshot.value.toString(), DataModel::class.java)
//                        } catch (e: Exception) {
//                            null
//                        }
//
//                        event?.let {
//                            Log.d("FirebaseClient", "Parsed event: $it") // 🔥 Debug log
//                            listener.onLatestEventReceived(it)
//                        }
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//                        Log.e("FirebaseClient", "Firebase subscription cancelled: ${error.message}")
//                    }
//                }
//            )
//        } catch (e: Exception) {
//            Log.e("FirebaseClient", "Error in subscribeForLatestEvent: ${e.message}")
//        }
//    }
    fun subscribeForLatestEvent(username: String, listener: Listener) {
        try {
            Log.d("FirebaseClient", "Subscribing for latest events for user: $username") // Debug log

            databaseRef.child(username).child(LATEST_EVENT).addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d("FirebaseClient", "📥 DataSnapshot received: $snapshot")

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
//                        Log.e("FirebaseClient", "❌ Firebase subscription cancelled: ${error.message}")
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("FirebaseClient", "🔥 Error in subscribeForLatestEvent: ${e.message}")
        }
    }


    fun sendMessageToOtherClient(username: String,message: DataModel, success: (Boolean) -> Unit) {
        val convertedMessage = gson.toJson(message.copy(sender = username))
        Log.d("FirebaseClient", "📤 Sending message to: ${message.target}, Data: $convertedMessage") // Debug log

        databaseRef.child(message.target).child(LATEST_EVENT).setValue(convertedMessage)
            .addOnCompleteListener {
                Log.d("FirebaseClient", "✅ Message sent successfully")
                success(true)
            }
            .addOnFailureListener { e ->
                success(false)
            }

    }


    interface Listener{
        fun onLatestEventReceived(event: DataModel)
    }
}