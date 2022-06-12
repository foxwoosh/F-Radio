package com.foxwoosh.radio.data.websocket

sealed class SocketState {
    object Initial : SocketState()
    object Connecting : SocketState()
    object Connected : SocketState()
    class Disconnected(val code: Int) : SocketState()
    class Failure(val throwable: Throwable) : SocketState()
}