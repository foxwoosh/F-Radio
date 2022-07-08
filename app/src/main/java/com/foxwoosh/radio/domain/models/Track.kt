package com.foxwoosh.radio.domain.models

import android.graphics.Bitmap
import com.foxwoosh.radio.player.models.PlayerColors

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String?,
    val cover: Bitmap?,
    val colors: PlayerColors,
    val metadata: String,
    val date: String,
    val time: String,
    val youtubeMusicUrl: String?,
    val youtubeUrl: String?,
    val spotifyUrl: String?,
    val iTunesUrl: String?,
    val yandexMusicUrl: String?
)