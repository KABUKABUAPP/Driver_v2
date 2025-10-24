package com.kabukabu.driver.core.navigation

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.kabukabu.driver.features.home.presentation.HomeScreen
import com.kabukabu.driver.features.profile.data.Document
import com.kabukabu.driver.features.profile.data.ProfileData
import com.kabukabu.driver.features.profile.presentation.ProfileScreen
import com.kabukabu.driver.features.promotions.presentation.PromotionsScreen
import com.kabukabu.driver.features.repair_loan.presentation.RepairLoanScreen
import com.kabukabu.driver.features.support.presentation.SupportDetailScreen
import com.kabukabu.driver.features.support.presentation.SupportNewTicketScreen
import com.kabukabu.driver.features.support.presentation.SupportScreen
import com.kabukabu.driver.features.trips.presentation.MyTripsScreen
import com.kabukabu.driver.features.wallet.presentation.PaymentHistoryScreen
import com.kabukabu.driver.features.wallet.presentation.SharpPaymentScreen
import com.kabukabu.driver.features.wallet.presentation.WalletScreen
import kotlinx.coroutines.delay

@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val userPreferences = KabukabuDriverApp.getInstance().userPreferences
    val userDetails by userPreferences.userDetails.collectAsState(initial = null)
    val authToken by userPreferences.authToken.collectAsState(initial = null)
