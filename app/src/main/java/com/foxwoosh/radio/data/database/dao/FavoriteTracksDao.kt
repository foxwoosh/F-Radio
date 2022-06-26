package com.foxwoosh.radio.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.foxwoosh.radio.data.database.models.FavoriteTrackDb
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteTracksDao : AppDao<FavoriteTrackDb> {

    @Query("SELECT * FROM favorite_tracks")
    fun getAll(): Flow<List<FavoriteTrackDb>>
}