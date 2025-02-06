package com.example.minh_messenger_test.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessengerService: FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
//        Log.e("TOKEN", token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
    }
}