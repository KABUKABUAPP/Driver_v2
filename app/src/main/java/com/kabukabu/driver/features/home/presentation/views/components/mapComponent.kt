package com.kabukabu.driver.features.home.presentation.views.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.kabukabu.driver.R
import com.kabukabu.driver.features.home.presentation.viewmodel.TripStatus
import com.kabukabu.driver.features.profile.data.ActiveTrip
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.core.constants.Constants
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.scalebar.scalebar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.coroutines.delay

/**
 * Holds real-time route information from Mapbox Directions API
 */
data class RouteState(
    val distanceInMeters: Double? = null,
    val durationInSeconds: Double? = null,
    val lastUpdated: Long = 0L
) {
    val distanceInKm: Double?
        get() = distanceInMeters?.let { it / 1000 }

    val durationInMinutes: Int?
        get() = durationInSeconds?.let { (it / 60).toInt() }

    val formattedDuration: String
        get() = durationInMinutes?.let { "Est. $it mins" } ?: "Calculating..."

    val formattedDistance: String
        get() = distanceInKm?.let { "%.1f km".format(it) } ?: "Calculating..."
}

@Composable
 fun MapComponent(
    currentLocation: Location?,
    activeTrip: ActiveTrip? = null,
    pendingTripEvent: com.kabukabu.driver.core.data.socket.TripFoundEvent? = null,
    tripStatus: TripStatus? = null,
    onRouteInfoUpdated: (RouteState) -> Unit = {},
    mapViewState: MutableState<MapView?>? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    // Expose mapView to parent if requested
    LaunchedEffect(mapView) {
        mapViewState?.value = mapView
    }

    // Track if style is loaded
    val isStyleLoaded = remember { mutableStateOf(false) }

    // Track if initial camera has been set
    val initialCameraSet = remember { mutableStateOf(false) }

    // Track the active trip ID to detect when it changes
    val previousTripId = remember { mutableStateOf<String?>(null) }

    // Remember annotation managers to update them without recreating
    val pointAnnotationManager = remember { mutableStateOf<com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager?>(null) }
    val polylineAnnotationManager = remember { mutableStateOf<com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager?>(null) }

    // Track individual annotation instances for smooth updates
    val driverAnnotationId = remember { mutableStateOf<String?>(null) }
    val riderAnnotationId = remember { mutableStateOf<String?>(null) }
    val routeAnnotationId = remember { mutableStateOf<String?>(null) }

    // Track last route calculation position for distance-based updates
    val lastRouteCalcPosition = remember { mutableStateOf<Location?>(null) }
    val lastRouteCalcTime = remember { mutableStateOf<Long>(0L) }

    // Track last marker type to detect when we need to change the icon
    val lastMarkerWasDestination = remember { mutableStateOf(false) }

    // Minimum distance (in meters) before redrawing route to avoid flickering
    val minDistanceForRouteRedraw = 50f

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )

    // Periodic route updates for active trips (hybrid: time-based OR distance-based)
    // Use activeTrip if available, otherwise use pendingTripEvent
    LaunchedEffect(activeTrip?.id, pendingTripEvent?.eventId, tripStatus) {
        // Get trip data from either source
        val tripId = activeTrip?.id ?: pendingTripEvent?.eventId
        val startPoint = activeTrip?.startPoint ?: pendingTripEvent?.startPoint

        if (tripId != null && startPoint != null && startPoint.size >= 2) {
            while (true) {
                delay(60000) // Check every 60 seconds

                val lastCalc = lastRouteCalcPosition.value
                val timeSinceLastCalc = System.currentTimeMillis() - lastRouteCalcTime.value

                // Update if 60 seconds passed OR driver moved 500m
                val shouldUpdate =
                    timeSinceLastCalc > 60000 ||
                    (lastCalc != null && currentLocation != null && currentLocation.distanceTo(lastCalc) > 500)

                if (shouldUpdate && currentLocation != null) {
                    Log.d("MapComponent", "Periodic route update triggered")

                    // Determine route based on trip status
                    val isTripStarted = tripStatus == com.kabukabu.driver.features.home.presentation.viewmodel.TripStatus.TRIP_STARTED
                    val routeOrigin: Point
                    val routeDestination: Point

                    val endPoint = activeTrip?.endPoint

                    if (isTripStarted && endPoint != null && endPoint.size >= 2) {
                        // TRIP STARTED: Show driver's current location to destination
                        routeOrigin = Point.fromLngLat(currentLocation.longitude, currentLocation.latitude)
                        routeDestination = Point.fromLngLat(endPoint[0], endPoint[1])
                    } else {
                        // TRIP NOT STARTED: Show driver's current location to pickup
                        routeOrigin = Point.fromLngLat(currentLocation.longitude, currentLocation.latitude)
                        routeDestination = Point.fromLngLat(startPoint[0], startPoint[1])
                    }

                    polylineAnnotationManager.value?.let { polyMgr ->
                        fetchAndDrawRoute(
                            mapView,
                            routeOrigin,
                            routeDestination,
                            polyMgr,
                            onRouteInfoUpdated
                        )
                        lastRouteCalcPosition.value = currentLocation
                        lastRouteCalcTime.value = System.currentTimeMillis()
                    }
                }
            }
        }
    }

    // Initial map setup - Load style FIRST
    LaunchedEffect(mapView) {
        Log.d("MapComponent", "Loading map style...")

        // Use the modern Mapbox API for loading styles
        mapView.mapboxMap.loadStyle("mapbox://styles/kabukabuapp/cmd1e8u2s009k01s913jc4ywh") { style ->
            // This callback fires when style is fully loaded
            isStyleLoaded.value = true
            Log.d("MapComponent", "✅ Map style loaded successfully")
        }

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

    // Handle trip changes (when trip starts/ends) - This SHOULD reset camera
    LaunchedEffect(activeTrip?.id, isStyleLoaded.value) {
        if (!isStyleLoaded.value) {
            Log.d("MapComponent", "Waiting for style to load...")
            return@LaunchedEffect
        }

        // Use activeTrip ID if available, otherwise use pendingTripEvent ID
        val currentTripId = activeTrip?.id ?: pendingTripEvent?.eventId
        val tripChanged = currentTripId != previousTripId.value

        if (tripChanged) {
            Log.d("MapComponent", "Trip status changed: ${previousTripId.value} -> $currentTripId")
            previousTripId.value = currentTripId
            initialCameraSet.value = false // Reset camera flag when trip changes

            // Clean up old annotations when trip changes
            mapView.annotations.cleanup()
            pointAnnotationManager.value = null
            polylineAnnotationManager.value = null

            // Clear annotation IDs to force fresh markers
            driverAnnotationId.value = null
            riderAnnotationId.value = null
            routeAnnotationId.value = null

            // Reset marker type tracking
            lastMarkerWasDestination.value = false
        }
    }

    // Track previous trip status to detect changes
    val lastTripStatus = remember { mutableStateOf<com.kabukabu.driver.features.home.presentation.viewmodel.TripStatus?>(null) }

    // Update markers when location changes - WITHOUT resetting camera (unless initial)
    // Include pendingTripEvent in dependencies to react immediately when trip is accepted
    LaunchedEffect(currentLocation, activeTrip?.id, pendingTripEvent?.eventId, tripStatus, isStyleLoaded.value) {
        // Wait for style to be loaded before adding any annotations
        if (!isStyleLoaded.value || currentLocation == null) {
            Log.d("MapComponent", "Waiting for style to load or location... Style: ${isStyleLoaded.value}, Location: $currentLocation")
            return@LaunchedEffect
        }

        val driverPoint = Point.fromLngLat(currentLocation.longitude, currentLocation.latitude)
        val shouldSetCamera = !initialCameraSet.value

        // Get trip data from either activeTrip or pendingTripEvent
        val startPoint = activeTrip?.startPoint ?: pendingTripEvent?.startPoint
        val endPoint = activeTrip?.endPoint

        // Handle active trip with route (use pendingTripEvent as fallback)
        if (startPoint != null && startPoint.size >= 2) {
            // Determine which route to show based on trip status
            val isTripStarted = tripStatus == TripStatus.TRIP_STARTED

            val routeOrigin: Point
            val routeDestination: Point
            val riderMarkerPoint: Point // Position for the rider/destination marker

            if (isTripStarted && endPoint != null && endPoint.size >= 2) {
                // Trip has started: Show route from pickup (startPoint) to destination (endPoint)
                routeOrigin = Point.fromLngLat(startPoint[0], startPoint[1])
                routeDestination = Point.fromLngLat(endPoint[0], endPoint[1])
                riderMarkerPoint = routeDestination // Marker shows destination
                Log.d("MapComponent", "Trip started - showing route from pickup to destination")
            } else {
                // Trip not started yet: Show route from driver to pickup (startPoint)
                routeOrigin = driverPoint
                routeDestination = Point.fromLngLat(startPoint[0], startPoint[1])
                riderMarkerPoint = routeDestination // Marker shows pickup
                Log.d("MapComponent", "Trip not started - showing route from driver to pickup (using ${if (activeTrip != null) "activeTrip" else "pendingTripEvent"})")
            }

            // Check if trip status changed (force redraw if status changed)
            val tripStatusChanged = lastTripStatus.value != tripStatus
            if (tripStatusChanged) {
                Log.d("MapComponent", "Trip status changed from ${lastTripStatus.value} to $tripStatus - forcing route redraw")
                lastTripStatus.value = tripStatus
            }

            // Check if marker icon type needs to change (pickup icon -> destination icon)
            val markerIconNeedsUpdate = lastMarkerWasDestination.value != isTripStarted
            if (markerIconNeedsUpdate) {
                Log.d("MapComponent", "Marker icon type changed (was destination: ${lastMarkerWasDestination.value}, now: $isTripStarted) - forcing marker recreation")
                // Clear rider marker ID to force recreation with new icon
                riderAnnotationId.value = null
                lastMarkerWasDestination.value = isTripStarted
            }

            // Check if we need to redraw the route (if driver moved significantly OR trip status changed)
            val lastCalcPosition = lastRouteCalcPosition.value
            val shouldRedrawRoute = lastCalcPosition == null ||
                currentLocation.distanceTo(lastCalcPosition) > minDistanceForRouteRedraw ||
                tripStatusChanged // ✅ Force redraw when status changes

            Log.d("MapComponent", "Active Trip - Updating markers (camera reset: $shouldSetCamera, redraw route: $shouldRedrawRoute, status changed: $tripStatusChanged, marker icon changed: $markerIconNeedsUpdate)")

            // Update route and markers with smart redrawing
            updateRouteWithMarkers(
                mapView,
                routeOrigin,
                routeDestination,
                driverPoint,
                riderMarkerPoint,
                context,
                pointAnnotationManager,
                polylineAnnotationManager,
                driverAnnotationId,
                riderAnnotationId,
                shouldRedrawRoute,
                onRouteInfoUpdated,
                isTripStarted
            )

            // Track position for distance-based updates (only if we redrew the route)
            if (shouldRedrawRoute) {
                lastRouteCalcPosition.value = currentLocation
                lastRouteCalcTime.value = System.currentTimeMillis()
            }

            // Adjust camera on initial load, trip change, or trip status change
            if (shouldSetCamera || tripStatusChanged) {
                adjustCameraToShowRoute(mapView, routeOrigin, routeDestination)
                initialCameraSet.value = true
                Log.d("MapComponent", "Camera adjusted for active trip (initial: $shouldSetCamera, status changed: $tripStatusChanged)")
            } else {
                Log.d("MapComponent", "Markers updated, preserving user's camera position")
            }
        } else {
            // No active trip - just show driver marker
            Log.d("MapComponent", "No active trip - updating driver marker (camera reset: $shouldSetCamera)")

            // Update driver marker smoothly
            updateDriverMarker(
                mapView,
                driverPoint,
                context,
                pointAnnotationManager,
                driverAnnotationId
            )

            // Only center camera on initial load
            if (shouldSetCamera) {
                val cameraOptions = cameraOptions {
                    center(driverPoint)
                    zoom(17.0)
                }
                mapView.mapboxMap.setCamera(cameraOptions)
                initialCameraSet.value = true
                Log.d("MapComponent", "Initial camera set for driver")
            } else {
                Log.d("MapComponent", "Driver marker updated, preserving user's camera position")
            }
        }
    }
}

