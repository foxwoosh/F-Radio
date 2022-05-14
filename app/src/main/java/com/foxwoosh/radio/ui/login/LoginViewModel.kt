package com.foxwoosh.radio.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {
    init {
        Log.i("DDLOG", "LoginViewModel init")
    }

    override fun onCleared() {
        super.onCleared()

        Log.i("DDLOG", "LoginViewModel onCleared")
    }

    fun saveLogin(login: String, password: String) {

    }
}