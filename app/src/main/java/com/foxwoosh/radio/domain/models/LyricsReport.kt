package com.foxwoosh.radio.domain.models

data class LyricsReport(
    val reportID: String,
    val lyricsID: Int,
    val state: LyricsReportState?,
    val moderatorID: Long?,
    val moderatorComment: String?
)