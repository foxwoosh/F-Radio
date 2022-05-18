package com.foxwoosh.radio.storage.remote.ultra

import com.foxwoosh.radio.api.ApiService
import com.foxwoosh.radio.storage.models.Track
import javax.inject.Inject

class UltraDataRemoteStorage @Inject constructor(
    private val apiService: ApiService
) : IUltraDataRemoteStorage {

    override suspend fun loadCurrentData(): Track {
        val result = apiService
            .api
            .getCurrentTrack(System.currentTimeMillis())

        return Track(
            result.id,
            result.title,
            result.artist,
            result.album,
            "${result.root}${result.cover}",
            result.youtubeMusicUrl,
            result.youtubeUrl,
            result.spotifyUrl,
            result.iTunesUrl,
            result.yandexMusicUrl
        )
    }

    override suspend fun getUniqueID() = apiService
        .api
        .checkID(System.currentTimeMillis())
        .uniqueID
}