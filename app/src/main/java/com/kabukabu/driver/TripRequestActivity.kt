package com.kabukabu.driver

import TripRequestModalAnimated
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kabukabu.driver.core.theme.KabukabuDriverTheme
import com.kabukabu.driver.core.utils.TripUiState
import com.kabukabu.driver.features.home.presentation.viewmodel.DriverViewModel
import com.kabukabu.driver.features.home.presentation.viewmodel.TripViewModel


/**
 * Activity that shows trip request modal as an overlay
 * Launched by TripRequestService when app is in background
 */
class TripRequestActivity : ComponentActivity() {

    private lateinit var tripViewModel: TripViewModel
    private lateinit var driverViewModel: DriverViewModel

    private val actionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.kabukabu.driver.ACCEPT_TRIP" -> {
                    tripViewModel.acceptTrip(driverViewModel)
                    finish()
                }
                "com.kabukabu.driver.DECLINE_TRIP" -> {
                    tripViewModel.declineTrip(null)
                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make this activity show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        // Register broadcast receiver for action buttons
        val filter = IntentFilter().apply {
            addAction("com.kabukabu.driver.ACCEPT_TRIP")
            addAction("com.kabukabu.driver.DECLINE_TRIP")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(actionReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(actionReceiver, filter)
        }

        setContent {
            KabukabuDriverTheme {
                TripRequestOverlay()
            }
        }
    }

    @Composable
    fun TripRequestOverlay() {
        tripViewModel = viewModel()
        driverViewModel = viewModel()

        val tripUiState by tripViewModel.uiState.collectAsState()
        val isAccepting by tripViewModel.isAccepting.collectAsState()
        val isDeclining by tripViewModel.isDeclining.collectAsState()

        // Close activity if no trip request
        LaunchedEffect(tripUiState) {
            if (tripUiState is TripUiState.Idle || tripUiState is TripUiState.NoTrip) {
                Log.d("TripRequestActivity", "No active trip request, closing activity")
                finish()
            }
        }

        // Only show UI if there's an active trip request
        if (tripUiState is TripUiState.TripRequest) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .pointerInput(Unit) {
                        detectTapGestures {
                            // Clicking outside closes the overlay
                            finish()
                        }
                    },
                contentAlignment = Alignment.BottomCenter
            ) {
                // Trip request modal
                TripRequestModalAnimated(
                    distanceInfo = null,
                    isVisible = true,
                    tripDetails = (tripUiState as TripUiState.TripRequest).tripDetails,
                    driverLocation = null, // Location will be fetched by the modal if needed
                    remainingTime = (tripUiState as TripUiState.TripRequest).remainingTime,
                    isAccepting = isAccepting,
                    isDeclining = isDeclining,
                    onAccept = {
                        tripViewModel.acceptTrip(driverViewModel)
                        finish()
                    },
                    onDecline = {
                        tripViewModel.declineTrip(null)
                        finish()
                    },
                    onTimeout = {
                        tripViewModel.declineTrip(null)
                        finish()
                    },

                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(actionReceiver)
        } catch (e: Exception) {
            Log.e("TripRequestActivity", "Error unregistering receiver: ${e.message}")
        }
    }
}

