package com.foxwoosh.radio.ui.settings.models

import com.foxwoosh.radio.domain.models.LyricsReport
import java.util.*

fun LyricsReport.mapToUiModel() = LyricsReportUiModel(
    reportID,
    lyricsID,
    title,
    artist,
    comment,
    state,
    moderatorID,
    moderatorComment,
    Date(createdAt),
    updatedAt?.let { Date(it) }
)