package com.foxwoosh.radio.domain.models

class Lyrics(
    val id: Int,
    val lyrics: String,
    val reportState: LyricsReportState?,
    val moderatorComment: String?
)