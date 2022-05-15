package com.foxwoosh.radio.storage.remote.current_data

import com.foxwoosh.radio.storage.models.Track

interface ICurrentDataRemoteStorage {
    suspend fun loadCurrentData(): Track
}