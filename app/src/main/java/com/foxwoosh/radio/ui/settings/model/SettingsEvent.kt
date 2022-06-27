package com.foxwoosh.radio.ui.settings.model

import androidx.annotation.StringRes

sealed class SettingsEvent {
    data class Error(@StringRes val errorTextResId: Int) : SettingsEvent()
    object EmptyName : SettingsEvent()
    object EmptyPassword : SettingsEvent()
    object ConflictName : SettingsEvent()
    object WrongPassword : SettingsEvent()
}