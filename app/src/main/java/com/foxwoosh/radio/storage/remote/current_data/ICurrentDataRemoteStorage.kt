package com.foxwoosh.radio.storage.remote.current_data

interface ICurrentDataRemoteStorage {
    suspend fun loadCurrentData()
}