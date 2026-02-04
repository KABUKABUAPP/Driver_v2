package com.kabukabu.driver.core.navigation


sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object SplashScreenCover : Screen("/splash_screen-cover")
    object Login : Screen("login")
    object OtpVerification : Screen("otp_verification")
    object Home : Screen("home")
    object Wallet : Screen("wallet") {
        // Helper to build route with optional refresh flag
        fun withRefresh(refresh: Boolean): String = if (refresh) "wallet?refresh=true" else "wallet?refresh=false"
    }
    object PaymentHistory : Screen("payment_history")
    object SharpPayment : Screen("sharp_payment")
    object Analytics : Screen("analytics")
    object MyTrips : Screen("my_trips")
    object Promotions : Screen("promotions")
    object Support : Screen("support")
    object SupportTickets : Screen("support_tickets")
    object SelectSupportTripScreen : Screen("trip_support_list")
    object SupportDetail : Screen("support_detail") {
        fun createRoute(supportId: String, status: String? = null): String {
            return if (status != null) {
                "support_detail/$supportId?status=$status"
            } else {
                "support_detail/$supportId"
            }
        }
    }
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
    object Chat : Screen("chat/{orderId}/{riderName}/{riderPhone}") {
        fun createRoute(orderId: String, riderName: String = "Rider", riderPhone: String = "") = "chat/$orderId/$riderName/$riderPhone"
    }
    object TripDetail : Screen("trip_detail/{tripId}") {
        fun createRoute(tripId: String) = "trip_detail/$tripId"
    }
    object TripReceipt : Screen("trip_receipt/{tripId}") {
        fun createRoute(tripId: String) = "trip_receipt/$tripId"
    }

}




sealed class NavArg(val key: String) {
    object Email : NavArg("email")
    object SupportId : NavArg("supportId")
    object documentId: NavArg("documentId")
}
