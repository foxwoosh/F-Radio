package com.foxwoosh.radio.player.models

import android.graphics.Bitmap

sealed class TrackDataState {
    object Idle : TrackDataState()
    object Loading : TrackDataState()
    data class Error(val error: PlayerError) : TrackDataState()
    data class Ready(
        val id: String,
        val title: String,
        val artist: String,
        val album: String?,
        val cover: Bitmap,
        val colors: PlayerColors,
        val musicServices: MusicServicesData,
        val details: TrackDetails
    ) : TrackDataState()
}