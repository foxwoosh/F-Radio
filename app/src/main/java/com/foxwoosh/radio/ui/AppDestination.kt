package com.foxwoosh.radio.ui

sealed class AppDestination(val route: String) {
    object Player : AppDestination("navigation_screen_player")
    object Settings : AppDestination("navigation_screen_settings")
    object UserReports : AppDestination("navigation_user_reports")
}