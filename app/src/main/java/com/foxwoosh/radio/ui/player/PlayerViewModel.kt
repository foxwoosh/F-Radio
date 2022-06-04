package com.foxwoosh.radio.ui.player

import android.util.Log
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

    val trackDataFlow = playerLocalStorage.trackData.asStateFlow()
    val playerStateFlow = playerLocalStorage.playerState.asStateFlow()

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
        Log.i("DDLOG", "trying to fetch lyrics")
        val trackData = trackDataFlow.value
        if (trackData is TrackDataState.Ready && lastFetchedLyricsTrackID != trackData.id) {
            viewModelScope.launch {
                Log.i("DDLOG", "fetching lyrics")
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