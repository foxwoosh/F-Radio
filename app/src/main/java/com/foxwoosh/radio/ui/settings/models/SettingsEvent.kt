package com.foxwoosh.radio.ui.settings.models

import androidx.annotation.StringRes

sealed class SettingsEvent {
    data class Error(@StringRes val errorTextResId: Int) : SettingsEvent()
}