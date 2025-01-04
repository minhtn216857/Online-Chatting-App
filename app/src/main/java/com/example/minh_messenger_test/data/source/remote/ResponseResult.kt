package com.example.minh_messenger_test.data.source.remote

// Định nghĩa lớp ResponseResult để biểu diễn kết quả phản hồi từ server
class ResponseResult(
    val success: Boolean,  // Biến success biểu diễn trạng thái thành công (true) hay thất bại (false) của yêu cầu
    val error: String?     // Biến error chứa thông báo lỗi (nếu có), có thể là null nếu không có lỗi
) {
}
