package com.foxwoosh.radio.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.foxwoosh.radio.database.dao.FavoriteTracksDao
import com.foxwoosh.radio.database.dao.PreviousTracksDao
import com.foxwoosh.radio.database.models.FavoriteTrackDb
import com.foxwoosh.radio.database.models.PreviousTrackDb

@Database(
    entities = [
        FavoriteTrackDb::class,
        PreviousTrackDb::class
    ],
    version = 1,
    exportSchema = true
)
abstract class DatabaseProvider : RoomDatabase() {
    abstract fun favoriteTracks(): FavoriteTracksDao
    abstract fun previousTracks(): PreviousTracksDao
}