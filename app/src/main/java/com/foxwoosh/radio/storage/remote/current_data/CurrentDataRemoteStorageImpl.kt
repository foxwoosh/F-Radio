package com.foxwoosh.radio.storage.remote.current_data

import com.foxwoosh.radio.api.ApiService
import com.foxwoosh.radio.storage.models.Track
import javax.inject.Inject

class CurrentDataRemoteStorageImpl @Inject constructor(
    private val apiService: ApiService
) : CurrentDataRemoteStorage {

    override suspend fun loadCurrentData(): Track {
        val result = apiService.api.getCurrentTrack(System.currentTimeMillis())

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
}