package com.foxwoosh.radio.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foxwoosh.radio.R
import com.foxwoosh.radio.domain.interactors.settings.ISettingsInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val interactor: ISettingsInteractor
) : ViewModel() {
    private val mutableEventFlow = MutableSharedFlow<SettingsEvent>()
    val eventFlow = mutableEventFlow.asSharedFlow()

    val userState = interactor.user

    fun register(login: String, password: String, name: String, email: String) {
        viewModelScope.launch {
            try {
                interactor.register(login, password, name, email)
            } catch (e: Exception) {
                mutableEventFlow.emit(
                    SettingsEvent.Error(R.string.settings_error_registration)
                )
            }
        }
    }
}