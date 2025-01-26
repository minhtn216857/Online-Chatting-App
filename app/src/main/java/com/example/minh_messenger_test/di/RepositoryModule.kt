package com.example.minh_messenger_test.di

import com.example.minh_messenger_test.data.source.DefaultRepository
import com.example.minh_messenger_test.data.source.Repository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindRepository(
        defaultRepository: DefaultRepository
    ): Repository
}
