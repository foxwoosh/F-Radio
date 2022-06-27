package com.foxwoosh.radio.ui.settings.model

data class AuthFieldsState(
    val loginError: Boolean = false,
    val passwordError: Boolean = false,
    val nameError: Boolean = false,
    val emailError: Boolean = false
) {
    fun toLogin() = copy(loginError = true)

    fun toPassword() = copy(passwordError = true)

    fun toName() = copy(nameError = true)

    fun toEmail() = copy(emailError = true)

    fun toNone() = copy(
        loginError = false,
        passwordError = false,
        nameError = false,
        emailError = false
    )
}