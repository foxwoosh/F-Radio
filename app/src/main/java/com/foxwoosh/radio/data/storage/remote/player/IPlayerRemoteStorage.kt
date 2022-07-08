package com.foxwoosh.radio.data.storage.remote.player

import com.foxwoosh.radio.data.websocket.SocketState
import com.foxwoosh.radio.domain.models.Track
import com.foxwoosh.radio.player.models.Station
import kotlinx.coroutines.flow.StateFlow

interface IPlayerRemoteStorage {
    val socketState: StateFlow<SocketState>
    val track: StateFlow<Track?>

    suspend fun selectStation(station: Station?)
}