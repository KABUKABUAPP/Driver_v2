//package com.kabukabu.driver.features.home.presentation.views.components.map
//
//import android.content.Context
//import android.util.Log
//import androidx.compose.runtime.MutableState
//import com.kabukabu.driver.R
//import com.mapbox.api.directions.v5.DirectionsCriteria
//import com.mapbox.api.directions.v5.MapboxDirections
//import com.mapbox.api.directions.v5.models.DirectionsResponse
//import com.mapbox.core.constants.Constants
//import com.mapbox.geojson.LineString
//import com.mapbox.geojson.Point
//import com.mapbox.maps.MapView
//import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
//import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
///**
// * Manages route fetching, drawing, and updates on the map
// */
//object MapRouteManager {
//
//    /**
//     * Fetches route from Mapbox Directions API and draws it on the map
//     */
//    fun fetchAndDrawRoute(
//        mapView: MapView,
//        origin: Point,
//        destination: Point,
//        polylineAnnotationManager: PolylineAnnotationManager,
//        onRouteInfoUpdated: (RouteState) -> Unit = {},
//        routeRequestVersion: MutableState<Int>,
//        routeAnnotationId: MutableState<String?>,
//        routeCallState: MutableState<MapboxDirections?>,
//        useDashedLine: Boolean = false
//    ) {
//        Log.d("MapRouteManager", "Fetching route from (${origin.longitude()},${origin.latitude()}) to (${destination.longitude()},${destination.latitude()})")
//
//        // Validate that origin and destination are different
//        val distance = RouteGeometryUtils.haversineDistance(origin, destination)
//        if (distance < 10.0) {
//            Log.w("MapRouteManager", "Origin and destination are too close (${distance}m), skipping route fetch")
//            return
//        }
//
//        val accessToken = mapView.context.getString(R.string.mapbox_access_token)
//        Log.d("MapRouteManager", "Using token: ${accessToken.take(10)}...")
//
//        // Cancel any previous in-flight MapboxDirections request
//        routeCallState.value?.let { previousClient ->
//            try {
//                previousClient.cancelCall()
//                Log.d("MapRouteManager", "Cancelled previous MapboxDirections request")
//            } catch (e: Exception) {
//                Log.w("MapRouteManager", "Failed to cancel previous directions client: ${e.message}")
//            }
//        }
//
//        // Build a new MapboxDirections client
//        val client = MapboxDirections.builder()
//            .origin(origin)
//            .destination(destination)
//            .overview(DirectionsCriteria.OVERVIEW_FULL)
//            .profile(DirectionsCriteria.PROFILE_DRIVING)
//            .accessToken(accessToken)
//            .build()
//
//        // Store the new MapboxDirections client
//        routeCallState.value = client
//
//        // Capture the version for this request
//        val thisRequestVersion = routeRequestVersion.value
//
//        // Start the call
//        client.enqueueCall(object : Callback<DirectionsResponse> {
//            override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
//                // Ignore stale responses
//                if (thisRequestVersion != routeRequestVersion.value) {
//                    Log.d("MapRouteManager", "Ignored stale response (version mismatch)")
//                    if (routeCallState.value === client) routeCallState.value = null
//                    return
//                }
//
//                if (!response.isSuccessful) {
//                    Log.e("MapRouteManager", "Request failed with code: ${response.code()}")
//                    drawStraightLine(polylineAnnotationManager, origin, destination)
//                    if (routeCallState.value === client) routeCallState.value = null
//                    return
//                }
//
//                val routes = response.body()?.routes()
//                if (routes.isNullOrEmpty()) {
//                    Log.e("MapRouteManager", "No routes found in response")
//                    drawStraightLine(polylineAnnotationManager, origin, destination)
//                    if (routeCallState.value === client) routeCallState.value = null
//                    return
//                }
//
//                val currentRoute = routes[0]
//                val routeGeometry = currentRoute.geometry()
//
//                if (routeGeometry == null) {
//                    Log.e("MapRouteManager", "Route geometry is null")
//                    drawStraightLine(polylineAnnotationManager, origin, destination)
//                    if (routeCallState.value === client) routeCallState.value = null
//                    return
//                }
//
//                try {
//                    val points = LineString.fromPolyline(routeGeometry, Constants.PRECISION_6).coordinates()
//
//                    if (points.isEmpty()) {
//                        Log.e("MapRouteManager", "Route points list is empty after parsing")
//                        drawStraightLine(polylineAnnotationManager, origin, destination)
//                        if (routeCallState.value === client) routeCallState.value = null
//                        return
//                    }
//
//                    Log.d("MapRouteManager", "Creating polyline with ${points.size} points")
//
//                    // Trim points to start from driver's current location
//                    val adjustedPoints = try {
//                        RouteGeometryUtils.trimRoutePointsForDriver(points, origin, 50.0)
//                    } catch (e: Exception) {
//                        Log.w("MapRouteManager", "Failed to trim route points: ${e.message}")
//                        points
//                    }
//
//                    // Create polyline options
//                    val polylineOptions = PolylineAnnotationOptions()
//                        .withPoints(adjustedPoints)
//                        .withLineColor("#000000") // Black color
//                        .withLineWidth(5.0)
//
//                    // Add dashed pattern if requested
//                    if (useDashedLine) {
//                        polylineOptions.withLineDasharray(listOf(2.0, 2.0))
//                    }
//
//                    // Create the new polyline
//                    val createdAnnotation = polylineAnnotationManager.create(polylineOptions)
//                    Log.d("MapRouteManager", "✅ Route drawn - annotation ID: ${createdAnnotation.id}")
//
//                    // Delete previous polyline if any
//                    val previousId = routeAnnotationId.value
//                    routeAnnotationId.value = createdAnnotation.id
//                    if (previousId != null && previousId != createdAnnotation.id) {
//                        polylineAnnotationManager.annotations.find { it.id == previousId }?.let { oldAnno ->
//                            try {
//                                polylineAnnotationManager.delete(listOf(oldAnno))
//                                Log.d("MapRouteManager", "Deleted previous route annotation: $previousId")
//                            } catch (e: Exception) {
//                                Log.w("MapRouteManager", "Failed to delete previous route: ${e.message}")
//                            }
//                        }
//                    }
//
//                    // Extract distance and duration
//                    val distance = currentRoute.distance()
//                    val duration = currentRoute.duration()
//                    Log.d("MapRouteManager", "Route distance: ${distance}m, duration: ${duration}s")
//
//                    // Update route state
//                    val routeState = RouteState(
//                        distanceInMeters = distance,
//                        durationInSeconds = duration,
//                        lastUpdated = System.currentTimeMillis()
//                    )
//                    onRouteInfoUpdated(routeState)
//
//                    // Clear stored client reference
//                    if (routeCallState.value === client) routeCallState.value = null
//
//                } catch (e: Exception) {
//                    Log.e("MapRouteManager", "Error parsing route geometry: ${e.message}", e)
//                    drawStraightLine(polylineAnnotationManager, origin, destination)
//                    if (routeCallState.value === client) routeCallState.value = null
//                }
//            }
//
//            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
//                // Ignore failures from stale requests
//                if (thisRequestVersion != routeRequestVersion.value) {
//                    Log.d("MapRouteManager", "Ignored stale failure (version mismatch)")
//                    if (routeCallState.value === client) routeCallState.value = null
//                    return
//                }
//                Log.e("MapRouteManager", "API call failed", t)
//                drawStraightLine(polylineAnnotationManager, origin, destination)
//                if (routeCallState.value === client) routeCallState.value = null
//            }
//        })
//    }
//
//    /**
//     * Updates existing polyline by trimming based on driver's current position
//     */
//    fun trimExistingRoute(
//        polylineManager: PolylineAnnotationManager,
//        driverPoint: Point,
//        routeAnnotationId: MutableState<String?>
//    ) {
//        val currentRouteId = routeAnnotationId.value ?: return
//
//        polylineManager.annotations.find { it.id == currentRouteId }?.let { existingPolyline ->
//            try {
//                val existingPoints = existingPolyline.points
//                if (existingPoints != null && existingPoints.isNotEmpty()) {
//                    val trimmed = try {
//                        RouteGeometryUtils.trimRoutePointsForDriver(existingPoints, driverPoint, 50.0)
//                    } catch (e: Exception) {
//                        Log.w("MapRouteManager", "Trimming failed: ${e.message}")
//                        existingPoints
//                    }
//
//                    // Update if points changed
//                    if (trimmed.size != existingPoints.size || (trimmed.isNotEmpty() && trimmed[0] != existingPoints[0])) {
//                        existingPolyline.points = trimmed
//                        try {
//                            polylineManager.update(existingPolyline)
//                            Log.d("MapRouteManager", "Trimmed route - new points: ${trimmed.size}")
//                        } catch (e: Exception) {
//                            Log.w("MapRouteManager", "Failed to update polyline: ${e.message}")
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                Log.w("MapRouteManager", "Error while trimming route: ${e.message}")
//            }
//        }
//    }
//
//    /**
//     * Draws a fallback straight line when route fetching fails
//     */
//    private fun drawStraightLine(
//        polylineAnnotationManager: PolylineAnnotationManager,
//        origin: Point,
//        destination: Point
//    ) {
//        Log.d("MapRouteManager", "Drawing fallback straight line")
//        val polylineOptions = PolylineAnnotationOptions()
//            .withPoints(listOf(origin, destination))
//            .withLineColor("#000000")
//            .withLineWidth(5.0)
//        polylineAnnotationManager.create(polylineOptions)
//    }
//}
//
