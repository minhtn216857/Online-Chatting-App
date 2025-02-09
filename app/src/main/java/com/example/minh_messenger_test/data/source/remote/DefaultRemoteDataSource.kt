package com.example.minh_messenger_test.data.source.remote

import android.util.Log
import com.example.minh_messenger_test.data.model.Account
import com.example.minh_messenger_test.data.model.Mesagge
import com.example.minh_messenger_test.data.source.DataSource
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

// Lớp DefaultRemoteDataSource kế thừa từ DataSource.RemoteDataSource
class DefaultRemoteDataSource @Inject constructor(): DataSource.RemoteDataSource {

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

    override suspend fun addFriend(username: String, userNameFriend: String): String {
        return try {
            val baseUrl = "https://addfriend-pxgdcdndsa-uc.a.run.app"
            val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)
            val result = retrofit.addFriend(username, userNameFriend)

            if (result.isSuccessful) {
                val responseObj = result.body()
                return when {
                    responseObj == null -> "null"
                    responseObj.success -> "success"
                    else -> responseObj.error ?: "null"
                }
            } else {
                "null"
            }
        } catch (e: Exception) {
            "null"
        }
    }



    override suspend fun unFriend(username: String, userNameFriend: String): String {
        val baseUrl = "https://unfriend-pxgdcdndsa-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)
        val result = retrofit.unFriend(username, userNameFriend)
        return if(result.isSuccessful){
            val responseObj = result.body()
            if(responseObj != null){
                if(responseObj.success){
                    "Hủy kết bạn thành công"
                }else{
                    responseObj.error!!
                }
            }else{
                "null"
            }
        }else{
            result.body()?.error!!
        }
    }


    override suspend fun updateAccount(account: Account): Boolean {
        val baseUrl = "https://updateaccount-pxgdcdndsa-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)

        Log.d("API_UPDATE", "📤 Gửi request updateAccount: $account")

        val result = retrofit.updateAccount(account)

        // Log chi tiết response từ server
        Log.d("API_UPDATE", "📥 Kết quả trả về từ server: ${result.code()} - ${result.message()}")

        if (result.isSuccessful) {
            val responseBody = result.body()
            Log.d("API_UPDATE", "📥 Response body: $responseBody")
            return responseBody?.success ?: false
        } else {
            val errorBody = result.errorBody()?.string()
            Log.e("API_UPDATE", "❌ Lỗi API: ${result.code()} - $errorBody")
            return false
        }
    }


    override suspend fun getChat(sender: String, receiver: String): List<Mesagge> {
        val baseUrl = "https://getchat-pxgdcdndsa-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)
        val result = retrofit.getChat(sender, receiver)
        if(result.isSuccessful){
            return result.body() ?: emptyList()
        }
        return emptyList()
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
        val gson = GsonBuilder().serializeNulls().create()
        return Retrofit.Builder()
            .baseUrl(baseUrl)  // Đặt URL cơ sở cho retrofit
            .addConverterFactory(GsonConverterFactory.create(gson))  // Thêm converter factory cho Gson
            .build()  // Xây dựng đối tượng Retrofit
    }

    override suspend fun sendMessage(message: Mesagge): Boolean {
        // URL cơ sở cho dịch vụ đăng nhập
        val baseUrl = "https://sendmessage-pxgdcdndsa-uc.a.run.app"
        // Tạo retrofit service từ URL cơ sở
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)
        // Gọi phương thức login từ MessageService
        val result = retrofit.sendMessage(message)
        // Trả về đối tượng Account nếu yêu cầu thành công, ngược lại trả về null
        if (result.isSuccessful){
            return result.body()?.success ?: false
        }
        return false
    }




}
