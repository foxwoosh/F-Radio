package com.foxwoosh.radio.player.models

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import com.foxwoosh.radio.storage.models.PreviousTrack

sealed class TrackDataState {
    object Idle : TrackDataState()
    object Loading : TrackDataState()
    data class Ready(
        val title: String,
        val artist: String,
        val album: String?,
        val cover: Bitmap,
        val colors: PlayerColors,
        val musicServices: MusicServicesData,
        val previousTracks: List<PreviousTrack>,
        val lyrics: String
    ) : TrackDataState()
}