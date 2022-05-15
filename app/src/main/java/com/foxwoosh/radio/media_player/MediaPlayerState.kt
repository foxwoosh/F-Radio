package com.foxwoosh.radio.media_player

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color

sealed class MediaPlayerState {
    object Buffering : MediaPlayerState()
    object Paused : MediaPlayerState()

    data class Playing(
        val trackTitle: String,
        val artist: String,
        val cover: Bitmap,
        val surfaceColor: Color,
        val primaryTextColor: Color,
        val secondaryTextColor: Color,
        val musicServices: MusicServicesData
    ) : MediaPlayerState()
}