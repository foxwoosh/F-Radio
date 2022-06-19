package com.foxwoosh.radio

import android.animation.ValueAnimator
import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import android.view.animation.LinearInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
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

    companion object {
        var wait = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            initiateSplash()
//        }

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

    private fun initiateSplash() {
        var animationCompleted = false
        val s = installSplashScreen()

        s.setOnExitAnimationListener { viewProvider ->
            val a = ValueAnimator.ofFloat(1f, 0f).apply {
                addUpdateListener {
                    viewProvider.view.alpha = it.animatedValue as Float
                }
                interpolator = LinearInterpolator()
                doOnEnd {
                    animationCompleted = true
                    viewProvider.remove()
                }
            }

            a.start()
        }

        s.setKeepOnScreenCondition { wait && !animationCompleted }
    }
}