package com.foxwoosh.radio.ui.settings.models

data class AuthFieldsState(
    val login: String = "",
    val password: String = "",
    val name: String = "",
    val email: String = ""
) {
    fun clear() = copy(login = "", password = "", name = "", email = "")

    enum class Type {
        LOGIN, PASSWORD, NAME, EMAIL
    }
}