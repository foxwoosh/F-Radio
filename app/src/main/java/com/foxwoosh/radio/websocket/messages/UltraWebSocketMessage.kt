package com.foxwoosh.radio.websocket.messages

import com.foxwoosh.radio.websocket.UltraWebSocketResponseType

interface UltraWebSocketMessage {
    val type: UltraWebSocketResponseType
}