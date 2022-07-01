package com.foxwoosh.radio.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foxwoosh.radio.data.storage.local.player.IPlayerLocalStorage
import com.foxwoosh.radio.data.storage.local.user.IUserLocalStorage
import com.foxwoosh.radio.data.storage.remote.lyrics.ILyricsRemoteStorage
import com.foxwoosh.radio.player.models.TrackDataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    playerLocalStorage: IPlayerLocalStorage,
    userLocalStorage: IUserLocalStorage,
    private val lyricsRemoteStorage: ILyricsRemoteStorage
) : ViewModel() {

    val trackDataFlow = playerLocalStorage.trackData.asStateFlow()
    val previousTracksFlow = playerLocalStorage.previousTracks.asStateFlow()
    val playerStateFlow = playerLocalStorage.playerState.asStateFlow()
    val stationFlow = playerLocalStorage.station.asStateFlow()
    val userFlow = userLocalStorage.currentUser.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
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
}