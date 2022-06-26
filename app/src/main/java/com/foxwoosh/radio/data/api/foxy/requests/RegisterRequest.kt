package com.foxwoosh.radio.data.api.foxy.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    @SerialName("login") val login: String,
    @SerialName("password") val passwordHash: String,
    @SerialName("email") val email: String,
    @SerialName("name") val name: String
)