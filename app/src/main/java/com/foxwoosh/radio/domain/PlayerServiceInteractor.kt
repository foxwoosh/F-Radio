package com.foxwoosh.radio.domain

import com.foxwoosh.radio.di.modules.PlayerServiceCoroutineScope
import com.foxwoosh.radio.image_provider.ImageProvider
import com.foxwoosh.radio.player.helpers.CoverColorExtractor
import com.foxwoosh.radio.player.models.MusicServicesData
import com.foxwoosh.radio.player.models.PlayerState
import com.foxwoosh.radio.player.models.Station
import com.foxwoosh.radio.player.models.TrackDataState
import com.foxwoosh.radio.storage.local.player.IPlayerLocalStorage
import com.foxwoosh.radio.storage.remote.ultra.IUltraRemoteStorage
import com.foxwoosh.radio.websocket.ConnectionState
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
                        colors = CoverColorExtractor.extractColors(image),
                        musicServices = MusicServicesData(
                            track.youtubeMusicUrl,
                            track.youtubeUrl,
                            track.spotifyUrl,
                            track.iTunesUrl,
                            track.yandexMusicUrl
                        )
                    )
                )

                delay(500)

                playerLocalStorage.previousTracksData.emit(track.previousTracks)
            }.launchIn(scope)

        ultraRemoteStorage
            .dataConnectionState
            .onEach { state ->
                when (state) {
                    is ConnectionState.Connecting -> {
                        playerLocalStorage.trackData.emit(TrackDataState.Loading)
                    }
                    is ConnectionState.Failure -> {
                        playerLocalStorage.trackData.emit(TrackDataState.Error(state.throwable.message ?: ""))
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
        playerLocalStorage.previousTracksData.value = emptyList()
        playerLocalStorage.playerState.value = PlayerState.IDLE

        when (station) {
            Station.ULTRA,
            Station.ULTRA_HD -> ultraRemoteStorage.stopFetching()
        }
    }
}