package com.kabukabu.driver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
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
import com.kabukabu.driver.core.navigation.NavArg
import com.kabukabu.driver.core.navigation.Screen
import com.kabukabu.driver.features.wallet.presentation.WalletScreen
import com.kabukabu.driver.features.wallet.presentation.PaymentHistoryScreen
import com.kabukabu.driver.features.wallet.presentation.SharpPaymentScreen
import com.kabukabu.driver.features.home.presentation.HomeScreen
import com.kabukabu.driver.features.auth.presentation.LoginScreen
import com.kabukabu.driver.features.auth.presentation.OtpVerificationScreen
import com.kabukabu.driver.features.auth.presentation.SplashScreen
import com.kabukabu.driver.features.analytics.presentation.AnalyticsScreen
import com.kabukabu.driver.features.trips.presentation.MyTripsScreen
import com.kabukabu.driver.features.promotions.presentation.PromotionsScreen
import com.kabukabu.driver.features.support.presentation.SupportScreen
import com.kabukabu.driver.features.about.presentation.AboutScreen
import com.kabukabu.driver.features.repair_loan.presentation.RepairLoanScreen
import com.kabukabu.driver.features.support.presentation.SupportDetailScreen
import com.kabukabu.driver.features.support.presentation.SupportNewTicketScreen
import com.kabukabu.driver.features.profile.presentation.ProfileScreen
import com.kabukabu.driver.core.theme.KabukabuDriverTheme
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.view.WindowCompat
import com.kabukabu.driver.core.navigation.AppNavigation
import com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride.AccountDeclinedScreen
import com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride.InspectionScreen
import com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride.KabuRideCarDetailsScreen
import com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride.KabuRideCarDocumentsUploadScreen
import com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride.KabuRideGuarantorDetail
import com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride.KabuRideReuploadDocumentsUploadScreen
import com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride.PendingAccountApprovalScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            KabukabuDriverTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding(), // Respect bottom safe area only
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