/**
 * Updates driver marker only - smoothly updates position without recreating
 */
private fun updateDriverMarker(
    mapView: MapView,
    driverPoint: Point,
    context: Context,
    managerState: MutableState<com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager?>,
    annotationIdState: MutableState<String?>
) {
    // Get or create annotation manager
    val manager = managerState.value ?: run {
        val newManager = mapView.annotations.createPointAnnotationManager()
        managerState.value = newManager
        newManager
    }

    val existingId = annotationIdState.value

    if (existingId != null) {
        // Update existing marker position smoothly
        manager.annotations.find { it.id == existingId }?.let { annotation ->
            annotation.point = driverPoint
            manager.update(annotation)
            Log.d("MapComponent", "Driver marker position smoothly updated")
        } ?: run {
            // Annotation was deleted, create new one
            createDriverMarker(manager, driverPoint, context, annotationIdState)
        }
    } else {
        // First time - create marker
        createDriverMarker(manager, driverPoint, context, annotationIdState)
    }
}

private fun createDriverMarker(
    manager: com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager,
    driverPoint: Point,
    context: Context,
    annotationIdState: MutableState<String?>
) {
    bitmapFromDrawable(context, R.drawable._d_cars)?.let { bitmap ->
        val driverAnnotation = PointAnnotationOptions()
            .withPoint(driverPoint)
            .withIconImage(bitmap)
        val annotation = manager.create(driverAnnotation)
        annotationIdState.value = annotation.id
        Log.d("MapComponent", "Driver marker created with ID: ${annotation.id}")
    }
}

