package com.foxwoosh.radio.ui.settings

import androidx.annotation.StringRes

sealed class SettingsEvent {
    data class Error(@StringRes val errorTextResId: Int) : SettingsEvent()
}