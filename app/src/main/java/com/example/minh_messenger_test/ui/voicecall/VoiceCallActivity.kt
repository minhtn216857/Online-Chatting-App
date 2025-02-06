package com.example.minh_messenger_test.ui.voicecall

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.minh_messenger_test.MessengerApplication
import com.example.minh_messenger_test.R
import com.example.minh_messenger_test.databinding.ActivityVoiceCallBinding
import com.example.minh_messenger_test.service.MainService
import com.example.minh_messenger_test.service.MainServiceRepository
import com.example.minh_messenger_test.ui.login.LoginViewModel
import com.example.minh_messenger_test.ui.login.LoginViewModelFactory
import com.example.minh_messenger_test.utils.convertToHumanTime
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class VoiceCallActivity : AppCompatActivity(), MainService.EndCallListener {

    private lateinit var binding: ActivityVoiceCallBinding

    private var target: String? = null
    private var isVideoCall: Boolean = true
    private var isCaller:Boolean = true
    private var isMicrophoneMuted: Boolean = false
    private var isCameraMuted: Boolean = false

    @Inject lateinit var serviceRepository: MainServiceRepository
    private lateinit var loginViewModel: LoginViewModel
    private var username: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVoiceCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViewModel()
        init()
        }

    private fun setupViewModel(){
        val repository = (this.application as MessengerApplication).repository
        loginViewModel = ViewModelProvider(
            this,
            LoginViewModelFactory(repository)
        )[LoginViewModel::class.java]
        username = LoginViewModel.currentAccount.value?.username
        if (username == null) {
            Log.e("VoiceCallFragment", "Username is null! Không thể tiếp tục")
            return  // Dừng lại nếu username chưa được khởi tạo
        }
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

            endCallButton.setOnClickListener {
                serviceRepository.sendEndCall()
            }
            switchCameraButton.setOnClickListener {
                serviceRepository.switchCamera()
            }
            setupMicToggleClicked()
            setupCameraToggleClicked()

        }
        MainService.endCallListener = this

    }

    private fun setupCameraToggleClicked() {
        binding.apply {
            toggleCameraButton.setOnClickListener {
                if(!isCameraMuted){
                    serviceRepository.toggleCamera(true)
                    toggleCameraButton.setImageResource(R.drawable.ic_camera_on)
                }else{
                    serviceRepository.toggleCamera(false)
                    toggleCameraButton.setImageResource(R.drawable.ic_camera_off)
                }
                isCameraMuted = !isCameraMuted
            }
        }
    }

    private fun setupMicToggleClicked() {
        binding.apply {
            toggleMicrophoneButton.setOnClickListener {
                if(!isMicrophoneMuted){
                    serviceRepository.toggleMicrophone(true)
                    toggleMicrophoneButton.setImageResource(R.drawable.ic_mic_on)
                }else{
                    serviceRepository.toggleMicrophone(false)
                    toggleMicrophoneButton.setImageResource(R.drawable.ic_mic_off)
                }
                isMicrophoneMuted = !isMicrophoneMuted
            }
        }
    }


    override fun onCallEnded() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        MainService.remoteSurfaceView?.release()
        MainService.remoteSurfaceView = null
        MainService.localSurfaceView?.release()
        MainService.remoteSurfaceView = null
    }
}