/**
 * Updates the route line and both driver/rider markers - smoothly updates without flickering
 * @param routeOrigin The starting point for the route line
 * @param routeDestination The ending point for the route line
 * @param driverPoint The current driver location for the driver marker
 * @param riderPoint The destination marker position (pickup before trip starts, destination after)
 * @param isTripStarted Whether the trip has started (affects marker display)
 */
private fun updateRouteWithMarkers(
    mapView: MapView,
    routeOrigin: Point,
    routeDestination: Point,
    driverPoint: Point,
    riderPoint: Point,
    context: Context,
    pointManagerState: MutableState<com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager?>,
    polylineManagerState: MutableState<com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager?>,
    driverAnnotationIdState: MutableState<String?>,
    riderAnnotationIdState: MutableState<String?>,
    shouldRedrawRoute: Boolean,
    onRouteInfoUpdated: (RouteState) -> Unit = {},
    isTripStarted: Boolean = false
) {
    Log.d("MapComponent", "Updating route (redraw route: $shouldRedrawRoute, trip started: $isTripStarted)")

    // IMPORTANT: Create polyline manager FIRST, then point manager
    // This ensures polyline is drawn under the markers
    val polylineManager = polylineManagerState.value ?: run {
        val newPolylineManager = mapView.annotations.createPolylineAnnotationManager()
        polylineManagerState.value = newPolylineManager
        newPolylineManager
    }

    val pointManager = pointManagerState.value ?: run {
        val newPointManager = mapView.annotations.createPointAnnotationManager()
        pointManagerState.value = newPointManager
        newPointManager
    }

    // Draw route FIRST (if needed) so it appears under markers
    if (shouldRedrawRoute) {
        Log.d("MapComponent", "Redrawing route - clearing ${polylineManager.annotations.size} existing routes")
        polylineManager.deleteAll()
        // Fetch and draw the new route (asynchronous operation)
        fetchAndDrawRoute(mapView, routeOrigin, routeDestination, polylineManager, onRouteInfoUpdated)
    } else {
        Log.d("MapComponent", "Keeping existing route (${polylineManager.annotations.size} routes)")
    }

    // Then update markers (they will appear on top of the route)
    val driverAnnotationId = driverAnnotationIdState.value
    if (driverAnnotationId != null) {
        pointManager.annotations.find { it.id == driverAnnotationId }?.let { annotation ->
            annotation.point = driverPoint
            pointManager.update(annotation)
            Log.d("MapComponent", "Driver marker smoothly updated")
        } ?: createMarkers(pointManager, driverPoint, riderPoint, context, driverAnnotationIdState, riderAnnotationIdState, isTripStarted)
    } else {
        createMarkers(pointManager, driverPoint, riderPoint, context, driverAnnotationIdState, riderAnnotationIdState, isTripStarted)
    }

    // Smoothly update rider/pickup/destination marker
    val riderAnnotationId = riderAnnotationIdState.value
    if (riderAnnotationId != null) {
        pointManager.annotations.find { it.id == riderAnnotationId }?.let { annotation ->
            annotation.point = riderPoint
            pointManager.update(annotation)
            Log.d("MapComponent", "Rider/destination marker position smoothly updated")
        } ?: run {
            // Marker was deleted or doesn't exist, recreate it with correct icon
            Log.d("MapComponent", "Rider marker not found, recreating with correct icon")
            val riderIconRes = if (isTripStarted) R.drawable.destination else R.drawable.ride
            bitmapFromDrawable(context, riderIconRes)?.let { bitmap ->
                val riderAnnotation = PointAnnotationOptions()
                    .withPoint(riderPoint)
                    .withIconImage(bitmap)
                val annotation = pointManager.create(riderAnnotation)
                riderAnnotationIdState.value = annotation.id
                Log.d("MapComponent", if (isTripStarted) "Destination marker recreated" else "Pickup (rider) marker recreated")
            }
        }
    } else {
        // First time creating rider marker
        val riderIconRes = if (isTripStarted) R.drawable.destination else R.drawable.ride
        bitmapFromDrawable(context, riderIconRes)?.let { bitmap ->
            val riderAnnotation = PointAnnotationOptions()
                .withPoint(riderPoint)
                .withIconImage(bitmap)
            val annotation = pointManager.create(riderAnnotation)
            riderAnnotationIdState.value = annotation.id
            Log.d("MapComponent", if (isTripStarted) "Destination marker created" else "Pickup (rider) marker created")
        }
    }
}

