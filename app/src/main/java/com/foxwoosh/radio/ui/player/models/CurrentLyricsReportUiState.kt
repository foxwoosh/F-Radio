package com.foxwoosh.radio.ui.player.models

import com.foxwoosh.radio.domain.models.LyricsReportState

data class CurrentLyricsReportUiState(
    val lyricsID: Int? = null,
    val state: LyricsReportState? = null,
    val moderatorComment: String? = null
)