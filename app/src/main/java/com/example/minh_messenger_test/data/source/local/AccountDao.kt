package com.example.minh_messenger_test.data.source.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.minh_messenger_test.data.model.Account

@Dao
interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(acc: Account)

    @Delete
    suspend fun delete(acc: Account)

    @Update
    suspend fun update(acc: Account)

    @Query(
        "SELECT username, password, email, display_name, " +
                "gender, token, image_url FROM accounts " +
                "WHERE username = :username"
    )
    suspend fun findSingleAccount(username: String): Account?

}