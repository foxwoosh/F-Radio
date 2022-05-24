package com.foxwoosh.radio.storage.local.player

import com.foxwoosh.radio.player.models.TrackDataState
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerLocalStorage @Inject constructor() : IPlayerLocalStorage {
    override val trackData = MutableStateFlow<TrackDataState>(TrackDataState.Idle)
    override val isPlaying = MutableStateFlow(false)

    override suspend fun setPlayerTrackData(data: TrackDataState) {
        trackData.emit(data)
    }

    override suspend fun setPlayerIsPlaying(playing: Boolean) {
        isPlaying.emit(playing)
    }
}