package com.example.minh_messenger_test.data.source

import com.example.minh_messenger_test.data.model.Account

class DefaultRepository(
    private val localDataSource: DataSource.LocalDataSource,
    private val remoteDataSource: DataSource.RemoteDataSource
): Repository.RemoteRepository, Repository.LocalRepository {
    override suspend fun loginAccount(account: Account): Account? {
        return remoteDataSource.login(account)
    }

    override suspend fun createAccount(account: Account): String {
        return remoteDataSource.createAccount(account)
    }

    override suspend fun updateAccount(account: Account): Boolean {
        return remoteDataSource.updateAccount(account)
    }

    override suspend fun loadFriendAccounts(username: String): List<Account>? {
        return remoteDataSource.loadFriendAccounts(username)
    }

    override suspend fun insertAccount(account: Account) {
        localDataSource.createAccount(account)
    }

    override suspend fun deleteAccount(account: Account) {
        localDataSource.deleteAccount(account)
    }

    override suspend fun updateLocalAccount(account: Account) {
        localDataSource.updateAccount(account)
    }

    override suspend fun getAccount(username: String): Account? {
        return localDataSource.getSingleAccount(username)
    }
}