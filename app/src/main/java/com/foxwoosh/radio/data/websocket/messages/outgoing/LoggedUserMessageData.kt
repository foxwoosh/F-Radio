package com.foxwoosh.radio.data.websocket.messages.outgoing

import kotlinx.serialization.Serializable

@Serializable
data class LoggedUserMessageData(
    val id: Long
)