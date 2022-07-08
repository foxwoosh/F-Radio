package com.foxwoosh.radio.data.websocket

import android.os.Build
import com.foxwoosh.radio.AppJson
import com.foxwoosh.radio.BuildConfig
import com.foxwoosh.radio.data.websocket.messages.outgoing.*
import com.foxwoosh.radio.player.models.Station
import kotlinx.serialization.encodeToString
import okhttp3.WebSocket

fun WebSocket.sendClientInfo() {
    val message = WebSocketOutgoingMessage(
        WebSocketOutgoingMessage.Type.SUBSCRIBE,
        SubscriptionInitialMessageData(
            device = "${Build.MANUFACTURER} ${Build.MODEL}",
            os = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            appVersion = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
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

fun WebSocket.sendStationsSelect(station: Station?) {
    send(
        AppJson.encodeToString(
            WebSocketOutgoingMessage(
                WebSocketOutgoingMessage.Type.STATION_SELECT,
                StationSelectMessageData(station)
            )
        )
    )
}