package com.kabukabu.driver.features.home.presentation.views

import TripRequestModalAnimated
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.input.pointer.pointerInput
import com.kabukabu.driver.R
import com.kabukabu.driver.core.components.SwipeButton
import com.kabukabu.driver.core.data.socket.SocketService
import com.kabukabu.driver.core.data.socket.TripFoundEvent
import com.kabukabu.driver.core.theme.KabukabuYellow
import com.kabukabu.driver.core.utils.SoundPlayer
import com.kabukabu.driver.core.utils.TripUiState
import com.kabukabu.driver.features.chat.presentation.viewmodel.ChatViewModel
import com.kabukabu.driver.features.debug.SocketDebugScreen
import com.kabukabu.driver.features.home.data.EndTripData
import com.kabukabu.driver.features.home.presentation.viewmodel.DriverViewModel
import com.kabukabu.driver.features.home.presentation.viewmodel.TripStatus
import com.kabukabu.driver.features.home.presentation.viewmodel.TripViewModel
import com.kabukabu.driver.features.home.presentation.views.components.FullScreenDrawer
import com.kabukabu.driver.features.home.presentation.views.components.MapComponent
import com.kabukabu.driver.features.home.presentation.views.components.RouteState
import com.kabukabu.driver.features.home.presentation.views.components.UIOverlay
import com.kabukabu.driver.features.home.presentation.views.components.WaitingTimeComponent
import com.kabukabu.driver.features.home.presentation.views.components.openGoogleMapsNavigation
import com.kabukabu.driver.features.home.presentation.views.components.recenterMapOnDriver
import com.kabukabu.driver.features.home.presentation.views.components.recenterMapOnRoute
import com.kabukabu.driver.features.profile.data.ActiveTrip
import com.kabukabu.driver.features.wallet.presentation.KabuYellow
import com.kabukabu.driver.core.utils.composableSafeClickable
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView

