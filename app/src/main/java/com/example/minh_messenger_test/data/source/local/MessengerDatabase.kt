package com.example.minh_messenger_test.data.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.minh_messenger_test.data.model.Account


@Database(entities = [Account::class], version = 1)
abstract class MessengerDatabase: RoomDatabase() {
    abstract fun getAccountDao(): AccountDao

//    companion object{
//        @Volatile
//        private var instance: MessengerDatabase? = null
//
//        fun getDatabase(context: Context): MessengerDatabase{
//            return instance ?: synchronized(this){
//                val db = Room.databaseBuilder(
//                    context.applicationContext,
//                    MessengerDatabase::class.java,
//                    "messenger_db"
//                ).fallbackToDestructiveMigrationFrom()
//                    .build()
//                db
//            }
//        }
//    }
}