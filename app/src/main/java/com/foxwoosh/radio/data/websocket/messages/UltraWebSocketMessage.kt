package com.foxwoosh.radio.data.websocket.messages

import com.foxwoosh.radio.data.websocket.UltraWebSocketResponseType

interface UltraWebSocketMessage {
    val type: UltraWebSocketResponseType
}