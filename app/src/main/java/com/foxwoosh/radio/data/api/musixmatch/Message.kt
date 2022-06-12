package com.foxwoosh.radio.data.api.musixmatch

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val body: Body
)