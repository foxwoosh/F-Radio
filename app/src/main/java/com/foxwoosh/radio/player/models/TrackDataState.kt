package com.foxwoosh.radio.player.models

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color

sealed class TrackDataState {
    object Idle : TrackDataState()
    object Loading : TrackDataState()
    data class Ready(
        val title: String,
        val artist: String,
        val album: String?,
        val cover: Bitmap,
        val colors: PlayerColors,
        val musicServices: MusicServicesData
    ) : TrackDataState()
}