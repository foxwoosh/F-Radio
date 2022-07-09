package com.foxwoosh.radio.data.api.foxy.responses

import com.foxwoosh.radio.domain.models.LyricsReportState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LyricsResponse(
    @SerialName("id") val id: Int,
    @SerialName("lyrics") val lyrics: String,
    @SerialName("report_state") val reportState: String?,
    @SerialName("moderator_comment") val moderatorComment: String?
)