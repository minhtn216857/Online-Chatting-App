package com.example.minh_messenger_test.utils

import android.text.Editable
import android.text.TextWatcher
import com.example.minh_messenger_test.R
import com.google.android.material.textfield.TextInputEditText

object MessengerUtils {
    const val AVARTAR_BASE_URL = "https://img.upanh.tv/2024/11/19/"
    var token: String? = null
    val defaultAvatarId = R.drawable.avartar_nami_fas
    val defaultAvatars = mapOf(
        R.drawable.avartar_nami_fas to "avartar_nami_fas.jpg",
        R.drawable.avartar_zoro_fas to "avartar_zoro_fas.jpg",
        R.drawable.avartar_ace_fas to "avartar_ace_fas.png",
        R.drawable.avartar_luffy_fas to "avartar_luffy_fas.png",
        R.drawable.avartar_sanji_fas to "avartar_sanji_fas.jpg",
        R.drawable.avartar_chopper_fas to "avartar_chopper_fas.jpg"
    )
    // Hàm mở rộng cho TextInputEditText để xử lý sự kiện thay đổi văn bản
    fun TextInputEditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            // Được gọi để thông báo rằng các ký tự trong start và end trong s sắp được thay thế bằng văn bản mới với độ dài after.
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            // Được gọi để thông báo rằng ở một số vị trí trong start và end, văn bản đã được thay thế bằng count ký tự.
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            // Được gọi để thông báo rằng ở một số vị trí trong start và end, văn bản đã được thay thế bằng count ký tự.
            override fun afterTextChanged(editable: Editable) {
                // Gọi hàm lambda được cung cấp với văn bản mới
                afterTextChanged.invoke(editable.toString())
            }
        })
    }
}
