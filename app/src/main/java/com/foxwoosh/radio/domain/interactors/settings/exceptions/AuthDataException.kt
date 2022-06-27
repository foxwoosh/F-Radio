package com.foxwoosh.radio.domain.interactors.settings.exceptions

sealed class AuthDataException : Exception() {
    object Login : AuthDataException()
    object Email : AuthDataException()
    object Name : AuthDataException()
    object Password : AuthDataException()
}