package com.foxwoosh.radio.player.models

import android.graphics.Bitmap
import com.foxwoosh.radio.data.websocket.SocketError

sealed class TrackDataState {
    object Idle : TrackDataState()
    object Loading : TrackDataState()
    data class Error(val error: SocketError) : TrackDataState()
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