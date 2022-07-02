package com.foxwoosh.radio.data.websocket.messages

import com.foxwoosh.radio.data.websocket.WebSocketResponseType

interface WebSocketMessage {
    val type: WebSocketResponseType
}