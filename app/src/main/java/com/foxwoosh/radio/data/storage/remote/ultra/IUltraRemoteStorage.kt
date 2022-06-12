package com.foxwoosh.radio.data.storage.remote.ultra

import com.foxwoosh.radio.data.storage.models.Track
import com.foxwoosh.radio.data.websocket.ConnectionState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface IUltraRemoteStorage {
    val dataConnectionState: StateFlow<ConnectionState>
    val trackData: SharedFlow<Track>

    fun startFetching()
    fun stopFetching()
}