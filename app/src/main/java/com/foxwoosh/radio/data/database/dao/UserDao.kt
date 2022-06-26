package com.foxwoosh.radio.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.foxwoosh.radio.data.database.models.UserDb
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao : AppDao<UserDb> {

    @Query("SELECT * FROM user LIMIT 1")
    fun get(): Flow<UserDb?>
}