package com.foxwoosh.radio.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foxwoosh.radio.domain.interactors.player_screen.IPlayerScreenInteractor
import com.foxwoosh.radio.player.models.Station
import com.foxwoosh.radio.player.models.TrackDataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerScreenInteractor: IPlayerScreenInteractor
) : ViewModel() {

    val trackDataFlow = playerScreenInteractor.trackData
    val previousTracksFlow = playerScreenInteractor.previousTracks
    val playerStateFlow = playerScreenInteractor.playerState
    val stationFlow = playerScreenInteractor.station

    val userFlow = playerScreenInteractor.currentUser.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    private val mutableLyricsStateFlow = MutableStateFlow<LyricsDataState>(LyricsDataState.NoData)
    val lyricsStateFlow = mutableLyricsStateFlow.asStateFlow()

    private var lastFetchedLyricsTrackID: String? = null

    init {
        playerScreenInteractor
            .lyrics
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

                    playerScreenInteractor.fetchLyrics(
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

    fun selectStation(station: Station) {

    }
}