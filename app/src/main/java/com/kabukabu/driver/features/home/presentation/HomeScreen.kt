package com.kabukabu.driver.features.home.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import com.kabukabu.driver.features.home.presentation.DriverViewModel
import com.kabukabu.driver.core.data.local.UserPreferences
import kotlinx.coroutines.flow.firstOrNull
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.kabukabu.driver.R
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.mapbox.maps.plugin.gestures.gestures
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import kotlinx.coroutines.launch
import androidx.compose.animation.core.Animatable
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.maps.plugin.attribution.attribution
import androidx.compose.ui.draw.blur
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kabukabu.driver.features.profile.data.ActiveTrip
import com.kabukabu.driver.features.home.presentation.TripViewModel
import com.kabukabu.driver.core.utils.SoundPlayer
import com.kabukabu.driver.core.utils.TripUiState
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.core.constants.Constants
import com.mapbox.maps.EdgeInsets
import com.mapbox.geojson.LineString
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import com.kabukabu.driver.core.theme.KabukabuYellow
import com.kabukabu.driver.core.theme.KabukabuYellowLight
import com.kabukabu.driver.core.theme.SurfaceCard
import com.kabukabu.driver.core.theme.SurfaceCardAlt
import com.kabukabu.driver.core.theme.TextSecondary
import com.kabukabu.driver.core.theme.Success
import com.kabukabu.driver.core.theme.BorderSubtle

@Composable
fun HomeScreen(
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
    val (currentLocation, setCurrentLocation) = remember { mutableStateOf<Location?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }

    val tripViewModel: TripViewModel = viewModel()
    val driverViewModel: DriverViewModel = viewModel()
    val tripUiState by tripViewModel.uiState.collectAsState()
    val isAccepting by tripViewModel.isAccepting.collectAsState()
    val isDeclining by tripViewModel.isDeclining.collectAsState()
    val isOnline by driverViewModel.isOnline.collectAsState()
    val activeTrip by driverViewModel.activeTrip.collectAsState()
    val errorMessage by driverViewModel.errorMessage.collectAsState()
    
    // Snackbar host state for showing error messages
    val snackbarHostState = remember { SnackbarHostState() }

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

    // Check if we already have permission
    LaunchedEffect(Unit) {
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
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Continuous location updates
    DisposableEffect(hasLocationPermission) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        
        if (!hasLocationPermission) {
            return@DisposableEffect onDispose { }
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L // Update every 10 seconds
        ).apply {
            setMinUpdateIntervalMillis(5000L) // Fastest update every 5 seconds
            setWaitForAccurateLocation(false)
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    Log.d("HomeScreen", "Location updated: ${location.latitude}, ${location.longitude}")
                    setCurrentLocation(location)
                }
            }
        }

        try {
            // Get initial location immediately
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    Log.d("HomeScreen", "Initial location: ${it.latitude}, ${it.longitude}")
                    setCurrentLocation(it)
                }
            }
            
            // Start continuous updates
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                android.os.Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e("HomeScreen", "Location permission error: ${e.message}")
        }

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    // Play sound on new trip request
    LaunchedEffect(tripUiState) {
        if (tripUiState is TripUiState.TripRequest) {
            SoundPlayer.playTripAlert(context)
        }
    }

    // Profile is fetched in DriverViewModel.init()

    var isDrawerOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        val isTripIncoming = tripUiState is TripUiState.TripRequest
        MapComponent(
            currentLocation = currentLocation,
            activeTrip = activeTrip,
            modifier = Modifier.blur(if (isTripIncoming) 20.dp else 0.dp)
        )
        UIOverlay(
            currentLocation = currentLocation,
            onLogout = onLogout,
            isOnline = isOnline,
            onIsOnlineChange = { driverViewModel.updateOnlineStatus(it) },
            onMenuClick = { isDrawerOpen = true }
        )

        val currentTripState = tripUiState
        if (currentTripState is TripUiState.TripRequest) {
            // Dimming overlay
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
                TripRequestCard(
                    isVisible = true,
                    tripDetails = currentTripState.tripDetails,
                    driverLocation = currentLocation,
                    remainingTime = currentTripState.remainingTime,
                    isAccepting = isAccepting,
                    isDeclining = isDeclining,
                    onAccept = { tripViewModel.acceptTrip() },
                    onDecline = { tripViewModel.declineTrip() },
                    onTimeout = { tripViewModel.declineTrip() }
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
        
        // Snackbar host for error messages
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Active Trip Bottom Sheet
    activeTrip?.let { trip ->
        DraggableActiveTripSheet(trip = trip)
    }
}

@Composable
fun DraggableActiveTripSheet(trip: ActiveTrip) {
    val coroutineScope = rememberCoroutineScope()
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp

    // Drastically reduced heights
    val minHeight = screenHeightDp / 2.3f
    val maxHeight = screenHeightDp / 1.4f

    val minHeightPx = with(LocalDensity.current) { minHeight.toPx() }
    val maxHeightPx = with(LocalDensity.current) { maxHeight.toPx() }

    val currentHeightPx = remember { Animatable(minHeightPx) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter // Anchor to the bottom
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(LocalDensity.current) { currentHeightPx.value.toDp() })
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                // Snap to closest state
                                val midpoint = minHeightPx + (maxHeightPx - minHeightPx) / 2
                                if (currentHeightPx.value > midpoint) {
                                    currentHeightPx.animateTo(maxHeightPx, tween(150)) // Animate to expanded
                                } else {
                                    currentHeightPx.animateTo(minHeightPx, tween(150)) // Animate to collapsed
                                }
                            }
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            val newHeight = (currentHeightPx.value - dragAmount).coerceIn(minHeightPx, maxHeightPx)
                            currentHeightPx.snapTo(newHeight)
                        }
                    }
                },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color.LightGray, shape = CircleShape)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Active Trip", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Status: ${trip.status ?: "N/A"}")
                Text(text = "From: ${trip.startAddress?.fullAddress ?: "N/A"}")
                Text(text = "To: ${trip.endAddress?.fullAddress ?: "N/A"}")
                Text(text = "Price: ₦${trip.price ?: 0.0}")
            }
        }
    }
}

