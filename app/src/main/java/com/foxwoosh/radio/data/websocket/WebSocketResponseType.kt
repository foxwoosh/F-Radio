package com.foxwoosh.radio.data.websocket

enum class WebSocketResponseType {
    UNKNOWN,
    SONG_DATA,
    LYRICS_REPORT_STATE_CHANGE;

    companion object {
        fun fromValue(value: String?) = values().find { it.name == value } ?: UNKNOWN
    }
}