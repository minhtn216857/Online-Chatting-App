package com.example.minh_messenger_test

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.example.minh_messenger_test.data.source.DataSource
import com.example.minh_messenger_test.data.source.DefaultRepository
import com.example.minh_messenger_test.data.source.Repository
import com.example.minh_messenger_test.data.source.local.DefaultLocalDataSource
import com.example.minh_messenger_test.data.source.local.MessengerDatabase
import com.example.minh_messenger_test.data.source.remote.DefaultRemoteDataSource
import com.example.minh_messenger_test.ui.voicecall.repository.MainRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MessengerApplication: Application() {

    @Inject lateinit var repository: Repository
    lateinit var sharedReference: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        setupSharedPref()
    }

    private fun setupSharedPref() {
        sharedReference = getSharedPreferences(
            getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        )
    }

}