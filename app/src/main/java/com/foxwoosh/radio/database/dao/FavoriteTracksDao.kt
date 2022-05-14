package com.foxwoosh.radio.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.foxwoosh.radio.database.models.FavoriteTrackDb
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteTracksDao {
    @Query("SELECT * FROM favorite_tracks")
    fun getAll(): Flow<List<FavoriteTrackDb>>
}