package com.foxwoosh.radio.ui.settings.models

import com.foxwoosh.radio.domain.models.LyricsReportState
import java.util.*

data class LyricsReportUiModel(
    val reportID: String,
    val lyricsID: Int,
    val title: String,
    val artist: String,
    val comment: String,
    val state: LyricsReportState?,
    val moderatorID: Long?,
    val moderatorComment: String?,
    val createdAt: Date,
    val updatedAt: Date?
)