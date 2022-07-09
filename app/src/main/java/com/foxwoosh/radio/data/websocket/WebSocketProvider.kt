package com.foxwoosh.radio.data.websocket

import android.util.Log
import com.foxwoosh.radio.AppJson
import com.foxwoosh.radio.BuildConfig
import com.foxwoosh.radio.data.storage.local.user.IUserLocalStorage
import com.foxwoosh.radio.data.websocket.messages.incoming.LyricsReportUpdateIncomingMessage
import com.foxwoosh.radio.data.websocket.messages.incoming.SongDataIncomingMessage
import com.foxwoosh.radio.data.websocket.messages.incoming.WebSocketIncomingMessage
import com.foxwoosh.radio.providers.network_state_provider.NetworkState
import com.foxwoosh.radio.providers.network_state_provider.NetworkStateProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class WebSocketProvider @Inject constructor(
    private val networkStateProvider: NetworkStateProvider,
    private val userLocalStorage: IUserLocalStorage
) : CoroutineScope {

    private companion object {
        const val TAG = "WebSocketProvider"
    }

    private val job = SupervisorJob()
    override val coroutineContext = job + Dispatchers.IO

    private val url = StringBuilder()
        .append(if (BuildConfig.DEBUG) "ws://" else "wss://")
        .append(BuildConfig.BASE_URL)
        .append("/ultra")
        .toString()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null

    private val mutableMessagesFlow = MutableSharedFlow<WebSocketIncomingMessage>()
    val messagesFlow = mutableMessagesFlow.asSharedFlow()

    private val mutableSocketConnectionState =
        MutableStateFlow<SocketState>(SocketState.Initial)
    val socketConnectionState = mutableSocketConnectionState.asStateFlow()

    private var closedByUser = false

    private val reconnectDelay = 5_000
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

        connect()
    }

    private fun needReconnect(): Boolean {
        return isSocketFailed
            && !closedByUser
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

    private fun canConnect() = !closedByUser && !isSocketOpened && isNetworkConnected

    fun connect() {
        if (webSocket == null && !isSocketOpened) {
            closedByUser = false

            mutableSocketConnectionState.value = SocketState.Connecting

            webSocket = httpClient.newWebSocket(
                Request.Builder()
                    .url(url)
                    .build(),
                getSocketListener()
            )
        }
    }

    fun disconnect() {
        closedByUser = true
        webSocket?.close(1000, "Bye")
    }

    suspend fun sendMessage(sendAction: WebSocket.() -> Unit) {
        withContext(coroutineContext) {
            webSocket?.let {
                onConnected { sendAction(it) }
            } ?: run {
                Log.e(TAG, "Socket does not exist")
            }
        }
    }

    private fun releaseSocket() {
        webSocket?.cancel()
        webSocket = null
    }

    private fun getResponse(text: String): WebSocketIncomingMessage? {
        val type = WebSocketIncomingMessage.Type.fromValue(
            AppJson.parseToJsonElement(text).jsonObject["type"]?.jsonPrimitive?.content
        )

        return when (type) {
            WebSocketIncomingMessage.Type.SONG_DATA ->
                AppJson.decodeFromString<SongDataIncomingMessage>(text)
            WebSocketIncomingMessage.Type.REPORT_UPDATE -> {
                AppJson.decodeFromString<LyricsReportUpdateIncomingMessage>(text)
            }
            else -> null
        }
    }

    private fun getSocketListener() = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.i(TAG, "onOpen")

            mutableSocketConnectionState.value = SocketState.Connected

            launch {
                webSocket.sendClientInfo()

                userLocalStorage
                    .currentUser
                    .firstOrNull()
                    ?.let { webSocket.sendLoggedUserData(it.id) }
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            launch {
                getResponse(text)?.let {
                    Log.i("DDLOG", "onMessage: $it")
                    mutableMessagesFlow.emit(it)
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

    private suspend fun onConnected(action: suspend () -> Unit) {
        if (isSocketOpened) {
            action()
        } else {
            socketConnectionState
                .filter { it is SocketState.Connected }
                .take(1)
                .collect {
                    action()
                }
        }
    }
}