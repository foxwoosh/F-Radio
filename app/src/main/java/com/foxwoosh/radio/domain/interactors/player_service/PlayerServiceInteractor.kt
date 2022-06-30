package com.foxwoosh.radio.domain.interactors.player_service

import com.foxwoosh.radio.data.storage.local.player.IPlayerLocalStorage
import com.foxwoosh.radio.data.storage.remote.ultra.IUltraRemoteStorage
import com.foxwoosh.radio.data.websocket.SocketError
import com.foxwoosh.radio.data.websocket.SocketState
import com.foxwoosh.radio.di.modules.PlayerServiceCoroutineScope
import com.foxwoosh.radio.domain.models.Track
import com.foxwoosh.radio.player.helpers.CoverColorExtractor
import com.foxwoosh.radio.player.models.*
import com.foxwoosh.radio.providers.image_provider.ImageProvider
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.net.UnknownHostException
import javax.inject.Inject

@ServiceScoped
class PlayerServiceInteractor @Inject constructor(
    private val ultraRemoteStorage: IUltraRemoteStorage,
    private val playerLocalStorage: IPlayerLocalStorage,
    private val imageProvider: ImageProvider,
    @PlayerServiceCoroutineScope private val scope: CoroutineScope
) : IPlayerServiceInteractor {

    override val trackData = playerLocalStorage.trackData
    override val playerState = playerLocalStorage.playerState
    override val station = playerLocalStorage.station

    private var previousTrack: Track? = null

    init {
        ultraRemoteStorage
            .trackData
            .onEach { track ->
                val image = imageProvider.load(track.coverUrl)

                playerLocalStorage.trackData.emit(
                    TrackDataState.Ready(
                        id = track.id,
                        title = track.title,
                        artist = track.artist,
                        album = track.album,
                        cover = image,
                        colors = image?.let { CoverColorExtractor.extractColors(it) }
                            ?: PlayerColors.default,
                        musicServices = MusicServicesData(
                            track.youtubeMusicUrl,
                            track.youtubeUrl,
                            track.spotifyUrl,
                            track.iTunesUrl,
                            track.yandexMusicUrl
                        ),
                        details = TrackDetails(
                            track.album,
                            track.metadata,
                            track.date,
                            track.time
                        )
                    )
                )

                previousTrack?.let { previous ->
                    playerLocalStorage.previousTracks.update {
                        listOf(previous) + it
                    }
                }

                previousTrack = track
            }.launchIn(scope)

        ultraRemoteStorage
            .dataConnectionState
            .onEach { state ->
                when (state) {
                    is SocketState.Connecting -> {
                        playerLocalStorage.trackData.emit(TrackDataState.Loading)
                    }
                    is SocketState.Failure -> {
                        playerLocalStorage.trackData.emit(
                            TrackDataState.Error(
                                if (state.throwable is UnknownHostException) {
                                    SocketError.NO_INTERNET
                                } else {
                                    SocketError.DEFAULT
                                }
                            )
                        )
                    }
                    else -> { /* nothing to emit */ }
                }
            }.launchIn(scope)
    }

    override fun startFetching(station: Station) {
        when (station) {
            Station.ULTRA,
            Station.ULTRA_HD -> ultraRemoteStorage.startFetching()
        }
    }

    override fun stopFetching(station: Station) {
        playerLocalStorage.trackData.value = TrackDataState.Idle
        playerLocalStorage.playerState.value = PlayerState.IDLE
        previousTrack = null

        when (station) {
            Station.ULTRA,
            Station.ULTRA_HD -> ultraRemoteStorage.stopFetching()
        }
    }
}