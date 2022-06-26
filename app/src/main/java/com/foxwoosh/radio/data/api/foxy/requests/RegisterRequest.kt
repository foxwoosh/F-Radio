package com.foxwoosh.radio.data.api.foxy.requests

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val login: String,
    val password: String,
    val email: String,
    val name: String
)