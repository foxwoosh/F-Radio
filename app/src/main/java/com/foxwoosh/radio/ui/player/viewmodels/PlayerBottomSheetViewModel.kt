package com.foxwoosh.radio.ui.player.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foxwoosh.radio.data.storage.local.user.IUserLocalStorage
import com.foxwoosh.radio.data.storage.remote.lyrics.ILyricsRemoteStorage
import com.foxwoosh.radio.data.storage.remote.player.IPlayerRemoteStorage
import com.foxwoosh.radio.domain.models.LyricsReportState
import com.foxwoosh.radio.ui.player.LyricsDataState
import com.foxwoosh.radio.ui.player.models.PlayerBottomSheetEvent
import com.foxwoosh.radio.ui.player.models.TrackForLyricFetchUiModel
import com.foxwoosh.radio.ui.player.models.InProgressReportUiState
import com.foxwoosh.radio.ui.player.models.CurrentLyricsReportUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerBottomSheetViewModel @Inject constructor(
    private val lyricsRemoteStorage: ILyricsRemoteStorage,
    private val playerRemoteStorage: IPlayerRemoteStorage,
    private val userLocalStorage: IUserLocalStorage
) : ViewModel() {

    private var lastFetchedLyricsTrackID: String? = null
    private var trackToFetch: TrackForLyricFetchUiModel? = null

    private val mutableLyricsStateFlow = MutableStateFlow<LyricsDataState>(
        LyricsDataState.Loading(false)
    )
    val lyricsStateFlow = mutableLyricsStateFlow.asStateFlow()

    val previousTracksFlow = playerRemoteStorage.previousTracks

    val userFlow = userLocalStorage.currentUser.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        null
    )

    private val mutableInProgressReportState = MutableStateFlow<InProgressReportUiState?>(null)
    val inProgressReportState = mutableInProgressReportState.asStateFlow()

    private val mutableSendingReportProgress = MutableStateFlow(false)
    val sendingReportProgress = mutableSendingReportProgress.asStateFlow()

    private val mutableCurrentLyricsReportState = MutableStateFlow(CurrentLyricsReportUiState())
    val currentLyricsReportState = mutableCurrentLyricsReportState.asStateFlow()

    private val mutableEvents = MutableSharedFlow<PlayerBottomSheetEvent>()
    val events = mutableEvents.asSharedFlow()

    init {
        lyricsRemoteStorage
            .lyrics
            .onEach {
                mutableLyricsStateFlow.emit(
                    if (it.lyrics.isEmpty()) {
                        LyricsDataState.NoData(it.id)
                    } else {
                        LyricsDataState.Ready(it.id, it.lyrics)
                    }
                )

                mutableCurrentLyricsReportState.emit(
                    CurrentLyricsReportUiState(
                        it.id,
                        it.reportState,
                        it.moderatorComment
                    )
                )
            }
            .launchIn(viewModelScope)

        playerRemoteStorage
            .track
            .onEach { currentTrack ->
                trackToFetch = currentTrack?.let { TrackForLyricFetchUiModel(it.id, it.artist, it.title) }
                mutableEvents.emit(PlayerBottomSheetEvent.TrackChanged)
            }
            .launchIn(viewModelScope)

        lyricsRemoteStorage
            .lyricsReportUpdate
            .onEach { updatedReport ->
                if (currentLyricsReportState.value.lyricsID == updatedReport.lyricsID) {
                    mutableCurrentLyricsReportState.update {
                        it.copy(
                            state = updatedReport.state,
                            moderatorComment = updatedReport.moderatorComment
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun fetchLyricsForCurrentTrack() {
        trackToFetch?.let { track ->
            val loadingWithRequest =
                (lyricsStateFlow.value as? LyricsDataState.Loading)?.request ?: false

            if (lastFetchedLyricsTrackID != track.id && !loadingWithRequest) {
                viewModelScope.launch {
                    try {
                        mutableLyricsStateFlow.emit(LyricsDataState.Loading(true))

                        lyricsRemoteStorage.fetchLyrics(
                            track.title,
                            track.artist
                        )

                        lastFetchedLyricsTrackID = track.id
                    } catch (e: Exception) {
                        mutableLyricsStateFlow.emit(LyricsDataState.Error(e.message!!))
                    }
                }
            }
        }
    }

    fun initReport() {
        val track = trackToFetch ?: return

        if (inProgressReportState.value == null) {
            when (val lyricsState = lyricsStateFlow.value) {
                is LyricsDataState.NoData -> mutableInProgressReportState.update {
                    InProgressReportUiState(
                        lyricsID = lyricsState.id,
                        artist = track.artist,
                        title = track.title
                    )
                }
                is LyricsDataState.Ready -> mutableInProgressReportState.update {
                    InProgressReportUiState(
                        lyricsID = lyricsState.id,
                        artist = track.artist,
                        title = track.title
                    )
                }
                else -> return
            }
        }
    }

    fun setReportComment(comment: String) {
        mutableInProgressReportState.update {
            it?.copy(comment = comment)
        }
    }

    fun dismissReport() {
        mutableInProgressReportState.update { null }
    }

    fun sendReport() {
        val report = mutableInProgressReportState.value ?: return

        viewModelScope.launch {
            try {
                mutableSendingReportProgress.emit(true)

                lyricsRemoteStorage.reportLyrics(
                    report.lyricsID,
                    report.comment
                )

                dismissReport()
            } catch (e: Exception) {
                // handle error
            } finally {
                mutableSendingReportProgress.emit(false)
            }
        }
    }
}