private fun createMarkers(
    manager: com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager,
    driverPoint: Point,
    riderPoint: Point,
    context: Context,
    driverAnnotationIdState: MutableState<String?>,
    riderAnnotationIdState: MutableState<String?>,
    isTripStarted: Boolean
) {
    // Create driver marker (unchanged)
    bitmapFromDrawable(context, R.drawable._d_cars)?.let { bitmap ->
        val driverAnnotation = PointAnnotationOptions()
            .withPoint(driverPoint)
            .withIconImage(bitmap)
        val annotation = manager.create(driverAnnotation)
        driverAnnotationIdState.value = annotation.id
        Log.d("MapComponent", "Driver marker created")
    }

    // Choose icon based on trip phase:
    // - Before trip starts: rider/pickup marker uses existing pickup icon (ride)
    // - After trip starts: destination marker uses new destination.png
    val riderIconRes = if (isTripStarted) R.drawable.destination else R.drawable.ride

    bitmapFromDrawable(context, riderIconRes)?.let { bitmap ->
        val riderAnnotation = PointAnnotationOptions()
            .withPoint(riderPoint)
            .withIconImage(bitmap)
        val annotation = manager.create(riderAnnotation)
        riderAnnotationIdState.value = annotation.id
        Log.d(
            "MapComponent",
            if (isTripStarted) "Destination marker created" else "Pickup (rider) marker created"
        )
    }
}