// Linear interpolation
private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return (1 - fraction) * start + fraction * stop
}

@Composable
private fun MapComponent(
    currentLocation: Location?,
    activeTrip: ActiveTrip? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    
    // Remember the annotation manager to avoid creating multiple markers
    val pointAnnotationManager = remember {
        mapView.annotations.createPointAnnotationManager()
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )

    // Animate map to current location and draw polyline if there's an active trip
    LaunchedEffect(currentLocation, activeTrip, mapView) {
        if (currentLocation != null) {
            val driverPoint = Point.fromLngLat(currentLocation.longitude, currentLocation.latitude)

            // Set camera position
            val cameraOptions = cameraOptions {
                center(driverPoint)
                zoom(17.0) // Increased from 15.0
            }
            mapView.getMapboxMap().setCamera(cameraOptions)

            // Clear existing markers and add updated driver marker
            pointAnnotationManager.deleteAll()
            
            // Add rider marker and polyline if there's an active trip
            activeTrip?.startPoint?.let { startPoint ->
                if (startPoint.size >= 2) {
                    val riderPoint = Point.fromLngLat(startPoint[0], startPoint[1])
                    getRouteAndDrawPolyline(mapView, driverPoint, riderPoint)
                    
                    val fixedCameraPosition = CameraOptions.Builder()
                        .center(driverPoint)
                        .zoom(17.0) // Increased from 15.0
                        .build()
                    mapView.getMapboxMap().flyTo(
                        cameraOptions = fixedCameraPosition,
                        animationOptions = MapAnimationOptions.Builder().duration(1000L).build()
                    )

                    Log.d("MapComponent", "Driver location: ${driverPoint.longitude()}, ${driverPoint.latitude()}")
                    Log.d("MapComponent", "Rider location: ${riderPoint.longitude()}, ${riderPoint.latitude()}")
                }
            }
            
            // Always add the driver marker (whether there's an active trip or not)
            bitmapFromDrawable(context, R.drawable._d_cars)?.let { bitmap ->
                val driverAnnotationOptions = PointAnnotationOptions()
                    .withPoint(driverPoint)
                    .withIconImage(bitmap)
                pointAnnotationManager.create(driverAnnotationOptions)
            }
        }
    }

    // Initial map setup
    LaunchedEffect(mapView) {
        mapView.getMapboxMap().loadStyleUri("mapbox://styles/kabukabuapp/cmd1e8u2s009k01s913jc4ywh")

        // Enable map interaction using the gestures plugin
        mapView.gestures.apply {
            pinchToZoomEnabled = true
            rotateEnabled = true
            scrollEnabled = true
            doubleTapToZoomInEnabled = true
        }

        // Disable default Mapbox UI components
        mapView.logo.enabled = false
        mapView.attribution.enabled = false
        mapView.compass.enabled = false
        mapView.scalebar.enabled = false
    }
}

