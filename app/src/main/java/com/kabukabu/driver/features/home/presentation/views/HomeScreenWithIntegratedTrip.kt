package com.kabukabu.driver.features.home.presentation.views

import TripRequestModalAnimated
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.kabukabu.driver.R
import com.kabukabu.driver.core.components.SwipeButton
import com.kabukabu.driver.core.utils.SoundPlayer
import com.kabukabu.driver.core.utils.TripUiState
import com.kabukabu.driver.features.home.presentation.viewmodel.DriverViewModel
import com.kabukabu.driver.features.home.presentation.viewmodel.TripViewModel
import com.kabukabu.driver.features.home.presentation.views.components.*
import com.kabukabu.driver.features.profile.data.ActiveTrip
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

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
    val errorMessage by driverViewModel.errorMessage.collectAsState()
    val currentTripState = tripUiState

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
                message = message,
                duration = SnackbarDuration.Short
            )
            driverViewModel.clearErrorMessage()
        }
    }

    // Track if we should show permission dialog
    var showFullScreenIntentDialog by remember { mutableStateOf(false) }

    // Start/Stop TripRequestService based on online status
    LaunchedEffect(isOnline) {
//        if (isOnline) {
//            Log.d("HomeScreen", "Driver is online - starting TripRequestService")
//            com.kabukabu.driver.services.TripRequestService.startService(context)
//
//            // Check if full-screen intent permission is needed
//            if (com.kabukabu.driver.core.utils.FullScreenIntentHelper.shouldRequestPermission(context)) {
//                showFullScreenIntentDialog = true
//            }
//        } else {
//            Log.d("HomeScreen", "Driver is offline - stopping TripRequestService")
//            com.kabukabu.driver.services.TripRequestService.stopService(context)
//        }
    }



    // Check if we already have permission
    LaunchedEffect(Unit) {
//        driverViewModel.fetchUserProfile()
        hasLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            hasLocationPermission = true
        }
    }

    // Request permission if not granted
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            val permissionsToRequest = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
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
        com.kabukabu.driver.core.data.socket.SocketService.tripCancelledEvent.collect { tripCancelledEvent ->
            Log.d("HomeScreen", "Trip cancelled event received: ${tripCancelledEvent.order.id}, Status: ${tripCancelledEvent.status}")
            // Fetch profile in background to update active trip (set to null)
            driverViewModel.fetchUserProfile()
        }
    }

    // Emit driver location during active trip
    LaunchedEffect(activeTrip, currentLocation) {
        // Capture activeTrip in a local variable to avoid smart cast issues
        val trip = activeTrip
        if (trip != null) {
            Log.d("HomeScreen", "Starting location emission for trip: ${trip.id}")

            // Track trip start time for elapsed time calculation
            val tripStartTime = System.currentTimeMillis()
            var lastEmissionLocation: Location? = null
            var totalDistance = 0.0

            while (isActive) {
                // Re-check if trip is still active
                if (activeTrip == null) {
                    Log.d("HomeScreen", "Trip ended, stopping location emission")
                    break
                }

                currentLocation?.let { location ->
//                    // Calculate elapsed time in seconds
//                    val elapsedTimeSeconds = ((System.currentTimeMillis() - tripStartTime) / 1000).toInt()
//
//                    // Calculate distance from last emission point
//                    if (lastEmissionLocation != null) {
//                        val distanceToLast = lastEmissionLocation.distanceTo(location)
//                        totalDistance += (distanceToLast / 1000.0) // Convert meters to km
//                    }

                    // Emit location to backend
                    com.kabukabu.driver.core.data.socket.SocketService.emitLocation(
                        lat = location.latitude,
                        long = location.longitude,
                        orderId = trip.id,

                    )

                    lastEmissionLocation = location

                    Log.d("HomeScreen", "Emitted location for trip ${trip.id}: ")
                }

                // Emit location every 5 seconds
                delay(5000)
            }

            Log.d("HomeScreen", "Stopped location emission for trip")
        }
    }

    var isDrawerOpen by remember { mutableStateOf(false) }

    // Determine if we should show the active trip bottom sheet
    val shouldShowActiveTripSheet = activeTrip != null

    if (shouldShowActiveTripSheet) {
        IntegratedActiveTripScreen(
            currentLocation = currentLocation,
            activeTrip = activeTrip,
            isOnline = isOnline,
            driverViewModel = driverViewModel,
            onMenuClick = { isDrawerOpen = true },
            onLogout = onLogout
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

                }
            )

            // Trip found card
            if (currentTripState is TripUiState.TripRequest) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(
                            enabled = true,
                            onClick = {},
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                )

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
                        onDecline = { tripViewModel.declineTrip() },
                        onTimeout = { tripViewModel.declineTrip() },
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
            onClose = { isDrawerOpen = false },
            onLogout = {
                isDrawerOpen = false
                onLogout()
            },
            onNavigateToWallet = {
                isDrawerOpen = false
                onNavigateToWallet()
            },
            onNavigateToAnalytics = {
                isDrawerOpen = false
                onNavigateToAnalytics()
            },
            onNavigateToMyTrips = {
                isDrawerOpen = false
                onNavigateToMyTrips()
            },
            onNavigateToPromotions = {
                isDrawerOpen = false
                onNavigateToPromotions()
            },
            onNavigateToSupport = {
                isDrawerOpen = false
                onNavigateToSupport()
            },
            onNavigateToAbout = {
                isDrawerOpen = false
                onNavigateToAbout()
            },
            onNavigateToRepairLoan = {
                isDrawerOpen = false
                onNavigateToRepairLoan()
            },
            onNavigateToProfile = {
                isDrawerOpen = false
                onNavigateToProfile()
            }
        )
    }

    // Permission dialog for Android 14+
    if (showFullScreenIntentDialog) {
        AlertDialog(
            onDismissRequest = { showFullScreenIntentDialog = false },
            title = { Text("Enable Trip Alerts") },
            text = {
                Text(
                    "To receive trip requests when your phone is locked or the app is in background, " +
                    "please enable 'Display over other apps' permission.\n\n" +
                    "This ensures you never miss a trip!"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFullScreenIntentDialog = false
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            com.kabukabu.driver.core.utils.FullScreenIntentHelper.openFullScreenIntentSettings(context)
                        }
                    }
                ) {
                    Text("Enable")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFullScreenIntentDialog = false }) {
                    Text("Later")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntegratedActiveTripScreen(
    currentLocation: Location?,
    activeTrip: ActiveTrip?,
    isOnline: Boolean,
    driverViewModel: DriverViewModel,
    onMenuClick: () -> Unit,
    onLogout: () -> Unit
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val sheetMaxHeight = 600.dp
    val context = LocalContext.current

    // Route state management
    val routeState = remember { mutableStateOf<RouteState?>(null) }

    // MapView reference for recenter functionality
    val mapViewState = remember { mutableStateOf<MapView?>(null) }

    // Calculate rider point for map operations
    val riderPoint = remember(activeTrip?.startPoint) {
        activeTrip?.startPoint?.let {
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
    val currentTripStatus by driverViewModel.currentTripStatus.collectAsState()

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
                    showArrivedButton = showArrivedButton,
                    isArrivedButtonLoading = isArrivedButtonLoading,
                    isStartTripLoading = isStartTripLoading,
                    currentTripStatus = currentTripStatus,
                    trip = activeTrip,
                    routeState = routeState.value
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // THE REAL MAP from HomeScreen
            MapComponent(
                currentLocation = currentLocation,
                activeTrip = activeTrip,
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
                currentTripStatus = currentTripStatus,
                sheetState = scaffoldState.bottomSheetState,
                isExpanded = isExpanded,
                offsetY = 16.dp,
                currentLocation = currentLocation,
                riderPoint = riderPoint,
                mapView = mapViewState.value,
                driverViewModel = driverViewModel,
                onOpenGoogleMaps = {
                    riderPoint?.let { destination ->
                        openGoogleMapsNavigation(context, currentLocation, destination)
                    }
                },
                onRecenterMap = {
                    if (riderPoint != null) {
                        recenterMapOnRoute(mapViewState.value, currentLocation, riderPoint)
                    } else {
                        recenterMapOnDriver(mapViewState.value, currentLocation)
                    }
                }
            )
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
                .clickable { onMenuClick() },
            contentAlignment = Alignment.Center
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnchoredMapControlsIntegrated(
    showArrivedButton: Boolean = false,
    isArrivedButtonLoading: Boolean = false,
    isStartTripLoading: Boolean = false,
    currentTripStatus: com.kabukabu.driver.features.home.presentation.viewmodel.TripStatus = com.kabukabu.driver.features.home.presentation.viewmodel.TripStatus.STANDBY,
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
            }
    ) {
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
                    onClick = onOpenGoogleMaps
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
                    modifier = Modifier.size(60.dp),
                    onClick = onRecenterMap
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

            // Show buttons based on trip status when not expanded
            if (!isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(15.dp))

                    when (currentTripStatus) {
                        com.kabukabu.driver.features.home.presentation.viewmodel.TripStatus.ACCEPTED -> {
                            // Show "I have arrived" button when driver is heading to pickup and within 500m
                            if (showArrivedButton) {
                                ArrivedButton(
                                    onClick = { driverViewModel.arrivedPickup() },
                                    isLoading = isArrivedButtonLoading
                                )
                            }
                        }
                        com.kabukabu.driver.features.home.presentation.viewmodel.TripStatus.ARRIVED_PICKUP -> {
                            // Show "Start Trip" swipe button when driver has arrived at pickup
                            SwipeButton(
                                isActive = false,
                                isLoading = isStartTripLoading,
                                inactiveText = "Swipe to start trip >",
                                loadingText = "Starting trip...",
                                onStateChange = { driverViewModel.startTrip() }
                            )
                        }
                        com.kabukabu.driver.features.home.presentation.viewmodel.TripStatus.TRIP_STARTED -> {
                            // No button shown when trip has started
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
val PrimaryYellow = Color(0xFFFBC02D)
val CancelRed = Color(0xFFEF2C5B)

@Composable
fun DriverTripModalContent(
    driverViewModel: DriverViewModel,
    trip: ActiveTrip?,
    showArrivedButton: Boolean = false,
    isArrivedButtonLoading: Boolean = false,
    isStartTripLoading: Boolean = false,
    currentTripStatus: com.kabukabu.driver.features.home.presentation.viewmodel.TripStatus = com.kabukabu.driver.features.home.presentation.viewmodel.TripStatus.STANDBY,
    routeState: RouteState? = null
) {
    val context = LocalContext.current


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        // 1. HEADER: Title + Time Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "You are driving to rider",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.W700,
                    color = ModalTextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = routeState?.formattedDuration
                        ?: trip?.durationInMinutes?.let { "Est. $it minutes" }
                        ?: "Calculating...",
                    fontSize = 12.sp,
                    color = ModalTextGray,
                    fontWeight = FontWeight.W500
                )
            }

            // Real-time "X Mins away" Badge
            Surface(
                color = ModalLightGrayBg,
                border = BorderStroke(1.dp, Color(0xFFF1F1F1)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.width(72.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${routeState?.durationInMinutes ?: trip?.durationInMinutes ?: "~"}",
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
                        text = trip?.startAddress?.fullAddress ?: "2 Ebinpejo Lane, Idumota, Lagos",
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
                        text = trip?.user?.fullName ?: "Solomon Mafoluku",
                        fontWeight = FontWeight.W700,
                        fontSize = 18.sp,
                        color = ModalTextDark
                    )
                    Text(
                        text = "${trip?.user?.totalTrips ?: 0} total trips",
                        fontSize = 12.sp,
                        color = ModalTextGray,
                        fontWeight = FontWeight.W600
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Chat & Call Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProfileIconButton(
                            id = R.drawable.message_1,
                            onClick = {
                                // TODO: Handle message action
                                Log.d("TripModal", "Message button clicked")
                            }
                        )
                        ProfileIconButton(
                            id = R.drawable.phone_1,
                            onClick = {
                                // Dial the user's phone number
                                val phoneNumber = trip?.user?.phoneNumber
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
                            }
                        )
                    }
                }

                // Right Side: Avatar Placeholder
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE0E0E0),
                    modifier = Modifier.size(64.dp)
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
                            append(if (trip != null) "₦${trip.priceRange.last()}" else "₦2,000")

                        }
                    }
                )

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

        // 5. BUTTONS ("I have arrived", "Start Trip" & "Cancel")
        when (currentTripStatus) {
            com.kabukabu.driver.features.home.presentation.viewmodel.TripStatus.ACCEPTED -> {
                // Show "I have arrived" button when driver is heading to pickup and within 500m
                if (showArrivedButton) {
                    ArrivedButton(
                        onClick = { driverViewModel.arrivedPickup() },
                        isLoading = isArrivedButtonLoading
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            com.kabukabu.driver.features.home.presentation.viewmodel.TripStatus.ARRIVED_PICKUP -> {
                // Show "Start Trip" swipe button when driver has arrived at pickup
                SwipeButton(
                    isActive = false,
                    isLoading = isStartTripLoading,
                    inactiveText = "Swipe to start trip >",
                    loadingText = "Starting trip...",
                    onStateChange = { driverViewModel.startTrip() }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            com.kabukabu.driver.features.home.presentation.viewmodel.TripStatus.TRIP_STARTED -> {
                // No button shown when trip has started
            }
            else -> {
                // No button for other statuses
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { /* TODO */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0XFFF1F1F1)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Text(
                text = "Cancel Trip",
                color = CancelRed,
                fontWeight = FontWeight.W500,
                fontSize = 14.sp
            )
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
        modifier = Modifier.size(42.dp),
        onClick = onClick
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
fun ArrivedButton(
    onClick: () -> Unit = {},
    isLoading: Boolean = false
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
                modifier = Modifier.size(20.dp),
                color = ModalTextDark,
                strokeWidth = 2.dp
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

