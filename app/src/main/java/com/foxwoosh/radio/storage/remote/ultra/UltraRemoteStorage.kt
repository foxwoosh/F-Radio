package com.foxwoosh.radio.storage.remote.ultra

import com.foxwoosh.radio.websocket.UltraWebSocketProvider
import javax.inject.Inject

class UltraRemoteStorage @Inject constructor(
    private val ultraWebSocketProvider: UltraWebSocketProvider
) : IUltraRemoteStorage {

    override val dataConnectionState = ultraWebSocketProvider.connectionState
    override val trackData = ultraWebSocketProvider.trackFlow

    override fun startFetching() {
        ultraWebSocketProvider.connect()
    }

    override fun stopFetching() {
        ultraWebSocketProvider.disconnect()
    }
}