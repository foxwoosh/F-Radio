package com.foxwoosh.radio.data.api.foxy.requests

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class LyricsReportRequest(
    @SerialName("lyrics_id") val lyricsID: Int,
    @SerialName("comment") val comment: String
)