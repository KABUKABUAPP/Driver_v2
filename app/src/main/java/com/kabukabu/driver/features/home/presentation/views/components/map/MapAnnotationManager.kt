package com.kabukabu.driver.features.home.presentation.views.components.map

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import com.kabukabu.driver.R
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager

/**
 * Manages map annotations (markers) for driver, rider/pickup, and destination
 */
object MapAnnotationManager {

    /**
     * Updates driver marker only - smoothly updates position without recreating
     */
    fun updateDriverMarker(
        mapView: MapView,
        driverPoint: Point,
        context: Context,
        managerState: MutableState<PointAnnotationManager?>,
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
                Log.d("MapAnnotationManager", "Driver marker position smoothly updated")
            } ?: run {
                // Annotation was deleted, create new one
                createDriverMarker(manager, driverPoint, context, annotationIdState)
            }
        } else {
            // First time - create marker
            createDriverMarker(manager, driverPoint, context, annotationIdState)
        }
    }

    /**
     * Updates destination/rider marker based on trip status
     */
    fun updateDestinationMarker(
        pointManager: PointAnnotationManager,
        destinationPoint: Point,
        context: Context,
        annotationIdState: MutableState<String?>,
        isTripStarted: Boolean
    ) {
        val secondMarkerAnnotationId = annotationIdState.value
        val drawableRes = if (isTripStarted) R.drawable.destination else R.drawable.ride
        val markerType = if (isTripStarted) "destination" else "rider/pickup"

        if (secondMarkerAnnotationId != null) {
            // Check if existing marker exists and update it, or create new one
            val existingAnnotation = pointManager.annotations.find { it.id == secondMarkerAnnotationId }
            if (existingAnnotation != null) {
                existingAnnotation.point = destinationPoint
                pointManager.update(existingAnnotation)
                Log.d("MapAnnotationManager", "$markerType marker position updated")
            } else {
                // Create marker
                MapBitmapUtils.bitmapFromDrawable(context, drawableRes)?.let { bitmap ->
                    val annotation = PointAnnotationOptions()
                        .withPoint(destinationPoint)
                        .withIconImage(bitmap)
                    val createdAnnotation = pointManager.create(annotation)
                    annotationIdState.value = createdAnnotation.id
                    Log.d("MapAnnotationManager", "$markerType marker created")
                }
            }
        } else {
            // Create new marker
            MapBitmapUtils.bitmapFromDrawable(context, drawableRes)?.let { bitmap ->
                val annotation = PointAnnotationOptions()
                    .withPoint(destinationPoint)
                    .withIconImage(bitmap)
                val createdAnnotation = pointManager.create(annotation)
                annotationIdState.value = createdAnnotation.id
                Log.d("MapAnnotationManager", "$markerType marker created (first time)")
            }
        }
    }

    /**
     * Clears all annotations except the driver marker
     */
    fun clearTripAnnotations(
        pointManager: PointAnnotationManager?,
        polylineManager: com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager?,
        riderAnnotationIdState: MutableState<String?>,
        routeAnnotationIdState: MutableState<String?>
    ) {
        Log.d("MapAnnotationManager", "Clearing trip annotations (polylines and destination marker)")

        // Clear destination/rider marker
        riderAnnotationIdState.value?.let { annotationId ->
            pointManager?.annotations?.find { it.id == annotationId }?.let { annotation ->
                pointManager.delete(annotation)
                Log.d("MapAnnotationManager", "Deleted destination/rider marker")
            }
            riderAnnotationIdState.value = null
        }

        // Clear polylines
        routeAnnotationIdState.value?.let { annotationId ->
            polylineManager?.annotations?.find { it.id == annotationId }?.let { annotation ->
                polylineManager.delete(annotation)
                Log.d("MapAnnotationManager", "Deleted route polyline")
            }
            routeAnnotationIdState.value = null
        }
    }

    private fun createDriverMarker(
        manager: PointAnnotationManager,
        driverPoint: Point,
        context: Context,
        annotationIdState: MutableState<String?>
    ) {
        MapBitmapUtils.bitmapFromDrawable(context, R.drawable._d_cars)?.let { bitmap ->
            val driverAnnotation = PointAnnotationOptions()
                .withPoint(driverPoint)
                .withIconImage(bitmap)
            val annotation = manager.create(driverAnnotation)
            annotationIdState.value = annotation.id
            Log.d("MapAnnotationManager", "Driver marker created with ID: ${annotation.id}")
        }
    }
}

