package com.foxwoosh.radio.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.foxwoosh.radio.data.database.dao.FavoriteTracksDao
import com.foxwoosh.radio.data.database.dao.PreviousTracksDao
import com.foxwoosh.radio.data.database.dao.UserDao
import com.foxwoosh.radio.data.database.models.FavoriteTrackDb
import com.foxwoosh.radio.data.database.models.PreviousTrackDb
import com.foxwoosh.radio.data.database.models.UserDb

@Database(
    entities = [
        FavoriteTrackDb::class,
        PreviousTrackDb::class,
        UserDb::class
    ],
    version = 2,
    exportSchema = true
)
abstract class DatabaseProvider : RoomDatabase() {
    abstract fun favoriteTracks(): FavoriteTracksDao
    abstract fun previousTracks(): PreviousTracksDao
    abstract fun user(): UserDao
}