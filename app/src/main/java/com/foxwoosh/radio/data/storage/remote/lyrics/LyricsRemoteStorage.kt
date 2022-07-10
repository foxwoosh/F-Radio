package com.foxwoosh.radio.data.storage.remote.lyrics

import com.foxwoosh.radio.data.api.ApiService
import com.foxwoosh.radio.data.api.foxy.requests.LyricsReportRequest
import com.foxwoosh.radio.data.websocket.WebSocketProvider
import com.foxwoosh.radio.data.websocket.messages.incoming.LyricsReportUpdateIncomingMessage
import com.foxwoosh.radio.domain.models.Lyrics
import com.foxwoosh.radio.domain.models.LyricsReport
import com.foxwoosh.radio.domain.models.LyricsReportState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LyricsRemoteStorage @Inject constructor(
    private val apiService: ApiService,
    private val webSocketProvider: WebSocketProvider
) : ILyricsRemoteStorage {
    override val lyrics = MutableSharedFlow<Lyrics>()

    override val lyricsReportUpdate = webSocketProvider
        .messagesFlow
        .filterIsInstance<LyricsReportUpdateIncomingMessage>()
        .map {
            LyricsReport(
                it.reportID,
                it.lyricsID,
                it.title,
                it.artist,
                it.comment,
                LyricsReportState.get(it.state),
                it.moderatorID,
                it.moderatorComment,
                it.createdAt,
                it.updatedAt
            )
        }

    override suspend fun fetchLyrics(title: String, artist: String) {
        val response = apiService
            .foxy
            .getLyrics(artist, title)

        lyrics.emit(
            Lyrics(
                response.id,
                response.lyrics,
                LyricsReportState.get(response.reportState),
                response.moderatorComment
            )
        )
    }

    override suspend fun reportLyrics(lyricsID: Int, comment: String) {
        apiService
            .foxy
            .reportLyrics(
                LyricsReportRequest(lyricsID, comment)
            )
    }

    override suspend fun getUserReports(): List<LyricsReport> {
        return apiService
            .foxy
            .getUserReports()
            .map {
                LyricsReport(
                    it.id,
                    it.lyricsID,
                    it.title,
                    it.artist,
                    it.userComment,
                    LyricsReportState.get(it.state),
                    it.moderatorID,
                    it.moderatorComment,
                    it.createdAt,
                    it.updatedAt
                )
            }
    }
}