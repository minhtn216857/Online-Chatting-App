package com.example.minh_messenger_test.data.source.local

import com.example.minh_messenger_test.data.model.Account
import com.example.minh_messenger_test.data.source.DataSource

class DefaultLocalDataSource(
    database: MessengerDatabase
): DataSource.LocalDataSource {
    private val accountDao = database.getAccountDao()

    override suspend fun createAccount(account: Account) {
        return accountDao.insert(account)
    }

    override suspend fun updateAccount(account: Account) {
        return accountDao.update(account)
    }

    override suspend fun deleteAccount(account: Account) {
        return accountDao.delete(account)
    }

    override suspend fun getSingleAccount(username: String): Account? {
        return accountDao.findSingleAccount(username)
    }
}