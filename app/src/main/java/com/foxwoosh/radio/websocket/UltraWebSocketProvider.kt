package com.foxwoosh.radio.websocket

import android.util.Log
import com.foxwoosh.radio.storage.models.PreviousTrack
import com.foxwoosh.radio.storage.models.Track
import com.foxwoosh.radio.websocket.messages.UltraSongDataWebSocketMessage
import com.foxwoosh.radio.websocket.messages.UltraWebSocketMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UltraWebSocketProvider @Inject constructor() : CoroutineScope {

    private val job = SupervisorJob()
    override val coroutineContext = job + Dispatchers.IO

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null

    private val mutableTrackFlow = MutableSharedFlow<Track>()
    val trackFlow = mutableTrackFlow.asSharedFlow()

    private val mutableConnectionState = MutableStateFlow<ConnectionState>(ConnectionState.Initial)
    val connectionState = mutableConnectionState.asStateFlow()

    val isOpened: Boolean
        get() = connectionState.value is ConnectionState.Connected

    fun connect() {
        if (webSocket == null)
            mutableConnectionState.value = ConnectionState.Connecting

            webSocket = httpClient.newWebSocket(
                Request.Builder()
                    .url("wss://foxwoosh.space/ultra")
                    .build(),
                listener
            )
    }

    fun disconnect() {
        webSocket?.close(1000, "Bye")
    }

    private fun releaseSocket() {
        webSocket?.cancel()
        webSocket = null

        job.cancelChildren()
    }

    private fun getResponse(text: String): UltraWebSocketMessage? {
        val type = UltraWebSocketResponseType.fromValue(
            Json.parseToJsonElement(text).jsonObject["type"]?.jsonPrimitive?.content
        )

        return when (type) {
            UltraWebSocketResponseType.SONG_DATA ->
                Json.decodeFromString<UltraSongDataWebSocketMessage>(text)
            else -> null
        }
    }

    private suspend fun handleDataSongMessage(message: UltraSongDataWebSocketMessage) {
        mutableTrackFlow.emit(
            Track(
                message.id,
                message.title,
                message.artist,
                message.album,
                "${message.root}${message.cover}",
                message.youtubeMusicUrl,
                message.youtubeUrl,
                message.spotifyUrl,
                message.iTunesUrl,
                message.yandexMusicUrl,
                message.previousTracks.map {
                    PreviousTrack(it.title, it.artist, "${message.root}${it.cover}")
                }.reversed()
            )
        )
    }

    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.i("DDLOG", "onOpen")

            mutableConnectionState.value = ConnectionState.Connected
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.i("DDLOG", "onMessage")

            launch {
                Log.i("DDLOG", "onMessageLaunch")
                when (val response = getResponse(text)) {
                    is UltraSongDataWebSocketMessage -> {
                        handleDataSongMessage(response)
                    }
                }
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.i("DDLOG", "onClosing $code, $reason")
            webSocket.close(code, reason)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.i("DDLOG", "onClosed $code, $reason")

            releaseSocket()
            mutableConnectionState.value = ConnectionState.Disconnected(code)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.i("DDLOG", "onFailure: ${t.message}")

            releaseSocket()
            mutableConnectionState.value = ConnectionState.Failure(t)
        }
    }
}