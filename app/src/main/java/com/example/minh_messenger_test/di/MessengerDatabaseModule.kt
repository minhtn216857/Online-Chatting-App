package com.example.minh_messenger_test.di

import android.content.Context
import androidx.room.Room
import com.example.minh_messenger_test.data.source.local.AccountDao
import com.example.minh_messenger_test.data.source.local.MessengerDatabase
import com.example.minh_messenger_test.ui.voicecall.firebaseClient.FirebaseClient
import com.example.minh_messenger_test.ui.voicecall.repository.MainRepository
import com.example.minh_messenger_test.webrtc.WebRTCClient
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object MessengerDatabaseModule {

    @Provides
    @Singleton
    fun provideMainRepository(
        gson: Gson,
        webRTCClient: WebRTCClient,
        firebaseClient: FirebaseClient
    ): MainRepository {
        return MainRepository(gson, webRTCClient,firebaseClient)
    }

    @Provides
    @Singleton
    fun provideMessengerDatabase(@ApplicationContext context: Context): MessengerDatabase{
        return Room.databaseBuilder(
            context.applicationContext,
            MessengerDatabase::class.java,
            "messenger_db"
        ).fallbackToDestructiveMigrationFrom()
            .build()
    }

    @Provides
    fun provideContext(@ApplicationContext context:Context) : Context = context.applicationContext

    @Provides
    fun provideAccountDao(database: MessengerDatabase): AccountDao{
        return database.getAccountDao()
    }

    @Provides
    fun provideDatabaseInstance(): FirebaseDatabase = FirebaseDatabase.getInstance()

    @Provides
    fun provideDatabaseReference(db: FirebaseDatabase):DatabaseReference = db.reference

    @Provides
    fun provideGson(): Gson = Gson()
}