@OptIn(ExperimentalMaterial3Api::class)
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
    chatViewModel: com.kabukabu.driver.features.chat.presentation.viewmodel.ChatViewModel
) {
    val context = LocalContext.current
    var hasLocationPermission by remember { mutableStateOf(false) }

    val tripViewModel: TripViewModel = viewModel()
    val driverViewModel: DriverViewModel = viewModel()
    val tripUiState by tripViewModel.uiState.collectAsState()
    val isAccepting by tripViewModel.isAccepting.collectAsState()
    val distanceInfo by tripViewModel.distanceInfo.collectAsState()
    val isDeclining by tripViewModel.isDeclining.collectAsState()
    val isOnline by driverViewModel.isOnline.collectAsState()
    val activeTrip by driverViewModel.activeTrip.collectAsState()
    val pendingTripEvent by driverViewModel.pendingTripEvent.collectAsState()
    val errorMessage by driverViewModel.errorMessage.collectAsState()
    val currentTripState = tripUiState

    // Use activeTrip if available, otherwise use pendingTripEvent
    // This ensures immediate display when trip is accepted
    val hasActiveTrip = activeTrip != null || pendingTripEvent != null

    // Get location from LocationRepository (single source of truth)
    val locationRepository = remember {
        com.kabukabu.driver.core.data.location.LocationRepository.getInstance(context)
    }
    val currentLocation by locationRepository.currentLocation.collectAsState()

    // Snackbar host state for showing error messages
    val snackbarHostState = remember { SnackbarHostState() }
    val mapViewState = remember { mutableStateOf<MapView?>(null) }

    Log.d("HomeScreen", "Observed isOnline state: $isOnline")
    Log.d("HomeScreen", "Observed activeTrip state: $activeTrip")

    // Show snackbar when there's an error message
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message, duration = SnackbarDuration.Short
            )
            driverViewModel.clearErrorMessage()
        }
    }

    // Always prompt for overlay permission on Home screen if missing.
    val scope = rememberCoroutineScope()
    val userPreferences = com.kabukabu.driver.core.data.local.UserPreferences.getInstance(context)
    var showOverlayPrompt by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // If overlay is not granted, prompt user on homescreen
        if (!android.provider.Settings.canDrawOverlays(context)) {
            showOverlayPrompt = true
        }
    }

    // Launcher to open overlay settings (system has no direct result; onResume will check)
    val openOverlaySettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { /* onResume handles permission detection */ }
    )

    if (showOverlayPrompt) {
        AlertDialog(
            onDismissRequest = { showOverlayPrompt = false },
            title = { Text(text = "Enable Overlay") },
            text = { Text(text = "Kabukabu requires overlay permission to display incoming trip overlays. Open settings to allow overlays now?") },
            confirmButton = {
                TextButton(onClick = {
                    showOverlayPrompt = false
                    // Open overlay settings
                    val intent = Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                        data = android.net.Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    openOverlaySettingsLauncher.launch(intent)
                }) { Text(text = "Open settings") }
            },
            dismissButton = {
                TextButton(onClick = { showOverlayPrompt = false }) { Text(text = "Cancel") }
            }
        )
    }

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


    // Join trip room when there's an active trip
    LaunchedEffect(activeTrip?.id, pendingTripEvent?.eventId) {
        val tripId = activeTrip?.order ?: pendingTripEvent?.eventId
        if (tripId != null) {
            Log.d("HomeScreen", "Active trip detected - joining room: $tripId")
            com.kabukabu.driver.core.data.socket.SocketService.joinTripRoom(tripId)
        }
    }

    // Check if we already have permission
    LaunchedEffect(Unit) {
//        driverViewModel.fetchUserProfile()
        hasLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            hasLocationPermission = true
        }
    }

    // Request permission if not granted
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            val permissionsToRequest = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            )

            // Add notification permission for Android 13+
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }

            locationPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

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

    // Play sound on new trip request
    LaunchedEffect(tripUiState) {
        if (tripUiState is TripUiState.TripRequest) {
            SoundPlayer.playTripAlert(context)
        }
    }

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
//            driverViewModel.fetchUserProfile()
        }
    }

    // Location emission now handled in DriverViewModel - continues running even when navigating to ChatScreen

    var isDrawerOpen by remember { mutableStateOf(false) }
    var showSocketDebug by remember { mutableStateOf(false) }

    val showTripCompletionSheet by driverViewModel.showTripCompletionSheet.collectAsState()
    val endTripData by driverViewModel.endTripData.collectAsState()

    // Determine if we should show the active trip bottom sheet
    // Hide it when trip completion sheet should be shown
    // Show if either activeTrip or pendingTripEvent exists
    val shouldShowActiveTripSheet = hasActiveTrip && !showTripCompletionSheet

    if (shouldShowActiveTripSheet) {
        IntegratedActiveTripScreen(
            currentLocation = currentLocation,
            activeTrip = activeTrip,
            pendingTripEvent = pendingTripEvent,
            isOnline = isOnline,
            driverViewModel = driverViewModel,
            tripViewModel = tripViewModel,
            onMenuClick = { isDrawerOpen = true },
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
                onMenuClick = { isDrawerOpen = true },
                driverViewModel = driverViewModel,
                onRecenterMap = {
                    recenterMapOnDriver(mapViewState.value, currentLocation)
                })

            // Trip found card
            if (currentTripState is TripUiState.TripRequest) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .pointerInput(Unit) { detectTapGestures { /* consume touch */ } })

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 30.dp)
                ) {
                    TripRequestModalAnimated(
                        isVisible = true,
                        tripDetails = currentTripState.tripDetails,
                        driverLocation = currentLocation,
                        remainingTime = currentTripState.remainingTime,
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
                hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    // Full-screen drawer overlay (render last to be on top)
    AnimatedVisibility(
        visible = isDrawerOpen,
        enter = slideInHorizontally(initialOffsetX = { -it }),
        exit = slideOutHorizontally(targetOffsetX = { -it })
    ) {
        FullScreenDrawer(onClose = { isDrawerOpen = false }, onLogout = {
            isDrawerOpen = false

            onLogout()
            SocketService.disconnect()
        }, onNavigateToWallet = {
            isDrawerOpen = false
            onNavigateToWallet()
        }, onNavigateToAnalytics = {
            isDrawerOpen = false
            onNavigateToAnalytics()
        }, onNavigateToMyTrips = {
            isDrawerOpen = false
            onNavigateToMyTrips()
        }, onNavigateToPromotions = {
            isDrawerOpen = false
            onNavigateToPromotions()
        }, onNavigateToSupport = {
            isDrawerOpen = false
            onNavigateToSupport()
        }, onNavigateToAbout = {
            isDrawerOpen = false
            onNavigateToAbout()
        }, onNavigateToRepairLoan = {
            isDrawerOpen = false
            onNavigateToRepairLoan()
        }, onNavigateToProfile = {
            isDrawerOpen = false
            onNavigateToProfile()
        }, onNavigateToSocketDebug = {
            isDrawerOpen = false
            showSocketDebug = true
        })
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
                onBack = { showSocketDebug = false }
            )
        }
    }
}

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

