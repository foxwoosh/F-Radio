package com.foxwoosh.radio.database.models

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_tracks")
data class FavoriteTrackDb(
    @PrimaryKey val id: String,

    val artist: String,
    val title: String,
    val album: String
)