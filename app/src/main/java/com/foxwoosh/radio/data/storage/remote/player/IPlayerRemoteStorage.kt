package com.foxwoosh.radio.data.storage.remote.player

import com.foxwoosh.radio.data.websocket.SocketState
import com.foxwoosh.radio.domain.models.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface IPlayerRemoteStorage {
    val dataConnectionState: StateFlow<SocketState>
    val trackData: Flow<Track>

    fun startFetching()
    fun stopFetching()
}