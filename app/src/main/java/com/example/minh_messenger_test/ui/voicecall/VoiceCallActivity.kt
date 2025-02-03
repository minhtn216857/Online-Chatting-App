package com.example.minh_messenger_test.ui.voicecall

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.minh_messenger_test.R
import com.example.minh_messenger_test.databinding.ActivityVoiceCallBinding
import com.example.minh_messenger_test.service.MainService
import com.example.minh_messenger_test.service.MainServiceRepository
import com.example.minh_messenger_test.utils.convertToHumanTime
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class VoiceCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVoiceCallBinding
    private var target: String? = null
    private var isVideoCall: Boolean = true
    private var isCaller:Boolean = true
    @Inject lateinit var serviceRepository: MainServiceRepository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVoiceCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        }

    private fun init(){
        intent?.getStringExtra("target")?.let {
            this.target = it
        }?: kotlin.run {
            finish()
        }
        isVideoCall = intent.getBooleanExtra("isVideoCall", true)
        isCaller = intent.getBooleanExtra("isCaller", true)

        binding.apply {
            callTitleTv.text = "In call with ${target}"
            CoroutineScope(Dispatchers.IO).launch{
                for(i in 0..3600){
                    delay(1000)
                    withContext(Dispatchers.Main){
                        callTimerTv.text = i.convertToHumanTime()
                    }
                }
            }
            if(!isVideoCall){
                toggleCameraButton.isVisible = false
                switchCameraButton.isVisible = false
                screenShareButton.isVisible = false
            }
            MainService.localSurfaceView = localView
            MainService.remoteSurfaceView = remoteView
            serviceRepository.setupViews(isVideoCall, isCaller, target)
        }

    }

}