/**
 * Fetches route from Mapbox Directions API and draws it
 */
private fun fetchAndDrawRoute(
    mapView: MapView,
    origin: Point,
    destination: Point,
    polylineAnnotationManager: com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager,
    onRouteInfoUpdated: (RouteState) -> Unit = {}
) {
    Log.d("DirectionsAPI", "Fetching route from ${origin.longitude()},${origin.latitude()} to ${destination.longitude()},${destination.latitude()}")

    // Validate that origin and destination are different
    val distance = origin.distanceTo(destination)
    if (distance < 10.0) {
        Log.w("DirectionsAPI", "Origin and destination are too close (${distance}m), skipping route fetch")
        return
    }

    val accessToken = mapView.context.getString(R.string.mapbox_access_token)
    Log.d("DirectionsAPI", "Using token: ${accessToken.take(10)}...")

    val client = MapboxDirections.builder()
        .origin(origin)
        .destination(destination)
        .overview(DirectionsCriteria.OVERVIEW_FULL)
        .profile(DirectionsCriteria.PROFILE_DRIVING)
        .accessToken(accessToken)
        .build()

    client.enqueueCall(object : Callback<DirectionsResponse> {
        override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
            if (!response.isSuccessful) {
                Log.e("DirectionsAPI", "Request failed with code: ${response.code()}")
                drawStraightLine(polylineAnnotationManager, origin, destination)
                return
            }

            val routes = response.body()?.routes()
            if (routes.isNullOrEmpty()) {
                Log.e("DirectionsAPI", "No routes found in response")
                drawStraightLine(polylineAnnotationManager, origin, destination)
                return
            }

            val currentRoute = routes[0]
            val routeGeometry = currentRoute.geometry()

            if (routeGeometry == null) {
                Log.e("DirectionsAPI", "Route geometry is null")
                drawStraightLine(polylineAnnotationManager, origin, destination)
                return
            }

            try {
                val points = LineString.fromPolyline(routeGeometry, Constants.PRECISION_6).coordinates()

                if (points.isEmpty()) {
                    Log.e("DirectionsAPI", "Route points list is empty after parsing")
                    drawStraightLine(polylineAnnotationManager, origin, destination)
                    return
                }

                Log.d("DirectionsAPI", "Creating polyline with ${points.size} points")
                val polylineOptions = PolylineAnnotationOptions()
                    .withPoints(points)
                    .withLineColor("#000000") // Black color
                    .withLineWidth(5.0) // Increased width for visibility

                val createdAnnotation = polylineAnnotationManager.create(polylineOptions)
                Log.d("DirectionsAPI", "✅ Route drawn successfully - annotation ID: ${createdAnnotation.id}, total annotations: ${polylineAnnotationManager.annotations.size}")

                // Extract distance and duration
                val distance = currentRoute.distance()
                val duration = currentRoute.duration()
                Log.d("DirectionsAPI", "Route distance: ${distance}m, duration: ${duration}s")

                // Update route state with real-time data
                val routeState = RouteState(
                    distanceInMeters = distance,
                    durationInSeconds = duration,
                    lastUpdated = System.currentTimeMillis()
                )
                onRouteInfoUpdated(routeState)
                Log.d("DirectionsAPI", "Route info updated: ${routeState.formattedDistance}, ${routeState.formattedDuration}")

            } catch (e: Exception) {
                Log.e("DirectionsAPI", "Error parsing route geometry: ${e.message}", e)
                drawStraightLine(polylineAnnotationManager, origin, destination)
            }
        }

        override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
            Log.e("DirectionsAPI", "API call failed", t)
            drawStraightLine(polylineAnnotationManager, origin, destination)
        }
    })
}

