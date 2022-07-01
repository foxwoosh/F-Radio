package com.foxwoosh.radio.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foxwoosh.radio.R
import com.foxwoosh.radio.domain.interactors.settings.SettingsConstants
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
fun SettingsScreen(
    navigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val viewModel = hiltViewModel<SettingsViewModel>()
    val user by viewModel.userState.collectAsState()
    val authFieldsErrorState by viewModel.authFieldsErrorState.collectAsState()
    val authFieldsState by viewModel.authFieldsState.collectAsState()
    val authProgress by viewModel.authProgress.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var authFormVisible by remember { mutableStateOf(false) }
    var logoutDialogVisible by remember { mutableStateOf(false) }

    viewModel.events.collectAsEffect {
        snackbarHostState.currentSnackbarData?.dismiss()

        when (it) {
            is SettingsEvent.Error -> scope.launch {
                snackbarHostState.showSnackbar(context.getString(it.errorTextResId))
            }
        }
    }

    LaunchedEffect(user) {
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
            IconButton(onClick = navigateBack) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Close settings")
            }
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
                    .clickable(
                        onClick = { authFormVisible = !authFormVisible },
                        enabled = user == null && !authFormVisible
                    )
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
                    onAuthCloseAction = { authFormVisible = false },
                    loading = authProgress
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
                    Text(
                        text = stringResource(R.string.settings_auth_logout_text),
                        fontSize = 16.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            logoutDialogVisible = false
                            viewModel.logout()
                        }
                    ) { Text(text = stringResource(R.string.settings_auth_logout_positive_button)) }
                },
                dismissButton = {
                    TextButton(
                        onClick = { logoutDialogVisible = false }
                    ) {
                        Text(
                            text = stringResource(R.string.settings_auth_logout_negative_button),
                            color = Color.White
                        )
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
    onAuthCloseAction: () -> Unit,
    loading: Boolean
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
            onSelectAction = { onSelectIndex(it) },
            enabled = !loading
        )

        Spacer(modifier = Modifier.height(dp16))

        TextField(
            value = state.login,
            enabled = !loading,
            label = { Text(text = stringResource(R.string.settings_auth_field_login)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            isError = errorsState.loginError,
            onValueChange = {
                if (it.length <= SettingsConstants.LOGIN_MAX_LENGTH) onLoginChange(it)
            }
        )

        Spacer(modifier = Modifier.height(dp16))

        PasswordAuthField(
            password = state.password,
            loading = loading,
            onPasswordChange = onPasswordChange,
            selectedIndex = selectedIndex,
            onLogin = onLogin,
            error = errorsState.passwordError
        )

        Spacer(modifier = Modifier.height(dp16))

        AdditionalRegistrationFields(
            selectedIndex = selectedIndex,
            loading = loading,
            onNameChange = onNameChange,
            onEmailChange = onEmailChange,
            onRegister = onRegister,
            name = state.name,
            email = state.email,
            nameError = errorsState.nameError,
            emailError = errorsState.emailError
        )

        LoadingButton(
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
            ),
            loading = loading
        )

        Spacer(modifier = Modifier.height(dp8))

        Image(
            imageVector = Icons.Filled.KeyboardArrowUp,
            contentDescription = "Close auth form",
            colorFilter = ColorFilter.tint(Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = onAuthCloseAction,
                    enabled = !loading
                )
        )
    }
}

@Composable
private fun ColumnScope.AdditionalRegistrationFields(
    selectedIndex: Int,
    name: String,
    email: String,
    loading: Boolean,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onRegister: () -> Unit,
    nameError: Boolean,
    emailError: Boolean
) {
    AnimatedVisibility(
        visible = selectedIndex == AuthPage.REGISTRATION.ordinal
    ) {
        TextField(
            value = name,
            enabled = !loading,
            label = { Text(text = stringResource(R.string.settings_auth_field_name)) },
            singleLine = true,
            onValueChange = { onNameChange(it) },
            modifier = Modifier.padding(bottom = dp16),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                capitalization = KeyboardCapitalization.Words
            ),
            isError = nameError
        )
    }

    AnimatedVisibility(
        visible = selectedIndex == AuthPage.REGISTRATION.ordinal
    ) {
        TextField(
            value = email,
            enabled = !loading,
            label = { Text(text = stringResource(R.string.settings_auth_field_email)) },
            singleLine = true,
            onValueChange = { onEmailChange(it) },
            modifier = Modifier.padding(bottom = dp16),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onRegister() }),
            isError = emailError
        )
    }
}

@Composable
private fun PasswordAuthField(
    password: String,
    loading: Boolean,
    onPasswordChange: (String) -> Unit,
    selectedIndex: Int,
    onLogin: () -> Unit,
    error: Boolean
) {
    var passwordVisible by remember { mutableStateOf(false) }

    TextField(
        value = password,
        enabled = !loading,
        label = { Text(text = stringResource(R.string.settings_auth_field_password)) },
        singleLine = true,
        keyboardActions = KeyboardActions(onDone = { onLogin() }),
        colors = TextFieldDefaults.textFieldColors(),
        onValueChange = {
            if (it.length <= SettingsConstants.PASSWORD_MAX_LENGTH) onPasswordChange(it)
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = if (selectedIndex == AuthPage.REGISTRATION.ordinal) {
                ImeAction.Next
            } else {
                ImeAction.Done
            }
        ),
        visualTransformation = if (passwordVisible)
            VisualTransformation.None
        else
            PasswordVisualTransformation(),
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
        isError = error
    )
}

private enum class AuthPage {
    LOGIN, REGISTRATION
}