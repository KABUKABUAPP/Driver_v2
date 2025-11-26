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

    // Minimum distance (in meters) before redrawing route to avoid flickering
    val minDistanceForRouteRedraw = 50f

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )

    // Periodic route updates for active trips (hybrid: time-based OR distance-based)
    LaunchedEffect(activeTrip?.id) {
        if (activeTrip?.id != null && activeTrip.startPoint.size >= 2) {
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
                    val driverPoint = Point.fromLngLat(currentLocation.longitude, currentLocation.latitude)
                    val riderPoint = Point.fromLngLat(activeTrip.startPoint[0], activeTrip.startPoint[1])

                    polylineAnnotationManager.value?.let { polyMgr ->
                        fetchAndDrawRoute(
                            mapView,
                            driverPoint,
                            riderPoint,
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
        mapView.getMapboxMap().loadStyleUri(
            "mapbox://styles/kabukabuapp/cmd1e8u2s009k01s913jc4ywh"
        ) {
            // This callback fires when style is fully loaded
            isStyleLoaded.value = true
            Log.d("MapComponent", "Map style loaded successfully")
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

        val currentTripId = activeTrip?.id
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
        }
    }

    // Update markers when location changes - WITHOUT resetting camera (unless initial)
    LaunchedEffect(currentLocation, activeTrip?.id, isStyleLoaded.value) {
        // Wait for style to be loaded before adding any annotations
        if (!isStyleLoaded.value || currentLocation == null) {
            Log.d("MapComponent", "Waiting for style to load or location... Style: ${isStyleLoaded.value}, Location: $currentLocation")
            return@LaunchedEffect
        }

        val driverPoint = Point.fromLngLat(currentLocation.longitude, currentLocation.latitude)
        val shouldSetCamera = !initialCameraSet.value

        // Handle active trip with route
        if (activeTrip?.startPoint != null && activeTrip.startPoint.size >= 2) {
            // startPoint is [longitude, latitude]
            val riderPoint = Point.fromLngLat(activeTrip.startPoint[0], activeTrip.startPoint[1])

            // Check if we need to redraw the route (only if driver moved significantly)
            val lastCalcPosition = lastRouteCalcPosition.value
            val shouldRedrawRoute = lastCalcPosition == null ||
                currentLocation.distanceTo(lastCalcPosition) > minDistanceForRouteRedraw

            Log.d("MapComponent", "Active Trip - Updating markers (camera reset: $shouldSetCamera, redraw route: $shouldRedrawRoute)")

            // Update route and markers with smart redrawing
            updateRouteWithMarkers(
                mapView,
                driverPoint,
                riderPoint,
                context,
                pointAnnotationManager,
                polylineAnnotationManager,
                driverAnnotationId,
                riderAnnotationId,
                shouldRedrawRoute,
                onRouteInfoUpdated
            )

            // Track position for distance-based updates (only if we redrew the route)
            if (shouldRedrawRoute) {
                lastRouteCalcPosition.value = currentLocation
                lastRouteCalcTime.value = System.currentTimeMillis()
            }

            // Only adjust camera on initial load or trip change
            if (shouldSetCamera) {
                adjustCameraToShowRoute(mapView, driverPoint, riderPoint)
                initialCameraSet.value = true
                Log.d("MapComponent", "Initial camera set for active trip")
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
                mapView.getMapboxMap().setCamera(cameraOptions)
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
 */
private fun updateRouteWithMarkers(
    mapView: MapView,
    driverPoint: Point,
    riderPoint: Point,
    context: Context,
    pointManagerState: MutableState<com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager?>,
    polylineManagerState: MutableState<com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager?>,
    driverAnnotationIdState: MutableState<String?>,
    riderAnnotationIdState: MutableState<String?>,
    shouldRedrawRoute: Boolean,
    onRouteInfoUpdated: (RouteState) -> Unit = {}
) {
    Log.d("MapComponent", "Updating route from driver to rider (redraw route: $shouldRedrawRoute)")

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
        Log.d("MapComponent", "Redrawing route (driver moved significantly)")
        polylineManager.deleteAll()
        fetchAndDrawRoute(mapView, driverPoint, riderPoint, polylineManager, onRouteInfoUpdated)
    } else {
        Log.d("MapComponent", "Keeping existing route (driver movement minimal)")
    }

    // Then update markers (they will appear on top of the route)
    val driverAnnotationId = driverAnnotationIdState.value
    if (driverAnnotationId != null) {
        pointManager.annotations.find { it.id == driverAnnotationId }?.let { annotation ->
            annotation.point = driverPoint
            pointManager.update(annotation)
            Log.d("MapComponent", "Driver marker smoothly updated")
        } ?: createMarkers(pointManager, driverPoint, riderPoint, context, driverAnnotationIdState, riderAnnotationIdState)
    } else {
        createMarkers(pointManager, driverPoint, riderPoint, context, driverAnnotationIdState, riderAnnotationIdState)
    }

    // Smoothly update rider marker (usually static, but update for consistency)
    val riderAnnotationId = riderAnnotationIdState.value
    if (riderAnnotationId != null) {
        pointManager.annotations.find { it.id == riderAnnotationId }?.let { annotation ->
            annotation.point = riderPoint
            pointManager.update(annotation)
        }
    }
}

private fun createMarkers(
    manager: com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager,
    driverPoint: Point,
    riderPoint: Point,
    context: Context,
    driverAnnotationIdState: MutableState<String?>,
    riderAnnotationIdState: MutableState<String?>
) {
    // Create driver marker
    bitmapFromDrawable(context, R.drawable._d_cars)?.let { bitmap ->
        val driverAnnotation = PointAnnotationOptions()
            .withPoint(driverPoint)
            .withIconImage(bitmap)
        val annotation = manager.create(driverAnnotation)
        driverAnnotationIdState.value = annotation.id
        Log.d("MapComponent", "Driver marker created")
    }

    // Create rider marker
    bitmapFromDrawable(context, R.drawable.ride)?.let { bitmap ->
        val riderAnnotation = PointAnnotationOptions()
            .withPoint(riderPoint)
            .withIconImage(bitmap)
        val annotation = manager.create(riderAnnotation)
        riderAnnotationIdState.value = annotation.id
        Log.d("MapComponent", "Rider marker created")
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

                val polylineOptions = PolylineAnnotationOptions()
                    .withPoints(points)
                    .withLineColor("#000000") // Black color
                    .withLineWidth(5.0) // Increased width for visibility

                polylineAnnotationManager.create(polylineOptions)
                Log.d("DirectionsAPI", "✅ Route drawn successfully with ${points.size} points")

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
                Log.e("DirectionsAPI", "Error parsing route geometry", e)
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

    mapView.getMapboxMap().flyTo(
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