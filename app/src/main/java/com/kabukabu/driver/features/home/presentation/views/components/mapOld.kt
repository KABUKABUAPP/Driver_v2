//package com.kabukabu.driver.features.home.presentation.views.components
//
//import android.content.Context
//import android.graphics.Bitmap
//import android.graphics.Canvas
//import android.location.Location
//import android.util.Log
//import androidx.annotation.DrawableRes
//import androidx.appcompat.content.res.AppCompatResources
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.MutableState
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.viewinterop.AndroidView
//import com.kabukabu.driver.R
//import com.kabukabu.driver.core.data.socket.TripFoundEvent
//import com.kabukabu.driver.core.utils.MapViewManager
//import com.kabukabu.driver.features.home.presentation.viewmodel.TripStatus
//import com.kabukabu.driver.features.profile.data.ActiveTrip
//import com.mapbox.api.directions.v5.DirectionsCriteria
//import com.mapbox.api.directions.v5.MapboxDirections
//import com.mapbox.api.directions.v5.models.DirectionsResponse
//import com.mapbox.core.constants.Constants
//import com.mapbox.geojson.LineString
//import com.mapbox.geojson.Point
//import com.mapbox.maps.CameraOptions
//import com.mapbox.maps.MapView
//import com.mapbox.maps.dsl.cameraOptions
//import com.mapbox.maps.plugin.animation.MapAnimationOptions
//import com.mapbox.maps.plugin.animation.flyTo
//import com.mapbox.maps.plugin.annotation.annotations
//import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
//import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
//import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
//import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
//import com.mapbox.maps.plugin.attribution.attribution
//import com.mapbox.maps.plugin.compass.compass
//import com.mapbox.maps.plugin.gestures.gestures
//import com.mapbox.maps.plugin.logo.logo
//import com.mapbox.maps.plugin.scalebar.scalebar
//import com.mapbox.maps.extension.style.layers.addLayer
//import com.mapbox.maps.extension.style.layers.addLayerBelow
//import com.mapbox.maps.extension.style.layers.generated.lineLayer
//import com.mapbox.maps.extension.style.sources.addSource
//import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
//import com.mapbox.maps.extension.style.sources.getSourceAs
//import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
//import com.mapbox.geojson.Feature
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import kotlinx.coroutines.delay
//
//// Constants for dashed line layer (driver to route start connector)
//private const val DASHED_LINE_SOURCE_ID = "dashed-connector-source"
//private const val DASHED_LINE_LAYER_ID = "dashed-connector-layer"
//
///**
// * Holds real-time route information from Mapbox Directions API
// */
//data class RouteState(
//    val distanceInMeters: Double? = null,
//    val durationInSeconds: Double? = null,
//    val lastUpdated: Long = 0L
//) {
//    val distanceInKm: Double?
//        get() = distanceInMeters?.let { it / 1000 }
//
//    val durationInMinutes: Int?
//        get() = durationInSeconds?.let { (it / 60).toInt() }
//
//    val formattedDuration: String
//        get() = durationInMinutes?.let { "Est. $it mins" } ?: "Calculating..."
//
//    val formattedDistance: String
//        get() = distanceInKm?.let { "%.1f km".format(it) } ?: "Calculating..."
//}
//
///**
// * Holds the result of trimming route points for the driver.
// * Separates the prepended segment (driver to route start) from the main route
// * to allow drawing them with different line styles (dashed vs solid).
// */
//data class TrimmedRouteResult(
//    val prependedSegment: List<Point>, // Driver position to route start (draw as dashed line)
//    val mainRoute: List<Point>         // Actual route from API (draw as solid line)
//) {
//    val allPoints: List<Point>
//        get() = if (prependedSegment.isNotEmpty() && mainRoute.isNotEmpty()) {
//            // Prepended segment already ends at mainRoute start, avoid duplication
//            prependedSegment.dropLast(1) + mainRoute
//        } else if (prependedSegment.isNotEmpty()) {
//            prependedSegment
//        } else {
//            mainRoute
//        }
//}
//
//@Composable
//fun MapComponent(
//    currentLocation: Location?,
//    activeTrip: ActiveTrip? = null,
//    pendingTripEvent: TripFoundEvent? = null,
//    tripStatus: TripStatus? = null,
//    onRouteInfoUpdated: (RouteState) -> Unit = {},
//    mapViewState: MutableState<MapView?>? = null,
//    // Parameters for preserving map state across navigations
//    savedCameraPosition: CameraOptions? = null,
//    onCameraPositionChanged: (CameraOptions) -> Unit = {},
//    isMapAlreadyInitialized: Boolean = false,
//    onMapInitialized: () -> Unit = {},
//    modifier: Modifier = Modifier
//) {
//    val context = LocalContext.current
//
//    // Use MapViewManager to get a cached MapView that persists across navigations
//    // This prevents the map from reloading every time the user navigates away and back
//    val mapViewManager = com.kabukabu.driver.core.utils.MapViewManager
//    val mapView = remember {
//        mapViewManager.getOrCreateMapView(context)
//    }
//
//    // Track if this is a fresh map or a restored one
//    val isRestoredMap = remember { mapViewManager.isMapStyleLoaded() }
//
//    // Expose mapView to parent if requested
//    LaunchedEffect(mapView) {
//        mapViewState?.value = mapView
//    }
//
//    // Track if style is loaded
//    val isStyleLoaded = remember { mutableStateOf(false) }
//
//    // Track if initial camera has been set
//    // Use saved state if map was already initialized
//    val initialCameraSet = remember { mutableStateOf(isMapAlreadyInitialized) }
//
//    // Track the active trip ID to detect when it changes
//    val previousTripId = remember { mutableStateOf<String?>(null) }
//
//    // Remember annotation managers to update them without recreating
//    val pointAnnotationManager = remember { mutableStateOf<com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager?>(null) }
//    val polylineAnnotationManager = remember { mutableStateOf<com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager?>(null) }
//
//    // Track individual annotation instances for smooth updates
//    val driverAnnotationId = remember { mutableStateOf<String?>(null) }
//    val riderAnnotationId = remember { mutableStateOf<String?>(null) }
//    val routeAnnotationId = remember { mutableStateOf<String?>(null) } // track current visible route polyline annotation id (solid main route)
//    val dashedRouteAnnotationId = remember { mutableStateOf<String?>(null) } // track dashed line annotation (prepended segment)
//
//    // Track last route calculation position for distance-based updates
//    val lastRouteCalcPosition = remember { mutableStateOf<Location?>(null) }
//    val lastRouteCalcTime = remember { mutableStateOf<Long>(0L) }
//
//    // Track last marker type to detect when we need to change the icon
//    val lastMarkerWasDestination = remember { mutableStateOf(false) }
//
//    // Minimum distance (in meters) before redrawing route to avoid flickering
//    val minDistanceForRouteRedraw = 50f
//
//    // Simple integer version counter to ignore stale responses
//    val routeRequestVersion = remember { mutableStateOf(0) }
//
//    // Remember the currently in-flight directions client so we can cancel it when a new request starts
//    val routeCallState = remember { mutableStateOf<MapboxDirections?>(null) }
//
//    AndroidView(
//        factory = { ctx ->
//            // Detach mapView from any existing parent before returning
//            // This is needed because we're reusing the same MapView instance
//            mapViewManager.detachFromParent()
//            Log.d("MapComponent", "AndroidView factory - returning cached MapView")
//            mapView
//        },
//        update = { view ->
//            // This is called when the composable recomposes
//            // The MapView is already attached to the parent, so no action needed
//            // Just log for debugging
//            Log.d("MapComponent", "AndroidView update - MapView reattached, style loaded: ${isStyleLoaded.value}")
//        },
//        modifier = modifier
//    )
//
//    // Periodic route updates for active trips (hybrid: time-based OR distance-based)
//    // Use activeTrip if available, otherwise use pendingTripEvent
//    LaunchedEffect(activeTrip?.id, pendingTripEvent?.eventId, tripStatus) {
//        // Get trip data from either source
//        val tripId = activeTrip?.id ?: pendingTripEvent?.eventId
//        val startPoint = activeTrip?.startPoint ?: pendingTripEvent?.startPoint
//
//        if (tripId != null && startPoint != null && startPoint.size >= 2) {
//            while (true) {
//                delay(60000) // Check every 60 seconds
//
//                val lastCalc = lastRouteCalcPosition.value
//                val timeSinceLastCalc = System.currentTimeMillis() - lastRouteCalcTime.value
//
//                // Update if 60 seconds passed OR driver moved 500m
//                val shouldUpdate =
//                    timeSinceLastCalc > 60000 ||
//                            (lastCalc != null && currentLocation != null && currentLocation.distanceTo(lastCalc) > 500)
//
//                if (shouldUpdate && currentLocation != null) {
//                    Log.d("MapComponent", "Periodic route update triggered")
//
//                    // Determine route based on trip status
//                    val isTripStarted = tripStatus == TripStatus.TRIP_STARTED
//                    val routeOrigin: Point
//                    val routeDestination: Point
//
//                    val endPoint = activeTrip?.endPoint
//
//                    if (isTripStarted && endPoint != null && endPoint.size >= 2) {
//                        // TRIP STARTED: Show driver's current location to destination
//                        routeOrigin = Point.fromLngLat(currentLocation.longitude, currentLocation.latitude)
//                        routeDestination = Point.fromLngLat(endPoint[0], endPoint[1])
//                    } else {
//                        // TRIP NOT STARTED: Show driver's current location to pickup
//                        routeOrigin = Point.fromLngLat(currentLocation.longitude, currentLocation.latitude)
//                        routeDestination = Point.fromLngLat(startPoint[0], startPoint[1])
//                    }
//
//                    polylineAnnotationManager.value?.let { polyMgr ->
//                        // Use improved fetch that versions responses
//                        fetchAndDrawRoute(
//                            mapView,
//                            routeOrigin,
//                            routeDestination,
//                            polyMgr,
//                            onRouteInfoUpdated,
//                            routeRequestVersion,
//                            routeAnnotationId,
//                            dashedRouteAnnotationId,
//                            routeCallState
//                        )
//                        lastRouteCalcPosition.value = currentLocation
//                        lastRouteCalcTime.value = System.currentTimeMillis()
//                    }
//                }
//            }
//        }
//    }
//
//    // Initial map setup - Load style FIRST (or restore if already loaded)
//    LaunchedEffect(mapView) {
//        // Check if we're restoring a map with style already loaded
//        if (isRestoredMap) {
//            Log.d("MapComponent", "Restoring existing map - style already loaded")
//            isStyleLoaded.value = true
//
//            // Restore camera position from MapViewManager if available
//            val savedCamera = mapViewManager.getSavedCameraState()
//            if (savedCamera != null) {
//                Log.d("MapComponent", "Restoring saved camera position from MapViewManager")
//                mapView.mapboxMap.setCamera(savedCamera)
//                initialCameraSet.value = true
//            } else if (savedCameraPosition != null) {
//                Log.d("MapComponent", "Restoring saved camera position from parameter")
//                mapView.mapboxMap.setCamera(savedCameraPosition)
//                initialCameraSet.value = true
//            }
//
//            // Notify that map is initialized
//            onMapInitialized()
//        } else {
//            Log.d("MapComponent", "Loading map style for the first time...")
//
//            // Use the modern Mapbox API for loading styles
//            mapView.mapboxMap.loadStyle("mapbox://styles/kabukabuapp/cmd1e8u2s009k01s913jc4ywh") { style ->
//                // This callback fires when style is fully loaded
//                isStyleLoaded.value = true
//                mapViewManager.setStyleLoaded(true)
//                Log.d("MapComponent", "✅ Map style loaded successfully")
//
//                // Restore saved camera position if available and map was already initialized
//                if (isMapAlreadyInitialized && savedCameraPosition != null) {
//                    Log.d("MapComponent", "Restoring saved camera position")
//                    mapView.mapboxMap.setCamera(savedCameraPosition)
//                    initialCameraSet.value = true
//                }
//
//                // Notify that map is initialized
//                onMapInitialized()
//            }
//        }
//
//        // Enable map interaction using the gestures plugin
//        mapView.gestures.apply {
//            pinchToZoomEnabled = true
//            rotateEnabled = true
//            scrollEnabled = true
//            doubleTapToZoomInEnabled = true
//        }
//
//        // Disable default Mapbox UI components
//        mapView.logo.enabled = false
//        mapView.attribution.enabled = false
//        mapView.compass.enabled = false
//        mapView.scalebar.enabled = false
//    }
//
//    // Save camera position when it changes for later restoration
//    LaunchedEffect(isStyleLoaded.value) {
//        if (isStyleLoaded.value) {
//            // Listen for camera changes and save position
//            mapView.mapboxMap.subscribeCameraChanged { cameraChangedEvent ->
//                val cameraState = mapView.mapboxMap.cameraState
//                val cameraOptions = CameraOptions.Builder()
//                    .center(cameraState.center)
//                    .zoom(cameraState.zoom)
//                    .bearing(cameraState.bearing)
//                    .pitch(cameraState.pitch)
//                    .build()
//                // Save to both the callback and MapViewManager
//                onCameraPositionChanged(cameraOptions)
//                mapViewManager.saveCameraState(cameraOptions)
//            }
//        }
//    }
//
//    // Initial cleanup check: When MapComponent first loads without an active trip,
//    // ensure any stale annotations from previous trips are cleaned up.
//    // This handles the edge case where the user dismissed the trip completion sheet
//    // and we're now showing the normal home screen's MapComponent.
//    LaunchedEffect(isStyleLoaded.value) {
//        if (!isStyleLoaded.value) return@LaunchedEffect
//
//        // Only perform cleanup if:
//        // 1. There's no active trip or pending trip event
//        // 2. MapViewManager indicates there are stale annotations OR the map has annotations visible
//        val hasNoTrip = activeTrip == null && pendingTripEvent == null
//        val needsCleanup = mapViewManager.needsAnnotationCleanup()
//
//        if (hasNoTrip && needsCleanup) {
//            Log.d("MapComponent", "🧹 Initial cleanup: No active trip but stale annotations detected")
//
//            // Clear dashed line layer
//            clearDashedLine(mapView)
//
//            // Clear all annotations
//            try {
//                mapView.annotations.cleanup()
//                Log.d("MapComponent", "Initial global annotations cleanup completed")
//            } catch (e: Exception) {
//                Log.w("MapComponent", "Error in initial annotations cleanup: ${e.message}")
//            }
//
//            // Mark cleanup complete
//            mapViewManager.markAnnotationsCleanedUp()
//
//            // Reset annotation managers
//            pointAnnotationManager.value = null
//            polylineAnnotationManager.value = null
//
//            // Reset annotation tracking IDs
//            driverAnnotationId.value = null
//            riderAnnotationId.value = null
//            routeAnnotationId.value = null
//            dashedRouteAnnotationId.value = null
//            lastMarkerWasDestination.value = false
//
//            Log.d("MapComponent", "✅ Initial cleanup complete - map ready for standby mode")
//        }
//    }
//
//    // Handle trip changes (when trip starts/ends) - This SHOULD reset camera
//    LaunchedEffect(activeTrip?.id, pendingTripEvent?.eventId, isStyleLoaded.value) {
//        if (!isStyleLoaded.value) {
//            Log.d("MapComponent", "Waiting for style to load...")
//            return@LaunchedEffect
//        }
//
//        // Use activeTrip ID if available, otherwise use pendingTripEvent ID
//        val currentTripId = activeTrip?.id ?: pendingTripEvent?.eventId
//        val tripChanged = currentTripId != previousTripId.value
//        val tripEnded = previousTripId.value != null && currentTripId == null
//
//        // Also check MapViewManager for stale annotations from previous MapComponent instances
//        // This handles the case where IntegratedActiveTripScreen's MapComponent had a trip
//        // and now we're in the normal HomeScreen's MapComponent with no trip
//        val needsGlobalCleanup = mapViewManager.needsAnnotationCleanup()
//
//        if (tripChanged || needsGlobalCleanup) {
//            Log.d("MapComponent", "Trip status changed: ${previousTripId.value} -> $currentTripId (tripEnded: $tripEnded, needsGlobalCleanup: $needsGlobalCleanup)")
//
//            // Update MapViewManager with new trip ID (for cross-component tracking)
//            mapViewManager.updateTripId(currentTripId)
//
//            // If trip ended OR there are stale annotations from another MapComponent, perform thorough cleanup
//            if (tripEnded || needsGlobalCleanup) {
//                Log.d("MapComponent", "🧹 Trip ended or stale annotations detected - performing thorough cleanup")
//
//                // Clear dashed line layer FIRST (uses style layer, not annotations)
//                clearDashedLine(mapView)
//
//                // Delete all annotations using existing managers BEFORE nullifying them
//                polylineAnnotationManager.value?.let { polyMgr ->
//                    try {
//                        polyMgr.deleteAll()
//                        Log.d("MapComponent", "Deleted all polyline annotations")
//                    } catch (e: Exception) {
//                        Log.w("MapComponent", "Error deleting polylines: ${e.message}")
//                    }
//                }
//
//                pointAnnotationManager.value?.let { pointMgr ->
//                    try {
//                        // Delete ALL point annotations including destination marker
//                        // We need to delete everything because we want a clean slate
//                        // The driver marker will be recreated when the location updates
//                        pointMgr.deleteAll()
//                        Log.d("MapComponent", "Deleted all point annotations (driver + destination)")
//                    } catch (e: Exception) {
//                        Log.w("MapComponent", "Error deleting point annotations: ${e.message}")
//                    }
//                }
//
//                // Also try to clear via the global MapView annotations API
//                // This handles edge cases where managers weren't properly stored
//                try {
//                    mapView.annotations.cleanup()
//                    Log.d("MapComponent", "Global annotations cleanup completed")
//                } catch (e: Exception) {
//                    Log.w("MapComponent", "Error in global annotations cleanup: ${e.message}")
//                }
//
//                // Mark cleanup complete in MapViewManager
//                mapViewManager.markAnnotationsCleanedUp()
//
//                // Nullify managers so they get recreated fresh
//                pointAnnotationManager.value = null
//                polylineAnnotationManager.value = null
//            } else {
//                // Trip changed but not ended (e.g., new trip started) - still clean up old annotations
//                Log.d("MapComponent", "🔄 New trip started - clearing old annotations")
//
//                // Clear dashed line layer
//                clearDashedLine(mapView)
//
//                // Clear all annotations for a fresh start
//                mapView.annotations.cleanup()
//                pointAnnotationManager.value = null
//                polylineAnnotationManager.value = null
//            }
//
//            previousTripId.value = currentTripId
//            initialCameraSet.value = false // Reset camera flag when trip changes
//
//            // Clear annotation IDs to force fresh markers (for all trip changes)
//            driverAnnotationId.value = null
//            riderAnnotationId.value = null
//            routeAnnotationId.value = null
//            dashedRouteAnnotationId.value = null
//
//            // Reset marker type tracking
//            lastMarkerWasDestination.value = false
//
//            Log.d("MapComponent", "✅ Cleanup complete - ready for ${if (currentTripId != null) "new trip" else "standby"}")
//        }
//    }
//
//    // Track previous trip status to detect changes
//    val lastTripStatus = remember { mutableStateOf<TripStatus?>(null) }
//
//    // Update markers when location changes - WITHOUT resetting camera (unless initial)
//    // Include pendingTripEvent in dependencies to react immediately when trip is accepted
//    LaunchedEffect(currentLocation, activeTrip?.id, pendingTripEvent?.eventId, tripStatus, isStyleLoaded.value) {
//        // Wait for style to be loaded before adding any annotations
//        if (!isStyleLoaded.value || currentLocation == null) {
//            Log.d("MapComponent", "Waiting for style to load or location... Style: ${isStyleLoaded.value}, Location: $currentLocation")
//            return@LaunchedEffect
//        }
//
//        val driverPoint = Point.fromLngLat(currentLocation.longitude, currentLocation.latitude)
//        val shouldSetCamera = !initialCameraSet.value
//
//        // Get trip data from either activeTrip or pendingTripEvent
//        val startPoint = activeTrip?.startPoint ?: pendingTripEvent?.startPoint
//        val endPoint = activeTrip?.endPoint
//
//        // Handle active trip with route (use pendingTripEvent as fallback)
//        if (startPoint != null && startPoint.size >= 2) {
//            // Determine which route to show based on trip status
//            val isTripStarted = tripStatus == TripStatus.TRIP_STARTED
//
//            val routeOrigin: Point
//            val routeDestination: Point
//            val destinationMarkerPoint: Point // Position for the destination marker
//
//            if (isTripStarted && endPoint != null && endPoint.size >= 2) {
//                // Trip has started: Show route from driver's current location to destination (endPoint)
//                routeOrigin = driverPoint
//                routeDestination = Point.fromLngLat(endPoint[0], endPoint[1])
//                destinationMarkerPoint = routeDestination // Marker shows destination
//                Log.d("MapComponent", "Trip started - showing route from driver to destination")
//            } else {
//                // Trip not started yet: Show route from driver to pickup (startPoint)
//                routeOrigin = driverPoint
//                routeDestination = Point.fromLngLat(startPoint[0], startPoint[1])
//                destinationMarkerPoint = routeDestination // Marker shows pickup
//                Log.d("MapComponent", "Trip not started - showing route from driver to pickup (using ${if (activeTrip != null) "activeTrip" else "pendingTripEvent"})")
//            }
//
//            // Check if trip status changed (force redraw if status changed)
//            val tripStatusChanged = lastTripStatus.value != tripStatus
//            if (tripStatusChanged) {
//                Log.d("MapComponent", "Trip status changed from ${lastTripStatus.value} to $tripStatus - forcing route redraw")
//                lastTripStatus.value = tripStatus
//
//                // When trip status changes, clear ALL markers and recreate
//                // This ensures the rider marker is properly removed when trip starts
//                pointAnnotationManager.value?.deleteAll()
//                driverAnnotationId.value = null
//                riderAnnotationId.value = null
//                Log.d("MapComponent", "Cleared all markers due to trip status change")
//            }
//
//            // Check if marker icon type needs to change (pickup icon -> destination icon)
//            val markerIconNeedsUpdate = lastMarkerWasDestination.value != isTripStarted
//            if (markerIconNeedsUpdate) {
//                Log.d("MapComponent", "Marker icon type changed (was destination: ${lastMarkerWasDestination.value}, now: $isTripStarted) - forcing marker recreation")
//                lastMarkerWasDestination.value = isTripStarted
//            }
//
//            // Check if we need to redraw the route (if driver moved significantly OR trip status changed)
//            val lastCalcPosition = lastRouteCalcPosition.value
//            val shouldRedrawRoute = lastCalcPosition == null ||
//                    currentLocation.distanceTo(lastCalcPosition) > minDistanceForRouteRedraw ||
//                    tripStatusChanged // ✅ Force redraw when status changes
//
//            Log.d("MapComponent", "Active Trip - Updating markers (camera reset: $shouldSetCamera, redraw route: $shouldRedrawRoute, status changed: $tripStatusChanged, marker icon changed: $markerIconNeedsUpdate)")
//
//            // Update route and markers with smart redrawing
//            updateRouteWithMarkers(
//                mapView,
//                routeOrigin,
//                routeDestination,
//                driverPoint,
//                destinationMarkerPoint,
//                context,
//                pointAnnotationManager,
//                polylineAnnotationManager,
//                driverAnnotationId,
//                riderAnnotationId,
//                shouldRedrawRoute,
//                onRouteInfoUpdated,
//                isTripStarted,
//                routeRequestVersion,
//                routeAnnotationId,
//                dashedRouteAnnotationId,
//                routeCallState
//            )
//
//            // Track position for distance-based updates (only if we redrew the route)
//            if (shouldRedrawRoute) {
//                lastRouteCalcPosition.value = currentLocation
//                lastRouteCalcTime.value = System.currentTimeMillis()
//            }
//
//            // Adjust camera on initial load, trip change, or trip status change
//            if (shouldSetCamera || tripStatusChanged) {
//                adjustCameraToShowRoute(mapView, routeOrigin, routeDestination)
//                initialCameraSet.value = true
//                Log.d("MapComponent", "Camera adjusted for active trip (initial: $shouldSetCamera, status changed: $tripStatusChanged)")
//            } else {
//                Log.d("MapComponent", "Markers updated, preserving user's camera position")
//            }
//        } else {
//            // No active trip - just show driver marker
//            Log.d("MapComponent", "No active trip - updating driver marker (camera reset: $shouldSetCamera)")
//
//            // Clear any existing route polylines and destination marker when trip ends
//            // This ensures the map is clean when there's no active trip
//            polylineAnnotationManager.value?.let { polyMgr ->
//                // Clear all polyline annotations (route lines)
//                // Use try-catch to handle potential edge cases
//                try {
//                    val annotations = polyMgr.annotations
//                    if (annotations.isNotEmpty()) {
//                        polyMgr.deleteAll()
//                        Log.d("MapComponent", "Cleared ${annotations.size} route polylines - no active trip")
//                    }
//                } catch (e: Exception) {
//                    Log.w("MapComponent", "Error clearing polylines: ${e.message}")
//                }
//                routeAnnotationId.value = null
//                dashedRouteAnnotationId.value = null
//            }
//
//            // Clear dashed line layer (uses style layer, not annotations)
//            clearDashedLine(mapView)
//
//            // Clear rider/destination marker (keep only driver marker)
//            pointAnnotationManager.value?.let { pointMgr ->
//                // Try to find and delete the destination marker by ID
//                riderAnnotationId.value?.let { riderId ->
//                    pointMgr.annotations.find { it.id == riderId }?.let { annotation ->
//                        pointMgr.delete(annotation)
//                        Log.d("MapComponent", "Cleared destination marker by ID - no active trip")
//                    }
//                }
//
//                // Also delete any annotations that aren't the driver marker (safety net)
//                val driverId = driverAnnotationId.value
//                if (driverId != null) {
//                    val nonDriverAnnotations = pointMgr.annotations.filter { it.id != driverId }
//                    if (nonDriverAnnotations.isNotEmpty()) {
//                        nonDriverAnnotations.forEach { annotation ->
//                            try {
//                                pointMgr.delete(annotation)
//                            } catch (e: Exception) {
//                                Log.w("MapComponent", "Error deleting non-driver annotation: ${e.message}")
//                            }
//                        }
//                        Log.d("MapComponent", "Cleared ${nonDriverAnnotations.size} extra annotations - no active trip")
//                    }
//                }
//
//                riderAnnotationId.value = null
//            }
//
//            // Reset route calculation tracking
//            lastRouteCalcPosition.value = null
//            lastRouteCalcTime.value = 0L
//            lastTripStatus.value = null
//
//            // Update driver marker smoothly
//            updateDriverMarker(
//                mapView,
//                driverPoint,
//                context,
//                pointAnnotationManager,
//                driverAnnotationId
//            )
//
//            // Only center camera on initial load
//            if (shouldSetCamera) {
//                val cameraOptions = cameraOptions {
//                    center(driverPoint)
//                    zoom(17.0)
//                }
//                mapView.mapboxMap.setCamera(cameraOptions)
//                initialCameraSet.value = true
//                Log.d("MapComponent", "Initial camera set for driver")
//            } else {
//                Log.d("MapComponent", "Driver marker updated, preserving user's camera position")
//            }
//        }
//    }
//}
//
///**
// * Updates driver marker only - smoothly updates position without recreating
// */
//private fun updateDriverMarker(
//    mapView: MapView,
//    driverPoint: Point,
//    context: Context,
//    managerState: MutableState<com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager?>,
//    annotationIdState: MutableState<String?>
//) {
//    // Get or create annotation manager
//    val manager = managerState.value ?: run {
//        val newManager = mapView.annotations.createPointAnnotationManager()
//        managerState.value = newManager
//        // Register with MapViewManager so it can be cleared when trip ends
//        MapViewManager.registerPointAnnotationManager(newManager)
//        newManager
//    }
//
//    val existingId = annotationIdState.value
//
//    if (existingId != null) {
//        // Update existing marker position smoothly
//        manager.annotations.find { it.id == existingId }?.let { annotation ->
//            annotation.point = driverPoint
//            manager.update(annotation)
//            Log.d("MapComponent", "Driver marker position smoothly updated")
//        } ?: run {
//            // Annotation was deleted, create new one
//            createDriverMarker(manager, driverPoint, context, annotationIdState)
//        }
//    } else {
//        // First time - create marker
//        createDriverMarker(manager, driverPoint, context, annotationIdState)
//    }
//}
//
//private fun createDriverMarker(
//    manager: com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager,
//    driverPoint: Point,
//    context: Context,
//    annotationIdState: MutableState<String?>
//) {
//    bitmapFromDrawable(context, R.drawable._d_cars)?.let { bitmap ->
//        val driverAnnotation = PointAnnotationOptions()
//            .withPoint(driverPoint)
//            .withIconImage(bitmap)
//        val annotation = manager.create(driverAnnotation)
//        annotationIdState.value = annotation.id
//        Log.d("MapComponent", "Driver marker created with ID: ${annotation.id}")
//    }
//}
//
///**
// * Updates the route line and both driver/rider markers - smoothly updates without flickering
// * @param routeOrigin The starting point for the route line
// * @param routeDestination The ending point for the route line
// * @param driverPoint The current driver location for the driver marker
// * @param destinationPoint The destination marker position (pickup before trip starts, destination after)
// * @param isTripStarted Whether the trip has started (affects marker display - no rider marker when started)
// */
//private fun updateRouteWithMarkers(
//    mapView: MapView,
//    routeOrigin: Point,
//    routeDestination: Point,
//    driverPoint: Point,
//    destinationPoint: Point,
//    context: Context,
//    pointManagerState: MutableState<com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager?>,
//    polylineManagerState: MutableState<com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager?>,
//    driverAnnotationIdState: MutableState<String?>,
//    riderAnnotationIdState: MutableState<String?>,
//    shouldRedrawRoute: Boolean,
//    onRouteInfoUpdated: (RouteState) -> Unit = {},
//    isTripStarted: Boolean = false,
//    routeRequestVersion: MutableState<Int>,
//    routeAnnotationId: MutableState<String?>,
//    dashedRouteAnnotationId: MutableState<String?>,
//    routeCallState: MutableState<MapboxDirections?>
//) {
//    Log.d("MapComponent", "Updating route (redraw route: $shouldRedrawRoute, trip started: $isTripStarted)")
//
//    // IMPORTANT: Create polyline manager FIRST, then point manager
//    // This ensures polyline is drawn under the markers
//    val polylineManager = polylineManagerState.value ?: run {
//        val newPolylineManager = mapView.annotations.createPolylineAnnotationManager()
//        polylineManagerState.value = newPolylineManager
//        // Register with MapViewManager so it can be cleared when trip ends
//        MapViewManager.registerPolylineAnnotationManager(newPolylineManager)
//        newPolylineManager
//    }
//
//    val pointManager = pointManagerState.value ?: run {
//        val newPointManager = mapView.annotations.createPointAnnotationManager()
//        pointManagerState.value = newPointManager
//        // Register with MapViewManager so it can be cleared when trip ends
//        MapViewManager.registerPointAnnotationManager(newPointManager)
//        newPointManager
//    }
//
//    // Draw route FIRST (if needed) so it appears under markers
//    if (shouldRedrawRoute) {
//        Log.d("MapComponent", "Redrawing route - clearing ${polylineManager.annotations.size} existing routes")
//        // polylineManager.deleteAll() // Don't delete immediately, we'll manage this in fetchAndDrawRoute
//
//        // Increment version to ignore stale responses
//        routeRequestVersion.value++
//
//        // Fetch and draw the new route (asynchronous operation)
//        fetchAndDrawRoute(mapView, routeOrigin, routeDestination, polylineManager, onRouteInfoUpdated, routeRequestVersion, routeAnnotationId, dashedRouteAnnotationId, routeCallState)
//    } else {
//        Log.d("MapComponent", "Keeping existing route (${polylineManager.annotations.size} routes)")
//
//        // Continuous trimming: as the driver moves, update both main route and dashed connector
//        // Only attempt trimming if we have a known current route annotation id
//        val currentRouteId = routeAnnotationId.value
//        if (currentRouteId != null) {
//            polylineManager.annotations.find { it.id == currentRouteId }?.let { existingPolyline ->
//                try {
//                    val existingPoints = existingPolyline.points
//                    if (existingPoints != null && existingPoints.isNotEmpty()) {
//                        // Use the new function that separates prepended segment from main route
//                        val trimmedResult = try {
//                            trimRoutePointsForDriverWithSegments(existingPoints, driverPoint, 50.0)
//                        } catch (e: Exception) {
//                            Log.w("DirectionsAPI", "Trimming existing polyline failed: ${e.message}")
//                            TrimmedRouteResult(emptyList(), existingPoints)
//                        }
//
//                        // Update main route polyline if it changed
//                        val mainRouteChanged = trimmedResult.mainRoute.size != existingPoints.size ||
//                                (trimmedResult.mainRoute.isNotEmpty() && trimmedResult.mainRoute[0] != existingPoints[0])
//
//                        if (mainRouteChanged && trimmedResult.mainRoute.isNotEmpty()) {
//                            existingPolyline.points = trimmedResult.mainRoute
//                            try {
//                                polylineManager.update(existingPolyline)
//                                Log.d("DirectionsAPI", "Updated main route polyline in-place. New points: ${trimmedResult.mainRoute.size}")
//                            } catch (e: Exception) {
//                                Log.w("DirectionsAPI", "Failed to update main route polyline: ${e.message}")
//                            }
//                        }
//
//                        // Update or clear dashed line (prepended segment from driver to route start)
//                        // Uses proper Mapbox style layer for actual dashed line effect
//                        if (trimmedResult.prependedSegment.size >= 2) {
//                            drawOrUpdateDashedLine(mapView, trimmedResult.prependedSegment)
//                            dashedRouteAnnotationId.value = "dashed-layer"
//                            Log.d("DirectionsAPI", "Updated dashed connector line (style layer)")
//                        } else {
//                            // No prepended segment needed, clear the dashed line
//                            clearDashedLine(mapView)
//                            dashedRouteAnnotationId.value = null
//                            Log.d("DirectionsAPI", "Cleared dashed connector (driver near route)")
//                        }
//                    }
//                } catch (e: Exception) {
//                    Log.w("DirectionsAPI", "Error while trimming existing polyline: ${e.message}")
//                }
//            }
//        }
//    }
//
//    // Then update markers (they will appear on top of the route)
//    // Update or create driver marker
//    val driverAnnotationId = driverAnnotationIdState.value
//    if (driverAnnotationId != null) {
//        pointManager.annotations.find { it.id == driverAnnotationId }?.let { annotation ->
//            annotation.point = driverPoint
//            pointManager.update(annotation)
//            Log.d("MapComponent", "Driver marker smoothly updated")
//        } ?: run {
//            // Driver marker not found, create it
//            bitmapFromDrawable(context, R.drawable._d_cars)?.let { bitmap ->
//                val driverAnnotation = PointAnnotationOptions()
//                    .withPoint(driverPoint)
//                    .withIconImage(bitmap)
//                val annotation = pointManager.create(driverAnnotation)
//                driverAnnotationIdState.value = annotation.id
//                Log.d("MapComponent", "Driver marker recreated")
//            }
//        }
//    } else {
//        // First time creating driver marker
//        bitmapFromDrawable(context, R.drawable._d_cars)?.let { bitmap ->
//            val driverAnnotation = PointAnnotationOptions()
//                .withPoint(driverPoint)
//                .withIconImage(bitmap)
//            val annotation = pointManager.create(driverAnnotation)
//            driverAnnotationIdState.value = annotation.id
//            Log.d("MapComponent", "Driver marker created")
//        }
//    }
//
//    // Handle second marker based on trip status
//    // When trip has started: show ONLY destination marker (no rider/pickup marker)
//    // When trip NOT started: show rider/pickup marker
//    val secondMarkerAnnotationId = riderAnnotationIdState.value
//
//    if (isTripStarted) {
//        // TRIP STARTED: Only show destination marker
//        Log.d("MapComponent", "Trip started - managing destination marker only")
//
//        if (secondMarkerAnnotationId != null) {
//            // Check if existing marker exists and update it, or create new one
//            val existingAnnotation = pointManager.annotations.find { it.id == secondMarkerAnnotationId }
//            if (existingAnnotation != null) {
//                existingAnnotation.point = destinationPoint
//                pointManager.update(existingAnnotation)
//                Log.d("MapComponent", "Destination marker position updated")
//            } else {
//                // Create destination marker
//                bitmapFromDrawable(context, R.drawable.destination)?.let { bitmap ->
//                    val destinationAnnotation = PointAnnotationOptions()
//                        .withPoint(destinationPoint)
//                        .withIconImage(bitmap)
//                    val annotation = pointManager.create(destinationAnnotation)
//                    riderAnnotationIdState.value = annotation.id
//                    Log.d("MapComponent", "Destination marker created (trip started)")
//                }
//            }
//        } else {
//            // Create new destination marker
//            bitmapFromDrawable(context, R.drawable.destination)?.let { bitmap ->
//                val destinationAnnotation = PointAnnotationOptions()
//                    .withPoint(destinationPoint)
//                    .withIconImage(bitmap)
//                val annotation = pointManager.create(destinationAnnotation)
//                riderAnnotationIdState.value = annotation.id
//                Log.d("MapComponent", "Destination marker created (trip started, first time)")
//            }
//        }
//    } else {
//        // TRIP NOT STARTED: Show rider/pickup marker
//        Log.d("MapComponent", "Trip not started - managing rider/pickup marker")
//
//        if (secondMarkerAnnotationId != null) {
//            val existingAnnotation = pointManager.annotations.find { it.id == secondMarkerAnnotationId }
//            if (existingAnnotation != null) {
//                existingAnnotation.point = destinationPoint
//                pointManager.update(existingAnnotation)
//                Log.d("MapComponent", "Rider/pickup marker position updated")
//            } else {
//                // Create rider marker
//                bitmapFromDrawable(context, R.drawable.ride)?.let { bitmap ->
//                    val riderAnnotation = PointAnnotationOptions()
//                        .withPoint(destinationPoint)
//                        .withIconImage(bitmap)
//                    val annotation = pointManager.create(riderAnnotation)
//                    riderAnnotationIdState.value = annotation.id
//                    Log.d("MapComponent", "Rider/pickup marker recreated")
//                }
//            }
//        } else {
//            // Create new rider marker
//            bitmapFromDrawable(context, R.drawable.ride)?.let { bitmap ->
//                val riderAnnotation = PointAnnotationOptions()
//                    .withPoint(destinationPoint)
//                    .withIconImage(bitmap)
//                val annotation = pointManager.create(riderAnnotation)
//                riderAnnotationIdState.value = annotation.id
//                Log.d("MapComponent", "Rider/pickup marker created (first time)")
//            }
//        }
//    }
//}
//
///**
// * Fetches route from Mapbox Directions API and draws it
// * Draws the prepended segment (driver to route start) as a dashed line
// * and the main route as a solid line
// */
//private fun fetchAndDrawRoute(
//    mapView: MapView,
//    origin: Point,
//    destination: Point,
//    polylineAnnotationManager: com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager,
//    onRouteInfoUpdated: (RouteState) -> Unit = {},
//    routeRequestVersion: MutableState<Int>,
//    routeAnnotationId: MutableState<String?>,
//    dashedRouteAnnotationId: MutableState<String?>,
//    routeCallState: MutableState<MapboxDirections?>
//) {
//    Log.d("DirectionsAPI", "Fetching route from ${origin.longitude()},${origin.latitude()} to ${destination.longitude()},${destination.latitude()}")
//
//    // Validate that origin and destination are different
//    val distance = origin.distanceTo(destination)
//    if (distance < 10.0) {
//        Log.w("DirectionsAPI", "Origin and destination are too close (${distance}m), skipping route fetch")
//        return
//    }
//
//    val accessToken = mapView.context.getString(R.string.mapbox_access_token)
//    Log.d("DirectionsAPI", "Using token: ${accessToken.take(10)}...")
//
//    // Cancel any previous in-flight MapboxDirections request to avoid overlapping responses and wasted bandwidth
//    routeCallState.value?.let { previousClient ->
//        try {
//            // MapboxDirections exposes cancelCall() to cancel the underlying request
//            previousClient.cancelCall()
//            Log.d("DirectionsAPI", "Cancelled previous MapboxDirections request")
//        } catch (e: Exception) {
//            Log.w("DirectionsAPI", "Failed to cancel previous directions client: ${e.message}")
//        }
//    }
//
//    // Build a new MapboxDirections client (this client enqueues an internal retrofit call)
//    val client = MapboxDirections.builder()
//        .origin(origin)
//        .destination(destination)
//        .overview(DirectionsCriteria.OVERVIEW_FULL)
//        .profile(DirectionsCriteria.PROFILE_DRIVING)
//        .accessToken(accessToken)
//        .build()
//
//    // Store the new MapboxDirections client so future requests can cancel it
//    routeCallState.value = client
//
//    // Capture the version for this request so we can ignore stale responses
//    val thisRequestVersion = routeRequestVersion.value
//
//    // Start the call and keep a reference so it can be cancelled by subsequent requests
//    client.enqueueCall(object : Callback<DirectionsResponse> {
//        override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
//            // Ignore stale responses using the versioning mechanism
//            if (thisRequestVersion != routeRequestVersion.value) {
//                Log.d("DirectionsAPI", "Ignored stale response (version mismatch). thisRequest=$thisRequestVersion current=${routeRequestVersion.value}")
//                // Clear stored client reference only if it still points to this client
//                if (routeCallState.value === client) routeCallState.value = null
//                return
//            }
//
//            if (!response.isSuccessful) {
//                Log.e("DirectionsAPI", "Request failed with code: ${response.code()}")
//                drawStraightLine(polylineAnnotationManager, origin, destination)
//                if (routeCallState.value === client) routeCallState.value = null
//                return
//            }
//
//            val routes = response.body()?.routes()
//            if (routes.isNullOrEmpty()) {
//                Log.e("DirectionsAPI", "No routes found in response")
//                drawStraightLine(polylineAnnotationManager, origin, destination)
//                if (routeCallState.value === client) routeCallState.value = null
//                return
//            }
//
//            val currentRoute = routes[0]
//            val routeGeometry = currentRoute.geometry()
//
//            if (routeGeometry == null) {
//                Log.e("DirectionsAPI", "Route geometry is null")
//                drawStraightLine(polylineAnnotationManager, origin, destination, routeAnnotationId, dashedRouteAnnotationId)
//                if (routeCallState.value === client) routeCallState.value = null
//                return
//            }
//
//            try {
//                val points = LineString.fromPolyline(routeGeometry, Constants.PRECISION_6).coordinates()
//
//                if (points.isEmpty()) {
//                    Log.e("DirectionsAPI", "Route points list is empty after parsing")
//                    drawStraightLine(polylineAnnotationManager, origin, destination, routeAnnotationId, dashedRouteAnnotationId)
//                    if (routeCallState.value === client) routeCallState.value = null
//                    return
//                }
//
//                Log.d("DirectionsAPI", "Creating polyline with ${points.size} points")
//
//                // Trim and adjust points, separating prepended segment from main route
//                val trimmedResult = try {
//                    trimRoutePointsForDriverWithSegments(points, origin, 50.0)
//                } catch (e: Exception) {
//                    Log.w("DirectionsAPI", "Failed to trim route points, falling back to full route: ${e.message}")
//                    TrimmedRouteResult(emptyList(), points)
//                }
//
//                // Track old annotation IDs to delete after creating new ones
//                val previousMainId = routeAnnotationId.value
//
//                // Draw main route (solid black line)
//                if (trimmedResult.mainRoute.isNotEmpty()) {
//                    val mainPolylineOptions = PolylineAnnotationOptions()
//                        .withPoints(trimmedResult.mainRoute)
//                        .withLineColor("#000000") // Black color
//                        .withLineWidth(5.0) // Solid line width
//
//                    val mainAnnotation = polylineAnnotationManager.create(mainPolylineOptions)
//                    routeAnnotationId.value = mainAnnotation.id
//                    Log.d("DirectionsAPI", "✅ Main route (solid) drawn - annotation ID: ${mainAnnotation.id}")
//                }
//
//                // Draw prepended segment (dashed line) - driver to route start
//                // Uses proper Mapbox style layer for actual dashed line effect
//                if (trimmedResult.prependedSegment.size >= 2) {
//                    drawOrUpdateDashedLine(mapView, trimmedResult.prependedSegment)
//                    dashedRouteAnnotationId.value = "dashed-layer" // Mark as having a dashed line
//                    Log.d("DirectionsAPI", "✅ Prepended segment (dashed line) drawn using style layer")
//                } else {
//                    // No prepended segment needed, clear the dashed line
//                    clearDashedLine(mapView)
//                    dashedRouteAnnotationId.value = null
//                }
//
//                Log.d("DirectionsAPI", "✅ Route drawn successfully - total annotations: ${polylineAnnotationManager.annotations.size}")
//
//                // Delete previous main route annotation now that new one exists
//                if (previousMainId != null && previousMainId != routeAnnotationId.value) {
//                    polylineAnnotationManager.annotations.find { it.id == previousMainId }?.let { oldAnno ->
//                        try {
//                            polylineAnnotationManager.delete(listOf(oldAnno))
//                            Log.d("DirectionsAPI", "Deleted previous main route annotation: $previousMainId")
//                        } catch (e: Exception) {
//                            Log.w("DirectionsAPI", "Failed to delete previous main route annotation: $previousMainId - ${e.message}")
//                        }
//                    }
//                }
//                // Note: Dashed line cleanup is handled by clearDashedLine() when needed
//
//                // Extract distance and duration
//                val distance = currentRoute.distance()
//                val duration = currentRoute.duration()
//                Log.d("DirectionsAPI", "Route distance: ${distance}m, duration: ${duration}s")
//
//                // Update route state with real-time data
//                val routeState = RouteState(
//                    distanceInMeters = distance,
//                    durationInSeconds = duration,
//                    lastUpdated = System.currentTimeMillis()
//                )
//                onRouteInfoUpdated(routeState)
//                Log.d("DirectionsAPI", "Route info updated: ${routeState.formattedDistance}, ${routeState.formattedDuration}")
//
//                // Clear stored client reference only if it still points to this client
//                if (routeCallState.value === client) routeCallState.value = null
//
//            } catch (e: Exception) {
//                Log.e("DirectionsAPI", "Error parsing route geometry: ${e.message}", e)
//                drawStraightLine(polylineAnnotationManager, origin, destination, routeAnnotationId, dashedRouteAnnotationId)
//                if (routeCallState.value === client) routeCallState.value = null
//            }
//        }
//
//        override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
//            // Ignore failures from stale requests
//            if (thisRequestVersion != routeRequestVersion.value) {
//                Log.d("DirectionsAPI", "Ignored stale failure (version mismatch)")
//                if (routeCallState.value === client) routeCallState.value = null
//                return
//            }
//            Log.e("DirectionsAPI", "API call failed", t)
//            drawStraightLine(polylineAnnotationManager, origin, destination, routeAnnotationId, dashedRouteAnnotationId)
//            if (routeCallState.value === client) routeCallState.value = null
//        }
//    })
//}
//
///**
// * Fallback: draws a straight line between two points
// */
//private fun drawStraightLine(
//    polylineAnnotationManager: com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager,
//    origin: Point,
//    destination: Point,
//    routeAnnotationId: MutableState<String?>? = null,
//    dashedRouteAnnotationId: MutableState<String?>? = null
//) {
//    Log.d("DirectionsAPI", "Drawing fallback straight line")
//    val polylineOptions = PolylineAnnotationOptions()
//        .withPoints(listOf(origin, destination))
//        .withLineColor("#000000")
//        .withLineWidth(5.0)
//    val annotation = polylineAnnotationManager.create(polylineOptions)
//    routeAnnotationId?.value = annotation.id
//    dashedRouteAnnotationId?.value = null // Clear dashed annotation for fallback
//}
//
///**
// * Draws or updates a dashed line from driver position to route start using Mapbox style layers.
// * This creates a proper dashed/dotted line effect similar to Google Maps.
// *
// * @param mapView The MapView instance
// * @param points The points for the dashed line (typically driver position to route start)
// */
//private fun drawOrUpdateDashedLine(mapView: MapView, points: List<Point>) {
//    if (points.size < 2) {
//        Log.d("DirectionsAPI", "Not enough points for dashed line, clearing")
//        clearDashedLine(mapView)
//        return
//    }
//
//    val lineString = LineString.fromLngLats(points)
//    val feature = Feature.fromGeometry(lineString)
//
//    mapView.mapboxMap.getStyle { style ->
//        try {
//            // Check if source already exists
//            val existingSource = style.getSourceAs<GeoJsonSource>(DASHED_LINE_SOURCE_ID)
//            if (existingSource != null) {
//                // Update existing source with new geometry
//                existingSource.feature(feature)
//                Log.d("DirectionsAPI", "Updated dashed line source with ${points.size} points")
//            } else {
//                // Create new source and layer
//                style.addSource(geoJsonSource(DASHED_LINE_SOURCE_ID) {
//                    feature(feature)
//                })
//
//                val dashedLineLayer = lineLayer(DASHED_LINE_LAYER_ID, DASHED_LINE_SOURCE_ID) {
//                    lineColor("#808080") // Gray color
//                    lineWidth(4.0)
//                    lineDasharray(listOf(2.0, 2.0)) // Dash pattern: 2 units dash, 2 units gap
//                    lineCap(com.mapbox.maps.extension.style.layers.properties.generated.LineCap.ROUND)
//                    lineJoin(com.mapbox.maps.extension.style.layers.properties.generated.LineJoin.ROUND)
//                }
//
//                // Try to add layer below annotation layers so car marker appears on top
//                // Point annotation managers typically create layers starting with "mapbox-android-pointAnnotation"
//                val annotationLayerId = style.styleLayers.firstOrNull {
//                    it.id.contains("pointAnnotation", ignoreCase = true) ||
//                            it.id.contains("PointAnnotation", ignoreCase = true)
//                }?.id
//
//                if (annotationLayerId != null) {
//                    style.addLayerBelow(dashedLineLayer, annotationLayerId)
//                    Log.d("DirectionsAPI", "Created dashed line layer BELOW annotation layer: $annotationLayerId")
//                } else {
//                    // Fallback: just add layer normally
//                    style.addLayer(dashedLineLayer)
//                    Log.d("DirectionsAPI", "Created new dashed line layer (no annotation layer found)")
//                }
//                Log.d("DirectionsAPI", "Created new dashed line layer with ${points.size} points")
//            }
//        } catch (e: Exception) {
//            Log.e("DirectionsAPI", "Error drawing dashed line: ${e.message}", e)
//        }
//    }
//}
//
///**
// * Clears the dashed line from the map (removes source and layer)
// * This function tries both async and sync approaches to ensure cleanup
// */
//private fun clearDashedLine(mapView: MapView) {
//    // Try synchronous approach first using style property directly
//    try {
//        val style = mapView.mapboxMap.style
//        if (style != null) {
//            if (style.styleLayerExists(DASHED_LINE_LAYER_ID)) {
//                style.removeStyleLayer(DASHED_LINE_LAYER_ID)
//                Log.d("DirectionsAPI", "Removed dashed line layer (sync)")
//            }
//            if (style.styleSourceExists(DASHED_LINE_SOURCE_ID)) {
//                style.removeStyleSource(DASHED_LINE_SOURCE_ID)
//                Log.d("DirectionsAPI", "Removed dashed line source (sync)")
//            }
//            return // Success, no need for async approach
//        }
//    } catch (e: Exception) {
//        Log.w("DirectionsAPI", "Sync dashed line cleanup failed, trying async: ${e.message}")
//    }
//
//    // Fallback to async approach
//    mapView.mapboxMap.getStyle { style ->
//        try {
//            // Remove layer first, then source
//            if (style.styleLayerExists(DASHED_LINE_LAYER_ID)) {
//                style.removeStyleLayer(DASHED_LINE_LAYER_ID)
//                Log.d("DirectionsAPI", "Removed dashed line layer (async)")
//            }
//            if (style.styleSourceExists(DASHED_LINE_SOURCE_ID)) {
//                style.removeStyleSource(DASHED_LINE_SOURCE_ID)
//                Log.d("DirectionsAPI", "Removed dashed line source")
//            }
//        } catch (e: Exception) {
//            Log.w("DirectionsAPI", "Error clearing dashed line: ${e.message}")
//        }
//    }
//}
//
///**
// * Adjusts camera to show both driver and rider markers with padding
// */
//private fun adjustCameraToShowRoute(mapView: MapView, driverPoint: Point, riderPoint: Point) {
//    // Calculate bounds to fit both points
//    val minLng = minOf(driverPoint.longitude(), riderPoint.longitude())
//    val maxLng = maxOf(driverPoint.longitude(), riderPoint.longitude())
//    val minLat = minOf(driverPoint.latitude(), riderPoint.latitude())
//    val maxLat = maxOf(driverPoint.latitude(), riderPoint.latitude())
//
//    // Add padding (10% on each side)
//    val lngPadding = (maxLng - minLng) * 0.1
//    val latPadding = (maxLat - minLat) * 0.1
//
//    // Calculate center point
//    val centerLng = (minLng + maxLng) / 2
//    val centerLat = (minLat + maxLat) / 2
//    val centerPoint = Point.fromLngLat(centerLng, centerLat)
//
//    // Calculate appropriate zoom level (rough estimation)
//    val lngDiff = maxLng - minLng + (lngPadding * 2)
//    val latDiff = maxLat - minLat + (latPadding * 2)
//    val maxDiff = maxOf(lngDiff, latDiff)
//
//    // Zoom calculation: smaller diff = higher zoom
//    val zoom = when {
//        maxDiff > 0.1 -> 11.0
//        maxDiff > 0.05 -> 12.0
//        maxDiff > 0.02 -> 13.0
//        maxDiff > 0.01 -> 14.0
//        maxDiff > 0.005 -> 15.0
//        else -> 16.0
//    }
//
//    Log.d("MapComponent", "Adjusting camera - Center: $centerLng, $centerLat, Zoom: $zoom")
//
//    val cameraOptions = CameraOptions.Builder()
//        .center(centerPoint)
//        .zoom(zoom)
//        .build()
//
//    mapView.mapboxMap.flyTo(
//        cameraOptions = cameraOptions,
//        animationOptions = MapAnimationOptions.Builder().duration(1500L).build()
//    )
//}
//
//
//
//private fun bitmapFromDrawable(context: Context, @DrawableRes resId: Int): Bitmap? {
//    return AppCompatResources.getDrawable(context, resId)?.let { drawable ->
//        val bitmap = Bitmap.createBitmap(
//            drawable.intrinsicWidth,
//            drawable.intrinsicHeight,
//            Bitmap.Config.ARGB_8888
//        )
//        val canvas = Canvas(bitmap)
//        drawable.setBounds(0, 0, canvas.width, canvas.height)
//        drawable.draw(canvas)
//        bitmap
//    }
//}
//
///**
// * Trim route points so the visible polyline starts near the driver's current position.
// * Uses nearest-segment projection for better accuracy. If the driver is already close to the
// * first point, ensures the driver is included as the leading point. Otherwise, finds the
// * nearest projection on the polyline segments, drops preceding points, and prepends the
// * driver's position so the polyline visually starts at the driver.
// *
// * Returns a TrimmedRouteResult that separates the prepended segment (driver to route start)
// * from the main route, allowing for different line styles (dashed vs solid).
// */
//private fun trimRoutePointsForDriverWithSegments(points: List<Point>, driver: Point, maxDistanceToFirstPointMeters: Double = 50.0): TrimmedRouteResult {
//    if (points.isEmpty()) return TrimmedRouteResult(emptyList(), emptyList())
//
//    // If driver is already close to the first point, just ensure driver is included as leading point
//    val first = points[0]
//    val distToFirst = driver.distanceTo(first)
//    if (distToFirst <= maxDistanceToFirstPointMeters) {
//        // Driver is close to route start - prepended segment from driver to first point (dashed)
//        // Main route is the original points (solid)
//        val prependedSegment = if (driver.distanceTo(first) > 1.0) {
//            listOf(driver, first)
//        } else {
//            emptyList()
//        }
//        return TrimmedRouteResult(prependedSegment, points)
//    }
//
//    // Find nearest projection on any segment
//    var bestIdx = 0
//    var bestDist = Double.MAX_VALUE
//
//    for (i in 0 until points.size - 1) {
//        val a = points[i]
//        val b = points[i + 1]
//        val (proj, _) = projectPointToSegment(driver, a, b)
//        val d = driver.distanceTo(proj)
//        if (d < bestDist) {
//            bestDist = d
//            bestIdx = i
//        }
//    }
//
//    // Compute trimmed list starting from the projection point
//    val mainRoute = mutableListOf<Point>()
//
//    // Build projected starting point between points[bestIdx] and points[bestIdx+1]
//    val a = points[bestIdx]
//    val b = points.getOrNull(bestIdx + 1) ?: a
//    val (projPoint, _) = projectPointToSegment(driver, a, b)
//
//    // Add projection point as the start of main route
//    mainRoute.add(projPoint)
//
//    // Add remaining points after the projection
//    for (j in bestIdx + 1 until points.size) {
//        mainRoute.add(points[j])
//    }
//
//    // Create prepended segment from driver to projection point (for dashed line)
//    val prependedSegment = if (mainRoute.isNotEmpty() && driver.distanceTo(mainRoute[0]) > 1.0) {
//        listOf(driver, mainRoute[0])
//    } else {
//        emptyList()
//    }
//
//    return TrimmedRouteResult(prependedSegment, mainRoute)
//}
//
///**
// * Projects point p onto segment ab and returns the projected Point and the t parameter (0..1)
// * using a simple equirectangular projection approximation (sufficient for short distances).
// */
//private fun projectPointToSegment(p: Point, a: Point, b: Point): Pair<Point, Double> {
//    // Convert to simple Cartesian coordinates in degrees, but scale longitude by cos(meanLat)
//    val meanLat = Math.toRadians((a.latitude() + b.latitude()) / 2.0)
//    val scale = Math.cos(meanLat)
//
//    val ax = a.longitude() * scale
//    val ay = a.latitude()
//    val bx = b.longitude() * scale
//    val by = b.latitude()
//    val px = p.longitude() * scale
//    val py = p.latitude()
//
//    val abx = bx - ax
//    val aby = by - ay
//    val apx = px - ax
//    val apy = py - ay
//
//    val abLen2 = abx * abx + aby * aby
//    if (abLen2 == 0.0) {
//        return Pair(a, 0.0)
//    }
//
//    var t = (apx * abx + apy * aby) / abLen2
//    if (t < 0.0) t = 0.0
//    if (t > 1.0) t = 1.0
//
//    val projX = ax + t * abx
//    val projY = ay + t * aby
//
//    // Convert back to lon/lat degrees
//    val projLon = projX / scale
//    val projLat = projY
//
//    val projPoint = Point.fromLngLat(projLon, projLat)
//    return Pair(projPoint, t)
//}
