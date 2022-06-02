package com.foxwoosh.radio.storage.remote.ultra

import com.foxwoosh.radio.storage.models.Track
import kotlinx.coroutines.flow.SharedFlow

interface IUltraRemoteStorage {
    val trackData: SharedFlow<Track>

    fun startFetching()
    fun stopFetching()
}