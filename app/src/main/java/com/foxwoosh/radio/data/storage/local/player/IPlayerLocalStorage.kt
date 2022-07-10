package com.foxwoosh.radio.data.storage.local.player

import com.foxwoosh.radio.player.models.PlayerState
import com.foxwoosh.radio.player.models.Station
import kotlinx.coroutines.flow.MutableStateFlow

interface IPlayerLocalStorage {
    val station: MutableStateFlow<Station?>
    val playerState: MutableStateFlow<PlayerState>
}