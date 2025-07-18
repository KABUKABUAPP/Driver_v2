package com.kabukabu.driver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kabukabu.driver.navigation.NavArg
import com.kabukabu.driver.navigation.Screen
import com.kabukabu.driver.ui.screens.HomeScreen
import com.kabukabu.driver.ui.screens.LoginScreen
import com.kabukabu.driver.ui.screens.OtpVerificationScreen
import com.kabukabu.driver.ui.screens.SplashScreen
import com.kabukabu.driver.ui.theme.KabukabuDriverTheme
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KabukabuDriverTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

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
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Screen.Home.route) {
                            HomeScreen()
                        }
                    }
                }
            }
        }
    }
} 