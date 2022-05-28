package com.foxwoosh.radio.player.models

import android.graphics.Bitmap
import com.foxwoosh.radio.storage.models.PreviousTrack

sealed class TrackDataState {
    object Idle : TrackDataState()
    object Loading : TrackDataState()
    data class Ready(
        val id: String,
        val title: String,
        val artist: String,
        val album: String?,
        val cover: Bitmap,
        val colors: PlayerColors,
        val musicServices: MusicServicesData,
        val previousTracks: List<PreviousTrack>
    ) : TrackDataState()
}