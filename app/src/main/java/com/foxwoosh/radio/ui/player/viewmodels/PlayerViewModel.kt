package com.foxwoosh.radio.ui.player.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foxwoosh.radio.data.storage.local.player.IPlayerLocalStorage
import com.foxwoosh.radio.data.storage.remote.player.IPlayerRemoteStorage
import com.foxwoosh.radio.player.PlayerService
import com.foxwoosh.radio.player.models.Station
import com.foxwoosh.radio.player.models.TrackDataState
import com.foxwoosh.radio.ui.player.toReadyState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerLocalStorage: IPlayerLocalStorage,
    private val playerRemoteStorage: IPlayerRemoteStorage
) : ViewModel() {

    val trackDataFlow = playerLocalStorage
        .station
        .combine(playerRemoteStorage.track) { station, track ->
            when {
                station != null && track != null -> track.toReadyState()
                station != null && track == null -> TrackDataState.Loading
                else -> TrackDataState.Idle
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, TrackDataState.Idle)

    val playerStateFlow = playerLocalStorage.playerState
    val stationFlow = playerLocalStorage.station
    val socketState = playerRemoteStorage.socketState

    fun selectStation(context: Context, station: Station) {
        if (playerLocalStorage.station.value == station) return

        viewModelScope.launch {
            PlayerService.selectSource(context, station)
            playerLocalStorage.station.emit(station)
            playerRemoteStorage.subscribeToStationData(station)
        }
    }

    fun pause(context: Context) {
        PlayerService.pause(context)
    }

    fun play(context: Context) {
        PlayerService.play(context)
    }

    fun stop(context: Context) {
        PlayerService.stop(context)
    }
}