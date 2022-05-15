package com.foxwoosh.radio.storage.remote.current_data

import com.foxwoosh.radio.storage.models.Track

interface CurrentDataRemoteStorage {
    suspend fun loadCurrentData(): Track
}