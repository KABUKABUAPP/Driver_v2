package com.kabukabu.driver.features.home.presentation.views.components

import android.content.Context
import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.kabukabu.driver.core.data.location.LocationRepository
import com.kabukabu.driver.core.data.socket.SocketService
import com.kabukabu.driver.core.data.socket.TripFoundEvent
import com.kabukabu.driver.core.utils.SoundPlayer
import com.kabukabu.driver.core.utils.TripUiState
import com.kabukabu.driver.features.chat.presentation.viewmodel.ChatViewModel
import com.kabukabu.driver.features.home.presentation.viewmodel.DriverViewModel
import com.kabukabu.driver.features.profile.data.ActiveTrip

@Composable
fun TripServiceHandler(
    context: Context,
    isOnline: Boolean
) {
    // Start/Stop TripRequestService based on online status
    LaunchedEffect(isOnline) {
        if (isOnline) {
            Log.d("HomeScreen", "Driver is online - ensuring TripRequestService is running")
            com.kabukabu.driver.services.TripRequestService.startService(context)
        } else {
            Log.d("HomeScreen", "Driver is offline - stopping TripRequestService")
            com.kabukabu.driver.services.TripRequestService.stopService(context)
        }
    }
}

@Composable
fun TripRoomHandler(
    activeTrip: ActiveTrip?,
    pendingTripEvent: TripFoundEvent?
) {
    // Join trip room when there's an active trip
    LaunchedEffect(activeTrip?.id, pendingTripEvent?.eventId) {
        val tripId = activeTrip?.order ?: pendingTripEvent?.eventId
        if (tripId != null) {
            Log.d("HomeScreen", "Active trip detected - joining room: $tripId")
            SocketService.joinTripRoom(tripId)
        }
    }
}

@Composable
fun LocationUpdateHandler(
    context: Context,
    hasLocationPermission: Boolean
) {
    val locationRepository = LocationRepository.getInstance(context)

    // Start/Stop location updates using LocationRepository
    DisposableEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            Log.d("HomeScreen", "Starting location updates via LocationRepository")
            locationRepository.startLocationUpdates()
        }

        onDispose {
            Log.d("HomeScreen", "Stopping location updates via LocationRepository")
            locationRepository.stopLocationUpdates()
        }
    }
}

@Composable
fun TripSoundHandler(tripUiState: TripUiState, context: Context) {
    // Play sound on new trip request
    LaunchedEffect(tripUiState) {
        if (tripUiState is TripUiState.TripRequest) {
            SoundPlayer.playTripAlert(context)
        }
    }
}

@Composable
fun TripCancellationHandler(
    driverViewModel: DriverViewModel,
    chatViewModel: ChatViewModel
) {
    // Listen for trip cancelled events and refresh profile
    LaunchedEffect(Unit) {
        SocketService.tripCancelledEvent.collect { tripCancelledEvent ->
            Log.d(
                "HomeScreen",
                "Trip cancelled event received: ${tripCancelledEvent.order.id}, Status: ${tripCancelledEvent.status}"
            )
            // Fetch profile in background to update active trip (set to null)
            driverViewModel.cancelTrippedByRider()
            chatViewModel.clearChatData()
        }
    }
}

@Composable
fun ErrorMessageHandler(
    errorMessage: String?,
    snackbarHostState: SnackbarHostState,
    onClearError: () -> Unit
) {
    // Show snackbar when there's an error message
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            onClearError()
        }
    }
}

