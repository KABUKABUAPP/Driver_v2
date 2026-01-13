package com.kabukabu.driver.features.home.presentation.views.components

import android.location.Location
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kabukabu.driver.core.data.socket.TripFoundEvent
import com.kabukabu.driver.features.home.presentation.components.ActiveTripTopBar
import com.kabukabu.driver.features.home.presentation.components.AnchoredMapControlsIntegrated
import com.kabukabu.driver.features.home.presentation.components.DriverTripModalContent
import com.kabukabu.driver.features.home.presentation.viewmodel.DriverViewModel
import com.kabukabu.driver.features.home.presentation.viewmodel.TripStatus
import com.kabukabu.driver.features.home.presentation.viewmodel.TripViewModel
import com.kabukabu.driver.features.profile.data.ActiveTrip
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntegratedActiveTripScreen(
    currentLocation: Location?,
    activeTrip: ActiveTrip?,
    pendingTripEvent: TripFoundEvent?,
    isOnline: Boolean,
    driverViewModel: DriverViewModel,
    tripViewModel: TripViewModel,
    onMenuClick: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToChat: (orderId: String, riderName: String, riderPhone: String?) -> Unit = { _, _, _ -> },
    chatViewModel: com.kabukabu.driver.features.chat.presentation.viewmodel.ChatViewModel
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val sheetMaxHeight = 600.dp
    val context = LocalContext.current

    // Route state management
    val routeState = remember { mutableStateOf<RouteState?>(null) }

    // MapView reference for recenter functionality
    val mapViewState = remember { mutableStateOf<MapView?>(null) }

    // Get current trip status to determine destination
    val currentTripStatus by driverViewModel.currentTripStatus.collectAsState()


    LaunchedEffect(currentTripStatus) {
        Log.d("HomeScreen", "Trip status changed: $currentTripStatus")
    }

    // Calculate destination point for map operations (pickup before trip starts, destination after)
    // Use activeTrip if available, otherwise fall back to pendingTripEvent
    val destinationPoint = remember(
        activeTrip?.startPoint,
        activeTrip?.endPoint,
        pendingTripEvent?.startPoint,
        currentTripStatus
    ) {
        val isTripStarted =
            currentTripStatus == TripStatus.TRIP_STARTED

        if (isTripStarted && activeTrip?.endPoint != null && activeTrip.endPoint.size >= 2) {
            // Trip has started: use destination (endPoint)
            Point.fromLngLat(activeTrip.endPoint[0], activeTrip.endPoint[1])
        } else {
            // Trip not started: use pickup location (startPoint)
            // Try activeTrip first, then pendingTripEvent
            val startPoint = activeTrip?.startPoint ?: pendingTripEvent?.startPoint
            startPoint?.let {
                if (it.size >= 2) Point.fromLngLat(it[0], it[1]) else null
            }
        }
    }

    // Keep riderPoint for recenter functionality (always pickup location for camera framing)
    // Use activeTrip if available, otherwise fall back to pendingTripEvent
    val riderPoint = remember(activeTrip?.startPoint, pendingTripEvent?.startPoint) {
        val startPoint = activeTrip?.startPoint ?: pendingTripEvent?.startPoint
        startPoint?.let {
            if (it.size >= 2) Point.fromLngLat(it[0], it[1]) else null
        }
    }

    // Detect if sheet is fully expanded
    val isExpanded by remember {
        derivedStateOf {
            scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded
        }
    }

    // Debug print to track state changes
    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            println("📍 Bottom Sheet is at MAX Height")
        } else {
            println("📍 Bottom Sheet is Collapsed/Dragging")
        }
    }

    val showArrivedButton by driverViewModel.showArrivedButton.collectAsState()
    val isArrivedButtonLoading by driverViewModel.isArrivedButtonLoading.collectAsState()
    val isStartTripLoading by driverViewModel.isStartTripLoading.collectAsState()
    val showEndTripButton by driverViewModel.showEndTripButton.collectAsState()
    val isEndTripLoading by driverViewModel.isEndTripLoading.collectAsState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 300.dp,
        sheetShape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        sheetContainerColor = Color(0xFFFDFDFD),
        containerColor = Color.White,
        sheetDragHandle = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFDFDFD)),
            ) {
                BottomSheetDefaults.DragHandle()
            }
        },
        sheetContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White)
                    .heightIn(max = sheetMaxHeight)
                    .padding(bottom = 20.dp)
            ) {
                DriverTripModalContent(
                    driverViewModel = driverViewModel,
                    tripViewModel = tripViewModel,
                    showArrivedButton = showArrivedButton,
                    isArrivedButtonLoading = isArrivedButtonLoading,
                    isStartTripLoading = isStartTripLoading,
                    showEndTripButton = showEndTripButton,
                    isEndTripLoading = isEndTripLoading,
                    currentTripStatus = currentTripStatus,
                    trip = activeTrip,
                    pendingTripEvent = pendingTripEvent,
                    routeState = routeState.value,
                    isExpanded = isExpanded,
                    onNavigateToChat = onNavigateToChat,
                    chatViewModel = chatViewModel
                )
            }
        }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // THE REAL MAP from HomeScreen
            MapComponent(
                currentLocation = currentLocation,
                activeTrip = activeTrip,
                pendingTripEvent = pendingTripEvent,
                tripStatus = currentTripStatus,
                onRouteInfoUpdated = { routeInfo ->
                    routeState.value = routeInfo
                },
                mapViewState = mapViewState,
                modifier = Modifier.fillMaxSize()
            )

            // Top UI overlay (menu button and location)
            ActiveTripTopBar(
                currentLocation = currentLocation,
                onMenuClick = onMenuClick
            )

            // Floating Anchored Controls (Google Maps, Recenter, Arrived button)
            AnchoredMapControlsIntegrated(
                showArrivedButton = showArrivedButton,
                isArrivedButtonLoading = isArrivedButtonLoading,
                isStartTripLoading = isStartTripLoading,
                showEndTripButton = showEndTripButton,
                isEndTripLoading = isEndTripLoading,
                currentTripStatus = currentTripStatus,
                sheetState = scaffoldState.bottomSheetState,
                isExpanded = isExpanded,
                offsetY = 16.dp,
                currentLocation = currentLocation,
                riderPoint = riderPoint,
                mapView = mapViewState.value,
                driverViewModel = driverViewModel,
                onOpenGoogleMaps = {
                    // Use destinationPoint which adapts based on trip status
                    destinationPoint?.let { destination ->
                        openGoogleMapsNavigation(context, currentLocation, destination)
                    }
                },
                onRecenterMap = {
                    if (riderPoint != null) {
                        recenterMapOnRoute(mapViewState.value, currentLocation, riderPoint)
                    } else {
                        recenterMapOnDriver(mapViewState.value, currentLocation)
                    }
                })
        }
    }
}

