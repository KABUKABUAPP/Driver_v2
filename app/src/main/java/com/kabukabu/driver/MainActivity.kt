package com.kabukabu.driver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kabukabu.driver.navigation.NavArg
import com.kabukabu.driver.navigation.Screen
import com.kabukabu.driver.ui.screens.WalletScreen
import com.kabukabu.driver.ui.screens.PaymentHistoryScreen
import com.kabukabu.driver.ui.screens.SharpPaymentScreen
import com.kabukabu.driver.ui.screens.HomeScreen
import com.kabukabu.driver.ui.screens.LoginScreen
import com.kabukabu.driver.ui.screens.OtpVerificationScreen
import com.kabukabu.driver.ui.screens.SplashScreen
import com.kabukabu.driver.ui.screens.AnalyticsScreen
import com.kabukabu.driver.ui.screens.MyTripsScreen
import com.kabukabu.driver.ui.screens.PromotionsScreen
import com.kabukabu.driver.ui.screens.SupportScreen
import com.kabukabu.driver.ui.screens.AboutScreen
import com.kabukabu.driver.ui.screens.RepairLoanScreen
import com.kabukabu.driver.ui.theme.KabukabuDriverTheme
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            KabukabuDriverTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val userPreferences = KabukabuDriverApp.getInstance().userPreferences
    val authToken by userPreferences.authToken.collectAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()
    
    // Check for auth token and navigate accordingly
    LaunchedEffect(authToken) {
        if (!authToken.isNullOrBlank()) {
            Log.d("AppNavigation", "Auth token found: ${authToken?.take(10)}..., navigating to home")
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
                onNavigateToRepairLoan = { navController.navigate(Screen.RepairLoan.route) }
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
            SupportScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.About.route) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.RepairLoan.route) {
            RepairLoanScreen(onBack = { navController.popBackStack() })
        }
    }
} 