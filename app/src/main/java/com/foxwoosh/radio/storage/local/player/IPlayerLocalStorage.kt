package com.foxwoosh.radio.storage.local.player

import com.foxwoosh.radio.player.models.PlayerState
import com.foxwoosh.radio.player.models.TrackDataState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface IPlayerLocalStorage {
    val trackData: MutableStateFlow<TrackDataState>
    val playerState: MutableStateFlow<PlayerState>
}