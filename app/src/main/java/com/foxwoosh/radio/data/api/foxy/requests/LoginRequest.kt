package com.foxwoosh.radio.data.api.foxy.requests

import kotlinx.serialization.Serializable

@Serializable
class LoginRequest(
    val login: String,
    val password: String
)