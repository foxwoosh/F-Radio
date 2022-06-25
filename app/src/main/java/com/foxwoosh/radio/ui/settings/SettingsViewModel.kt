package com.foxwoosh.radio.ui.settings

import androidx.lifecycle.ViewModel
import com.foxwoosh.radio.domain.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {
    val user = MutableStateFlow<User?>(null)
}