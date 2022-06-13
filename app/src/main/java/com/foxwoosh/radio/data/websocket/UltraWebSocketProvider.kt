package com.foxwoosh.radio.data.websocket

import android.os.Build
import android.util.Log
import com.foxwoosh.radio.AppJson
import com.foxwoosh.radio.data.websocket.messages.ParametrizedMessage
import com.foxwoosh.radio.data.websocket.messages.UltraSongDataWebSocketMessage
import com.foxwoosh.radio.data.websocket.messages.UltraWebSocketMessage
import com.foxwoosh.radio.domain.models.Track
import com.foxwoosh.radio.providers.network_state_provider.NetworkState
import com.foxwoosh.radio.providers.network_state_provider.NetworkStateProvider
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

@Singleton
class UltraWebSocketProvider @Inject constructor(
    private val networkStateProvider: NetworkStateProvider
) : CoroutineScope {

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

    private val mutableSocketConnectionState =
        MutableStateFlow<SocketState>(SocketState.Initial)
    val socketConnectionState = mutableSocketConnectionState.asStateFlow()

    private val reconnectDelay = 2_000
    private var reconnectJob: Job? = null
    private var lastReconnectTry = Duration.ZERO

    private val isSocketOpened: Boolean
        get() = socketConnectionState.value is SocketState.Connected

    private val isSocketFailed: Boolean
        get() = socketConnectionState.value is SocketState.Failure

    private val isNetworkConnected: Boolean
        get() = networkStateProvider.networkState.value == NetworkState.CONNECTED

    init {
        socketConnectionState
            .onEach {
                if (needReconnect()) {
                    reconnect()
                }
            }
            .launchIn(this)

        networkStateProvider
            .networkState
            .drop(1) // don't need the first collect
            .onEach { tryConnect() }
            .launchIn(this)
    }

    private fun needReconnect(): Boolean {
        return isSocketFailed
            && isNetworkConnected
            && reconnectJob == null
            && canConnect()
    }

    private fun reconnect() {
        reconnectJob = launch {
            val currentTime = System.currentTimeMillis().milliseconds
            val diff = currentTime.minus(lastReconnectTry).inWholeMilliseconds
            if (diff < reconnectDelay) {
                delay(reconnectDelay - diff)
            }
            lastReconnectTry = System.currentTimeMillis().milliseconds

            tryConnect()
            reconnectJob = null
        }
    }

    private fun tryConnect() {
        if (canConnect()) {
            connect()
        }
    }

    private fun canConnect() = !isSocketOpened && isNetworkConnected

    fun connect() {
        if (webSocket == null && !isSocketOpened) {
            mutableSocketConnectionState.value = SocketState.Connecting

            webSocket = httpClient.newWebSocket(
                Request.Builder()
                    .url("wss://foxwoosh.space/ultra")
                    .build(),
                listener
            )
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "Bye")
    }

    private fun releaseSocket() {
        webSocket?.cancel()
        webSocket = null

//        job.cancelChildren()
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
        mutableTrackFlow.emit(message.mapToModel())
    }

    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.i(TAG, "onOpen, ${this@UltraWebSocketProvider.hashCode()}")

            mutableSocketConnectionState.value = SocketState.Connected

            webSocket.send(AppJson.encodeToString(getClientInfoMessage()))
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
            mutableSocketConnectionState.value = SocketState.Disconnected(code)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.i(TAG, "onFailure: ${t.message}")

            releaseSocket()
            mutableSocketConnectionState.value = SocketState.Failure(t)
        }
    }

    private fun getClientInfoMessage() = ParametrizedMessage(
        ParametrizedMessage.Type.SUBSCRIBE,
        mapOf(
            "info" to "${Build.MANUFACTURER} ${Build.MODEL} Android ${Build.VERSION.RELEASE} (api ${Build.VERSION.SDK_INT})"
        )
    )
}