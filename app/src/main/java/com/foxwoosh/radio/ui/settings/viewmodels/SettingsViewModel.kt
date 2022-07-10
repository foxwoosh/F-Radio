package com.foxwoosh.radio.ui.settings.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foxwoosh.radio.R
import com.foxwoosh.radio.domain.interactors.settings.ISettingsInteractor
import com.foxwoosh.radio.domain.interactors.settings.exceptions.AuthDataException
import com.foxwoosh.radio.ui.settings.models.AuthFieldsErrorUiState
import com.foxwoosh.radio.ui.settings.models.AuthFieldsUiState
import com.foxwoosh.radio.ui.settings.models.SettingsEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val interactor: ISettingsInteractor
) : ViewModel() {
    private val mutableEvents = MutableSharedFlow<SettingsEvent>()
    val events = mutableEvents.asSharedFlow()

    private val mutableAuthFieldsErrorState = MutableStateFlow(AuthFieldsErrorUiState())
    val authFieldsErrorState = mutableAuthFieldsErrorState.asStateFlow()

    private val mutableAuthFieldsState = MutableStateFlow(AuthFieldsUiState())
    val authFieldsState = mutableAuthFieldsState.asStateFlow()

    private val mutableAuthProgress = MutableStateFlow(false)
    val authProgress = mutableAuthProgress.asStateFlow()

    val userState = interactor.user.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = null
    )

    fun register() {
        viewModelScope.launch {
            try {
                mutableAuthProgress.value = true

                mutableAuthFieldsErrorState.update { it.toNone() }
                with(authFieldsState.value) {
                    interactor.register(login, password, name, email)
                }

                delay(1000)
                mutableAuthFieldsState.update { it.clear() }
                mutableAuthProgress.value = false
            } catch (e: Exception) {
                mutableAuthProgress.value = false
                handleRegistrationError(e)
            }
        }
    }

    fun login() {
        viewModelScope.launch {
            try {
                mutableAuthProgress.value = true

                with(authFieldsState.value) {
                    interactor.login(login, password)
                }

                delay(1000)
                mutableAuthFieldsState.update { it.clear() }
                mutableAuthProgress.value = false
            } catch (e: Exception) {
                mutableAuthProgress.value = false
                handleLoginError(e)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            interactor.logout()
        }
    }

    fun setAuthField(type: AuthFieldsUiState.Type, data: String) {
        mutableAuthFieldsState.update {
            when (type) {
                AuthFieldsUiState.Type.LOGIN -> it.copy(login = data)
                AuthFieldsUiState.Type.PASSWORD -> it.copy(password = data)
                AuthFieldsUiState.Type.NAME -> it.copy(name = data)
                AuthFieldsUiState.Type.EMAIL -> it.copy(email = data)
            }
        }
        if (mutableAuthFieldsErrorState.value.any()) {
            mutableAuthFieldsErrorState.update { it.toNone() }
        }
    }

    private suspend fun handleRegistrationError(e: Exception) {
        when (e) {
            is AuthDataException.Login -> {
                mutableAuthFieldsErrorState.update { it.toLogin() }
                mutableEvents.emit(
                    SettingsEvent.Error(R.string.settings_auth_error_login_verification)
                )
            }
            is AuthDataException.Password -> {
                mutableAuthFieldsErrorState.update { it.toPassword() }
                mutableEvents.emit(
                    SettingsEvent.Error(R.string.settings_auth_error_password_verification)
                )
            }
            is AuthDataException.Name -> {
                mutableAuthFieldsErrorState.update { it.toName() }
                mutableEvents.emit(
                    SettingsEvent.Error(R.string.settings_auth_error_name_verification)
                )
            }
            is AuthDataException.Email -> {
                mutableAuthFieldsErrorState.update { it.toEmail() }
                mutableEvents.emit(
                    SettingsEvent.Error(R.string.settings_auth_error_email_verification)
                )
            }
            is HttpException -> {
                when (e.code()) {
                    409 -> {
                        mutableAuthFieldsErrorState.update { it.toLogin() }
                        mutableEvents.emit(
                            SettingsEvent.Error(R.string.settings_auth_error_login_conflict)
                        )
                    }
                    else -> mutableEvents.emit(SettingsEvent.Error(R.string.common_error))
                }
            }
            else -> mutableEvents.emit(SettingsEvent.Error(R.string.common_error))
        }
    }

    private suspend fun handleLoginError(e: Exception) {
        when (e) {
            is AuthDataException.Login -> {
                mutableAuthFieldsErrorState.update { it.toLogin() }
                mutableEvents.emit(
                    SettingsEvent.Error(R.string.settings_auth_error_login_verification)
                )
            }
            is AuthDataException.Password -> {
                mutableAuthFieldsErrorState.update { it.toPassword() }
                mutableEvents.emit(
                    SettingsEvent.Error(R.string.settings_auth_error_password_verification)
                )
            }
            is HttpException -> {
                when (e.code()) {
                    403 -> mutableEvents.emit(
                        SettingsEvent.Error(R.string.settings_auth_error_wrong_credentials)
                    )
                    else -> mutableEvents.emit(SettingsEvent.Error(R.string.common_error))
                }
            }
            else -> mutableEvents.emit(SettingsEvent.Error(R.string.common_error))
        }
    }
}