package com.example.minh_messenger_test.utils

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.permissionx.guolindev.PermissionX

fun AppCompatActivity.getCameraAndMicPermission(success: () -> Unit){
    PermissionX.init(this)
        .permissions(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.CAMERA)
        .request{allGranted, _, _ ->
            if(allGranted){
                success()
            }else{
                Toast.makeText(this,
                    "camera and mic permission is required",
                    Toast.LENGTH_SHORT)
                    .show()
            }
        }
}