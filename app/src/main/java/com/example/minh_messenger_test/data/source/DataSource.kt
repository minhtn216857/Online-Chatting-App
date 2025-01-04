package com.example.minh_messenger_test.data.source

import com.example.minh_messenger_test.data.model.Account

interface DataSource {
    interface RemoteDataSource{
        suspend fun createAccount(account: Account): String
        suspend fun updateAccount(account: Account): Boolean
        suspend fun login(account: Account): Account?
        abstract suspend fun loadFriendAccounts(username: String): List<Account>?
    }

    interface LocalDataSource{
        suspend fun createAccount(account: Account)

        suspend fun updateAccount(account: Account)

        suspend fun deleteAccount(account: Account)

        suspend fun getSingleAccount(username: String): Account?
    }
}