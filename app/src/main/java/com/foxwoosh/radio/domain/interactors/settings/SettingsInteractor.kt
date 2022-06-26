package com.foxwoosh.radio.domain.interactors.settings

import com.foxwoosh.radio.data.storage.local.user.IUserLocalStorage
import com.foxwoosh.radio.data.storage.remote.user.IUserRemoteStorage
import javax.inject.Inject

class SettingsInteractor @Inject constructor(
    private val userLocalStorage: IUserLocalStorage,
    private val userRemoteStorage: IUserRemoteStorage
) : ISettingsInteractor {
    override val user = userLocalStorage.user

    override suspend fun register(login: String, password: String, name: String, email: String) {
        val user = userRemoteStorage.register(login.trim(), password, name, email) {
            userLocalStorage.saveToken(it)
        }
        userLocalStorage.saveUser(user.id, user.login, user.name, user.email)
    }

    override suspend fun login(login: String, password: String) {
        TODO("Not yet implemented")
    }
}