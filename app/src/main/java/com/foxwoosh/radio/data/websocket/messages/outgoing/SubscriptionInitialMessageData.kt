package com.foxwoosh.radio.data.websocket.messages.outgoing

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionInitialMessageData(
    @SerialName("device") val device: String,
    @SerialName("os") val os: String,
    @SerialName("app_version") val appVersion: String
)