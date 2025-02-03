package com.example.minh_messenger_test.data.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.minh_messenger_test.data.model.Account


@Database(entities = [Account::class], version = 1)
abstract class MessengerDatabase: RoomDatabase() {
    abstract fun getAccountDao(): AccountDao

}