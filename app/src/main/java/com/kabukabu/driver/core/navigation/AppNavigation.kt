package com.kabukabu.driver.core.navigation

import ProfileScreen
import SupportScreenNew
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.features.about.presentation.AboutScreen
import com.kabukabu.driver.features.analytics.presentation.AnalyticsScreen
import com.kabukabu.driver.features.auth.presentation.LoginScreen
import com.kabukabu.driver.features.auth.presentation.OtpVerificationScreen
import com.kabukabu.driver.features.auth.presentation.SplashScreen
import com.kabukabu.driver.features.auth.presentation.SplashScreenCover
import com.kabukabu.driver.features.auth.presentation.sign_up.view.DriverBioDataScreen
import com.kabukabu.driver.features.auth.presentation.sign_up.view.SelectVehicleScreen
import com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride.KabuRideAccountDeclinedScreen
import com.kabukabu.driver.features.auth.presentation.sign_up.view.KabuRideCarDetailsScreen
import com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride.KabuRideCarDocumentsUploadScreen
import com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride.KabuRideDocumentsReUploadScreen
import com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride.KabuRideGuarantorDetail
import com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride.InspectionHubsScreen
import com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride.KabuRidePendingAccountApprovalScreen
import com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride.KabuRideSelfieVerificationScreen
import com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride.KabuRideTermsAndConditionsScreen
import com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride.ReuploadGuarantorDetail
//import com.kabukabu.driver.features.home.presentation.views.HomeScreen
import com.kabukabu.driver.features.home.presentation.views.HomeScreenWithIntegratedTrip
import com.kabukabu.driver.features.chat.presentation.view.ChatScreenIntegrated
import com.kabukabu.driver.features.profile.data.Document
import com.kabukabu.driver.features.profile.data.ProfileData
//import com.kabukabu.driver.features.profile.presentation.ProfileScreen
import com.kabukabu.driver.features.promotions.presentation.PromotionsScreen
import com.kabukabu.driver.features.repair_loan.presentation.RepairLoanScreen
import com.kabukabu.driver.features.support.presentation.SelectSupportTripScreen
import com.kabukabu.driver.features.support.presentation.SupportDetailScreen
import com.kabukabu.driver.features.support.presentation.SupportNewTicketScreen
import com.kabukabu.driver.features.support.presentation.TicketListScreen
import com.kabukabu.driver.features.trips.presentation.TripDetailScreen
import com.kabukabu.driver.features.trips.presentation.TripsScreen
import com.kabukabu.driver.features.wallet.presentation.KabukabuWalletApp
import com.kabukabu.driver.features.wallet.presentation.PaymentHistoryScreen
import com.kabukabu.driver.features.wallet.presentation.SharpPaymentScreen
import com.kabukabu.driver.features.wallet.presentation.PaymentWebViewScreen
import com.kabukabu.driver.features.wallet.presentation.WithdrawalScreen
//import com.kabukabu.driver.features.wallet.presentation.WalletScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val userPreferences = KabukabuDriverApp.getInstance().userPreferences
    val userDetails by userPreferences.userDetails.collectAsState(initial = null)
    val authToken by userPreferences.authToken.collectAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()
    val navigation = Navigator(navController)

    // Create shared ChatViewModel at NavHost level so it persists across navigation
    val sharedChatViewModel: com.kabukabu.driver.features.chat.presentation.viewmodel.ChatViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel()

    // Create a nav-scoped TripSharedViewModel to pass TripItem objects between list and detail
    val tripSharedViewModel: com.kabukabu.driver.features.trips.presentation.TripSharedViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel()

    // Create nav-graph scoped ViewModels for HomeScreen to preserve state across navigations
    // This ensures the map doesn't reload when navigating away and back
    val sharedDriverViewModel: com.kabukabu.driver.features.home.presentation.viewmodel.DriverViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel()

    val sharedTripViewModel: com.kabukabu.driver.features.home.presentation.viewmodel.TripViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel()

    val sharedHomeMapViewModel: com.kabukabu.driver.features.home.presentation.viewmodel.HomeMapViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel()

    // Log changes whenever userDetails updates
    LaunchedEffect(userDetails) {
//        Log.i("DataStoreDebug in AppNav", "userDetails emitted: $userDetails")
    }

    // Listen for 401 Unauthorized events and redirect to login
    LaunchedEffect(Unit) {
//        AuthEventManager.authEvents.collect { event ->
//            when (event) {
//                is AuthEventManager.AuthEvent.Unauthorized,
//                is AuthEventManager.AuthEvent.SessionExpired -> {
//                    Log.w("AppNavigation", "Auth event received: $event - clearing session and navigating to login")
//                    // Clear stored credentials
//                    coroutineScope.launch {
//                        userPreferences.clear()
//                    }
//                    // Navigate to login screen
//                    navController.navigate(Screen.Login.route) {
//                        popUpTo(0) { inclusive = true }
//                    }
//                }
//            }
//        }
    }

    // Auth check logic
    LaunchedEffect(authToken  ) {
        delay(400)

        if (userDetails == null) {
            navController.navigate(Screen.Login.route)
            return@LaunchedEffect
        }

        if (!authToken.isNullOrBlank() && userDetails?.user?.isOnboardingComplete == true) {
            navController.navigate(Screen.Home.route) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
            Log.d("OnboardingNavigation", "User is Logged in ${authToken?.take(10)}...")
        } else {
            navigateBasedOnOnboardingStatus(navigation, navController, userDetails)
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.SplashScreenCover.route,
        enterTransition = {
            fadeIn(animationSpec = tween(500))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(500))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(500))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(500))
        }
    ) {


        composable(Screen.SplashScreenCover.route) {
            SplashScreenCover()
        }

        composable(Screen.Splash.route) {
            SplashScreen(
                onGetStartedClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToOtp = { email ->
                    navController.navigate("${Screen.OtpVerification.route}/$email")
                }
            )
        }

        composable(
            route = "${Screen.OtpVerification.route}/{${NavArg.Email.key}}",
            arguments = listOf(
                navArgument(NavArg.Email.key) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString(NavArg.Email.key) ?: ""
            OtpVerificationScreen(
                email = email,
                navigateToDriverDetailsScreen = {
                    navController.navigate(Screen.DriverBioDataScreen.route)
                },
                onNavigateToHome = {
                    Log.d("AppNavigation", "Navigating to home from OTP screen")
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.OtpVerification.route) { inclusive = true }
                    }
                },
                navToSelectVehicleScreen = {
                    navController.navigate(Screen.SelectVehicleScreen.route)
                },
                navToSelfieScreen = {
                    navController.navigate(Screen.KabuRideSelfieVerificationScreen.route)
                },
                navigator = navigation
            )
        }

        composable(Screen.DriverBioDataScreen.route) {
            DriverBioDataScreen(
                navToSelectVehicleScreen = {
                    navController.navigate(Screen.SelectVehicleScreen.route)
                }
            )
        }

        composable(Screen.SelectVehicleScreen.route) {
            SelectVehicleScreen(
                onNavToTermsAndCondition = { navController.navigate(Screen.KabuRideTAndC.route) }
            )
        }

        composable(Screen.KabuRideTAndC.route) {
            KabuRideTermsAndConditionsScreen(
                onNavToSelfieVerification = { navController.navigate(Screen.KabuRideSelfieVerificationScreen.route) }
            )
        }

        composable(Screen.KabuRideSelfieVerificationScreen.route) {
            KabuRideSelfieVerificationScreen(
                onNavToKabuCarDetailsScreen = { navController.navigate(Screen.KabuRideCarDetails.route) }
            )
        }

        composable(Screen.KabuRideCarDetails.route) {
            KabuRideCarDetailsScreen(navigation)
        }

        composable(Screen.KabuRideCarDocsUpload.route) {
            KabuRideCarDocumentsUploadScreen(navigation)
        }

        composable(Screen.KabuRideGuarantorDetails.route) {
            KabuRideGuarantorDetail(navigation)
        }

        composable(Screen.KabuRidePendingApproval.route) {
            KabuRidePendingAccountApprovalScreen(
//                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                navigation
            )
        }

        composable(Screen.KabuRideAccountDeclined.route) {
            KabuRideAccountDeclinedScreen(
                navigation
            )
        }

        composable(Screen.KabuRideGuarantorReUpload.route) {
            ReuploadGuarantorDetail(
                navigation
            )
        }

        composable(
            route = Screen.KabuRideDocReUpload.route + "/{id}/{title}",
            arguments = listOf(
                navArgument("id") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { arg ->
            val docId = arg.arguments?.getString("id") ?: ""
            val title = arg.arguments?.getString("title") ?: ""

            KabuRideDocumentsReUploadScreen(
                id = docId,
                title = title,
                navigator = navigation,
            )
        }


        composable(Screen.KabuRideInspection.route) {
            InspectionHubsScreen(
                onNavToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }




        composable(Screen.Home.route) {
            Log.d("AppNavigation", "Home screen composable called")
            HomeScreenWithIntegratedTrip(
                onLogout = {
                    coroutineScope.launch {
                        userPreferences.clearUserDetails()
                    }
                    // Clear map state on logout
                    sharedHomeMapViewModel.clearState()
                    // Clear the cached MapView to ensure fresh map on next login
                    com.kabukabu.driver.core.utils.MapViewManager.clearMapView()
                    Log.d("AppNavigation", "Logout triggered, navigating to login")
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToWallet = {
                    // Navigate to wallet with refresh=false on normal entry
                    // Use launchSingleTop to prevent duplicate destinations
                    navController.navigate(Screen.Wallet.withRefresh(false)) {
                        launchSingleTop = true
                    }
                },
                onNavigateToAnalytics = {
                    navController.navigate(Screen.Analytics.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToMyTrips = {
                    navController.navigate(Screen.MyTrips.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToPromotions = {
                    navController.navigate(Screen.Promotions.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToSupport = {
                    navController.navigate(Screen.Support.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToAbout = {
                    navController.navigate(Screen.About.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToRepairLoan = {
                    navController.navigate(Screen.RepairLoan.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToChat = { orderId, riderName, riderPhone ->
                    navController.navigate(Screen.Chat.createRoute(orderId, riderName, riderPhone ?: "")) {
                        launchSingleTop = true
                    }
                },
                chatViewModel = sharedChatViewModel,
//                driverViewModel = sharedDriverViewModel,
//                tripViewModel = sharedTripViewModel,
//                homeMapViewModel = sharedHomeMapViewModel
            )
        }

        // Chat screen route with orderId, riderName, and riderPhone parameters
        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("orderId") { type = NavType.StringType },
                navArgument("riderName") {
                    type = NavType.StringType
                    defaultValue = "Rider"
                },
                navArgument("riderPhone") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            val riderName = backStackEntry.arguments?.getString("riderName") ?: "Rider"
            val riderPhone = backStackEntry.arguments?.getString("riderPhone")?.takeIf { it.isNotBlank() }

            android.util.Log.d("AppNavigation", "Chat composable created for orderId: $orderId, ViewModel: ${sharedChatViewModel.hashCode()}")

            ChatScreenIntegrated(
                orderId = orderId,
                riderName = riderName,
                riderPhone = riderPhone,
                onBackClick = { navController.popBackStack() },
                viewModel = sharedChatViewModel
            )
        }

        composable(
            route = Screen.Wallet.route + "?refresh={refresh}",
            arguments = listOf(
                navArgument("refresh") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val refresh = backStackEntry.arguments?.getBoolean("refresh") ?: false

            // Scope ViewModel to the navigation graph (not individual back stack entry)
            // This ensures the ViewModel persists across all wallet navigations
            // The ViewModel will only be cleared when the user fully exits the wallet flow
            val viewModel: com.kabukabu.driver.features.wallet.presentation.WalletViewModel =
                androidx.lifecycle.viewmodel.compose.viewModel(
                    viewModelStoreOwner = navController.getViewModelStoreOwner(navController.graph.id)
                )

            // Use backStackEntry.id as navigation key to detect each navigation
            val navigationKey = backStackEntry.id

            KabukabuWalletApp(
                onBack = { navController.popBackStack() },
                onNavigateToPaymentHistory = { navController.navigate(Screen.PaymentHistory.route) },
                onNavigateToPaymentWebView = { paymentUrl ->
                    val encodedUrl = java.net.URLEncoder.encode(paymentUrl, "UTF-8")
                    navController.navigate("paymentWebView/$encodedUrl")
                },
                onNavigateToWithdrawal = { navController.navigate("withdrawal") },
                refreshOnLaunch = refresh,
                navigationKey = navigationKey,
                viewModel = viewModel
            )
        }

        composable(Screen.PaymentHistory.route) {
            PaymentHistoryScreen(onBack = { navController.popBackStack() })
        }

        composable("withdrawal") {
            WithdrawalScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.SharpPayment.route) {
            SharpPaymentScreen(onBack = { navController.popBackStack() })
        }

        // Payment WebView route for top-up payments
        composable(
            route = "paymentWebView/{paymentUrl}",
            arguments = listOf(
                navArgument("paymentUrl") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("paymentUrl") ?: ""
            val paymentUrl = java.net.URLDecoder.decode(encodedUrl, "UTF-8")

            PaymentWebViewScreen(
                paymentUrl = paymentUrl,
                onPaymentSuccess = {
                    // On success, navigate to Wallet with refresh=true and clear previous Wallet from stack
                    navController.navigate(Screen.Wallet.withRefresh(true)) {
                        popUpTo(Screen.Wallet.route) { inclusive = true }
                    }
                },
                onPaymentCancelled = {
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }



        composable(Screen.Analytics.route) {
            AnalyticsScreen(onBack = { navController.popBackStack() },  onNavigateToWallet = {
                navController.navigate(Screen.Wallet.withRefresh(false))
            })
        }
        composable(Screen.MyTrips.route) {
            TripsScreen(onBack = { navController.popBackStack() }, onTripClick = { tripItem ->
                // Use the nav-scoped TripSharedViewModel to store the selected trip
                tripSharedViewModel.selectTrip(tripItem)
                navController.navigate(Screen.TripDetail.createRoute(tripItem.id ?: ""))
            })
        }
        composable(Screen.Promotions.route) {
            PromotionsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Support.route) {
            SupportScreenNew(
                onBack = { navController.popBackStack() },
                onOpenTicket = { sid ->
                    navController.navigate("${Screen.SupportDetail.route}/$sid")
                },
                onClickSupport = { navController.navigate(Screen.SupportTickets.route) }
            )
        }
        composable(Screen.SupportTickets.route) {
            val result = navController.currentBackStackEntry?.savedStateHandle?.get<com.kabukabu.driver.features.support.presentation.SupportTrip>("selected_trip")
            TicketListScreen(
                onBack = { navController.popBackStack() },
                onNavigateToTripSupport = { navController.navigate(Screen.SelectSupportTripScreen.route) },
                selectedTrip = result,
                onTicketClick = { ticketId ->
                    navController.navigate("${Screen.SupportDetail.route}/$ticketId")
                }
            )
        }
        composable(Screen.SelectSupportTripScreen.route) {
            SelectSupportTripScreen(
                onBack = { navController.popBackStack() },
                onTripSelected = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_trip", it)
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = "${Screen.SupportDetail.route}/{${NavArg.SupportId.key}}",
            arguments = listOf(navArgument(NavArg.SupportId.key) { type = NavType.StringType })
        ) { backStackEntry ->
            val sid = backStackEntry.arguments?.getString(NavArg.SupportId.key) ?: ""
            SupportDetailScreen(
                supportId = sid,
                onBack = { navController.popBackStack() },
                onViewTrip = {}
            )
        }
        composable(Screen.SupportNew.route) {
            SupportNewTicketScreen(
                onBack = { navController.popBackStack() },
                onCreated = { navController.popBackStack() }
            )
        }
        composable(Screen.About.route) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.RepairLoan.route) {
            RepairLoanScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Profile.route) {
            ProfileScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = "${Screen.TripDetail.route}",
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Use the nav-scoped TripSharedViewModel declared earlier in this AppNavigation scope
            val selectedTrip by tripSharedViewModel.selectedTrip.collectAsState()
            TripDetailScreen(trip = selectedTrip, onBack = {
                // Clear the selected trip to avoid stale data when returning
                tripSharedViewModel.clear()
                navController.popBackStack()
            })
        }
    }
}


private const val TAG = "OnboardingNavigation"

private fun navigateBasedOnOnboardingStatus(
    navigator: Navigator,
    navController: NavHostController,
    userDetails: ProfileData?
) {
    Log.d(TAG, "Checking navigation status. UserDetails: $userDetails")

//     If onboarding is complete, go home immediately
    if (userDetails?.user?.isOnboardingComplete == true) {
        Log.d(TAG, "✅ Onboarding complete. Navigating to Home.")
        navController.navigate(Screen.Home.route) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
        return
    }

    Log.d(TAG, "Onboarding NOT complete. Checking step...")

    userDetails?.user?.onboardingStep?.let { step ->
        Log.d(TAG, "Current onboarding step: $step")

        if (step > 5) {  // user is awaiting admin approval checks
            Log.d(TAG, "Step > 5: User is awaiting admin approval.")
            val driver = userDetails.user.driver
            if (driver != null) {
                Log.d(TAG, "User is a driver.")
                if (driver.carOwner == true) {
                    // user has a car (KabuRide)
                    Log.d(TAG, "Driver type: KabuRide (Car Owner).")
                    val adminApprovalStatus = driver.adminApproval?.lowercase()
                    Log.d(TAG, "KabuRide adminApprovalStatus: $adminApprovalStatus")

                    when {
                        // 1️⃣ Either admin declined OR one of the docs was declined
                        adminApprovalStatus == ApprovalStatus.declined.name ||
                                !areAllDocumentsApproved(userDetails.documents) -> {
                            Log.d(
                                TAG,
                                "Navigating to: KabuRideAccountDeclinedScreen (Admin or Docs declined)."
                            )
                            navigator.navToKabuRideAccountDeclinedScreen()
                        }

                        userDetails.user.guarantorStatus?.lowercase() == ApprovalStatus.declined.name -> {
                            Log.d(
                                TAG,
                                "Navigating to: Reupload Guarantor details screen"
                            )
                            navigator.navToKabuRideReuploadGuarantorDetails()
                        }

                        // 2 Admin still reviewing the driver
                        adminApprovalStatus == ApprovalStatus.pending.name -> {
                            Log.d(
                                TAG,
                                "Navigating to: KabuRidePendingAccountApprovalScreen (Admin pending)."
                            )
                            navigator.navToKabuRidePendingAccountApprovalScreen()
                        }

                        // 3️⃣ Admin has approved the driver
                        adminApprovalStatus == ApprovalStatus.approved.name -> {
                            Log.d(
                                TAG,
                                "Admin status: 'approved'. Checking internal approval status..."
                            )
                            val approvalStatus = driver.approvalStatus
                            Log.d(TAG, "Internal approvalStatus: $approvalStatus")
                            when (approvalStatus) {
                                ApprovalStatus.active.name -> {
                                    Log.d(TAG, "Navigating to: Home (Internal status 'active').")
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(navController.graph.id) { inclusive = true }
                                    }
                                }

                                ApprovalStatus.pending.name -> {
                                    Log.d(
                                        TAG,
                                        "Navigating to: KabuRideInspection (Internal status 'pending')."
                                    )
                                    //here is the issue
                                    navigator.navToKabuRideInspection()
                                }

                                ApprovalStatus.declined.name -> {
                                    Log.d(
                                        TAG,
                                        "Navigating to: KabuRideAccountDeclinedScreen (Internal status 'declined')."
                                    )
                                    navigator.navToKabuRideAccountDeclinedScreen()
                                }

                                else -> {
                                    Log.w(
                                        TAG,
                                        "Unhandled internal approvalStatus: $approvalStatus. No navigation."
                                    )
                                }
                            }
                        }

                        // 4️⃣ Fallback (no valid status)
                        else -> {
                            Log.d(
                                TAG,
                                "Navigating to: KabuRidePendingAccountApprovalScreen (Fallback status)."
                            )
                            navigator.navToKabuRidePendingAccountApprovalScreen()
                        }
                    }
                }
                else {
                    // user doesn't own a car → KabuSharp
                    Log.d(TAG, "Driver type: KabuSharp (No Car).")
                    val sharpStatus = driver.sharpApprovalStatus?.lowercase()
                    Log.d(TAG, "KabuSharp sharpApprovalStatus: $sharpStatus")
                    when (sharpStatus) {
                        ApprovalStatus.declined.name -> {
                            Log.d(
                                TAG,
                                "Navigating to: KabuRideAccountDeclinedScreen (Sharp 'declined')."
                            )
                            navigator.navToKabuRideAccountDeclinedScreen()
                        }

                        ApprovalStatus.pending.name -> {
                            Log.d(
                                TAG,
                                "Navigating to: KabuRidePendingAccountApprovalScreen (Sharp 'pending')."
                            )
                            navigator.navToKabuRidePendingAccountApprovalScreen()
                        }

                        ApprovalStatus.approved.name -> {
                            Log.d(TAG, "Navigating to: KabuRideInspection (Sharp 'approved').")
                            navigator.navToKabuRideInspection()
                        }

                        else -> {
                            Log.d(
                                TAG,
                                "Navigating to: KabuRidePendingAccountApprovalScreen (Sharp fallback status)."
                            )
                            navigator.navToKabuRidePendingAccountApprovalScreen()
                        }
                    }
                }
            } else {

                Log.w(TAG, "⚠️ Step > 5 but user.driver is NULL. No navigation triggered.")
            }
        } else {
            // user is yet to reach final admin approval step
            Log.d(TAG, "Step <= 5: User is in active onboarding flow.")
            when (step) {
                0 -> {
                    navController.navigate(Screen.DriverBioDataScreen.route)
                }
                1 -> {
                    Log.d(TAG, "Step 1: Navigating to KabuRideTAndC.")
                    navController.navigate(Screen.KabuRideTAndC.route)
                }
                2 -> {
                    Log.d(TAG, "Step 2: Navigating to KabuRideCarDetails.")
                    navigator.navToKabuRideCarDetails()
                }
                3 -> {
                    Log.d(TAG, "Step 3 or 4: Navigating to KabuRideCarDocsUpload.")
                    navigator.navToKabuRideCarDocsUpload()
                }
                4 -> {
                    Log.d(TAG, "Step 3 or 4: Navigating to KabuRideCarDocsUpload.")
                    navigator.navToKabuRideCarDocsUpload()
                }
                5 -> {
                    Log.d(TAG, "Step 5: Navigating to KabuRideGuarantorDetailsScreen.")
                    navigator.navToKabuRideGuarantorDetailsScreen()
                }
                null -> {
                    // This code is unreachable because of the `if (step > 5)` check above.
                    Log.w(
                        TAG,
                        "⚠️ UNREACHABLE CODE: 'case 6' was hit, but should be handled by 'if (step > 5)'."
                    )
                    navigator.navToKabuRidePendingAccountApprovalScreen()
                }
                else -> {
                    navController.navigate(Screen.DriverBioDataScreen.route)
                    Log.w(TAG, "⚠️ Unhandled step in 0-5 range: $step. No navigation triggered.")
                }
            }
        }
        return
    }

    navController.navigate(Screen.Splash.route)


}


//private fun navigateBasedOnOnboardingStatus(
//    navigator: Navigator,
//    navController: NavHostController,
//    userDetails: ProfileData?
//) {
//    println("user details on nav 1........$userDetails")
//    // If onboarding is complete, go home immediately
//    if (userDetails?.user?.isOnboardingComplete == true) {
//        navController.navigate(Screen.Home.route) {
//            popUpTo(navController.graph.id) { inclusive = true }
//        }
//        return
//    }
//
//    userDetails?.user?.onboardingStep?.let { step ->
//        if (step > 5) {  // user is awaiting admin approval checks
//            val driver = userDetails.user.driver
//            if (driver != null) {
//                // user is a driver, not a rider
//                if (driver.carOwner == true) {
//                    // user has a car (KabuRide)
//                    val adminApprovalStatus = driver.adminApproval?.lowercase()
//
//                    when {
//                        // 1️⃣ Either admin declined OR one of the docs was declined
//                        adminApprovalStatus == ApprovalStatus.declined.name ||
//                                !areAllDocumentsApproved(userDetails.documents) -> {
//                            navigator.navToKabuRideAccountDeclinedScreen()
//                        }
//
//                        // 2 Admin still reviewing the driver
//                        adminApprovalStatus == ApprovalStatus.pending.name -> {
//                            navigator.navToKabuRidePendingAccountApprovalScreen()
//                        }
//
//
//                        // 3️⃣ Admin has approved the driver
//                        adminApprovalStatus == ApprovalStatus.approved.name -> {
//                            val approvalStatus = driver.approvalStatus
//                            when (approvalStatus) {
//                                ApprovalStatus.active.name -> {
//                                    navController.navigate(Screen.Home.route) {
//                                        popUpTo(navController.graph.id) { inclusive = true }
//                                    }
//                                }
//                                ApprovalStatus.pending.name -> {
//                                    navigator.navToKabuRideInspection()
//                                }
//                                ApprovalStatus.declined.name -> {
//                                    navigator.navToKabuRideAccountDeclinedScreen()
//                                }
//                            }
//                        }
//
//                        // 4️⃣ Fallback (no valid status)
//                        else -> {
//                            navigator.navToKabuRidePendingAccountApprovalScreen()
//                        }
//                    }
//                } else {
//                    // user doesn't own a car → KabuSharp
//                    val sharpStatus = driver.sharpApprovalStatus?.lowercase()
//                    when (sharpStatus) {
//                        ApprovalStatus.declined.name -> {
//                            navigator.navToKabuRideAccountDeclinedScreen()
//                        }
//                        Approval.pending.name -> {
//                            navigator.navToKabuRidePendingAccountApprovalScreen()
//                        }
//                        ApprovalStatus.approved.name -> {
//                            navigator.navToKabuRideInspection()
//                        }
//                        else -> {
//                            navigator.navToKabuRidePendingAccountApprovalScreen()
//                        }
//                    }
//                }
//            }
//        } else {
//            // user is yet to reach final admin approval step
//            when (step) {
//                0 -> navController.navigate(Screen.DriverBioDataScreen.route)
//                1 -> navController.navigate(Screen.KabuRideTAndC.route)
//                2 -> navigator.navToKabuRideCarDetails()
//                3, 4 -> navigator.navToKabuRideCarDocsUpload()
//                5 -> navigator.navToKabuRideGuarantorDetailsScreen()
//                6 -> navigator.navToKabuRidePendingAccountApprovalScreen()
//            }
//        }
//    }
//}


fun areAllDocumentsApproved(documents: List<Document>?): Boolean {
    return documents?.none { it.status.equals(ApprovalStatus.declined.name, ignoreCase = true) }
        ?: true
}

enum class ApprovalStatus {
    pending,
    declined,
    approved,
    active
}