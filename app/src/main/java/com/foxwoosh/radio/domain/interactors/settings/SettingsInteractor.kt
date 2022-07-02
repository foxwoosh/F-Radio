package com.foxwoosh.radio.domain.interactors.settings

import com.foxwoosh.radio.data.storage.local.user.IUserLocalStorage
import com.foxwoosh.radio.data.storage.remote.user.IUserRemoteStorage
import com.foxwoosh.radio.domain.interactors.settings.exceptions.AuthDataException
import com.foxwoosh.radio.md5
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class SettingsInteractor @Inject constructor(
    private val userLocalStorage: IUserLocalStorage,
    private val userRemoteStorage: IUserRemoteStorage
) : ISettingsInteractor {
    override val user = userLocalStorage.currentUser

    private val loginRegex = Regex("[a-zA-Z0-9]*")
    private val emailRegex = Regex("(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+" +
            "(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)" +
            "*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])" +
            "*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])" +
            "?|\\[(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])\\.){3}" +
            "(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:" +
            "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])")

    override suspend fun register(login: String, password: String, name: String, email: String) {
        if (login.isEmpty()
            || login.length < SettingsConstants.LOGIN_MIN_LENGTH
            || login.matches(loginRegex).not()
        ) throw AuthDataException.Login

        if (password.isEmpty()
            || password.length < SettingsConstants.PASSWORD_MIN_LENGTH
        ) throw AuthDataException.Password

        if (name.isEmpty()) throw AuthDataException.Name

        if (email.isEmpty()
            || email.matches(emailRegex).not()
        ) throw AuthDataException.Email

        val user = userRemoteStorage.register(login.trim(), password.md5(), name, email) {
            userLocalStorage.saveToken(it)
        }
        userLocalStorage.saveCurrentUser(user)
    }

    override suspend fun login(login: String, password: String) {
        if (login.isEmpty()
            || login.length < SettingsConstants.LOGIN_MIN_LENGTH
            || login.matches(loginRegex).not()
        ) throw AuthDataException.Login

        if (password.isEmpty()
            || password.length < SettingsConstants.PASSWORD_MIN_LENGTH
        ) throw AuthDataException.Password

        val user = userRemoteStorage.login(login.trim(), password.md5()) {
            userLocalStorage.saveToken(it)
        }
        userLocalStorage.saveCurrentUser(user)
    }

    override suspend fun logout() {
        userLocalStorage.removeCurrentUser()
        userLocalStorage.removeToken()
        userRemoteStorage.logout()
    }
}