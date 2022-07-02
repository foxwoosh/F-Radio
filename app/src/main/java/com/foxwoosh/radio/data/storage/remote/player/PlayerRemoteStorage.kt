package com.foxwoosh.radio.data.storage.remote.player

import com.foxwoosh.radio.data.websocket.WebSocketProvider
import javax.inject.Inject

class PlayerRemoteStorage @Inject constructor(
    private val webSocketProvider: WebSocketProvider
) : IPlayerRemoteStorage {

    override val dataConnectionState = webSocketProvider.socketConnectionState
    override val trackData = webSocketProvider.trackFlow

    override fun startFetching() {
        webSocketProvider.connect()
    }

    override fun stopFetching() {
        webSocketProvider.disconnect()
    }
}