package com.example.minh_messenger_test.webrtc

import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

open class MySdpObserver : SdpObserver {
    override fun onCreateSuccess(desc: SessionDescription?) {
        // Log khi tạo thành công Offer hoặc Answer
        println("✅ SDP Offer/Answer created: ${desc?.description}")
    }

    override fun onSetSuccess() {
        println("✅ SDP set successfully")
    }

    override fun onCreateFailure(error: String?) {
        println("❌ SDP Offer/Answer failed: $error")
    }

    override fun onSetFailure(error: String?) {
        println("❌ SDP set failed: $error")
    }
}
