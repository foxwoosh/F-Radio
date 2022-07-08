package com.foxwoosh.radio.data.storage.remote.player

import com.foxwoosh.radio.data.websocket.WebSocketProvider
import com.foxwoosh.radio.data.websocket.messages.incoming.SongDataIncomingMessage
import com.foxwoosh.radio.data.websocket.sendStationsSelect
import com.foxwoosh.radio.domain.models.Track
import com.foxwoosh.radio.player.helpers.CoverColorExtractor
import com.foxwoosh.radio.player.models.Station
import com.foxwoosh.radio.providers.image_provider.ImageProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRemoteStorage @Inject constructor(
    private val webSocketProvider: WebSocketProvider,
    private val imageProvider: ImageProvider
) : IPlayerRemoteStorage {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val socketState = webSocketProvider.socketConnectionState
    override val track = webSocketProvider
        .messagesFlow
        .filterIsInstance<SongDataIncomingMessage>()
        .map { songDataMessage ->
            val cover = imageProvider.load("${songDataMessage.root}${songDataMessage.cover}")

            Track(
                id = songDataMessage.id,
                title = songDataMessage.title,
                artist = songDataMessage.artist,
                album = songDataMessage.album,
                cover = cover,
                colors = CoverColorExtractor.extractColors(cover),
                metadata = songDataMessage.metadata,
                date = songDataMessage.date,
                time = songDataMessage.time,
                youtubeMusicUrl = songDataMessage.youtubeMusicUrl,
                youtubeUrl = songDataMessage.youtubeUrl,
                spotifyUrl = songDataMessage.spotifyUrl,
                iTunesUrl = songDataMessage.iTunesUrl,
                yandexMusicUrl = songDataMessage.yandexMusicUrl
            )
        }
        .stateIn(scope, SharingStarted.Eagerly, null)

    override suspend fun selectStation(station: Station?) {
        webSocketProvider.sendMessage { sendStationsSelect(station = station) }
    }
}