package com.foxwoosh.radio.data.api.foxy.responses

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val id: Long,
    val name: String,
    val login: String,
    val email: String,
    val token: String
)