package com.example.minh_messenger_test.webrtc

import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

open class MySdpObserver: SdpObserver {
    override fun onCreateSuccess(desc: SessionDescription?) {
        TODO("Not yet implemented")
    }

    override fun onSetSuccess() {
        TODO("Not yet implemented")
    }

    override fun onCreateFailure(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun onSetFailure(p0: String?) {
        TODO("Not yet implemented")
    }
}