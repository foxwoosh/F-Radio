package com.foxwoosh.radio.data.storage.remote.player

import com.foxwoosh.radio.data.websocket.WebSocketProvider
import com.foxwoosh.radio.data.websocket.mapToModel
import com.foxwoosh.radio.data.websocket.messages.incoming.SongDataIncomingMessage
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlayerRemoteStorage @Inject constructor(
    private val webSocketProvider: WebSocketProvider
) : IPlayerRemoteStorage {

    override val dataConnectionState = webSocketProvider.socketConnectionState
    override val trackData = webSocketProvider
        .messagesFlow
        .filterIsInstance<SongDataIncomingMessage>()
        .map { it.mapToModel() }

    val x = webSocketProvider
        .messagesFlow
        .filterIsInstance<SongDataIncomingMessage>()
        .map { it.mapToModel() }

    override fun startFetching() {
        webSocketProvider.connect()
    }

    override fun stopFetching() {
        webSocketProvider.disconnect()
    }
}