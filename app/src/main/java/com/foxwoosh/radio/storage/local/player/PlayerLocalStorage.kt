package com.foxwoosh.radio.storage.local.player

import com.foxwoosh.radio.player.models.PlayerState
import com.foxwoosh.radio.player.models.TrackDataState
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerLocalStorage @Inject constructor() : IPlayerLocalStorage {
    override val trackData = MutableStateFlow<TrackDataState>(TrackDataState.Idle)
    override val playerState = MutableStateFlow(PlayerState.IDLE)
}