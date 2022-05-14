package com.foxwoosh.radio.storage.remote.current_data

import com.foxwoosh.radio.api.ApiService
import com.foxwoosh.radio.storage.models.TrackData
import javax.inject.Inject

class CurrentDataRemoteStorage @Inject constructor(
    private val apiService: ApiService
) : ICurrentDataRemoteStorage {

    override suspend fun loadCurrentData(): TrackData {
        val result = apiService.api.getCurrent(System.currentTimeMillis())

        return TrackData(
            result.id,
            result.title,
            result.artist,
            result.album,
            "${result.root}${result.cover}"
        )
    }
}