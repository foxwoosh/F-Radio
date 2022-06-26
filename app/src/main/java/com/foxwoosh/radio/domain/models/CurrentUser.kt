package com.foxwoosh.radio.domain.models

data class CurrentUser(
    val id: Long,
    val login: String,
    val name: String,
    val email: String
)