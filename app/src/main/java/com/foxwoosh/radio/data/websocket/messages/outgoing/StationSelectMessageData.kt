package com.foxwoosh.radio.data.websocket.messages.outgoing

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class StationSelectMessageData(
    @SerialName("station") val stationCode: Int
)