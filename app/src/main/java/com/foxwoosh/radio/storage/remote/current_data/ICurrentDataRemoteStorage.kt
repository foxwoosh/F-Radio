package com.foxwoosh.radio.storage.remote.current_data

import com.foxwoosh.radio.storage.models.TrackData

interface ICurrentDataRemoteStorage {
    suspend fun loadCurrentData(): TrackData
}