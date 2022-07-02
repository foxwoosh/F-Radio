package com.foxwoosh.radio.data.websocket

import android.os.Build
import com.foxwoosh.radio.AppJson
import com.foxwoosh.radio.BuildConfig
import com.foxwoosh.radio.data.websocket.messages.outgoing.EmptyOutgoingMessageData
import com.foxwoosh.radio.data.websocket.messages.outgoing.LoggedUserMessageData
import com.foxwoosh.radio.data.websocket.messages.outgoing.SubscriptionInitialMessageData
import com.foxwoosh.radio.data.websocket.messages.outgoing.WebSocketOutgoingMessage
import kotlinx.serialization.encodeToString
import okhttp3.WebSocket

fun WebSocket.sendClientInfo() {
    val message = WebSocketOutgoingMessage(
        WebSocketOutgoingMessage.Type.SUBSCRIBE,
        SubscriptionInitialMessageData(
            "${Build.MANUFACTURER} ${Build.MODEL}",
            "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        )
    )

    send(AppJson.encodeToString(message))
}

fun WebSocket.sendLoggedUserData(id: Long) {
    val message = WebSocketOutgoingMessage(
        WebSocketOutgoingMessage.Type.LOGGED_USER_DATA,
        LoggedUserMessageData(id)
    )

    send(AppJson.encodeToString(message))
}

fun WebSocket.sendLogout() {
    send(
        AppJson.encodeToString(
            WebSocketOutgoingMessage(
                WebSocketOutgoingMessage.Type.USER_LOGOUT,
                EmptyOutgoingMessageData
            )
        )
    )
}