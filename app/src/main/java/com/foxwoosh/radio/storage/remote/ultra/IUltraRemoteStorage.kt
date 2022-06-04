package com.foxwoosh.radio.storage.remote.ultra

import com.foxwoosh.radio.storage.models.Track
import com.foxwoosh.radio.websocket.ConnectionState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface IUltraRemoteStorage {
    val dataConnectionState: StateFlow<ConnectionState>
    val trackData: SharedFlow<Track>

    fun startFetching()
    fun stopFetching()
}