package com.foxwoosh.radio.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foxwoosh.radio.R
import com.foxwoosh.radio.ui.*
import com.foxwoosh.radio.ui.settings.model.AuthFieldsState
import com.foxwoosh.radio.ui.settings.model.SettingsEvent
import com.foxwoosh.radio.ui.theme.CodGray
import com.foxwoosh.radio.ui.theme.dp16
import com.foxwoosh.radio.ui.theme.dp32
import com.foxwoosh.radio.ui.theme.dp8
import com.foxwoosh.radio.ui.widgets.DoubleSelector
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val insets by Insets.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val viewModel = hiltViewModel<SettingsViewModel>()
    val user by viewModel.userState.collectAsState(null)
    val authFieldsState by viewModel.authFieldsState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var authFormVisible by remember { mutableStateOf(false) }

    viewModel.events.collectAsEffect {
        when (it) {
            is SettingsEvent.Error -> scope.launch {
                snackbarHostState.showSnackbar(context.getString(it.errorTextResId))
            }
        }
    }

    LaunchedEffect(key1 = user) {
        if (authFormVisible && user != null) {
            authFormVisible = false
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
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
                .fillMaxSize()
                .padding(
                    start = dp16,
                    end = dp16,
                    top = insets.statusBar,
                    bottom = insets.systemBottom
                )
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(id = R.string.settings_hello),
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Medium
            )
            Row(
                modifier = Modifier.clickable { authFormVisible = !authFormVisible },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = (user?.name ?: stringResource(id = R.string.settings_stranger))
                        + " \uD83E\uDD8A",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.width(dp8))
                Text(
                    text = stringResource(
                        if (user == null)
                            R.string.settings_stranger_description
                        else
                            R.string.settings_stranger_description
                    ),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(dp32))

            var login by rememberSaveable { mutableStateOf("") }
            var password by rememberSaveable { mutableStateOf("") }
            var name by rememberSaveable { mutableStateOf("") }
            var email by rememberSaveable { mutableStateOf("") }

            var selectedIndex by rememberSaveable { mutableStateOf(AuthPage.LOGIN.ordinal) }

            AnimatedVisibility(visible = authFormVisible) {
                AuthForm(
                    selectedIndex = selectedIndex,
                    login = login,
                    password = password,
                    name = name,
                    email = email,
                    onSelectIndex = { selectedIndex = it },
                    onLoginChange = { login = it },
                    onPasswordChange = { password = it },
                    onNameChange = { name = it },
                    onEmailChange = { email = it },
                    onLogin = { viewModel.login(login, password) },
                    onRegister = { viewModel.register(login, password, name, email) },
                    state = authFieldsState
                )
            }
        }
    }
}

@Composable
fun AuthForm(
    selectedIndex: Int,
    login: String,
    password: String,
    name: String,
    email: String,
    onSelectIndex: (Int) -> Unit,
    onLoginChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    state: AuthFieldsState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DoubleSelector(
            selectedIndex = selectedIndex,
            firstItemText = stringResource(id = R.string.settings_page_title_login),
            secondItemText = stringResource(id = R.string.settings_page_title_registration),
            onSelectAction = { onSelectIndex(it) }
        )

        Spacer(modifier = Modifier.height(dp16))

        TextField(
            value = login,
            label = { Text(text = stringResource(R.string.settings_auth_field_login)) },
            singleLine = true,
            onValueChange = { if (it.length <= 20) onLoginChange(it) },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            isError = state.loginError
        )

        Spacer(modifier = Modifier.height(dp16))

        var passwordVisible by remember { mutableStateOf(false) }
        TextField(
            value = password,
            label = { Text(text = stringResource(R.string.settings_auth_field_password)) },
            singleLine = true,
            onValueChange = { if (it.length <= 50) onPasswordChange(it) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = if (selectedIndex == AuthPage.REGISTRATION.ordinal) {
                    ImeAction.Next
                } else {
                    ImeAction.Done
                }
            ),
            keyboardActions = KeyboardActions(onDone = { onLogin() }),
            visualTransformation = if (passwordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            colors = TextFieldDefaults.textFieldColors(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painterResource(
                            if (passwordVisible) {
                                R.drawable.ic_eye_opened
                            } else {
                                R.drawable.ic_eye_closed
                            }
                        ),
                        contentDescription = if (passwordVisible) {
                            "Hide password"
                        } else {
                            "Show password"
                        }
                    )
                }
            },
            isError = state.passwordError
        )

        Spacer(modifier = Modifier.height(dp16))

        AnimatedVisibility(
            visible = selectedIndex == AuthPage.REGISTRATION.ordinal
        ) {
            TextField(
                value = name,
                label = { Text(text = stringResource(R.string.settings_auth_field_name)) },
                singleLine = true,
                onValueChange = { onNameChange(it) },
                modifier = Modifier.padding(bottom = dp16),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                isError = state.nameError
            )
        }

        AnimatedVisibility(
            visible = selectedIndex == AuthPage.REGISTRATION.ordinal
        ) {
            TextField(
                value = email,
                label = { Text(text = stringResource(R.string.settings_auth_field_email)) },
                singleLine = true,
                onValueChange = { onEmailChange(it) },
                modifier = Modifier.padding(bottom = dp16),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onRegister() }),
                isError = state.emailError
            )
        }

        WideButton(
            onClick = {
                when (selectedIndex) {
                    AuthPage.LOGIN.ordinal -> onLogin()
                    AuthPage.REGISTRATION.ordinal -> onRegister()
                }
            },
            text = stringResource(
                when (selectedIndex) {
                    AuthPage.LOGIN.ordinal -> R.string.settings_auth_button_login
                    AuthPage.REGISTRATION.ordinal -> R.string.settings_auth_button_register
                    else -> throw IllegalArgumentException()
                }
            )
        )
    }
}

private enum class AuthPage {
    LOGIN, REGISTRATION
}