@Composable
fun ActiveTripTopBar(
    currentLocation: Location?,
    onMenuClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Menu Button
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp))
                .composableSafeClickable {
                    onMenuClick()
                }, contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.menu_right_square_alt),
                contentDescription = "Menu",
                modifier = Modifier.size(24.dp)
            )
        }

        // Center could show trip status or location
        Box(
            modifier = Modifier
                .wrapContentWidth()
                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.location_pin),
                    contentDescription = "Location Pin",
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Active Trip",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.W600,
                    fontSize = 12.sp
                )
            }
        }

        // Right Spacer for balance
        Spacer(Modifier.size(44.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AnchoredMapControlsIntegrated(
    showArrivedButton: Boolean = false,
    isArrivedButtonLoading: Boolean = false,
    isStartTripLoading: Boolean = false,
    showEndTripButton: Boolean = false,
    isEndTripLoading: Boolean = false,
    currentTripStatus: TripStatus = TripStatus.STANDBY,
    sheetState: SheetState,
    offsetY: androidx.compose.ui.unit.Dp,
    isExpanded: Boolean,
    currentLocation: Location?,
    riderPoint: Point?,
    mapView: MapView?,
    driverViewModel: DriverViewModel,
    onOpenGoogleMaps: () -> Unit,
    onRecenterMap: () -> Unit
) {
    val density = LocalDensity.current
    val offsetYPx = with(density) { offsetY.toPx() }

    // Collect the showArrivedButton state from ViewModel
    val showArrivedButton by driverViewModel.showArrivedButton.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                val sheetTop = try {
                    sheetState.requireOffset()
                } catch (e: Exception) {
                    size.height
                }
                translationY = sheetTop - size.height - offsetYPx
            }) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Google Maps Pill - Now functional!
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.composableSafeClickable(onClick = onOpenGoogleMaps)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.gmlogo),
                            contentDescription = "Maps Logo",
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open in Google Maps", fontWeight = FontWeight.W700, fontSize = 14.sp)
                    }
                }

                // Compass/Location Button - Now functional!
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(60.dp).composableSafeClickable(onClick = onRecenterMap)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.resetlocation),
                            contentDescription = "Recenter Map",
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
            }

            // Show waiting time countdown if active (always visible regardless of expanded state)
            val waitingTimeSeconds by driverViewModel.waitingTimeSeconds.collectAsState()
            if (waitingTimeSeconds != null && waitingTimeSeconds!! > 0 && currentTripStatus == TripStatus.ARRIVED_PICKUP) {
                Spacer(modifier = Modifier.height(15.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                ) {
                    WaitingTimeComponent(
                        remainingSeconds = waitingTimeSeconds!!, totalSeconds = 300
                    )
                }
//                Spacer(modifier = Modifier.height(15.dp))
            }

            // Show buttons based on trip status when not expanded
            if (!isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(15.dp))

                    when (currentTripStatus) {
                        TripStatus.ACCEPTED -> {
                            // Show "I have arrived" button when driver is heading to pickup and within 500m
                            if (showArrivedButton) {
                                ArrivedButton(
                                    onClick = { driverViewModel.arrivedPickup() },
                                    isLoading = isArrivedButtonLoading
                                )
                            }
                        }

                        TripStatus.ARRIVED_PICKUP -> {

                            // Show "Start Trip" swipe button when driver has arrived at pickup
                            SwipeButton(
                                isActive = true,
                                isLoading = isStartTripLoading,
                                activeText = "Swipe to start trip >",
                                inactiveText = "Swipe to start trip >",
                                loadingText = "Starting trip...",
                                onStateChange = { driverViewModel.startTrip() })
                        }

                        TripStatus.TRIP_STARTED -> {
                            // Show End Trip button when driver is near destination
                            if (showEndTripButton) {
                                SwipeButton(
                                    isActive = true,
                                    isLoading = isEndTripLoading,
                                    activeText = "Swipe to end trip >",
                                    inactiveText = "Swipe to end trip >",
                                    loadingText = "Ending trip...",
                                    onStateChange = { driverViewModel.endTrip() })
                            }
                        }

                        else -> {
                            // No button for other statuses
                        }
                    }
                }
            }
        }
    }
}

