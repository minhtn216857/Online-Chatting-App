package com.example.minh_messenger_test.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.minh_messenger_test.R
import com.example.minh_messenger_test.ui.voicecall.repository.MainRepository
import com.example.minh_messenger_test.utils.DataModel
import com.example.minh_messenger_test.utils.DataModelType
import com.example.minh_messenger_test.utils.isValid
import dagger.hilt.android.AndroidEntryPoint
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject

@AndroidEntryPoint
class MainService : Service(), MainRepository.Listener {

    private val TAG = "MainService"
    private var isServiceRunning = false
    private lateinit var notificationManager: NotificationManager
    @Inject lateinit var mainRepository: MainRepository
    private var username: String? = null

    companion object {
        var listener: Listener? = null
        var localSurfaceView: SurfaceViewRenderer? = null
        var remoteSurfaceView: SurfaceViewRenderer? = null
        var endCallListener: EndCallListener? = null
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(
            NotificationManager::class.java)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { incomingIntent ->
            when (incomingIntent.action) {
                MainServiceActions.START_SERVICE.name -> handleStartService(incomingIntent)
                MainServiceActions.SETUP_VIEWS.name -> handleSetupViews(incomingIntent)
                MainServiceActions.END_CALL.name -> handleEndCall()
                MainServiceActions.SWITCH_CAMERA.name -> handleSwitchCamera()
                MainServiceActions.TOGGLE_MICROPHONE.name -> handleToggleMicrophone(incomingIntent)
                MainServiceActions.TOGGLE_CAMERA.name -> handleToggleCamera(incomingIntent)
            }
        }
        return START_STICKY
    }

    private fun handleToggleCamera(incomingIntent: Intent) {
        val shouldBeMuted = incomingIntent.getBooleanExtra("shouldBeMuted", true)
        mainRepository.toggleVideo(shouldBeMuted)
    }

    private fun handleToggleMicrophone(incomingIntent: Intent) {
        val shouldBeMuted = incomingIntent.getBooleanExtra("shouldBeMuted", true)
        mainRepository.toggleAudio(shouldBeMuted)
    }

    private fun handleSwitchCamera() {
        mainRepository.switchCamera()
    }

    private fun handleSetupViews(incomingIntent: Intent){
        val isCaller = incomingIntent.getBooleanExtra("isCaller", false)
        val target = incomingIntent.getStringExtra("target")
        val isVideoCall = incomingIntent.getBooleanExtra("isVideoCall", true)

        Log.d("WebRTC1", "📞 handleSetupViews: isCaller=$isCaller, target=$target, isVideoCall=$isVideoCall")

        mainRepository.setTarget(target)

        mainRepository.initLocalSurfaceView(localSurfaceView!!, isVideoCall)
        mainRepository.initRemoteSurfaceView(remoteSurfaceView!!)
        if (!isCaller) {
            mainRepository.startCall()
        }
    }



    private fun handleStartService(incomingIntent: Intent) {
        username = incomingIntent.getStringExtra("username")
        if (!isServiceRunning) {
            isServiceRunning = true
            startServiceWithNotifications()
            //setup
            mainRepository.listener = this
            Log.d(TAG, "${username}")
            mainRepository.initFirebase(username!!)
            mainRepository.initWebRTCClient(username!!)
        }
    }

    private fun handleEndCall() {
        mainRepository.sendEndCall()
        endCallAndRestartRepository()
    }

    private fun endCallAndRestartRepository(){
        mainRepository.endCall()
        endCallListener?.onCallEnded()
        mainRepository.initWebRTCClient(username!!)
    }

    private fun startServiceWithNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "channel1", "Foreground Service", NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(notificationChannel)

            val notification = NotificationCompat.Builder(this, "channel1")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Messenger Service")
                .setContentText("Service is running...")
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            startForeground(1, notification.build())
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onLatestEventReceived(data: DataModel) {
        Log.d("FirebaseClient", "🔥 Data received in MainService: $data")
        if(data.isValid()){
            when(data.type){
                DataModelType.StartVideoCall,
                DataModelType.StartAudioCall -> {
                        listener?.onCallReceived(data)
                    }
                else -> Unit
            }
        }
    }

    override fun endCall() {
        endCallAndRestartRepository()
    }

    interface Listener{
        fun onCallReceived(model: DataModel)
    }

    interface EndCallListener{
        fun onCallEnded()
    }
}
