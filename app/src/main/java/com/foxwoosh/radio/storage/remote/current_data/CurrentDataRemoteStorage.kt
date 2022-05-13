package com.foxwoosh.radio.storage.remote.current_data

import com.foxwoosh.radio.api.ApiService
import javax.inject.Inject

class CurrentDataRemoteStorage @Inject constructor(
    private val apiService: ApiService
) : ICurrentDataRemoteStorage {

    override suspend fun loadCurrentData() {
        apiService.api.getCurrent(System.currentTimeMillis())
    }
}