// --- Content from activietripsheet.kt ---
val ModalTextDark = Color(0xFF1A1A1A)
val ModalTextGray = Color(0xFF9A9A9A)
val ModalLightGrayBg = Color(0xF8F8F8)
val PrimaryYellow = KabuYellow
val CancelRed = Color(0xFFEF2C5B)

@Composable
fun DriverTripModalContent(
    driverViewModel: DriverViewModel,
    tripViewModel: TripViewModel,
    trip: ActiveTrip?,
    pendingTripEvent: TripFoundEvent? = null,
    showArrivedButton: Boolean = false,
    isArrivedButtonLoading: Boolean = false,
    isStartTripLoading: Boolean = false,
    showEndTripButton: Boolean = false,
    isEndTripLoading: Boolean = false,
    currentTripStatus: TripStatus = TripStatus.STANDBY,
    routeState: RouteState? = null,
    isExpanded: Boolean = false,
    onNavigateToChat: (orderId: String, riderName: String, riderPhone: String?) -> Unit = { _, _, _ -> },
    chatViewModel: ChatViewModel? = null
) {
    val context = LocalContext.current
    val isCanceling by tripViewModel.isDeclining.collectAsState()

    // Helper functions to get data from either source
    val getTripDuration = { trip?.durationInMinutes ?: pendingTripEvent?.duration }
    val getPickupAddress = { trip?.startAddress?.fullAddress ?: pendingTripEvent?.pickupLocation?.name }
    val getRiderName = { trip?.user?.fullName ?: pendingTripEvent?.user?.fullname }
    val getRiderTrips = { trip?.user?.totalTrips ?: pendingTripEvent?.user?.totalTrips }
    val getRiderPhone = { trip?.user?.phoneNumber ?: pendingTripEvent?.user?.phoneNumber }
    val getDistance = { trip?.distanceInKm ?: pendingTripEvent?.distance }
    val getPriceRange = { trip?.priceRange ?: pendingTripEvent?.priceRange }
    val getDropoffAddress = { trip?.endAddress?.fullAddress ?: pendingTripEvent?.destinationLocation?.name }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        // 1. HEADER: Title + Time Badge (dynamic title based on trip status and distance)
        val headerText = when {
            showEndTripButton -> "You have arrived at destination"
            currentTripStatus == TripStatus.TRIP_STARTED -> "You are driving to destination"
            showArrivedButton -> "You have arrived at pickup location"
            currentTripStatus == TripStatus.ACCEPTED -> "You are driving to rider"
            else -> "You are driving to rider"
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = headerText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.W700,
                    color = ModalTextDark,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = routeState?.formattedDuration
                        ?: getTripDuration()?.let { "Est. $it minutes" } ?: "Calculating...",
                    fontSize = 12.sp,
                    color = ModalTextGray,
                    fontWeight = FontWeight.W500)
            }

            // Real-time "X Mins away" Badge - fixed width, won't be pushed
            Surface(
                color = ModalLightGrayBg,
                border = BorderStroke(1.dp, Color(0xFFF1F1F1)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.widthIn(min = 72.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${routeState?.durationInMinutes ?: getTripDuration() ?: "~"}",
                        fontWeight = FontWeight.W700,
                        fontSize = 16.sp,
                        color = ModalTextDark
                    )
                    Text(
                        text = "Mins away",
                        fontSize = 12.sp,
                        color = ModalTextDark,
                        fontWeight = FontWeight.W600
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. PICKUP LOCATION CARD
        DetailCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.pin_1),
                    contentDescription = "Pickup Pin",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Pickup Point",
                        fontSize = 12.sp,
                        color = ModalTextGray,
                        fontWeight = FontWeight.W400
                    )
                    Text(
                        text = getPickupAddress() ?: "Pickup location",
                        fontWeight = FontWeight.W700,
                        fontSize = 14.sp,
                        color = ModalTextDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. RIDER PROFILE CARD (Name, Trips, Buttons, Avatar)
        DetailCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Side: Text + Buttons
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = getRiderName() ?: "Rider",
                        fontWeight = FontWeight.W700,
                        fontSize = 18.sp,
                        color = ModalTextDark
                    )
                    Text(
                        text = "${getRiderTrips() ?: 0} total trips",
                        fontSize = 12.sp,
                        color = ModalTextGray,
                        fontWeight = FontWeight.W600
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Chat & Call Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Get unread message count from chatViewModel
                        val unreadCount = chatViewModel?.unreadMessageCount?.collectAsState()?.value ?: 0

                        BadgedIconButton(
                            id = R.drawable.message_1,
                            badgeCount = unreadCount,
                            onClick = {
                                // Navigate to chat screen with shared ViewModel
                                val orderId = trip?.order ?: ""
                                val riderName = getRiderName() ?: "Rider"
                                val riderPhone = getRiderPhone()
                                if (orderId.isNotBlank()) {
                                    // Navigate immediately - ChatScreen will mark as read
                                    onNavigateToChat(orderId, riderName, riderPhone)
                                    Log.d("TripModal", "Navigating to chat - orderId: $orderId, riderName: $riderName, riderPhone: $riderPhone")
                                } else {
                                    Log.w("TripModal", "Cannot navigate to chat: orderId is blank")
                                }
                            })
                        ProfileIconButton(
                            id = R.drawable.phone_1, onClick = {
                                // Dial the user's phone number
                                val phoneNumber = getRiderPhone()
                                if (!phoneNumber.isNullOrBlank()) {
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:$phoneNumber")
                                        }
                                        context.startActivity(intent)
                                        Log.d("TripModal", "Dialing: $phoneNumber")
                                    } catch (e: Exception) {
                                        Log.e("TripModal", "Failed to open dialer: ${e.message}")
                                    }
                                } else {
                                    Log.w("TripModal", "Phone number is not available")
                                }
                            })
                    }
                }

                // Right Side: Avatar Placeholder
                Surface(
                    shape = CircleShape, color = Color(0xFFE0E0E0), modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Rider Avatar",
                            tint = Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. PRICE SECTION (Wallet, Strikethrough price)
        DetailCard {
            Column {
                // Top Row: "Estimated Price" --- "Wallet Payment"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Estimated Price",
                        fontSize = 12.sp,
                        color = ModalTextGray,
                        fontWeight = FontWeight.W400
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.wallet_1),
                            contentDescription = "Wallet",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Wallet Payment",
                            fontSize = 12.sp,
                            color = ModalTextDark,
                            fontWeight = FontWeight.W400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))


                // Price Text
                Text(
                    buildAnnotatedString {
//                        withStyle(
//                            style = SpanStyle(
//                                textDecoration = TextDecoration.LineThrough,
//                                color = ModalTextGray,
//                                fontWeight = FontWeight.W700,
//                                fontSize = 14.sp,
//                            )
//                        ) {
//                            append("₦4500 ")
//                        }
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.W700,
                                fontSize = 16.sp,
                                color = ModalTextDark
                            )
                        ) {
                            val priceRange = getPriceRange()
                            append(if (priceRange != null && priceRange.isNotEmpty()) "₦${priceRange.last()}" else "₦0")
                        }
                    })

