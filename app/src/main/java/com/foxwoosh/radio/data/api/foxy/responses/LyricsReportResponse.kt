package com.foxwoosh.radio.data.api.foxy.responses

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class LyricsReportResponse(
    @SerialName("id") val id: String,
    @SerialName("lyrics_id") val lyricsID: Int,
    @SerialName("title") val title: String?,
    @SerialName("artist") val artist: String?,
    @SerialName("user_comment") val userComment: String,
    @SerialName("state") val state: String,
    @SerialName("moderator_id") val moderatorID: Long?,
    @SerialName("moderator_comment") val moderatorComment: String?,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long?
)