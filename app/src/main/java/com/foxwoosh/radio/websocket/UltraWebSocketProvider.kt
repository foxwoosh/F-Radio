package com.foxwoosh.radio.websocket

import android.util.Log
import com.foxwoosh.radio.storage.models.PreviousTrack
import com.foxwoosh.radio.storage.models.Track
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UltraWebSocketProvider @Inject constructor() {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var isOpen = false

    private val mutableTrackFlow = MutableSharedFlow<Track>()
    val trackFlow = mutableTrackFlow.asSharedFlow()

    fun connect() {
        if (webSocket == null)
        webSocket = httpClient.newWebSocket(
            Request.Builder()
                .url("wss://foxwoosh.space/ultra")
                .build(),
            listener
        )
    }

    fun disconnect() {
        webSocket?.close(1000, "Closing connection")
        webSocket?.cancel()
        webSocket = null
    }

    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.i("DDLOG", "onOpen")

            isOpen = true
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.i("DDLOG", "onMessage")

            val result = Json.decodeFromString<UltraWebSocketResponse>(text).data

            GlobalScope.launch {
                mutableTrackFlow.emit(
                    Track(
                        result.id,
                        result.title,
                        result.artist,
                        result.album,
                        "${result.root}${result.coverWebp}",
                        result.youtubeMusicUrl,
                        result.youtubeUrl,
                        result.spotifyUrl,
                        result.iTunesUrl,
                        result.yandexMusicUrl,
                        result.previousTracks.map {
                            PreviousTrack(it.title, it.artist, "${result.root}${it.coverWebp}")
                        }.reversed()
                    )
                )

                Log.i("DDLOG", "emited")
            }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            super.onMessage(webSocket, bytes)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.i("DDLOG", "onFailure: ${t.message}")
            isOpen = false
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.i("DDLOG", "onClosing $code, $reason")
            disconnect()
            isOpen = false
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.i("DDLOG", "onClosed $code, $reason")
            isOpen = false
        }
    }
}