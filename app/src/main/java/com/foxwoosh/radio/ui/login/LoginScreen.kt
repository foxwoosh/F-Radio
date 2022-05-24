package com.foxwoosh.radio.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.foxwoosh.radio.ui.AppDestination
import com.foxwoosh.radio.ui.theme.FoxyRadioTheme

@Composable
fun LoginScreen(
    navigateToPlayer: () -> Unit
) {
    val viewModel = hiltViewModel<LoginViewModel>()

    var login by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = login,
                label = { Text(text = "Login") },
                singleLine = true,
                onValueChange = { login = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = password,
                label = { Text(text = "Password") },
                singleLine = true,
                onValueChange = { password = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.saveLogin(login, password)
                    navigateToPlayer()
                }
            ) {
                Text(text = "Login")
            }
        }
    }
}

@Preview
@Composable
fun Preview() {
    FoxyRadioTheme {
        LoginScreen {}
    }
}