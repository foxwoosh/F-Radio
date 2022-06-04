package com.foxwoosh.radio.websocket

sealed class ConnectionState {
    object Initial : ConnectionState()

    object Connecting : ConnectionState()

    object Connected : ConnectionState()

    class Disconnected(
        val code: Int
    ) : ConnectionState()

    class Failure(
        val throwable: Throwable
    ) : ConnectionState()
}