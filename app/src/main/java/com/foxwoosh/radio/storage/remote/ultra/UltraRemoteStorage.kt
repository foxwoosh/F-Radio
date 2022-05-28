package com.foxwoosh.radio.storage.remote.ultra

import com.foxwoosh.radio.api.ApiService
import com.foxwoosh.radio.storage.models.PreviousTrack
import com.foxwoosh.radio.storage.models.Track
import javax.inject.Inject

class UltraRemoteStorage @Inject constructor(
    private val apiService: ApiService
) : IUltraRemoteStorage {

    override suspend fun loadCurrentData(): Track {
        val result = apiService
            .ultra
            .getCurrentTrack(System.currentTimeMillis())

        return Track(
            result.id,
            result.title,
            result.artist,
            result.album,
            "${result.root}${result.coverWebp}",
            result.youtubeMusicUrl,
            result.youtubeUrl,
            result.spotifyUrl,
            result.iTunesUrl,
            result.yandexMusicUrl,
            result.previousTracks.map {
                PreviousTrack(it.title, it.artist, "${result.root}${it.coverWebp}")
            }.reversed()
        )
    }

    override suspend fun getUniqueID() = apiService
        .ultra
        .checkID(System.currentTimeMillis())
        .uniqueID
}