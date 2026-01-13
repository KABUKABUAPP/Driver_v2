package com.kabukabu.driver.features.home.presentation.views.components

import android.location.Location
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.kabukabu.driver.core.data.socket.TripFoundEvent
import com.kabukabu.driver.core.utils.TripUiState
import com.kabukabu.driver.features.chat.presentation.viewmodel.ChatViewModel
import com.kabukabu.driver.features.debug.SocketDebugScreen
import com.kabukabu.driver.features.home.presentation.components.TripCompletionBottomSheet
import com.kabukabu.driver.features.home.presentation.viewmodel.DriverViewModel
import com.kabukabu.driver.features.home.presentation.viewmodel.TripViewModel
import com.kabukabu.driver.features.home.presentation.viewmodel.DistanceInfo
import com.kabukabu.driver.features.profile.data.ActiveTrip
import com.kabukabu.driver.features.home.data.EndTripData
import com.mapbox.maps.MapView
import TripRequestModalAnimated

@Composable
fun HomeScreenContent(
    // State
    hasActiveTrip: Boolean,
    showTripCompletionSheet: Boolean,
    currentLocation: Location?,
    activeTrip: ActiveTrip?,
    pendingTripEvent: TripFoundEvent?,
    isOnline: Boolean,
    tripUiState: TripUiState,
    isAccepting: Boolean,
    isDeclining: Boolean,
    distanceInfo: DistanceInfo?,
    endTripData: EndTripData?,
    snackbarHostState: SnackbarHostState,
    mapViewState: MutableState<MapView?>,
    isDrawerOpen: Boolean,
    showSocketDebug: Boolean,

    // ViewModels
    driverViewModel: DriverViewModel,
    tripViewModel: TripViewModel,
    chatViewModel: ChatViewModel,

    // Callbacks
    onDrawerOpenChange: (Boolean) -> Unit,
    onSocketDebugChange: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToMyTrips: () -> Unit,
    onNavigateToPromotions: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToRepairLoan: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToChat: (orderId: String, riderName: String, riderPhone: String?) -> Unit
) {
    // Determine if we should show the active trip bottom sheet
    val shouldShowActiveTripSheet = hasActiveTrip && !showTripCompletionSheet

    if (shouldShowActiveTripSheet) {
        IntegratedActiveTripScreen(
            currentLocation = currentLocation,
            activeTrip = activeTrip,
            pendingTripEvent = pendingTripEvent,
            isOnline = isOnline,
            driverViewModel = driverViewModel,
            tripViewModel = tripViewModel,
            onMenuClick = { onDrawerOpenChange(true) },
            onLogout = onLogout,
            onNavigateToChat = onNavigateToChat,
            chatViewModel = chatViewModel
        )
    } else {
        // NORMAL HOME SCREEN: No active trip
        Box(modifier = Modifier.fillMaxSize()) {
            val isTripIncoming = tripUiState is TripUiState.TripRequest

            MapComponent(
                mapViewState = mapViewState,
                currentLocation = currentLocation,
                activeTrip = null,
                modifier = Modifier.blur(if (isTripIncoming) 20.dp else 0.dp)
            )

            UIOverlay(
                hideDriverStatusCard = isTripIncoming,
                currentLocation = currentLocation,
                onLogout = onLogout,
                isOnline = isOnline,
                onIsOnlineChange = { driverViewModel.updateOnlineStatus(it) },
                onMenuClick = { onDrawerOpenChange(true) },
                driverViewModel = driverViewModel,
                onRecenterMap = {
                    recenterMapOnDriver(mapViewState.value, currentLocation)
                }
            )

            // Trip found card
            if (tripUiState is TripUiState.TripRequest) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .pointerInput(Unit) { detectTapGestures { /* consume touch */ } }
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 30.dp)
                ) {
                    TripRequestModalAnimated(
                        isVisible = true,
                        tripDetails = tripUiState.tripDetails,
                        driverLocation = currentLocation,
                        remainingTime = tripUiState.remainingTime,
                        isAccepting = isAccepting,
                        isDeclining = isDeclining,
                        onAccept = { tripViewModel.acceptTrip(driverViewModel) },
                        onDecline = { tripViewModel.declineTrip(driverViewModel) },
                        onTimeout = { tripViewModel.declineTrip(driverViewModel) },
                        distanceInfo = distanceInfo
                    )
                }
            }

            // Snackbar host for error messages
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    // Full-screen drawer overlay (render last to be on top)
    AnimatedVisibility(
        visible = isDrawerOpen,
        enter = slideInHorizontally(initialOffsetX = { -it }),
        exit = slideOutHorizontally(targetOffsetX = { -it })
    ) {
        FullScreenDrawer(
            onClose = { onDrawerOpenChange(false) },
            onLogout = {
                onDrawerOpenChange(false)
                onLogout()
                com.kabukabu.driver.core.data.socket.SocketService.disconnect()
            },
            onNavigateToWallet = {
                onDrawerOpenChange(false)
                onNavigateToWallet()
            },
            onNavigateToAnalytics = {
                onDrawerOpenChange(false)
                onNavigateToAnalytics()
            },
            onNavigateToMyTrips = {
                onDrawerOpenChange(false)
                onNavigateToMyTrips()
            },
            onNavigateToPromotions = {
                onDrawerOpenChange(false)
                onNavigateToPromotions()
            },
            onNavigateToSupport = {
                onDrawerOpenChange(false)
                onNavigateToSupport()
            },
            onNavigateToAbout = {
                onDrawerOpenChange(false)
                onNavigateToAbout()
            },
            onNavigateToRepairLoan = {
                onDrawerOpenChange(false)
                onNavigateToRepairLoan()
            },
            onNavigateToProfile = {
                onDrawerOpenChange(false)
                onNavigateToProfile()
            },
            onNavigateToSocketDebug = {
                onDrawerOpenChange(false)
                onSocketDebugChange(true)
            }
        )
    }

    // Trip Completion Bottom Sheet - Shown after trip ends
    if (showTripCompletionSheet) {
        TripCompletionBottomSheet(
            endTripData = endTripData,
            driverViewModel = driverViewModel,
            onDismiss = { driverViewModel.dismissTripCompletionSheet() }
        )
    }

    // Socket Debug Screen - Full screen overlay
    if (showSocketDebug) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            SocketDebugScreen(
                onBack = { onSocketDebugChange(false) }
            )
        }
    }
}

