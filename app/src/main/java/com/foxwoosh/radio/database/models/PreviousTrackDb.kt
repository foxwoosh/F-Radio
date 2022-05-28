package com.foxwoosh.radio.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "previous_tracks")
data class PreviousTrackDb(
    @PrimaryKey val id: String,

    val artist: String,
    val title: String,
    val album: String,
    val time: String
)