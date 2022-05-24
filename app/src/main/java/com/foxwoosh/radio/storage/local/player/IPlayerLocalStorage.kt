package com.foxwoosh.radio.storage.local.player

import com.foxwoosh.radio.player.models.PlayerTrackData
import kotlinx.coroutines.flow.StateFlow

interface IPlayerLocalStorage {
    val trackData: StateFlow<PlayerTrackData>
    val isPlaying: StateFlow<Boolean>

    suspend fun setPlayerTrackData(data: PlayerTrackData)
    suspend fun setPlayerIsPlaying(playing: Boolean)
}