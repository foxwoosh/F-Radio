package com.foxwoosh.radio.websocket

enum class UltraWebSocketResponseType {
    UNKNOWN, SONG_DATA;

    companion object {
        fun fromValue(value: String?) = values().find { it.name == value } ?: UNKNOWN
    }
}