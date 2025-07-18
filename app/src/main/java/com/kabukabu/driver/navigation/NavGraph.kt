package com.kabukabu.driver.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object OtpVerification : Screen("otp_verification")
    object Home : Screen("home")
}

sealed class NavArg(val key: String) {
    object Email : NavArg("email")
} 