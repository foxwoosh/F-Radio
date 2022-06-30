package com.foxwoosh.radio.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.foxwoosh.radio.data.database.models.CurrentUserDb
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrentUserDao : AppDao<CurrentUserDb> {
    @Query("SELECT * FROM current_user LIMIT 1")
    fun get(): Flow<CurrentUserDb?>

    @Query("DELETE FROM current_user")
    fun remove()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(user: CurrentUserDb)
}