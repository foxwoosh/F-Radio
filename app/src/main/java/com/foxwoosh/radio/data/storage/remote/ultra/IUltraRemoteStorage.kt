package com.foxwoosh.radio.data.storage.remote.ultra

import com.foxwoosh.radio.data.websocket.SocketState
import com.foxwoosh.radio.domain.models.Track
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface IUltraRemoteStorage {
    val dataConnectionState: StateFlow<SocketState>
    val trackData: SharedFlow<Track>

    fun startFetching()
    fun stopFetching()
}