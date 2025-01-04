package com.example.minh_messenger_test.data.model

import com.example.minh_messenger_test.ui.home.HomeViewModel
import com.example.minh_messenger_test.ui.login.LoginViewModel
import java.util.Date


class Mesagge(
    val id: Long,
    val sender: String,
    val receiver: String,
    val data: Data,
    val notification: Notification,
    val timestamp: Long = Date().time,
    val status: MessageStatus,
    val token: String? = null
) {
    val isIncoming: Boolean
        get() = LoginViewModel.currentAccount.value?.username?.compareTo(sender) != 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Mesagge

        if (sender != other.sender) return false
        if (receiver != other.receiver) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sender.hashCode()
        result = 31 * result + receiver.hashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}

enum class MessageStatus{
    SENT,
    SEEN,
    SENDING,
    FAILED
}

data class Data(
    val text: String = "",
    val photoUrl: String? = null,
    val photoMimeType: String? = null
){}

data class Notification(
    val title: String = "",
    val body: String? = null
)