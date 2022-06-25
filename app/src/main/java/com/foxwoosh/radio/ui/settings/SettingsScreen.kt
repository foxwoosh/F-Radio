package com.foxwoosh.radio.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foxwoosh.radio.ui.Insets
import com.foxwoosh.radio.ui.navigationBar
import com.foxwoosh.radio.ui.statusBar
import com.foxwoosh.radio.ui.theme.CodGray
import com.foxwoosh.radio.R

@Composable
fun SettingsScreen() {
    val viewModel = hiltViewModel<SettingsViewModel>()
    val user by viewModel.user.collectAsState()

    val insets by Insets.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        backgroundColor = CodGray
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(
                    top = insets.statusBar,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = insets.navigationBar
                )
        ) {
            Text(
                text = stringResource(id = R.string.settings_hello),
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = (user?.name ?: stringResource(id = R.string.settings_stranger)) + " \uD83E\uDD8A",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}