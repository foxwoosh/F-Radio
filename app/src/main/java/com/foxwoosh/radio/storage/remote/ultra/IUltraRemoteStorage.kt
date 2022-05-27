package com.foxwoosh.radio.storage.remote.ultra

import com.foxwoosh.radio.storage.models.Track

interface IUltraRemoteStorage {
    suspend fun loadCurrentData(): Track
    suspend fun getUniqueID(): String
}