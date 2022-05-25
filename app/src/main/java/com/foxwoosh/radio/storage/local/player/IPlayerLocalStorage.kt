package com.foxwoosh.radio.storage.local.player

import com.foxwoosh.radio.player.models.PlayerState
import com.foxwoosh.radio.player.models.TrackDataState
import kotlinx.coroutines.flow.StateFlow

interface IPlayerLocalStorage {
    val trackData: StateFlow<TrackDataState>
    val playerState: StateFlow<PlayerState>

    suspend fun setPlayerTrackData(data: TrackDataState)
    suspend fun setPlayerState(state: PlayerState)
}