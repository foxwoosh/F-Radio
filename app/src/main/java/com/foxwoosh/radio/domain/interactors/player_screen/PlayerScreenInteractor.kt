package com.foxwoosh.radio.domain.interactors.player_screen

import com.foxwoosh.radio.data.storage.local.player.IPlayerLocalStorage
import com.foxwoosh.radio.data.storage.local.user.IUserLocalStorage
import com.foxwoosh.radio.data.storage.remote.lyrics.ILyricsRemoteStorage
import javax.inject.Inject

class PlayerScreenInteractor @Inject constructor(
    playerLocalStorage: IPlayerLocalStorage,
    userLocalStorage: IUserLocalStorage,
    private val lyricsRemoteStorage: ILyricsRemoteStorage
) : IPlayerScreenInteractor {

    override val trackData = playerLocalStorage.trackData
    override val previousTracks = playerLocalStorage.previousTracks
    override val playerState = playerLocalStorage.playerState
    override val station = playerLocalStorage.station
    override val currentUser = userLocalStorage.currentUser
    override val lyrics = lyricsRemoteStorage.lyricsFlow

    override suspend fun fetchLyrics(title: String, artist: String) {
        lyricsRemoteStorage.fetchLyrics(title, artist)
    }
}