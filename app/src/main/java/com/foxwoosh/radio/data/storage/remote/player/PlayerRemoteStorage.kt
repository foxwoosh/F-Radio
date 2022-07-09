package com.foxwoosh.radio.data.storage.remote.player

import com.foxwoosh.radio.data.websocket.WebSocketProvider
import com.foxwoosh.radio.data.websocket.messages.incoming.SongDataIncomingMessage
import com.foxwoosh.radio.data.websocket.sendStationsSelect
import com.foxwoosh.radio.domain.models.PreviousTrack
import com.foxwoosh.radio.domain.models.Track
import com.foxwoosh.radio.player.helpers.CoverColorExtractor
import com.foxwoosh.radio.player.models.Station
import com.foxwoosh.radio.providers.image_provider.ImageProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRemoteStorage @Inject constructor(
    private val webSocketProvider: WebSocketProvider,
    private val imageProvider: ImageProvider
) : IPlayerRemoteStorage {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val socketState = webSocketProvider.socketConnectionState
    override val previousTracks = MutableStateFlow<List<Track>>(emptyList())
    override val track = webSocketProvider
        .messagesFlow
        .filterIsInstance<SongDataIncomingMessage>()
        .map { songDataMessage ->
            val coverUrl = "${songDataMessage.root}${songDataMessage.cover}"
            val cover = imageProvider.load(coverUrl)

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

    private var previousTrack: Track? = null

    init {
        track
            .onEach { currentTrack ->
                previousTrack?.let { previousTrack ->
                    previousTracks.update {
                        val previousIsTheSame = it.isNotEmpty() && it.first() == previousTrack
                        val previousIsCurrent = currentTrack == previousTrack

                        if (previousIsTheSame || previousIsCurrent) {
                            it
                        } else mutableListOf<Track>().apply {
                            add(previousTrack)
                            addAll(it)
                        }
                    }
                }
                previousTrack = currentTrack
            }
            .launchIn(scope)
    }

    override suspend fun subscribeToStationData(station: Station?) {
        if (station == null) {
            previousTrack = null
        }
        webSocketProvider.sendMessage { sendStationsSelect(station = station) }
    }
}