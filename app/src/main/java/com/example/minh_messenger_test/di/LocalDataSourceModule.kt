package com.example.minh_messenger_test.di

import com.example.minh_messenger_test.data.source.DataSource
import com.example.minh_messenger_test.data.source.local.DefaultLocalDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalDataSourceModule {

    @Binds
    @Singleton
    abstract fun bindLocalDataSource(
        defaultLocalDataSource: DefaultLocalDataSource
    ): DataSource.LocalDataSource
}
