package com.foxwoosh.radio.websocket

import com.foxwoosh.radio.api.ultra.responses.CurrentTrackResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class UltraWebSocketResponse(
    @SerialName("t") val type: String,
    @SerialName("d") val data: CurrentTrackResponse
)