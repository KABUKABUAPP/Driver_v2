package com.kabukabu.driver.core.navigation


sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object SplashScreenCover : Screen("/splash_screen-cover")
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
    object DriverBioDataScreen : Screen("/driver-biodata-screen")
    object SelectVehicleScreen : Screen("/select-vehicle-screen")
    object KabuRideSelfieVerificationScreen : Screen("/kabu-ride-selfie-verification-screen")
    object KabuRideTAndC : Screen("/kabu-ride-terms-and-condition")
    object KabuRideCarDetails : Screen("/kabu-ride-car-details")
    object KabuRideCarDocsUpload : Screen("/kabu-ride-document-upload")
    object KabuRideGuarantorDetails : Screen("/kabu-ride-guarantor-details")
    object KabuRidePendingApproval : Screen("/kabu-ride-pending-approval")
    object KabuRideAccountDeclined : Screen("/kabu-ride-account-declined")
    object KabuRideDocReUpload : Screen("/kabu-ride-document-re-upload")
    object KabuRideGuarantorReUpload : Screen("/kabu-ride-guarantor-re-upload")
    object KabuRideInspection : Screen("/kabu-ride-inspection")

}





sealed class NavArg(val key: String) {
    object Email : NavArg("email")
    object SupportId : NavArg("supportId")
    object documentId: NavArg("documentId")
} 