@Composable
private fun UIOverlay(
    currentLocation: Location?,
    onLogout: () -> Unit,
    isOnline: Boolean,
    onIsOnlineChange: (Boolean) -> Unit,
    onMenuClick: () -> Unit
) {
    var isCardExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var locationName by remember { mutableStateOf("Loading location...") }

    // This effect runs when `currentLocation` changes.
    LaunchedEffect(currentLocation) {
        if (currentLocation != null) {
            // Use a coroutine to avoid blocking the main thread
            launch(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(context)
                    val addresses = geocoder.getFromLocation(
                        currentLocation.latitude,
                        currentLocation.longitude,
                        1
                    )
                    if (addresses?.isNotEmpty() == true) {
                        locationName = addresses[0].thoroughfare ?: "Unknown Location"
                    }
                } catch (e: Exception) {
                    locationName = "Location not found"
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Top UI Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
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
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                )
            }

            // Center Location Display
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
                        text = locationName,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.W600,
                        fontSize = 12.sp
                    )
                }
            }

            // Right Spacer for balance
            Spacer(Modifier.size(44.dp))
        }

        // Bottom Expandable Card
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            ExpandableDriverStatusCard(
                onLogout = onLogout,
                onOnlineStatusChanged = onIsOnlineChange,
                isOnline = isOnline
            )
        }
    }
}

@Composable
private fun ExpandableDriverStatusCard(
    onLogout: () -> Unit,
    onOnlineStatusChanged: (Boolean) -> Unit,
    isOnline: Boolean
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column {
        Box {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OfflineStatus(isOnline = isOnline)
                        Spacer(modifier = Modifier.height(16.dp))
                        DriverStats()
                    }

                    if (isExpanded) {
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("More content goes here...")
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onLogout) {
                                Text("Logout")
                            }
                        }
                    }
                }
            }

            ViewMoreButton(
                modifier = Modifier.align(Alignment.BottomCenter),
                isExpanded = isExpanded,
                onClick = { isExpanded = !isExpanded }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OnlineSlider(
            isOnline = isOnline,
            onStateChange = { newStatus ->
                onOnlineStatusChanged(newStatus)
            }
        )
    }
}

@Composable
private fun ViewMoreButton(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.arrows_expand),
                contentDescription = if (isExpanded) "View Less" else "View More",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isExpanded) "View Less" else "View More",
                fontWeight = FontWeight.W600,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun OfflineStatus(isOnline: Boolean) {
    val statusText = if (isOnline) "You are online" else "You are offline"
    val dotColor = if (isOnline) Success else TextSecondary
    val textColor = if (isOnline) MaterialTheme.colorScheme.onSurface else TextSecondary

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(dotColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = statusText,
            color = textColor,
            fontWeight = FontWeight.W500,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun DriverStats() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "0",
                fontWeight = FontWeight.W700,
                fontSize = 18.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Trips today",
                fontWeight = FontWeight.W600,
                fontSize = 12.sp,
                color = Color(0xFF9A9A9A)
            )
        }

        Divider(
            modifier = Modifier
                .height(40.dp)
                .width(1.dp),
            color = BorderSubtle
        )

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "₦0",
                fontWeight = FontWeight.W700,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Earned today",
                fontWeight = FontWeight.W600,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun OnlineSlider(
    isOnline: Boolean,
    onStateChange: (Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }

    // Online colors
    val onlineContainerColor = Color(0xFFFFBF00)
    val onlineTextColor = Color(0xFFFFF5D8)
    val onlineDraggerColor = Color(0xFFC39200)
    val onlineIconColor = Color(0xFFFDFDFD)

    // Offline colors
    val offlineContainerColor = Color(0xFF161616) // rgba(22, 22, 22, 1)
    val offlineTextColor = Color(0xFF9A9A9A)      // rgba(154, 154, 154, 1)
    val offlineDraggerColor = Color(0xFF3C3B3B)   // rgba(60, 59, 59, 1)
    val offlineIconColor = Color(0xFFFFBF00)      // rgba(255, 191, 0, 1)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(
                color = if (isOnline) onlineContainerColor else offlineContainerColor,
                shape = RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        val containerWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val handleSizePx = with(LocalDensity.current) { 44.dp.toPx() }
        val maxOffset = containerWidthPx - handleSizePx - with(LocalDensity.current) { (7.dp * 2).toPx() }

        Text(
            text = if (isOnline) "Swipe to go offline >" else "Swipe to go online >",
            color = if (isOnline) onlineTextColor else offlineTextColor,
            fontWeight = FontWeight.W600,
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = 16.dp)
        )

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .padding(horizontal = 7.dp)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        coroutineScope.launch {
                            offsetX.snapTo((offsetX.value + delta).coerceIn(0f, maxOffset))
                        }
                    },
                    onDragStopped = {
                        coroutineScope.launch {
                            if (offsetX.value > maxOffset / 2) {
                                onStateChange(!isOnline)
                                // Animate to the end, hold, then animate back to start
                                offsetX.animateTo(maxOffset, animationSpec = tween(100))
                                delay(200)
                                offsetX.animateTo(0f, animationSpec = tween(300))
                            } else {
                                // Animate back to start
                                offsetX.animateTo(0f, animationSpec = tween(200))
                            }
                        }
                    }
                )
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = if (isOnline) onlineDraggerColor else offlineDraggerColor,
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.caret_right),
                    contentDescription = "Slide to toggle online status",
                    modifier = Modifier.size(width = 30.dp, height = 20.dp),
                    colorFilter = ColorFilter.tint(if (isOnline) onlineIconColor else offlineIconColor)
                )
            }
        }
    }
}



