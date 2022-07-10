@file:OptIn(ExperimentalAnimationApi::class)

package com.foxwoosh.radio

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.animation.LinearInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.foxwoosh.radio.ui.AppDestination
import com.foxwoosh.radio.ui.player.PlayerScreen
import com.foxwoosh.radio.ui.settings.SettingsScreen
import com.foxwoosh.radio.ui.settings.UserReportsScreen
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

        setContent {
            FoxyRadioTheme {
                val navController = rememberAnimatedNavController()

                AnimatedNavHost(
                    navController = navController,
                    startDestination = AppDestination.Player.route
                ) {
                    playerRoute(navController)
                    settingsRoute(navController)
                    userReportsRoute(navController)
                }
            }
        }
    }

    private fun NavGraphBuilder.playerRoute(navController: NavHostController) {
        composable(route = AppDestination.Player.route) {
            PlayerScreen(
                navigateToSettings = { navController.navigate(AppDestination.Settings) }
            )
        }
    }

    private fun NavGraphBuilder.userReportsRoute(navController: NavHostController) {
        composable(
            route = AppDestination.UserReports.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(500)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it / 2 },
                    animationSpec = tween(500)
                )
            }

        ) {
            UserReportsScreen(
                navigateBack = { navController.popBackStack() }
            )
        }
    }

    private fun NavGraphBuilder.settingsRoute(navController: NavHostController) {
        composable(
            route = AppDestination.Settings.route,
            enterTransition = {
                slideInVertically(initialOffsetY = { it })
            },
            exitTransition = {
                slideOutHorizontally(animationSpec = tween(500))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(500)
                )
            },
            popExitTransition = {
                slideOutVertically(targetOffsetY = { it })
            }
        ) {
            SettingsScreen(
                navigateBack = { navController.popBackStack() },
                navigateToUserReports = {
                    navController.navigate(AppDestination.UserReports)
                }
            )
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