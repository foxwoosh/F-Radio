package com.foxwoosh.radio.data.websocket.messages.incoming

import com.foxwoosh.radio.domain.models.LyricsReportState
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
class LyricsReportUpdateIncomingMessage(
    @SerialName("type") override val type: WebSocketIncomingMessage.Type,
    @SerialName("report_id") val reportID: String,
    @SerialName("author_id") val authorID: String,
    @SerialName("lyrics_id") val lyricsID: String,
    @SerialName("state") val state: LyricsReportState,
    @SerialName("moderator_id") val moderatorID: Long?,
    @SerialName("moderator_comment") val moderatorComment: String?
) : WebSocketIncomingMessage