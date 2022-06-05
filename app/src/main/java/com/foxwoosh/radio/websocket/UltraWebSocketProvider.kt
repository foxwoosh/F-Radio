package com.foxwoosh.radio.websocket

import android.os.Build
import android.util.Log
import com.foxwoosh.radio.AppJson
import com.foxwoosh.radio.storage.models.PreviousTrack
import com.foxwoosh.radio.storage.models.Track
import com.foxwoosh.radio.websocket.messages.ParametrizedMessage
import com.foxwoosh.radio.websocket.messages.UltraSongDataWebSocketMessage
import com.foxwoosh.radio.websocket.messages.UltraWebSocketMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

@Singleton
class UltraWebSocketProvider @Inject constructor() : CoroutineScope {

    private companion object {
        const val TAG = "UltraWebSocket"
    }

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

    private val reconnectDelay = 2_000
    private var reconnectJob: Job? = null
    private var lastReconnectTry = Duration.ZERO

    val isOpened: Boolean
        get() = connectionState.value is ConnectionState.Connected

    init {
        connectionState
            .onEach {
                if (needReconnect(it)) reconnect()
            }
            .launchIn(this)
    }

    private fun needReconnect(status: ConnectionState): Boolean =
        status is ConnectionState.Failure && reconnectJob == null

    private fun reconnect() {
        reconnectJob = launch {
            val currentTime = System.currentTimeMillis().milliseconds
            val diff = currentTime.minus(lastReconnectTry).inWholeMilliseconds
            if (diff < reconnectDelay) {
                delay(reconnectDelay - diff)
            }
            lastReconnectTry = System.currentTimeMillis().milliseconds

            connect()
            reconnectJob = null
        }
    }

    fun connect() {
        if (webSocket == null && !isOpened)
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
            Log.i(TAG, "onOpen")

            mutableConnectionState.value = ConnectionState.Connected

            val clientInfoMessage = ParametrizedMessage(
                ParametrizedMessage.Type.SUBSCRIBE,
                mapOf(
                    "info" to "${Build.MANUFACTURER} ${Build.MODEL} ${Build.VERSION.RELEASE}"
                )
            )
            webSocket.send(
                AppJson.encodeToString(clientInfoMessage)
            )
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.i(TAG, "onMessage")

            launch {
                when (val response = getResponse(text)) {
                    is UltraSongDataWebSocketMessage -> {
                        handleDataSongMessage(response)
                    }
                }
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.i(TAG, "onClosing $code, $reason")
            webSocket.close(code, reason)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.i(TAG, "onClosed $code, $reason")

            releaseSocket()
            mutableConnectionState.value = ConnectionState.Disconnected(code)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.i(TAG, "onFailure: ${t.message}")

            releaseSocket()
            mutableConnectionState.value = ConnectionState.Failure(t)
        }
    }
}