//    val coroutineScope = rememberCoroutineScope()
    val navigation = Navigator(navController)

    // Check for auth token and navigate accordingly
    LaunchedEffect(authToken) {
        if (!authToken.isNullOrBlank()) {
            Log.d(
                "AppNavigation",
                "Auth token found: ${authToken?.take(10)}..., navigating to home"
            )
            // Add a delay to ensure the NavHost is fully set up
            delay(500)
            navController.navigate(Screen.DriverBioDataScreen.route)

//            navigateBasedOnOnboardingStatus(navigation, navController, userDetails)
            // User is logged in, navigate to home screen
//            navController.navigate(Screen.Home.route) {
//                popUpTo(navController.graph.id) { inclusive = true }
//            }
        } else {
            delay(700)
            navController.navigate(Screen.DriverBioDataScreen.route)
            //navigate to Login because authToken is not found, and restart proccess.
//            navController.navigate(Screen.Login.route) {
//                popUpTo(Screen.Splash.route) { inclusive = true }
//            }
            Log.d("AppNavigation", "No auth token found")
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
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
//                    coroutineScope.launch {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
//                    }
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
            KabuRidePendingAccountApprovalScreen(onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                navigation
            )
        }

        composable(Screen.KabuRideAccountDeclined.route) {
            KabuRideAccountDeclinedScreen(
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
            InspectionHubsScreen( onNavToInspectionScreen = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            }
            )
        }




        composable(Screen.Home.route) {
            Log.d("AppNavigation", "Home screen composable called")
            HomeScreen(
                onLogout = {
                    Log.d("AppNavigation", "Logout triggered, navigating to login")
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToWallet = {
                    navController.navigate(Screen.Wallet.route)
                },
                onNavigateToAnalytics = { navController.navigate(Screen.Analytics.route) },
                onNavigateToMyTrips = { navController.navigate(Screen.MyTrips.route) },
                onNavigateToPromotions = { navController.navigate(Screen.Promotions.route) },
                onNavigateToSupport = { navController.navigate(Screen.Support.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) },
                onNavigateToRepairLoan = { navController.navigate(Screen.RepairLoan.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )
        }

        composable(Screen.Wallet.route) {
            WalletScreen(
                onBack = { navController.popBackStack() },
                onNavigatePaymentHistory = { navController.navigate(Screen.PaymentHistory.route) },
                onNavigateSharpPayment = { navController.navigate(Screen.SharpPayment.route) }
            )
        }

        composable(Screen.PaymentHistory.route) {
            PaymentHistoryScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.SharpPayment.route) {
            SharpPaymentScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Analytics.route) {
            AnalyticsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.MyTrips.route) {
            MyTripsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Promotions.route) {
            PromotionsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Support.route) {
            SupportScreen(
                onBack = { navController.popBackStack() },
                onOpenTicket = { sid ->
                    navController.navigate("${Screen.SupportDetail.route}/$sid")
                },
                onCreateNew = { navController.navigate(Screen.SupportNew.route) }
            )
        }
        composable(
            route = "${Screen.SupportDetail.route}/{${NavArg.SupportId.key}}",
            arguments = listOf(navArgument(NavArg.SupportId.key) { type = NavType.StringType })
        ) { backStackEntry ->
            val sid = backStackEntry.arguments?.getString(NavArg.SupportId.key) ?: ""
            SupportDetailScreen(
                supportId = sid,
                onBack = { navController.popBackStack() }
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
    }
}

private fun navigateBasedOnOnboardingStatus(
    navigator: Navigator,
    navController: NavHostController,
    userDetails: ProfileData?
) {
    // If onboarding is complete, go home immediately
    if (userDetails?.user?.isOnboardingComplete == true) {
        navController.navigate(Screen.Home.route) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
        return
    }

    userDetails?.user?.onboardingStep?.let { step ->
        if (step > 5) {  // user is awaiting admin approval checks
            val driver = userDetails.user.driver
            if (driver != null) {
                // user is a driver, not a rider
                if (driver.carOwner == true) {
                    // user has a car (KabuRide)
                    val adminApprovalStatus = driver.adminApproval?.lowercase()

                    when {
                        // 1️⃣ Either admin declined OR one of the docs was declined
                        adminApprovalStatus == ApprovalStatus.declined.name ||
                                !areAllDocumentsApproved(userDetails.documents) -> {
                            navigator.navToKabuRideAccountDeclinedScreen()
                        }

                        // 2 Admin still reviewing the driver
                        adminApprovalStatus == ApprovalStatus.pending.name -> {
                            navigator.navToKabuRidePendingAccountApprovalScreen()
                        }


                        // 3️⃣ Admin has approved the driver
                        adminApprovalStatus == ApprovalStatus.approved.name -> {
                            val approvalStatus = driver.approvalStatus
                            when (approvalStatus) {
                                ApprovalStatus.active.name -> {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(navController.graph.id) { inclusive = true }
                                    }
                                }
                                ApprovalStatus.pending.name -> {
                                    navigator.navToKabuRideInspection()
                                }
                                ApprovalStatus.declined.name -> {
                                    navigator.navToKabuRideAccountDeclinedScreen()
                                }
                            }
                        }

                        // 4️⃣ Fallback (no valid status)
                        else -> {
                            navigator.navToKabuRidePendingAccountApprovalScreen()
                        }
                    }
                } else {
                    // user doesn't own a car → KabuSharp
                    val sharpStatus = driver.sharpApprovalStatus?.lowercase()
                    when (sharpStatus) {
                        ApprovalStatus.declined.name -> {
                            navigator.navToKabuRideAccountDeclinedScreen()
                        }
                        ApprovalStatus.pending.name -> {
                            navigator.navToKabuRidePendingAccountApprovalScreen()
                        }
                        ApprovalStatus.approved.name -> {
                            navigator.navToKabuRideInspection()
                        }
                        else -> {
                            navigator.navToKabuRidePendingAccountApprovalScreen()
                        }
                    }
                }
            }
        } else {
            // user is yet to reach final admin approval step
            when (step) {
                0 -> navController.navigate(Screen.DriverBioDataScreen.route)
                1 -> navController.navigate(Screen.KabuRideTAndC.route)
                2 -> navigator.navToKabuRideCarDetails()
                3, 4 -> navigator.navToKabuRideCarDocsUpload()
                5 -> navigator.navToKabuRideGuarantorDetailsScreen()
                6 -> navigator.navToKabuRidePendingAccountApprovalScreen()
            }
        }
    }
}



fun areAllDocumentsApproved(documents: List<Document>?): Boolean {
    return documents?.none { it.status.equals(ApprovalStatus.declined.name, ignoreCase = true)} ?: true
}

enum class ApprovalStatus {
    pending,
    declined,
    approved,
    active
}