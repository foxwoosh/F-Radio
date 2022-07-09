package com.foxwoosh.radio.data.storage.remote.player

import com.foxwoosh.radio.data.websocket.SocketState
import com.foxwoosh.radio.domain.models.PreviousTrack
import com.foxwoosh.radio.domain.models.Track
import com.foxwoosh.radio.player.models.Station
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface IPlayerRemoteStorage {
    val socketState: StateFlow<SocketState>
    val previousTracks: StateFlow<List<Track>>
    val track: StateFlow<Track?>

    suspend fun subscribeToStationData(station: Station?)
}