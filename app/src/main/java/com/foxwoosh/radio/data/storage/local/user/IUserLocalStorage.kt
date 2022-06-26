package com.foxwoosh.radio.data.storage.local.user

import com.foxwoosh.radio.domain.models.CurrentUser
import kotlinx.coroutines.flow.Flow

interface IUserLocalStorage {
    val user: Flow<CurrentUser?>

    suspend fun saveToken(token: String)

    suspend fun saveUser(id: Long, login: String, name: String, email: String)
}