private fun bitmapFromDrawable(context: Context, @DrawableRes resId: Int): Bitmap? {
    return AppCompatResources.getDrawable(context, resId)?.let { drawable ->
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    }
} 

private fun getRouteAndDrawPolyline(mapView: MapView, origin: Point, destination: Point) {
    Log.d("DirectionsAPI", "Requesting route from ${origin.longitude()},${origin.latitude()} to ${destination.longitude()},${destination.latitude()}")

    val annotationApi = mapView.annotations
    annotationApi.cleanup()

    // Create managers
    val polylineAnnotationManager = annotationApi.createPolylineAnnotationManager()
    val pointAnnotationManager = annotationApi.createPointAnnotationManager()
    
    // Add driver marker
    bitmapFromDrawable(mapView.context, R.drawable._d_cars)?.let {
        pointAnnotationManager.create(PointAnnotationOptions().withPoint(origin).withIconImage(it))
    }
    // Add rider marker
    bitmapFromDrawable(mapView.context, R.drawable.ic_red_marker)?.let {
        pointAnnotationManager.create(PointAnnotationOptions().withPoint(destination).withIconImage(it))
    }

    Log.d("DirectionsAPI", "Built directions client with token: ${mapView.context.getString(R.string.mapbox_access_token).take(10)}...")
    
    val client = MapboxDirections.builder()
        .origin(origin)
        .destination(destination)
        .overview(DirectionsCriteria.OVERVIEW_FULL)
        .profile(DirectionsCriteria.PROFILE_DRIVING)
        .accessToken(mapView.context.getString(R.string.mapbox_access_token))
        .build()

    client.enqueueCall(object : retrofit2.Callback<DirectionsResponse> {
        override fun onResponse(call: retrofit2.Call<DirectionsResponse>, response: retrofit2.Response<DirectionsResponse>) {
             if (!response.isSuccessful || response.body() == null || response.body()!!.routes().isEmpty()) {
                Log.e("DirectionsAPI", "No route found, drawing straight line. Code: ${response.code()}")
                 // Fallback to a straight line
                 polylineAnnotationManager.create(
                     PolylineAnnotationOptions()
                         .withPoints(listOf(origin, destination))
                         .withLineColor("#FF0000") // Red
                         .withLineWidth(4.0)
                 )
                return
            }

            val currentRoute = response.body()!!.routes()[0]
            val routeGeometry = currentRoute.geometry()
            Log.d("DirectionsAPI", "Route geometry: $routeGeometry")
            
            if (routeGeometry != null) {
                val points = LineString.fromPolyline(routeGeometry, Constants.PRECISION_6).coordinates()
                polylineAnnotationManager.create(
                    PolylineAnnotationOptions()
                        .withPoints(points)
                        .withLineColor("#FF0000") // Red
                        .withLineWidth(4.0)
                )
                 Log.d("DirectionsAPI", "Successfully drew route with ${points.size} points.")
            }
        }

        override fun onFailure(call: retrofit2.Call<DirectionsResponse>, t: Throwable) {
            Log.e("DirectionsAPI", "Directions API call failed", t)
        }
    })
} 

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullScreenDrawer(
    onClose: () -> Unit,
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
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        color = Color.White
    ) {
        val context = LocalContext.current
        LaunchedEffect(Unit) {
            try {
                val userPreferences = UserPreferences.getInstance(context)
                val token = userPreferences.authToken.firstOrNull()
                android.util.Log.d("AuthToken", "FULL TOKEN: $token")
            } catch (e: Exception) {
                android.util.Log.e("AuthToken", "Failed to read token: ${e.message}")
            }
        }

        // Observe user profile for drawer header
        val driverViewModel: DriverViewModel = viewModel()
        val userProfile by driverViewModel.userProfile.collectAsState()

        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "My Account",
                        color = Color.Black,
                        fontWeight = FontWeight.W600,
                        fontSize = 18.sp,
                        // TODO: Replace with actual Sofia Sans font if available in the project assets
                        // fontFamily = FontFamily(Font(R.font.sofia_sans_semibold))
                        // Fallback until font is provided:
                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(36.dp)
                            .border(
                                width = 1.dp,
                                color = Color(0xFFE1E1E1),
                                shape = CircleShape
                            )
                            .clickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF9A9A9A),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Profile header container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(Color(0xFFF9F9F9), shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp)
                        .clickable { onNavigateToProfile() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Avatar + Name/Link
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = userProfile?.profileImage,
                                contentDescription = "Profile image",
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEFEFEF), shape = CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = userProfile?.fullName ?: "",
                                    color = Color(0xFF161616),
                                    fontWeight = FontWeight.W600,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "view profile",
                                    color = Color(0xFF3D3D3D),
                                    fontWeight = FontWeight.W400,
                                    fontSize = 12.sp,
                                )
                            }
                        }

                        // Right: rating
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFFFBF00),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = (userProfile?.averageRating?.value ?: 0.0).let { String.format("%.1f", it) },
                                color = Color(0xFF161616),
                                fontWeight = FontWeight.W700,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Drawer menu items
                Column(modifier = Modifier.fillMaxWidth()) {
                    // General group
                    Text(
                        text = "General",
                        color = Color(0xFF161616),
                        fontWeight = FontWeight.W700,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                            DrawerMenuItem(
                                iconRes = R.drawable.wallet_drawer,
                                title = "Wallet",
                                onClick = {
                                    onNavigateToWallet()
                                }
                            )
                            DrawerMenuItem(
                                iconRes = R.drawable.solar_stopwatch_linear,
                                title = "Analytics",
                                onClick = { onNavigateToAnalytics() }
                            )
                            DrawerMenuItem(
                                iconRes = R.drawable.taxi_drawer,
                                title = "My Trip",
                                onClick = { onNavigateToMyTrips() }
                            )
                            DrawerMenuItem(
                                iconRes = R.drawable.receipt_percent,
                                title = "Promotions",
                                onClick = { onNavigateToPromotions() }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Support group
                    Text(
                        text = "Support",
                        color = Color(0xFF161616),
                        fontWeight = FontWeight.W700,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                            DrawerMenuItem(
                                iconRes = R.drawable.info_circle,
                                title = "About",
                                onClick = { onNavigateToAbout() }
                            )
                            DrawerMenuItem(
                                iconRes = R.drawable.mynaui_shield_solid,
                                title = "Repair Loan",
                                onClick = { onNavigateToRepairLoan() }
                            )
                            DrawerMenuItem(
                                iconRes = R.drawable.message_text,
                                title = "Support",
                                onClick = { onNavigateToSupport() }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Final unnamed group with only Logout
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                            DrawerMenuItem(
                                iconRes = R.drawable.log_out,
                                title = "Logout",
                                onClick = {
                                    onLogout()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerMenuItem(
    iconRes: Int,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = title,
            modifier = Modifier
                .size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            color = Color.Black,
            fontWeight = FontWeight.W600,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun TripRequestCard(
    isVisible: Boolean,
    tripDetails: Any?,
    driverLocation: Location?,
    remainingTime: Int,
    isAccepting: Boolean,
    isDeclining: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onTimeout: () -> Unit
) {
    if (isVisible) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Trip Request",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Time remaining: ${remainingTime}s",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onDecline,
                        enabled = !isDeclining && !isAccepting,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        if (isDeclining) {
                            Text("Declining...")
                        } else {
                            Text("Decline")
                        }
                    }
                    
                    Button(
                        onClick = onAccept,
                        enabled = !isAccepting && !isDeclining,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                    ) {
                        if (isAccepting) {
                            Text("Accepting...")
                        } else {
                            Text("Accept")
                        }
                    }
                }
            }
        }
    }
}