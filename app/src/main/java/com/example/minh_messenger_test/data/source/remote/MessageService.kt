package com.example.minh_messenger_test.data.source.remote

import com.example.minh_messenger_test.data.model.Account
import com.example.minh_messenger_test.data.model.Mesagge
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
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
    @Headers(
        "Content-Type: application/json",
        "Authorization: key = 20a4fd23903edc861607d3fd0e4b6777414c833f"
    )
    @POST("/")
    suspend fun sendMessage(@Body message: Mesagge): Response<ResponseResult>

    @GET("/")
    suspend fun getFriendAccounts(@Query("username") username: String): Response<List<Account>>

    @GET("/")
    suspend fun getChat(
        @Query("sender") sender: String,
        @Query("receiver") receiver: String
    ): Response<List<Mesagge>>

    @GET("/")
    suspend fun getLastMessages(@Query("username") username: String): Response<List<Mesagge>>
}
