package com.foxwoosh.radio.ui.settings.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foxwoosh.radio.data.storage.remote.lyrics.ILyricsRemoteStorage
import com.foxwoosh.radio.domain.models.LyricsReport
import com.foxwoosh.radio.ui.settings.models.UserReportsUiState
import com.foxwoosh.radio.ui.settings.models.mapToUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserReportsViewModel @Inject constructor(
    private val lyricsRemoteStorage: ILyricsRemoteStorage
) : ViewModel() {

    private val mutableReportsState =
        MutableStateFlow<UserReportsUiState>(UserReportsUiState.Loading)
    val reportsState = mutableReportsState.asStateFlow()

    init {
        lyricsRemoteStorage
            .lyricsReportUpdate
            .onEach(::updateReport)
            .launchIn(viewModelScope)

        viewModelScope.launch {
            val reports = lyricsRemoteStorage.getUserReports()

            mutableReportsState.emit(
                when {
                    reports.isEmpty() -> UserReportsUiState.Empty
                    else -> UserReportsUiState.Ready(
                        reports
                            .map { it.mapToUiModel() }
                            .sortedByDescending { it.createdAt }
                    )
                }
            )
        }
    }

    private suspend fun updateReport(report: LyricsReport) {
        val currentState = reportsState.value

        if (currentState !is UserReportsUiState.Ready) return

        val reportsList = currentState.list.toMutableList()
        val reportIndex = reportsList.indexOfFirst { it.reportID == report.reportID }
        if (reportIndex != -1) {
            reportsList[reportIndex] = report.mapToUiModel()
            mutableReportsState.emit(UserReportsUiState.Ready(reportsList))
        }
    }
}