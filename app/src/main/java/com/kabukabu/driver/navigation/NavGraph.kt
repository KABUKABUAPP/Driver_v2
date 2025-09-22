package com.kabukabu.driver.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object OtpVerification : Screen("otp_verification")
    object Home : Screen("home")
    object Wallet : Screen("wallet")
    object PaymentHistory : Screen("payment_history")
    object SharpPayment : Screen("sharp_payment")
    object Analytics : Screen("analytics")
    object MyTrips : Screen("my_trips")
    object Promotions : Screen("promotions")
    object Support : Screen("support")
    object About : Screen("about")
    object RepairLoan : Screen("repair_loan")
}

sealed class NavArg(val key: String) {
    object Email : NavArg("email")
} 