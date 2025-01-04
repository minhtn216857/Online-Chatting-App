package com.example.minh_messenger_test.data.source.remote

import android.os.Message
import com.example.minh_messenger_test.data.model.Account
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// Định nghĩa interface MessageService cho các dịch vụ liên quan đến tài khoản và tin nhắn
interface MessageService {

    // Phương thức createAccount để tạo tài khoản, sử dụng phương thức HTTP POST
    @POST("/")
    suspend fun createAccount(@Body account: Account): Response<ResponseResult>

    // Phương thức updateAccount để cập nhật tài khoản, sử dụng phương thức HTTP POST
    @POST("/")
    suspend fun updateAccount(@Body account: Account): Response<ResponseResult>

    // Phương thức login để đăng nhập, sử dụng phương thức HTTP POST
    @POST("/")
    suspend fun login(@Body account: Account): Response<Account>

    // Phương thức sendMessage để gửi tin nhắn, sử dụng phương thức HTTP POST
    @POST("/")
    suspend fun sendMessage(@Body message: Message): Response<ResponseResult>

    @GET("/")
    suspend fun getFriendAccounts(@Query("username") username: String): Response<List<Account>>
}