//                Spacer(modifier = Modifier.height(4.dp))
//                Text(
//                    buildAnnotatedString {
//                        withStyle(
//                            style = SpanStyle(
//                                color = ModalTextGray,
//                                fontWeight = FontWeight.W400,
//                                fontSize = 12.sp,
//                            )
//                        ) {
//                            append("Kabukabu will pay driver ")
//                        }
//                        withStyle(
//                            style = SpanStyle(
//                                fontWeight = FontWeight.W500,
//                                fontSize = 12.sp,
//                                color = ModalTextDark
//                            )
//                        ) {
//                            append("₦4500")
//                        }
//                        withStyle(
//                            style = SpanStyle(
//                                color = ModalTextGray,
//                                fontWeight = FontWeight.W400,
//                                fontSize = 12.sp,
//                            )
//                        ) {
//                            append(" balance")
//                        }
//                    }
//                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // 5. BUTTONS ("I have arrived", "Start Trip", "End Trip" & "Cancel")
        // Show buttons based on trip status - only when sheet is expanded
        if (isExpanded) {
            when (currentTripStatus) {
                TripStatus.ACCEPTED -> {
                    // Show "I have arrived" button when driver is heading to pickup and within 500m
                    if (showArrivedButton) {
                        ArrivedButton(
                            onClick = { driverViewModel.arrivedPickup() },
                            isLoading = isArrivedButtonLoading
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                TripStatus.ARRIVED_PICKUP -> {
                    // Show "Start Trip" swipe button when driver has arrived at pickup
                    SwipeButton(
                        isActive = true,
                        isLoading = isStartTripLoading,
                        activeText = "Swipe to start trip >",
                        inactiveText = "Swipe to start trip >",
                        loadingText = "Starting trip...",
                        onStateChange = { driverViewModel.startTrip() })
                    Spacer(modifier = Modifier.height(16.dp))
                }

                TripStatus.TRIP_STARTED -> {
                    // Show End Trip button when driver is near destination
                    if (showEndTripButton) {
                        SwipeButton(
                            isActive = true,
                            isLoading = isEndTripLoading,
                            activeText = "Swipe to end trip >",
                            inactiveText = "Swipe to end trip >",
                            loadingText = "Ending trip...",
                            onStateChange = { driverViewModel.endTrip() })
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                else -> {
                    // No button for other statuses
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if(currentTripStatus == TripStatus.ACCEPTED || currentTripStatus == TripStatus.ARRIVED_PICKUP){
            Button(
                onClick = {
                    // Get trip ID from either activeTrip or pendingTripEvent
                    val orderId = trip?.order ?: pendingTripEvent?.eventId
                    if (!orderId.isNullOrBlank()) {
                        tripViewModel.cancelActiveTrip(
                            orderId = orderId,
                            reason = "Driver cancelled",
                            driverViewModel = driverViewModel
                        )
                        chatViewModel?.clearChatData()
                    }
                },
                enabled = !isCanceling,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0XFFF1F1F1)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                if (isCanceling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = CancelRed,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Cancel Trip",
                        color = CancelRed,
                        fontWeight = FontWeight.W500,
                        fontSize = 14.sp
                    )
                }
            }

        }


        // Extra space at the bottom for better scrolling experience
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun DetailCard(content: @Composable () -> Unit) {
    Surface(
        color = ModalLightGrayBg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE6E6E6)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            content()
        }
    }
}

@Composable
fun ProfileIconButton(id: Int, onClick: () -> Unit = {}) {
    Surface(
        color = Color(0xFFEBEBEB),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(42.dp).composableSafeClickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = id),
                contentDescription = "Action Button",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun BadgedIconButton(id: Int, badgeCount: Int = 0, onClick: () -> Unit = {}) {
    Surface(
        color = Color(0xFFEBEBEB),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(42.dp).composableSafeClickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = id),
                contentDescription = "Action Button",
                modifier = Modifier.size(20.dp)
            )

            // Show badge if count > 0
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-2).dp, y = 2.dp)
                        .size(18.dp)
                        .background(Color.Red, CircleShape)
                        .wrapContentSize(Alignment.Center)
                ) {
                    Text(
                        text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        style = androidx.compose.ui.text.TextStyle(
                            lineHeight = 9.sp,
                            platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                                includeFontPadding = false
                            )
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ArrivedButton(
    onClick: () -> Unit = {}, isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow),
        shape = RoundedCornerShape(40),
        border = BorderStroke(1.dp, Color.Black),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp), color = ModalTextDark, strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "I have arrived at pickup location",
                color = ModalTextDark,
                fontWeight = FontWeight.W700,
                fontSize = 14.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TripCompletionBottomSheet(
    endTripData: EndTripData?,
    driverViewModel: DriverViewModel,
    onDismiss: () -> Unit
) {
    // Extract trip details from endTripData
    val endedTrip = endTripData?.endedTrip
    val paymentDetails = endTripData?.paymentDetails
    val driverEarned =
        paymentDetails?.tripPrice ?: endedTrip?.priceDetails?.driverEarned ?: endedTrip?.price ?: 0
    endedTrip?.paymentType ?: "cash"
    endedTrip?.distanceInKm ?: 0.0
    endedTrip?.durationInMinutes ?: 0
    val pickupAddress = endedTrip?.startAddress?.fullAddress ?: "Pickup location"
    val dropoffAddress = endedTrip?.endAddress?.fullAddress ?: "Destination"
    val orderId = endedTrip?.id ?: ""

    // Rating state - initially null (no rating given)
    var rating by remember { mutableStateOf<Int?>(null) }

    // Selected feedback chips
    var selectedFeedback by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Define chip groups once so they can be referenced when rating changes
    val negativeChips = listOf(
        "Rude rider",
        "Abusive rider",
        "Long waiting time",
        "Bad driving",
        "Dirty rider",
        "Bad communication"
    )
    val positiveChips = listOf(
        "Smooth ride",
        "Good rider",
        "Easy Navigation",
        "Excellent service",
        "Good communication"
    )

    // Ensure selectedFeedback stays relevant when rating changes
    LaunchedEffect(rating) {
        val allowed = if ((rating ?: 0) <= 2) negativeChips else positiveChips
        selectedFeedback = selectedFeedback.filter { it in allowed }.toSet()
    }

    // Root container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        // --- STACKED BOTTOM SHEET IMPLEMENTATION ---
        Box(
            modifier = Modifier.align(Alignment.BottomCenter),
            contentAlignment = Alignment.BottomCenter
        ) {
            // LAYER 1: The "Back" Sheet (Peeking out from behind)
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .fillMaxHeight(0.885f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = Color(0xFFD8D8D8),
                shadowElevation = 4.dp
            ) {
                // Decorative empty layer
            }

            // LAYER 2: The "Front" Main Sheet
            Surface(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .fillMaxHeight(0.88f)
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                // Content Scroll View
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(top = 24.dp, bottom = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Text(
                        text = "Arrived at destination",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F1F1F)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Please rate your rider", fontSize = 14.sp, color = Color(0xFF1F1F1F)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Price Card
                    TripCompletionContainerBox {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "You Earned", color = Color(0xFF1F1F1F), fontSize = 14.sp
                            )
                            Text(
                                text = "₦${driverEarned}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F1F1F)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Route Card
                    TripCompletionContainerBox {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Custom drawn route indicator
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(top = 4.dp, end = 12.dp)
                            ) {
                                // Hollow Red Circle
                                Canvas(modifier = Modifier.size(10.dp)) {
                                    drawCircle(
                                        color = Color(0xFFEF5350),
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                }

                                // Dashed Line
                                Canvas(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(30.dp)
                                        .padding(vertical = 4.dp)
                                ) {
                                    drawLine(
                                        color = Color.LightGray,
                                        start = Offset(0f, 0f),
                                        end = Offset(0f, size.height),
                                        pathEffect = PathEffect.dashPathEffect(
                                            floatArrayOf(10f, 10f), 0f
                                        ),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }

                                // Hollow Blue Circle
                                Canvas(modifier = Modifier.size(10.dp)) {
                                    drawCircle(
                                        color = Color(0xFF2962FF),
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                }
                            }

                            // Address Text
                            Column {
                                Text(
                                    text = pickupAddress,
                                    color = Color(0xFF1F1F1F),
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = dropoffAddress,
                                    color = Color(0xFF1F1F1F),
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Rating Section
                    TripCompletionContainerBox {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Rate your Rider",
                                color = Color(0xFF1F1F1F),
                                fontSize = 14.sp
                            )

                            // Show rating text only when rating is given
                            if (rating != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = when (rating) {
                                        1 -> "Poor"
                                        2 -> "Fair"
                                        3 -> "Good"
                                        4 -> "Very Good"
                                        5 -> "Excellent"
                                        else -> "Good"
                                    },
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1F1F1F)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Stars
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(5) { index ->
                                    val interactionSource = remember { MutableInteractionSource() }
                                    Icon(
                                        imageVector = if (rating != null && index < rating!!) Icons.Filled.Star else Icons.Outlined.Star,
                                        contentDescription = "Star ${index + 1}",
                                        tint = if (rating != null && index < rating!!) Color(0xFF66BB6A) else Color.LightGray,
                                        modifier = Modifier
                                            .size(32.dp).composableSafeClickable(onClick = {val newRating = index + 1
                                                rating = newRating})

                                    )
                                }
                            }

                            // Show feedback section only after rating is given
                            if (rating != null) {
                                Spacer(modifier = Modifier.height(20.dp))

                                Text(
                                    text = "Feedback (optional)",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Chips (dynamic by rating)
                                FlowRow(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    maxItemsInEachRow = 3
                                ) {
                                    val chipsToShow = if ((rating ?: 0) <= 2) negativeChips else positiveChips

                                    chipsToShow.forEach { label ->
                                        TripCompletionFeedbackChip(
                                            text = label,
                                            isSelected = selectedFeedback.contains(label),
                                            onClick = {
                                                selectedFeedback = if (selectedFeedback.contains(label)) {
                                                    selectedFeedback - label
                                                } else {
                                                    selectedFeedback + label
                                                }
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Continue Button
                    Button(
                        onClick = {
                            // If rating was given and feedback chips selected, submit with comment
                            if (rating != null && selectedFeedback.isNotEmpty()) {
                                val feedbackComment = selectedFeedback.joinToString(", ")
                                driverViewModel.rateRider(
                                    orderId = orderId,
                                    rating = rating!!.toDouble(),
                                    comment = feedbackComment
                                )
                            }
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KabukabuYellow
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Continue",
                            fontSize = 18.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TripCompletionContainerBox(content: @Composable () -> Unit) {
    // Outer surface for clean white background
    Surface(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color.White
    ) {
        // Inner box for the gray border
        Box(
            modifier = Modifier.border(
                    width = 1.dp,
                    color = Color(0xFFE0E0E0).copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            content()
        }
    }
}

@Composable
fun TripCompletionFeedbackChip(
    text: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Surface(
        color = if (isSelected) Color(0xFFF4C430) else Color(0xFFF5F5F5),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.composableSafeClickable(onClick = onClick)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color.Black else Color(0xFF1F1F1F)
        )
    }
}
