package com.foxwoosh.radio.domain

import com.foxwoosh.radio.player.models.PlayerState
import com.foxwoosh.radio.storage.models.Track
import kotlinx.coroutines.flow.MutableStateFlow

interface IPlayerServiceInteractor {
    val trackData: MutableStateFlow<Track>
    val playerState: MutableStateFlow<PlayerState>

    suspend fun fetchTrackDataIfNeeded(currentUniqueID: String?): String
}