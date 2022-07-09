package com.foxwoosh.radio.ui.player.models

data class InProgressReportUiState(
    val lyricsID: Int,
    val artist: String,
    val title: String,
    val comment: String = ""
)