package com.example.minh_messenger_test.data.source.remote

import com.example.minh_messenger_test.data.model.Account
import com.example.minh_messenger_test.data.source.DataSource
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Lớp DefaultRemoteDataSource kế thừa từ DataSource.RemoteDataSource
class DefaultRemoteDataSource : DataSource.RemoteDataSource {

    // Hàm createAccount để tạo tài khoản mới, trả về một chuỗi kết quả
    override suspend fun createAccount(account: Account): String {
        // URL cơ sở cho dịch vụ tạo tài khoản
        val baseUrl = "https://createaccount-pxgdcdndsa-uc.a.run.app"
        // Tạo retrofit service từ URL cơ sở
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)
        // Gọi phương thức createAccount từ MessageService
        val result = retrofit.createAccount(account)
        // Trả về "Success" nếu yêu cầu thành công, ngược lại trả về thông báo lỗi
        return if(result.isSuccessful){
            val responseObj = result.body()
            if(responseObj != null){
                if(responseObj.success){
                    "Success"
                }else{
                    responseObj.error!!
                }
            }else{
                "null"
            }
        } else {
            result.body()?.error!!
        }
    }

    // Hàm updateAccount để cập nhật thông tin tài khoản, trả về boolean
    override suspend fun updateAccount(account: Account): Boolean {
        // URL cơ sở cho dịch vụ cập nhật tài khoản
        val baseUrl = "https://updateaccount-pxgdcdndsa-uc.a.run.app"
        // Tạo retrofit service từ URL cơ sở
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)
        // Gọi phương thức updateAccount từ MessageService
        val result = retrofit.updateAccount(account)
        // Trả về kết quả yêu cầu (thành công hay không)
        return result.isSuccessful
    }

    // Hàm login để đăng nhập, trả về đối tượng Account nếu thành công, ngược lại trả về null
    override suspend fun login(account: Account): Account? {
        // URL cơ sở cho dịch vụ đăng nhập
        val baseUrl = "https://login-pxgdcdndsa-uc.a.run.app"
        // Tạo retrofit service từ URL cơ sở
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)
        // Gọi phương thức login từ MessageService
        val result = retrofit.login(account)
        // Trả về đối tượng Account nếu yêu cầu thành công, ngược lại trả về null
        if (result.isSuccessful){
            return result.body()
        }
        return null
    }

    override suspend fun loadFriendAccounts(username: String): List<Account> {
        val baseUrl = "https://getallfriends-pxgdcdndsa-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)
        val result = retrofit.getFriendAccounts(username)
        if(result.isSuccessful){
            return result.body() ?: emptyList()
        }
        return emptyList()
    }

    // Hàm tạo retrofit service từ URL cơ sở
    private fun createRetrofitService(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)  // Đặt URL cơ sở cho retrofit
            .addConverterFactory(GsonConverterFactory.create())  // Thêm converter factory cho Gson
            .build()  // Xây dựng đối tượng Retrofit
    }
}
