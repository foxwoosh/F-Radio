package com.foxwoosh.radio.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foxwoosh.radio.R
import com.foxwoosh.radio.domain.interactors.settings.ISettingsInteractor
import com.foxwoosh.radio.domain.interactors.settings.exceptions.AuthDataException
import com.foxwoosh.radio.ui.settings.model.AuthFieldsState
import com.foxwoosh.radio.ui.settings.model.SettingsEvent
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val mutableAuthFieldsState = MutableStateFlow(AuthFieldsState())
    val authFieldsState = mutableAuthFieldsState.asStateFlow()

    val userState = interactor.user

    fun register(login: String, password: String, name: String, email: String) {
        viewModelScope.launch {
            try {
                mutableAuthFieldsState.update { it.toNone() }
                interactor.register(login, password, name, email)
            } catch (e: Exception) {
                when (e) {
                    is AuthDataException.Login -> {
                        mutableAuthFieldsState.update { it.toLogin() }
                        mutableEvents.emit(
                            SettingsEvent.Error(R.string.settings_auth_error_login_verification)
                        )
                    }
                    is AuthDataException.Password -> {
                        mutableAuthFieldsState.update { it.toPassword() }
                        mutableEvents.emit(
                            SettingsEvent.Error(R.string.settings_auth_error_password_verification)
                        )
                    }
                    is AuthDataException.Name -> {
                        mutableAuthFieldsState.update { it.toName() }
                        mutableEvents.emit(
                            SettingsEvent.Error(R.string.settings_auth_error_name_verification)
                        )
                    }
                    is AuthDataException.Email -> {
                        mutableAuthFieldsState.update { it.toEmail() }
                        mutableEvents.emit(
                            SettingsEvent.Error(R.string.settings_auth_error_email_verification)
                        )
                    }
                    is HttpException -> {
                        when (e.code()) {
                            409 -> {
                                mutableAuthFieldsState.update { it.toLogin() }
                                mutableEvents.emit(
                                    SettingsEvent.Error(R.string.settings_auth_error_login_conflict)
                                )
                            }
                            else -> mutableEvents.emit(SettingsEvent.Error(R.string.common_error))
                        }
                    }
                }
            }
        }
    }

    fun login(login: String, password: String) {
        viewModelScope.launch {
            try {
                interactor.login(login, password)
            } catch (e: Exception) {
                when (e) {
                    is AuthDataException.Login -> {
                        mutableAuthFieldsState.update { it.toLogin() }
                        mutableEvents.emit(
                            SettingsEvent.Error(R.string.settings_auth_error_login_verification)
                        )
                    }
                    is AuthDataException.Password -> {
                        mutableAuthFieldsState.update { it.toPassword() }
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
                }
            }
        }
    }
}