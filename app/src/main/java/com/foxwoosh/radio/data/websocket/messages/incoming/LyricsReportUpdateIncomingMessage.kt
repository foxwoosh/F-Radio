package com.foxwoosh.radio.data.websocket.messages.incoming

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
class LyricsReportUpdateIncomingMessage(
    @SerialName("type") override val type: WebSocketIncomingMessage.Type,
    @SerialName("report_id") val reportID: String,
    @SerialName("author_id") val authorID: Long,
    @SerialName("lyrics_id") val lyricsID: Int,
    @SerialName("comment") val comment: String,
    @SerialName("title") val title: String,
    @SerialName("artist") val artist: String,
    @SerialName("state") val state: String,
    @SerialName("moderator_id") val moderatorID: Long?,
    @SerialName("moderator_comment") val moderatorComment: String?,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long?
) : WebSocketIncomingMessage