package com.foxwoosh.radio.api.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckIDResponse(
    @SerialName("uniqueid") val uniqueID: String
)