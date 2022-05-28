package com.foxwoosh.radio.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foxwoosh.radio.player.models.TrackDataState
import com.foxwoosh.radio.storage.local.player.IPlayerLocalStorage
import com.foxwoosh.radio.storage.remote.lyrics.ILyricsRemoteStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    playerLocalStorage: IPlayerLocalStorage,
    private val lyricsRemoteStorage: ILyricsRemoteStorage
) : ViewModel() {

    val trackDataFlow = playerLocalStorage.trackData
    val playerStateFlow = playerLocalStorage.playerState

    private val mutableLyricsStateFlow = MutableStateFlow<LyricsDataState>(LyricsDataState.Loading)
    val lyricsStateFlow = mutableLyricsStateFlow.asStateFlow()

    private var lastFetchedLyricsTrackID: String? = null

    init {
        lyricsRemoteStorage
            .lyricsFlow
            .filter { it.isNotEmpty() }
            .onEach { mutableLyricsStateFlow.emit(LyricsDataState.Ready(it)) }
            .launchIn(viewModelScope)
    }

    fun fetchLyricsForCurrentTrack() {
        val trackData = trackDataFlow.value
        if (trackData is TrackDataState.Ready && lastFetchedLyricsTrackID != trackData.id) {
            viewModelScope.launch {
                mutableLyricsStateFlow.emit(LyricsDataState.Loading)

                lyricsRemoteStorage.fetchLyrics(
                    trackData.title,
                    trackData.artist
                )

                lastFetchedLyricsTrackID = trackData.id
            }
        }
    }
}