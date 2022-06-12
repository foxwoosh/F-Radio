package com.foxwoosh.radio.data.storage.local.player

import com.foxwoosh.radio.player.models.PlayerState
import com.foxwoosh.radio.player.models.TrackDataState
import com.foxwoosh.radio.data.storage.models.PreviousTrack
import kotlinx.coroutines.flow.MutableStateFlow

interface IPlayerLocalStorage {
    val trackData: MutableStateFlow<TrackDataState>
    val previousTracksData: MutableStateFlow<List<PreviousTrack>>
    val playerState: MutableStateFlow<PlayerState>
}