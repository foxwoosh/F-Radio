package com.foxwoosh.radio.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.foxwoosh.radio.data.database.models.PreviousTrackDb
import kotlinx.coroutines.flow.Flow

@Dao
interface PreviousTracksDao : AppDao<PreviousTrackDb> {

    @Query("SELECT * FROM previous_tracks")
    fun getAll(): Flow<List<PreviousTrackDb>>
}