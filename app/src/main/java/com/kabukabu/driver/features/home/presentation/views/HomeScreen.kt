//package com.kabukabu.driver.features.home.presentation.views
//
//import TripRequestModalAnimated
//import android.Manifest
//import android.annotation.SuppressLint
//import android.content.Context
//import android.content.pm.PackageManager
//import android.graphics.Bitmap
//import android.graphics.Canvas
//import android.location.Geocoder
//import android.location.Location
//import android.os.Looper
//import android.util.Log
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.annotation.DrawableRes
//import androidx.appcompat.content.res.AppCompatResources
//import androidx.compose.animation.animateContentSize
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.gestures.Orientation
//import androidx.compose.foundation.gestures.draggable
//import androidx.compose.foundation.gestures.rememberDraggableState
//import androidx.compose.foundation.interaction.MutableInteractionSource
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Star
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.core.content.ContextCompat
//import androidx.compose.ui.draw.clip
//import coil.compose.AsyncImage
//import com.kabukabu.driver.features.home.presentation.viewmodel.DriverViewModel
//import com.kabukabu.driver.core.data.local.UserPreferences
//import kotlinx.coroutines.flow.firstOrNull
//import com.google.android.gms.location.LocationServices
//import com.google.android.gms.location.LocationRequest
//import com.google.android.gms.location.LocationCallback
//import com.google.android.gms.location.LocationResult
//import com.google.android.gms.location.Priority
//import com.kabukabu.driver.R
//import com.mapbox.geojson.Point
//import com.mapbox.maps.CameraOptions
//import com.mapbox.maps.MapView
//import com.mapbox.maps.dsl.cameraOptions
//import com.mapbox.maps.plugin.animation.MapAnimationOptions
//import com.mapbox.maps.plugin.animation.flyTo
//import com.mapbox.maps.plugin.annotation.annotations
//import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
//import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
//import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
//import com.mapbox.maps.plugin.gestures.gestures
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.unit.IntOffset
//import kotlin.math.roundToInt
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.delay
//import androidx.compose.ui.graphics.ColorFilter
//import androidx.compose.animation.core.tween
//import kotlinx.coroutines.launch
//import androidx.compose.animation.core.Animatable
//import com.mapbox.maps.plugin.compass.compass
//import com.mapbox.maps.plugin.logo.logo
//import com.mapbox.maps.plugin.scalebar.scalebar
//import com.mapbox.maps.plugin.attribution.attribution
//import androidx.compose.ui.draw.blur
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.kabukabu.driver.features.profile.data.ActiveTrip
//import com.kabukabu.driver.features.home.presentation.viewmodel.TripViewModel
//import com.kabukabu.driver.core.utils.SoundPlayer
//import com.kabukabu.driver.core.utils.TripUiState
//import com.mapbox.api.directions.v5.DirectionsCriteria
//import com.mapbox.api.directions.v5.MapboxDirections
//import com.mapbox.api.directions.v5.models.DirectionsResponse
//import com.mapbox.core.constants.Constants
//import com.mapbox.geojson.LineString
//import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.slideInHorizontally
//import androidx.compose.animation.slideOutHorizontally
//import androidx.compose.foundation.gestures.detectVerticalDragGestures
//import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.platform.LocalConfiguration
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.text.font.FontFamily
//import com.kabukabu.driver.core.theme.TextSecondary
//import com.kabukabu.driver.core.theme.Success
//import com.kabukabu.driver.core.theme.BorderSubtle
//import com.kabukabu.driver.features.home.presentation.views.components.DraggableActiveTripSheet
//import com.kabukabu.driver.features.home.presentation.views.components.DriverRideScreenWithAnchoredControls
//import com.kabukabu.driver.features.home.presentation.views.components.FullScreenDrawer
//import com.kabukabu.driver.features.home.presentation.views.components.MapComponent
//import com.kabukabu.driver.features.home.presentation.views.components.TripRequestCard
//import com.kabukabu.driver.features.home.presentation.views.components.UIOverlay
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//@Composable
//fun HomeScreen(
//    onLogout: () -> Unit,
//    onNavigateToWallet: () -> Unit,
//    onNavigateToAnalytics: () -> Unit,
//    onNavigateToMyTrips: () -> Unit,
//    onNavigateToPromotions: () -> Unit,
//    onNavigateToSupport: () -> Unit,
//    onNavigateToAbout: () -> Unit,
//    onNavigateToRepairLoan: () -> Unit,
//    onNavigateToProfile: () -> Unit,
//) {
//    val context = LocalContext.current
//    val (currentLocation, setCurrentLocation) = remember { mutableStateOf<Location?>(null) }
//    var hasLocationPermission by remember { mutableStateOf(false) }
//
//    val tripViewModel: TripViewModel = viewModel()
//    val driverViewModel: DriverViewModel = viewModel()
//    val tripUiState by tripViewModel.uiState.collectAsState()
//    val isAccepting by tripViewModel.isAccepting.collectAsState()
//    val isDeclining by tripViewModel.isDeclining.collectAsState()
//    val isOnline by driverViewModel.isOnline.collectAsState()
//    val activeTrip by driverViewModel.activeTrip.collectAsState()
//    val errorMessage by driverViewModel.errorMessage.collectAsState()
//    val currentTripState = tripUiState
//
//    // MapView state for recenter functionality
//    val mapViewState = remember { mutableStateOf<com.mapbox.maps.MapView?>(null) }
//
//    // Snackbar host state for showing error messages
//    val snackbarHostState = remember { SnackbarHostState() }
//
//    Log.d("HomeScreen", "Observed isOnline state: $isOnline")
//    Log.d("HomeScreen", "Observed activeTrip state: $activeTrip")
//
//    // Show snackbar when there's an error message
//    LaunchedEffect(errorMessage) {
//        errorMessage?.let { message ->
//            snackbarHostState.showSnackbar(
//                message = message,
//                duration = SnackbarDuration.Short
//            )
//            driverViewModel.clearErrorMessage()
//        }
//    }
//
//    // Check if we already have permission
//    LaunchedEffect(Unit) {
//        hasLocationPermission = ContextCompat.checkSelfPermission(
//            context,
//            Manifest.permission.ACCESS_FINE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
//            context,
//            Manifest.permission.ACCESS_COARSE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED
//    }
//
//    val locationPermissionLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestMultiplePermissions()
//    ) { permissions ->
//        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
//            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
//        ) {
//            hasLocationPermission = true
//        }
//    }
//
//    // Request permission if not granted
//    LaunchedEffect(Unit) {
//        if (!hasLocationPermission) {
//            locationPermissionLauncher.launch(
//                arrayOf(
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                )
//            )
//        }
//    }
//
//    // Continuous location updates
//    DisposableEffect(hasLocationPermission) {
//        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
//
//        if (!hasLocationPermission) {
//            return@DisposableEffect onDispose { }
//        }
//
//        val locationRequest = LocationRequest.Builder(
//            Priority.PRIORITY_HIGH_ACCURACY,
//            10000L // Update every 10 seconds
//        ).apply {
//            setMinUpdateIntervalMillis(5000L) // Fastest update every 5 seconds
//            setWaitForAccurateLocation(false)
//        }.build()
//
//        val locationCallback = object : LocationCallback() {
//            override fun onLocationResult(locationResult: LocationResult) {
//                locationResult.lastLocation?.let { location ->
//                    Log.d("HomeScreen", "Location updated: ${location.latitude}, ${location.longitude}")
//                    setCurrentLocation(location)
//                }
//            }
//        }
//
//        try {
//            // Get initial location immediately
//            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
//                location?.let {
//                    Log.d("HomeScreen", "Initial location: ${it.latitude}, ${it.longitude}")
//                    setCurrentLocation(it)
//                }
//            }
//
//            // Start continuous updates
//            fusedLocationClient.requestLocationUpdates(
//                locationRequest,
//                locationCallback,
//                Looper.getMainLooper()
//            )
//        } catch (e: SecurityException) {
//            Log.e("HomeScreen", "Location permission error: ${e.message}")
//        }
//
//        onDispose {
//            fusedLocationClient.removeLocationUpdates(locationCallback)
//        }
//    }
//
//    // Play sound on new trip request
//    LaunchedEffect(tripUiState) {
//        if (tripUiState is TripUiState.TripRequest) {
//            SoundPlayer.playTripAlert(context)
//        }
//    }
//
//    var isDrawerOpen by remember { mutableStateOf(false) }
//
//    Box(modifier = Modifier.fillMaxSize()) {
//        val isTripIncoming = tripUiState is TripUiState.TripRequest
//
//        MapComponent(
//            currentLocation = currentLocation,
//            activeTrip = activeTrip,
//            mapViewState = mapViewState,
//            modifier = Modifier.blur(if (isTripIncoming) 20.dp else 0.dp)
//        )
//        UIOverlay(
//            currentLocation = currentLocation,
//            onLogout = onLogout,
//            isOnline = isOnline,
//            onIsOnlineChange = { driverViewModel.updateOnlineStatus(it) },
//            onMenuClick = { isDrawerOpen = true },
//            driverViewModel = driverViewModel,
//            onRecenterMap = {
//                // Recenter map to driver's current location
//                currentLocation?.let { location ->
//                    mapViewState.value?.mapboxMap?.setCamera(
//                        com.mapbox.maps.CameraOptions.Builder()
//                            .center(com.mapbox.geojson.Point.fromLngLat(location.longitude, location.latitude))
//                            .zoom(15.0)
//                            .build()
//                    )
//                    android.util.Log.d("HomeScreen", "Map recentered to: ${location.latitude}, ${location.longitude}")
//                }
//            }
//        )
//
//
//
//      //Trip found card
//        if (currentTripState is TripUiState.TripRequest) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color.Black.copy(alpha = 0.5f))
//                    .clickable(
//                        enabled = true,
//                        onClick = {},
//                        indication = null,
//                        interactionSource = remember { MutableInteractionSource() }
//                    )
//            )
//
//            Box(
//                modifier = Modifier
//                    .align(Alignment.BottomCenter)
//                    .padding(bottom = 30.dp)
//            ) {
//                TripRequestModalAnimated(
//                    distanceInfo = null,
//                    isVisible = true,
//                    tripDetails = currentTripState.tripDetails,
//                    driverLocation = currentLocation,
//                    remainingTime = currentTripState.remainingTime,
//                    isAccepting = isAccepting,
//                    isDeclining = isDeclining,
//                    onAccept = { tripViewModel.acceptTrip(driverViewModel) },
//                    onDecline = { tripViewModel.declineTrip(driverViewModel) },
//                    onTimeout = { tripViewModel.declineTrip(driverViewModel) }
//                )
//            }
//        }
//
//        // Full-screen drawer overlay (render last to be on top)
//        AnimatedVisibility(
//            visible = isDrawerOpen,
//            enter = slideInHorizontally(initialOffsetX = { -it }),
//            exit = slideOutHorizontally(targetOffsetX = { -it })
//        ) {
//            FullScreenDrawer(
//                onClose = { isDrawerOpen = false },
//                onLogout = {
//                    isDrawerOpen = false
//                    onLogout()
//                },
//                onNavigateToWallet = {
//                    isDrawerOpen = false
//                    onNavigateToWallet()
//                },
//                onNavigateToAnalytics = {
//                    isDrawerOpen = false
//                    onNavigateToAnalytics()
//                },
//                onNavigateToMyTrips = {
//                    isDrawerOpen = false
//                    onNavigateToMyTrips()
//                },
//                onNavigateToPromotions = {
//                    isDrawerOpen = false
//                    onNavigateToPromotions()
//                },
//                onNavigateToSupport = {
//                    isDrawerOpen = false
//                    onNavigateToSupport()
//                },
//                onNavigateToAbout = {
//                    isDrawerOpen = false
//                    onNavigateToAbout()
//                },
//                onNavigateToRepairLoan = {
//                    isDrawerOpen = false
//                    onNavigateToRepairLoan()
//                },
//                onNavigateToProfile = {
//                    isDrawerOpen = false
//                    onNavigateToProfile()
//                }
//            )
//        }
//
//        // Snackbar host for error messages
//        SnackbarHost(
//            hostState = snackbarHostState,
//            modifier = Modifier.align(Alignment.BottomCenter)
//        )
//    }
//
//    // Active Trip Bottom Sheet
//    activeTrip?.let { trip ->
//       DriverRideScreenWithAnchoredControls(trip)
//    }
//}
//
//
//
//// Linear interpolation
////private fun lerp(start: Float, stop: Float, fraction: Float): Float {
////    return (1 - fraction) * start + fraction * stop
////}
//
//
//
