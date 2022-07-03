package com.foxwoosh.radio.data.websocket.messages.incoming

interface WebSocketIncomingMessage {
    val type: Type

    enum class Type {
        UNKNOWN,
        SONG_DATA,
        REPORT_UPDATE;

        companion object {
            fun fromValue(value: String?) =
                values()
                    .find { it.name == value }
                    ?: UNKNOWN
        }
    }
}