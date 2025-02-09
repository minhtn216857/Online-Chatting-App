package com.example.minh_messenger_test.service

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainServiceRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun startService(username: String) {
        Thread{
            Log.d("MainServiceRepository", "Starting service with username: $username")

            val intent = Intent(context, MainService::class.java).apply {
                putExtra("username", username)
                action = MainServiceActions.START_SERVICE.name
            }
            Log.d("MainServiceRepository", "Intent details: action=${intent.action}, extras=${intent.extras}")
            startServiceIntent(intent)
        }.start()
    }

    fun sendEndCall(){
        val intent = Intent(context, MainService::class.java)
        intent.apply {
            action = MainServiceActions.END_CALL.name
        }
        startServiceIntent(intent)

    }

    fun setupViews(videoCall: Boolean, caller: Boolean, target: String?) {
        val intent = Intent(context, MainService::class.java)
        intent.apply {
            action = MainServiceActions.SETUP_VIEWS.name
            putExtra("isVideoCall", videoCall)
            putExtra("isCaller", caller)
            putExtra("target", target)
        }
        startServiceIntent(intent)
    }

    private fun startServiceIntent(intent: Intent){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            context.startForegroundService(intent)
        }else{
            context.startService(intent)
        }
    }

    fun switchCamera() {
        val intent = Intent(context, MainService::class.java)
        intent.apply {
            action = MainServiceActions.SWITCH_CAMERA.name
        }
        startServiceIntent(intent)

    }

    fun toggleMicrophone(shouldBeMuted: Boolean) {
        val intent = Intent(context, MainService::class.java)
        intent.apply {
            action  = MainServiceActions.TOGGLE_MICROPHONE.name
            putExtra("shouldBeMuted", shouldBeMuted)
        }
        startServiceIntent(intent)
    }

    fun toggleCamera(shouldBeMuted: Boolean) {
        val intent = Intent(context, MainService::class.java)
        intent.apply {
            action  = MainServiceActions.TOGGLE_CAMERA.name
            putExtra("shouldBeMuted", shouldBeMuted)
        }
        startServiceIntent(intent)
    }


}