package com.kabukabu.driver

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.kabukabu.driver.core.theme.KabukabuDriverTheme
import androidx.core.view.WindowCompat
import com.kabukabu.driver.core.navigation.AppNavigation
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.kabukabu.driver.services.ChatHeadService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import android.util.Log
import com.kabukabu.driver.core.data.local.UserPreferences
import com.kabukabu.driver.services.TripRequestService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lock to portrait mode
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            // Get the current density from the local composition.
            val currentDensity = LocalDensity.current
            val newDensity = Density(density = currentDensity.density, fontScale = 1.0f)

            // Provide the new, non-scalable density to the entire composable hierarchy.
            CompositionLocalProvider(LocalDensity provides newDensity) {
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

        // Handle intent extras if this activity was launched with trip info
        handleTripIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        // Detect if overlay permission was granted while the user was in settings
        // NOTE: ChatHeadService should be started/stopped by KabukabuDriverApp lifecycle callbacks
        lifecycleScope.launch {
            try {
                val prefs = UserPreferences.getInstance(applicationContext)
                val canOverlay = Settings.canDrawOverlays(this@MainActivity)

                // If overlay is available, do NOT start ChatHeadService here — the Application lifecycle
                // is authoritative and will stop/start ChatHeadService when the app moves foreground/background.

                // If driver is online, ensure TripRequestService is running to receive trips while backgrounded
                val userDetails = prefs.userDetails.firstOrNull()
                val onlineStatus = userDetails?.user?.onlineStatus
                if (onlineStatus == "online") {
                    try {
                        TripRequestService.startService(applicationContext)
                     Log.d("MainActivity", "Ensured TripRequestService is running onResume")
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Failed to start TripRequestService onResume: ${e.message}")
                    }
                }

                // Clear the overlay-requested flag if it was set
                try {
                    val wasRequested = prefs.overlayPermissionRequested.first()
                    if (wasRequested && canOverlay) {
                        prefs.saveOverlayPermissionRequested(false)
                    }
                } catch (e: Exception) {
                    // ignore
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle incoming intents from notifications/overlays/chat-heads
        try {
            if (intent?.getBooleanExtra("from_chat_head", false) == true) {
                Log.d("MainActivity", "onNewIntent: launched from chat head")
            }
        } catch (e: Exception) {
            // ignore
        }
        handleTripIntent(intent)
    }

    private fun handleTripIntent(intent: Intent?) {
        try {
            val tripId = intent?.getStringExtra("trip_id")
            if (!tripId.isNullOrEmpty()) {
                Log.d("MainActivity", "handleTripIntent: tripId=$tripId")
                val tripDetails = intent.getStringExtra("trip_details")
                val t = Intent(this, TripRequestActivity::class.java).apply {
                    putExtra("trip_id", tripId)
                    putExtra("trip_details", tripDetails)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                try {
                    startActivity(t)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to start TripRequestActivity from handleTripIntent: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error handling trip intent: ${e.message}")
        }
    }
}
