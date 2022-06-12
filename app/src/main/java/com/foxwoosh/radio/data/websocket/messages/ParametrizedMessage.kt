package com.foxwoosh.radio.data.websocket.messages

import kotlinx.serialization.Serializable

@Serializable
data class ParametrizedMessage(
    val type: Type,
    val params: Map<String, String>?
) {
    operator fun get(key: String) = params?.get(key)

    enum class Type {
        SUBSCRIBE
    }
}