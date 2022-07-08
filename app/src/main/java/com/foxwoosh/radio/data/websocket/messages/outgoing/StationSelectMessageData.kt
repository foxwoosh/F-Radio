package com.foxwoosh.radio.data.websocket.messages.outgoing

import com.foxwoosh.radio.player.models.Station

@kotlinx.serialization.Serializable
data class StationSelectMessageData(
    val station: Station?
)