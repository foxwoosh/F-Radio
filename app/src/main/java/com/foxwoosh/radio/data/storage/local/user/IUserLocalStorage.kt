package com.foxwoosh.radio.data.storage.local.user

import com.foxwoosh.radio.domain.models.CurrentUser
import kotlinx.coroutines.flow.Flow

interface IUserLocalStorage {
    val currentUser: Flow<CurrentUser?>

    suspend fun getToken(): String?

    suspend fun saveToken(token: String)

    suspend fun removeToken()

    suspend fun saveCurrentUser(user: CurrentUser)

    suspend fun removeCurrentUser()
}