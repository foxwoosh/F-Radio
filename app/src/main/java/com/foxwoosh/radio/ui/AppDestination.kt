package com.foxwoosh.radio.ui

sealed class AppDestination(val route: String) {
    object Login : AppDestination("navigation_screen_login")
    object Player : AppDestination("navigation_screen_player")
}