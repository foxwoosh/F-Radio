package com.foxwoosh.radio

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.foxwoosh.radio.ui.AppDestination
import com.foxwoosh.radio.ui.login.LoginScreen
import com.foxwoosh.radio.ui.player.PlayerScreen
import com.foxwoosh.radio.ui.theme.FoxyRadioTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(
            window.decorView
        ) { _, insets ->
            Insets.value = insets
            insets
        }

        setContent {
            FoxyRadioTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = AppDestination.Player.route
                ) {
                    composable(route = AppDestination.Login.route) {
                        LoginScreen {
                            navController.navigate(AppDestination.Player.route)
                        }
                    }
                    composable(route = AppDestination.Player.route) {
                        PlayerScreen()
                    }
                }
            }
        }
    }
}