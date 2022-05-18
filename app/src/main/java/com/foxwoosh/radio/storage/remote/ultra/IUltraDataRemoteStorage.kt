package com.foxwoosh.radio.storage.remote.ultra

import com.foxwoosh.radio.storage.models.Track

interface IUltraDataRemoteStorage {
    suspend fun loadCurrentData(): Track
    suspend fun getUniqueID(): String
}