package com.foxwoosh.radio.data.storage.remote.user

import com.foxwoosh.radio.data.api.ApiService
import com.foxwoosh.radio.data.api.foxy.requests.LoginRequest
import com.foxwoosh.radio.data.api.foxy.requests.RegisterRequest
import com.foxwoosh.radio.domain.models.CurrentUser
import com.foxwoosh.radio.md5
import javax.inject.Inject

class UserRemoteStorage @Inject constructor(
    private val apiService: ApiService
) : IUserRemoteStorage {
    override suspend fun register(
        login: String,
        password: String,
        name: String,
        email: String,
        onTokenReceived: suspend (String) -> Unit
    ): CurrentUser {
        val response = apiService
            .foxy
            .register(
                RegisterRequest(
                    login,
                    password.md5(),
                    email,
                    name
                )
            )

        onTokenReceived(response.token)

        return CurrentUser(response.id, response.login, response.name, response.email)
    }

    override suspend fun login(
        login: String,
        password: String,
        onTokenReceived: (String) -> Unit
    ): CurrentUser {
        val response = apiService
            .foxy
            .login(
                LoginRequest(
                    login,
                    password.md5()
                )
            )

        onTokenReceived(response.token)

        return CurrentUser(response.id, response.login, response.name, response.email)
    }
}