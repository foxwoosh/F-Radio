package com.foxwoosh.radio.ui.player

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foxwoosh.radio.data.storage.local.player.IPlayerLocalStorage
import com.foxwoosh.radio.data.storage.local.user.IUserLocalStorage
import com.foxwoosh.radio.data.storage.remote.lyrics.ILyricsRemoteStorage
import com.foxwoosh.radio.data.storage.remote.player.IPlayerRemoteStorage
import com.foxwoosh.radio.data.websocket.SocketState
import com.foxwoosh.radio.player.PlayerService
import com.foxwoosh.radio.player.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    userLocalStorage: IUserLocalStorage,
    private val playerLocalStorage: IPlayerLocalStorage,
    private val playerRemoteStorage: IPlayerRemoteStorage,
    private val lyricsRemoteStorage: ILyricsRemoteStorage
) : ViewModel() {

    private val mutableTrackDataFlow = MutableStateFlow<TrackDataState>(TrackDataState.Idle)
    val trackDataFlow = mutableTrackDataFlow.asStateFlow()

    val previousTracksFlow = playerLocalStorage.previousTracks
    val playerStateFlow = playerLocalStorage.playerState
    val stationFlow = playerLocalStorage.station
    val socketState = playerRemoteStorage.socketState

    val userFlow = userLocalStorage.currentUser.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    private val mutableLyricsStateFlow = MutableStateFlow<LyricsDataState>(LyricsDataState.NoData)
    val lyricsStateFlow = mutableLyricsStateFlow.asStateFlow()

    private var lastFetchedLyricsTrackID: String? = null

    init {
        lyricsRemoteStorage
            .lyricsFlow
            .onEach {
                mutableLyricsStateFlow.emit(
                    if (it.isEmpty()) {
                        LyricsDataState.NoData
                    } else {
                        LyricsDataState.Ready(it)
                    }
                )
            }
            .launchIn(viewModelScope)

        playerRemoteStorage
            .track
            .combine(playerLocalStorage.station) { track, station ->
                mutableTrackDataFlow.emit(
                    when {
                        station != null && track != null -> track.toReadyState()
                        station != null && track == null -> TrackDataState.Loading
                        else -> TrackDataState.Idle
                    }
                )
            }
            .launchIn(viewModelScope)
    }

    fun fetchLyricsForCurrentTrack() {
        val trackData = trackDataFlow.value

        if (trackData is TrackDataState.Ready
            && lastFetchedLyricsTrackID != trackData.id
            && lyricsStateFlow.value !is LyricsDataState.Loading
        ) {
            viewModelScope.launch {
                try {
                    mutableLyricsStateFlow.emit(LyricsDataState.Loading)

                    lyricsRemoteStorage.fetchLyrics(
                        trackData.title,
                        trackData.artist
                    )

                    lastFetchedLyricsTrackID = trackData.id
                } catch (e: Exception) {
                    mutableLyricsStateFlow.emit(LyricsDataState.Error(e.message!!))
                }
            }
        }
    }

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