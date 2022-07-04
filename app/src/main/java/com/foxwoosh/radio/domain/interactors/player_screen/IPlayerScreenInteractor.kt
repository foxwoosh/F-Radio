package com.foxwoosh.radio.domain.interactors.player_screen

import com.foxwoosh.radio.domain.models.CurrentUser
import com.foxwoosh.radio.domain.models.Track
import com.foxwoosh.radio.player.models.PlayerState
import com.foxwoosh.radio.player.models.Station
import com.foxwoosh.radio.player.models.TrackDataState
import kotlinx.coroutines.flow.*

interface IPlayerScreenInteractor {
    val trackData: StateFlow<TrackDataState>
    val previousTracks: StateFlow<List<Track>>
    val playerState: StateFlow<PlayerState>
    val station: StateFlow<Station?>
    val currentUser: Flow<CurrentUser?>
    val lyrics: SharedFlow<String>

    suspend fun fetchLyrics(title: String, artist: String)
}