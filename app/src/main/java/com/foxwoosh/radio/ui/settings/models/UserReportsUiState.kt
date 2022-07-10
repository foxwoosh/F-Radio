package com.foxwoosh.radio.ui.settings.models

sealed class UserReportsUiState {
    object Loading : UserReportsUiState()
    object Empty : UserReportsUiState()
    data class Ready(val list: List<LyricsReportUiModel>) : UserReportsUiState()
}