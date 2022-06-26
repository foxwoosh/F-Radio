package com.foxwoosh.radio.data.storage.remote.user

import com.foxwoosh.radio.domain.models.CurrentUser

interface IUserRemoteStorage {
    suspend fun register(
        login: String,
        password: String,
        name: String,
        email: String,
        onTokenReceived: suspend (String) -> Unit
    ): CurrentUser

    suspend fun login(
        login: String,
        password: String,
        onTokenReceived: (String) -> Unit
    ): CurrentUser
}