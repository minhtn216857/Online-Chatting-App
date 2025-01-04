package com.example.minh_messenger_test.data.source

import com.example.minh_messenger_test.data.model.Account

// Định nghĩa interface Repository
interface Repository {
    interface RemoteRepository : Repository {
        // Hàm loginAccount để đăng nhập tài khoản, trả về đối tượng Account hoặc null
        suspend fun loginAccount(account: Account): Account?

        // Hàm createAccount để tạo tài khoản, trả về chuỗi kết quả
        suspend fun createAccount(account: Account): String

        // Hàm updateAccount để cập nhật tài khoản, trả về boolean kết quả
        suspend fun updateAccount(account: Account): Boolean

        suspend fun loadFriendAccounts(username: String): List<Account>?
    }

    interface LocalRepository : Repository {
        suspend fun insertAccount(account: Account)
        suspend fun deleteAccount(account: Account)
        suspend fun updateLocalAccount(account: Account)
        suspend fun getAccount(username: String): Account?
    }
}