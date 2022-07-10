package com.foxwoosh.radio.data.storage.local.player

import com.foxwoosh.radio.data.websocket.SocketState
import com.foxwoosh.radio.data.websocket.WebSocketProvider
import com.foxwoosh.radio.data.websocket.sendStationSelect
import com.foxwoosh.radio.player.models.PlayerState
import com.foxwoosh.radio.player.models.Station
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerLocalStorage @Inject constructor(
    private val webSocketProvider: WebSocketProvider
) : IPlayerLocalStorage {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val station = MutableStateFlow<Station?>(null)
    override val playerState = MutableStateFlow(PlayerState.IDLE)

    private var shouldResubscribeToStation = false

    init {
        webSocketProvider
            .socketConnectionState
            .onEach {
                when (it) {
                    is SocketState.Disconnected,
                    is SocketState.Failure -> {
                        shouldResubscribeToStation = true
                    }
                    is SocketState.Connected -> {
                        if (station.value != null && shouldResubscribeToStation) {
                            webSocketProvider.sendMessage { sendStationSelect(station.value) }
                        }

                        shouldResubscribeToStation = false
                    }
                    else -> { /* no need */ }
                }
            }
            .launchIn(scope)
    }
}