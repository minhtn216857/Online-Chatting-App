package com.example.minh_messenger_test.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "accounts")
data class Account(
    @SerializedName("username") @PrimaryKey var username: String = "",
    @SerializedName("password") var password: String? = "",
    @SerializedName("email") var email: String? = "",
    @SerializedName("displayName") @ColumnInfo("display_name") var displayName: String? = "",
    @SerializedName("gender") var gender: String? = null,
    @SerializedName("token") var token: String? = null,
    @SerializedName("imageUrl") @ColumnInfo("image_url") var imageUrl: String? = null,
    @SerializedName("friends") @Ignore var friends: MutableList<String>? = mutableListOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Account

        return username == other.username
    }

    override fun hashCode(): Int {
        return username?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "Account(username=$username, " +
                "password=$password, email=$email, " +
                "displayName=$displayName, gender=$gender, " +
                "token=$token, imageUrl=$imageUrl, friends=$friends)"
    }


}