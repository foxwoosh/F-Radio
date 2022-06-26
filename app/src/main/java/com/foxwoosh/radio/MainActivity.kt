@file:OptIn(ExperimentalAnimationApi::class)

package com.foxwoosh.radio

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.animation.LinearInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import com.foxwoosh.radio.ui.AppDestination
import com.foxwoosh.radio.ui.Insets
import com.foxwoosh.radio.ui.login.LoginScreen
import com.foxwoosh.radio.ui.player.PlayerScreen
import com.foxwoosh.radio.ui.settings.SettingsScreen
import com.foxwoosh.radio.ui.theme.FoxyRadioTheme
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        var waitForInitialDrawing = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        initiateSplash()

        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
            insets.also { Insets.value = it }
        }

        setContent {
            FoxyRadioTheme {
                val navController = rememberAnimatedNavController()

                AnimatedNavHost(
                    navController = navController,
                    startDestination = AppDestination.Player.route
                ) {
                    composable(route = AppDestination.Login.route) {
                        LoginScreen(
                            navigateToPlayer = { navController.navigate(AppDestination.Player) }
                        )
                    }
                    composable(route = AppDestination.Player.route) {
                        PlayerScreen(
                            navigateToSettings = { navController.navigate(AppDestination.Settings) }
                        )
                    }
                    composable(
                        route = AppDestination.Settings.route,
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentScope.SlideDirection.Up,
                                tween(300)
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                AnimatedContentScope.SlideDirection.Down,
                                tween(200)
                            )
                        }
                    ) {
                        SettingsScreen()
                    }
                }
            }
        }
    }

    private fun initiateSplash() {
        var animationCompleted = false
        val s = installSplashScreen()

        s.setOnExitAnimationListener { viewProvider ->
            val a = ValueAnimator.ofFloat(1f, 0f).apply {
                addUpdateListener {
                    viewProvider.view.alpha = it.animatedValue as Float
                }
                interpolator = LinearInterpolator()
                duration = 1000
                doOnEnd {
                    animationCompleted = true
                    viewProvider.remove()
                }
            }

            a.start()
        }

        s.setKeepOnScreenCondition { waitForInitialDrawing && !animationCompleted }
    }
}