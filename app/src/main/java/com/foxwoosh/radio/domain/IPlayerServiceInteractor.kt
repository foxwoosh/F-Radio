package com.foxwoosh.radio.domain

import com.foxwoosh.radio.player.models.PlayerState
import com.foxwoosh.radio.player.models.Station
import com.foxwoosh.radio.player.models.TrackDataState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface IPlayerServiceInteractor {
    val trackData: StateFlow<TrackDataState>
    val playerState: MutableStateFlow<PlayerState>

    fun startFetching(station: Station)
    fun stopFetching(station: Station)
}