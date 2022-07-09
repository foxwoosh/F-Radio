package com.foxwoosh.radio.data.storage.local.player

import com.foxwoosh.radio.domain.models.PreviousTrack
import com.foxwoosh.radio.domain.models.Track
import com.foxwoosh.radio.player.models.PlayerState
import com.foxwoosh.radio.player.models.Station
import com.foxwoosh.radio.player.models.TrackDataState
import kotlinx.coroutines.flow.MutableStateFlow

interface IPlayerLocalStorage {
    val station: MutableStateFlow<Station?>
    val playerState: MutableStateFlow<PlayerState>
}