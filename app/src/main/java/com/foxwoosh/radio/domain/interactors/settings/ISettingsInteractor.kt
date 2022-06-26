package com.foxwoosh.radio.domain.interactors.settings

import com.foxwoosh.radio.domain.models.CurrentUser
import kotlinx.coroutines.flow.Flow

interface ISettingsInteractor {
    val user: Flow<CurrentUser?>

    suspend fun register(login: String, password: String, name: String, email: String)

    suspend fun login(login: String, password: String)
}