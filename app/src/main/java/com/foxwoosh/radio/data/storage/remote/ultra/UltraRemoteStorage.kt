package com.foxwoosh.radio.data.storage.remote.ultra

import com.foxwoosh.radio.data.websocket.UltraWebSocketProvider
import javax.inject.Inject

class UltraRemoteStorage @Inject constructor(
    private val ultraWebSocketProvider: UltraWebSocketProvider
) : IUltraRemoteStorage {

    override val dataConnectionState = ultraWebSocketProvider.socketConnectionState
    override val trackData = ultraWebSocketProvider.trackFlow

    override fun startFetching() {
        ultraWebSocketProvider.connect()
    }

    override fun stopFetching() {
        ultraWebSocketProvider.disconnect()
    }
}