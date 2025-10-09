package com.kabukabu.driver.core.navigation

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import com.kabukabu.driver.features.home.presentation.HomeScreen
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
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val userPreferences = KabukabuDriverApp.getInstance().userPreferences
    val authToken by userPreferences.authToken.collectAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()

    // Check for auth token and navigate accordingly
    LaunchedEffect(authToken) {
        if (!authToken.isNullOrBlank()) {
            Log.d(
                "AppNavigation",
                "Auth token found: ${authToken?.take(10)}..., navigating to home"
            )
            // Add a delay to ensure the NavHost is fully set up
            delay(500)
            // User is logged in, navigate to home screen
            navController.navigate(Screen.Home.route) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        } else {
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
                onNavigateToHome = {
                    Log.d("AppNavigation", "Navigating to home from OTP screen")
                    coroutineScope.launch {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.OtpVerification.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Promotions.route) {
            PromotionsScreen(onBack = { navController.popBackStack() })
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