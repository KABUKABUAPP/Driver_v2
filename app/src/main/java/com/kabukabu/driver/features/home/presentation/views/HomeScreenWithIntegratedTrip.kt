package com.kabukabu.driver.features.home.presentation.views

import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kabukabu.driver.features.chat.presentation.viewmodel.ChatViewModel
import com.kabukabu.driver.features.home.presentation.viewmodel.DriverViewModel
import com.kabukabu.driver.features.home.presentation.viewmodel.TripViewModel
import com.kabukabu.driver.features.home.presentation.views.components.ErrorMessageHandler
import com.kabukabu.driver.features.home.presentation.views.components.HomeScreenContent
import com.kabukabu.driver.features.home.presentation.views.components.LocationUpdateHandler
import com.kabukabu.driver.features.home.presentation.views.components.OverlayPermissionPrompt
import com.kabukabu.driver.features.home.presentation.views.components.PermissionHandler
import com.kabukabu.driver.features.home.presentation.views.components.TripCancellationHandler
import com.kabukabu.driver.features.home.presentation.views.components.TripRoomHandler
import com.kabukabu.driver.features.home.presentation.views.components.TripServiceHandler
import com.kabukabu.driver.features.home.presentation.views.components.TripSoundHandler

@Composable
fun HomeScreenWithIntegratedTrip(
    onLogout: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToMyTrips: () -> Unit,
    onNavigateToPromotions: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToRepairLoan: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToChat: (orderId: String, riderName: String, riderPhone: String?) -> Unit = { _, _, _ -> },
    chatViewModel: ChatViewModel
) {
    val context = LocalContext.current
    var hasLocationPermission by remember { mutableStateOf(false) }
    var isDrawerOpen by remember { mutableStateOf(false) }
    var showSocketDebug by remember { mutableStateOf(false) }

    // ViewModels
    val tripViewModel: TripViewModel = viewModel()
    val driverViewModel: DriverViewModel = viewModel()

    // State from ViewModels
    val tripUiState by tripViewModel.uiState.collectAsState()
    val isAccepting by tripViewModel.isAccepting.collectAsState()
    val distanceInfo by tripViewModel.distanceInfo.collectAsState()
    val isDeclining by tripViewModel.isDeclining.collectAsState()
    val isOnline by driverViewModel.isOnline.collectAsState()
    val activeTrip by driverViewModel.activeTrip.collectAsState()
    val pendingTripEvent by driverViewModel.pendingTripEvent.collectAsState()
    val errorMessage by driverViewModel.errorMessage.collectAsState()
    val showTripCompletionSheet by driverViewModel.showTripCompletionSheet.collectAsState()
    val endTripData by driverViewModel.endTripData.collectAsState()

    // Get location from LocationRepository (single source of truth)
    val locationRepository = remember {
        com.kabukabu.driver.core.data.location.LocationRepository.getInstance(context)
    }
    val currentLocation by locationRepository.currentLocation.collectAsState()

    // UI State
    val snackbarHostState = remember { SnackbarHostState() }
    val mapViewState = remember { mutableStateOf<com.mapbox.maps.MapView?>(null) }
    val hasActiveTrip = activeTrip != null || pendingTripEvent != null

    Log.d("HomeScreen", "Observed isOnline state: $isOnline")
    Log.d("HomeScreen", "Observed activeTrip state: $activeTrip")

    // Permission Handlers
    PermissionHandler(
        onLocationPermissionGranted = { hasLocationPermission = it }
    )

    OverlayPermissionPrompt()

    // Trip Event Handlers
    TripServiceHandler(context = context, isOnline = isOnline)

    TripRoomHandler(activeTrip = activeTrip, pendingTripEvent = pendingTripEvent)

    LocationUpdateHandler(context = context, hasLocationPermission = hasLocationPermission)

    TripSoundHandler(tripUiState = tripUiState, context = context)

    TripCancellationHandler(driverViewModel = driverViewModel, chatViewModel = chatViewModel)

    ErrorMessageHandler(
        errorMessage = errorMessage,
        snackbarHostState = snackbarHostState,
        onClearError = { driverViewModel.clearErrorMessage() }
    )

    // Main Content
    HomeScreenContent(
        hasActiveTrip = hasActiveTrip,
        showTripCompletionSheet = showTripCompletionSheet,
        currentLocation = currentLocation,
        activeTrip = activeTrip,
        pendingTripEvent = pendingTripEvent,
        isOnline = isOnline,
        tripUiState = tripUiState,
        isAccepting = isAccepting,
        isDeclining = isDeclining,
        distanceInfo = distanceInfo,
        endTripData = endTripData,
        snackbarHostState = snackbarHostState,
        mapViewState = mapViewState,
        isDrawerOpen = isDrawerOpen,
        showSocketDebug = showSocketDebug,
        driverViewModel = driverViewModel,
        tripViewModel = tripViewModel,
        chatViewModel = chatViewModel,
        onDrawerOpenChange = { isDrawerOpen = it },
        onSocketDebugChange = { showSocketDebug = it },
        onLogout = onLogout,
        onNavigateToWallet = onNavigateToWallet,
        onNavigateToAnalytics = onNavigateToAnalytics,
        onNavigateToMyTrips = onNavigateToMyTrips,
        onNavigateToPromotions = onNavigateToPromotions,
        onNavigateToSupport = onNavigateToSupport,
        onNavigateToAbout = onNavigateToAbout,
        onNavigateToRepairLoan = onNavigateToRepairLoan,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToChat = onNavigateToChat
    )
}
