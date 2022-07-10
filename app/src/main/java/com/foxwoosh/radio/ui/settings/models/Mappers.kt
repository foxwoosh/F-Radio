package com.foxwoosh.radio.ui.settings.models

import com.foxwoosh.radio.domain.models.LyricsReport

fun LyricsReport.mapToUiModel() = LyricsReportUiModel(
    reportID,
    lyricsID,
    title,
    artist,
    comment,
    state,
    moderatorID,
    moderatorComment,
    createdAt,
    updatedAt
)