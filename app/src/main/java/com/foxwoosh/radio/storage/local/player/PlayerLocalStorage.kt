package com.foxwoosh.radio.storage.local.player

import com.foxwoosh.radio.player.PlayerTrackData
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerLocalStorage @Inject constructor() : IPlayerLocalStorage {
    override val trackData = MutableStateFlow(PlayerTrackData.waiting)
    override val isPlaying = MutableStateFlow(false)

    override suspend fun setPlayerTrackData(data: PlayerTrackData) {
        trackData.emit(data)
    }

    override suspend fun setPlayerIsPlaying(playing: Boolean) {
        isPlaying.emit(playing)
    }
}