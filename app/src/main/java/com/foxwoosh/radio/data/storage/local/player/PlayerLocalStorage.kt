package com.foxwoosh.radio.data.storage.local.player

import com.foxwoosh.radio.domain.models.Track
import com.foxwoosh.radio.player.models.PlayerState
import com.foxwoosh.radio.player.models.Station
import com.foxwoosh.radio.player.models.TrackDataState
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerLocalStorage @Inject constructor() : IPlayerLocalStorage {
    override val station = MutableStateFlow<Station?>(null)
    override val trackData = MutableStateFlow<TrackDataState>(TrackDataState.Idle)
    override val previousTracks = MutableStateFlow<List<Track>>(emptyList())
    override val playerState = MutableStateFlow(PlayerState.IDLE)
}