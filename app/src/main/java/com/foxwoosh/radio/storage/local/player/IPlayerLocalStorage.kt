package com.foxwoosh.radio.storage.local.player

import com.foxwoosh.radio.player.models.TrackDataState
import kotlinx.coroutines.flow.StateFlow

interface IPlayerLocalStorage {
    val trackData: StateFlow<TrackDataState>
    val isPlaying: StateFlow<Boolean>

    suspend fun setPlayerTrackData(data: TrackDataState)
    suspend fun setPlayerIsPlaying(playing: Boolean)
}