@file:OptIn(ExperimentalPagerApi::class)

package com.foxwoosh.radio.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foxwoosh.radio.R
import com.foxwoosh.radio.ui.*
import com.foxwoosh.radio.ui.theme.CodGray
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val insets by Insets.collectAsState()
    val scope = rememberCoroutineScope()

    val viewModel = hiltViewModel<SettingsViewModel>()
    val user by viewModel.userState.collectAsState(null)

    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.eventFlow.collectAsEffect {
        when (it) {
            is SettingsEvent.Error -> scope.launch {
                snackbarHostState.showSnackbar("FUCKING ERROR")
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        backgroundColor = CodGray,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = insets.systemBottom)
            )
        }
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
                text = (user?.name ?: stringResource(id = R.string.settings_stranger))
                    + " \uD83E\uDD8A",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(64.dp))

            var login by rememberSaveable { mutableStateOf("") }
            var password by rememberSaveable { mutableStateOf("") }

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
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.textFieldColors(

                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.register(login, password, "", "")
                }
            ) {
                Text(text = "Login")
            }
        }
    }
}

@Composable
fun AuthForm() {
    val pagerState = rememberPagerState()

    TabRow(
        selectedTabIndex = pagerState.currentPage,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier = Modifier
                    .pagerTabIndicatorOffset(pagerState, tabPositions),
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        AuthPage.values().forEach {
            Tab(
                text = {
                    Text(
                        text = stringResource(
                            id = when (it) {
                                AuthPage.LOGIN ->
                                    R.string.settings_page_login
                                AuthPage.REGISTER ->
                                    R.string.settings_page_register
                            }
                        ).uppercase()
                    )
                },
                selected = pagerState.currentPage == it.ordinal,
                onClick = {}
            )
        }
    }

    HorizontalPager(
        count = AuthPage.values().size,
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
    ) { pageIndex ->
        when (pageIndex) {
            AuthPage.LOGIN.ordinal -> {}
            AuthPage.REGISTER.ordinal -> {}
        }
    }
}

private enum class AuthPage {
    LOGIN, REGISTER
}