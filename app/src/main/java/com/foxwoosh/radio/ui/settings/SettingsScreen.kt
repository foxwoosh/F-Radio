package com.foxwoosh.radio.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foxwoosh.radio.R
import com.foxwoosh.radio.ui.*
import com.foxwoosh.radio.ui.settings.models.AuthFieldsErrorState
import com.foxwoosh.radio.ui.settings.models.AuthFieldsState
import com.foxwoosh.radio.ui.settings.models.SettingsEvent
import com.foxwoosh.radio.ui.theme.CodGray
import com.foxwoosh.radio.ui.theme.dp16
import com.foxwoosh.radio.ui.theme.dp32
import com.foxwoosh.radio.ui.theme.dp8
import com.foxwoosh.radio.ui.widgets.DoubleSelector
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val viewModel = hiltViewModel<SettingsViewModel>()
    val user by viewModel.userState.collectAsState()
    val authFieldsErrorState by viewModel.authFieldsErrorState.collectAsState()
    val authFieldsState by viewModel.authFieldsState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var authFormVisible by remember { mutableStateOf(false) }
    var logoutDialogVisible by remember { mutableStateOf(false) }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CodGray)
            .systemBarsPadding()
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            Text(
                text = stringResource(id = R.string.settings_hello),
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = dp16)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .singleCondition(user == null && !authFormVisible) {
                        clickable { authFormVisible = !authFormVisible }
                    }
                    .padding(horizontal = dp16, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = (user?.name ?: stringResource(id = R.string.settings_stranger))
                        + " \uD83E\uDD8A",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal,
                )
                if (user == null && !authFormVisible) {
                    Spacer(modifier = Modifier.width(dp8))
                    Text(
                        text = stringResource(R.string.settings_stranger_description),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(dp32))

            var selectedIndex by rememberSaveable { mutableStateOf(AuthPage.LOGIN.ordinal) }

            AnimatedVisibility(visible = authFormVisible) {
                AuthForm(
                    selectedIndex = selectedIndex,
                    onSelectIndex = { selectedIndex = it },
                    onLoginChange = { viewModel.setAuthField(AuthFieldsState.Type.LOGIN, it) },
                    onPasswordChange = {
                        viewModel.setAuthField(
                            AuthFieldsState.Type.PASSWORD,
                            it
                        )
                    },
                    onNameChange = { viewModel.setAuthField(AuthFieldsState.Type.NAME, it) },
                    onEmailChange = { viewModel.setAuthField(AuthFieldsState.Type.EMAIL, it) },
                    onLogin = { viewModel.login() },
                    onRegister = { viewModel.register() },
                    state = authFieldsState,
                    errorsState = authFieldsErrorState,
                    onAuthCloseAction = { authFormVisible = false }
                )
            }
        }

        if (user != null) {
            IconButton(
                onClick = { logoutDialogVisible = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
            ) {
                Icon(Icons.Filled.Logout, contentDescription = "Logout")
            }
        }

        if (logoutDialogVisible) {
            AlertDialog(
                onDismissRequest = { logoutDialogVisible = false },
                text = {
                    Text(text = "Sure?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            logoutDialogVisible = false
                            viewModel.logout()
                        }
                    ) {
                        Text(text = ("Yep"))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { logoutDialogVisible = false }
                    ) {
                        Text(text = ("Nope"))
                    }
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .imePadding()
        )
    }
}

@Composable
fun AuthForm(
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    onLoginChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    state: AuthFieldsState,
    errorsState: AuthFieldsErrorState,
    onAuthCloseAction: () -> Unit
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
            value = state.login,
            label = { Text(text = stringResource(R.string.settings_auth_field_login)) },
            singleLine = true,
            onValueChange = { if (it.length <= 20) onLoginChange(it) },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            isError = errorsState.loginError
        )

        Spacer(modifier = Modifier.height(dp16))

        var passwordVisible by remember { mutableStateOf(false) }
        TextField(
            value = state.password,
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
                        if (passwordVisible) {
                            Icons.Outlined.Visibility
                        } else {
                            Icons.Outlined.VisibilityOff
                        },
                        contentDescription = if (passwordVisible) {
                            "Hide password"
                        } else {
                            "Show password"
                        }
                    )
                }
            },
            isError = errorsState.passwordError
        )

        Spacer(modifier = Modifier.height(dp16))

        AnimatedVisibility(
            visible = selectedIndex == AuthPage.REGISTRATION.ordinal
        ) {
            TextField(
                value = state.name,
                label = { Text(text = stringResource(R.string.settings_auth_field_name)) },
                singleLine = true,
                onValueChange = { onNameChange(it) },
                modifier = Modifier.padding(bottom = dp16),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                isError = errorsState.nameError
            )
        }

        AnimatedVisibility(
            visible = selectedIndex == AuthPage.REGISTRATION.ordinal
        ) {
            TextField(
                value = state.email,
                label = { Text(text = stringResource(R.string.settings_auth_field_email)) },
                singleLine = true,
                onValueChange = { onEmailChange(it) },
                modifier = Modifier.padding(bottom = dp16),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onRegister() }),
                isError = errorsState.emailError
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

        Spacer(modifier = Modifier.height(dp8))

        Image(
            imageVector = Icons.Filled.KeyboardArrowUp,
            contentDescription = "Close auth form",
            colorFilter = ColorFilter.tint(Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onAuthCloseAction)
        )
    }
}

private enum class AuthPage {
    LOGIN, REGISTRATION
}