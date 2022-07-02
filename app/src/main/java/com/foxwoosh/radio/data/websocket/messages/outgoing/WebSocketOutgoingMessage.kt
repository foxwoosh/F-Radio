package com.foxwoosh.radio.data.websocket.messages.outgoing

@kotlinx.serialization.Serializable
data class WebSocketOutgoingMessage<T>(
    val type: Type,
    val params: T
) {
    enum class Type {
        SUBSCRIBE,
        LOGGED_USER_DATA,
        USER_LOGOUT
    }
}