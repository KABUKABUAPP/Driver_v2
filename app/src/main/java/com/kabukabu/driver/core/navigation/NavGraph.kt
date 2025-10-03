package com.kabukabu.driver.core.navigation

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
    object SupportDetail : Screen("support_detail")
    object SupportNew : Screen("support_new")
    object About : Screen("about")
    object RepairLoan : Screen("repair_loan")
    object Profile : Screen("profile")
}

sealed class NavArg(val key: String) {
    object Email : NavArg("email")
    object SupportId : NavArg("supportId")
} 