/**
 * Fallback: draws a straight line between two points
 */
private fun drawStraightLine(
    polylineAnnotationManager: com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager,
    origin: Point,
    destination: Point
) {
    Log.d("DirectionsAPI", "Drawing fallback straight line")
    val polylineOptions = PolylineAnnotationOptions()
        .withPoints(listOf(origin, destination))
        .withLineColor("#000000")
        .withLineWidth(5.0)
    polylineAnnotationManager.create(polylineOptions)
}

/**
 * Adjusts camera to show both driver and rider markers with padding
 */
private fun adjustCameraToShowRoute(mapView: MapView, driverPoint: Point, riderPoint: Point) {
    // Calculate bounds to fit both points
    val minLng = minOf(driverPoint.longitude(), riderPoint.longitude())
    val maxLng = maxOf(driverPoint.longitude(), riderPoint.longitude())
    val minLat = minOf(driverPoint.latitude(), riderPoint.latitude())
    val maxLat = maxOf(driverPoint.latitude(), riderPoint.latitude())

    // Add padding (10% on each side)
    val lngPadding = (maxLng - minLng) * 0.1
    val latPadding = (maxLat - minLat) * 0.1

    // Calculate center point
    val centerLng = (minLng + maxLng) / 2
    val centerLat = (minLat + maxLat) / 2
    val centerPoint = Point.fromLngLat(centerLng, centerLat)

    // Calculate appropriate zoom level (rough estimation)
    val lngDiff = maxLng - minLng + (lngPadding * 2)
    val latDiff = maxLat - minLat + (latPadding * 2)
    val maxDiff = maxOf(lngDiff, latDiff)

    // Zoom calculation: smaller diff = higher zoom
    val zoom = when {
        maxDiff > 0.1 -> 11.0
        maxDiff > 0.05 -> 12.0
        maxDiff > 0.02 -> 13.0
        maxDiff > 0.01 -> 14.0
        maxDiff > 0.005 -> 15.0
        else -> 16.0
    }

    Log.d("MapComponent", "Adjusting camera - Center: $centerLng, $centerLat, Zoom: $zoom")

    val cameraOptions = CameraOptions.Builder()
        .center(centerPoint)
        .zoom(zoom)
        .build()

    mapView.mapboxMap.flyTo(
        cameraOptions = cameraOptions,
        animationOptions = MapAnimationOptions.Builder().duration(1500L).build()
    )
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

/**
 * Calculate distance between two Points in meters
 */
private fun Point.distanceTo(other: Point): Double {
    val results = FloatArray(1)
    android.location.Location.distanceBetween(
        this.latitude(),
        this.longitude(),
        other.latitude(),
        other.longitude(),
        results
    )
    return results[0].toDouble()
}
