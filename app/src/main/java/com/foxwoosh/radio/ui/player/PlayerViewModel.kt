package com.foxwoosh.radio.ui.player

import android.content.Context
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
    playerLocalStorage: IPlayerLocalStorage,
    playerRemoteStorage: IPlayerRemoteStorage,
    private val lyricsRemoteStorage: ILyricsRemoteStorage
) : ViewModel() {

    private val mutableTrackDataFlow = MutableStateFlow<TrackDataState>(TrackDataState.Idle)
    val trackDataFlow = mutableTrackDataFlow.asStateFlow()

    val previousTracksFlow = playerLocalStorage.previousTracks
    val playerStateFlow = playerLocalStorage.playerState
    val stationFlow = playerLocalStorage.station

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
            .combine(playerRemoteStorage.socketState) { track, state ->
                mutableTrackDataFlow.emit(
                    when (state) {
                        is SocketState.Initial,
                        is SocketState.Disconnected -> TrackDataState.Idle
                        is SocketState.Connecting -> TrackDataState.Loading
                        is SocketState.Failure -> TrackDataState.Error(state.throwable)
                        is SocketState.Connected -> track?.toReadyState() ?: TrackDataState.Loading
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
        PlayerService.selectSource(context, station)
    }
}