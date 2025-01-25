package com.example.minh_messenger_test.di

import com.example.minh_messenger_test.data.source.DataSource
import com.example.minh_messenger_test.data.source.remote.DefaultRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindRemoteDataSource(
        defaultRemoteDataSource: DefaultRemoteDataSource
    ): DataSource.RemoteDataSource
}