package com.foxwoosh.radio.api.musixmatch

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val body: Body
)