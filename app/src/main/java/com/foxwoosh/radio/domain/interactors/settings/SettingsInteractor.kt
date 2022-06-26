package com.foxwoosh.radio.domain.interactors.settings

import com.foxwoosh.radio.data.storage.local.user.IUserLocalStorage
import com.foxwoosh.radio.data.storage.remote.user.IUserRemoteStorage
import com.foxwoosh.radio.di.modules.SettingsInteractorScope
import com.foxwoosh.radio.md5
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@ViewModelScoped
class SettingsInteractor @Inject constructor(
    private val userLocalStorage: IUserLocalStorage,
    private val userRemoteStorage: IUserRemoteStorage
) : ISettingsInteractor {
    override val user = userLocalStorage.currentUser

    override suspend fun register(login: String, password: String, name: String, email: String) {
        val user = userRemoteStorage.register(login.trim(), password.md5(), name, email) {
            userLocalStorage.saveToken(it)
        }
        userLocalStorage.saveCurrentUser(user)
    }

    override suspend fun login(login: String, password: String) {
        val user = userRemoteStorage.login(login.trim(), password.md5()) {
            userLocalStorage.saveToken(it)
        }
        userLocalStorage.saveCurrentUser(user)
    }
}