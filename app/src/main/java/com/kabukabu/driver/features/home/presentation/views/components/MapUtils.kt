package com.kabukabu.driver.features.home.presentation.views.components

import android.content.Context
import kotlin.math.*
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.util.Log
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo

/**
 * Opens Google Maps with navigation from driver location to destination
 */
fun openGoogleMapsNavigation(
    context: Context,
    driverLocation: Location?,
    destinationPoint: Point
) {
    if (driverLocation == null) {
        Log.e("MapUtils", "Cannot open Google Maps: driver location is null")
        return
    }

    // Option 1: Use google.navigation scheme (directly opens navigation)
    val navigationUri = Uri.parse(
        "google.navigation:q=${destinationPoint.latitude()},${destinationPoint.longitude()}&mode=d"
    )

    val navigationIntent = Intent(Intent.ACTION_VIEW, navigationUri)
    navigationIntent.setPackage("com.google.android.apps.maps")

    // Try to open Google Maps app
    if (navigationIntent.resolveActivity(context.packageManager) != null) {
        Log.d("MapUtils", "Opening Google Maps navigation to: ${destinationPoint.latitude()}, ${destinationPoint.longitude()}")
        context.startActivity(navigationIntent)
    } else {
        // Fallback: Open in browser using web URL
        Log.w("MapUtils", "Google Maps app not found, opening in browser")
        val webUrl = "https://www.google.com/maps/dir/?api=1" +
                "&origin=${driverLocation.latitude},${driverLocation.longitude}" +
                "&destination=${destinationPoint.latitude()},${destinationPoint.longitude()}" +
                "&travelmode=driving"

        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
        context.startActivity(browserIntent)
    }
}

/**
 * Opens Google Maps with directions (shows route preview before navigation)
 */
fun openGoogleMapsDirections(
    context: Context,
    driverLocation: Location?,
    destinationPoint: Point
) {
    if (driverLocation == null) {
        Log.e("MapUtils", "Cannot open Google Maps: driver location is null")
        return
    }

    val directionsUri = Uri.parse(
        "https://www.google.com/maps/dir/?api=1" +
                "&origin=${driverLocation.latitude},${driverLocation.longitude}" +
                "&destination=${destinationPoint.latitude()},${destinationPoint.longitude()}" +
                "&travelmode=driving"
    )

    val intent = Intent(Intent.ACTION_VIEW, directionsUri)
    intent.setPackage("com.google.android.apps.maps")

    if (intent.resolveActivity(context.packageManager) != null) {
        Log.d("MapUtils", "Opening Google Maps directions to: ${destinationPoint.latitude()}, ${destinationPoint.longitude()}")
        context.startActivity(intent)
    } else {
        // Fallback to browser
        val browserIntent = Intent(Intent.ACTION_VIEW, directionsUri)
        context.startActivity(browserIntent)
    }
}

/**
 * Recenters map to show driver location only
 */
fun recenterMapOnDriver(
    mapView: MapView?,
    driverLocation: Location?,
    zoom: Double = 17.0,
    animationDuration: Long = 1000L
) {
    if (mapView == null || driverLocation == null) {
        Log.w("MapUtils", "Cannot recenter map: mapView=$mapView, location=$driverLocation")
        return
    }

    val driverPoint = Point.fromLngLat(driverLocation.longitude, driverLocation.latitude)
    val cameraOptions = CameraOptions.Builder()
        .center(driverPoint)
        .zoom(zoom)
        .build()

    mapView.getMapboxMap().flyTo(
        cameraOptions = cameraOptions,
        animationOptions = MapAnimationOptions.Builder().duration(animationDuration).build()
    )

    Log.d("MapUtils", "Recentered map on driver at: ${driverLocation.latitude}, ${driverLocation.longitude}")
}

/**
 * Recenters map to show both driver and rider (full route view)
 */
fun recenterMapOnRoute(
    mapView: MapView?,
    driverLocation: Location?,
    riderPoint: Point?,
    animationDuration: Long = 1500L
) {
    if (mapView == null || driverLocation == null || riderPoint == null) {
        Log.w("MapUtils", "Cannot recenter on route: mapView=$mapView, driver=$driverLocation, rider=$riderPoint")
        return
    }

    val driverPoint = Point.fromLngLat(driverLocation.longitude, driverLocation.latitude)

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

    // Calculate appropriate zoom level
    val lngDiff = maxLng - minLng + (lngPadding * 2)
    val latDiff = maxLat - minLat + (latPadding * 2)
    val maxDiff = maxOf(lngDiff, latDiff)

    val zoom = when {
        maxDiff > 0.1 -> 11.0
        maxDiff > 0.05 -> 12.0
        maxDiff > 0.02 -> 13.0
        maxDiff > 0.01 -> 14.0
        maxDiff > 0.005 -> 15.0
        else -> 16.0
    }

    val cameraOptions = CameraOptions.Builder()
        .center(centerPoint)
        .zoom(zoom)
        .build()

    mapView.getMapboxMap().flyTo(
        cameraOptions = cameraOptions,
        animationOptions = MapAnimationOptions.Builder().duration(animationDuration).build()
    )

    Log.d("MapUtils", "Recentered map on route: center($centerLng, $centerLat), zoom=$zoom")
}



fun Coordinate.distanceTo(other: Coordinate): Double {
    val R = 6371000.0 // Earth's radius in meters
    val toRads: (Double) -> Double = { it * PI / 180.0 }

    val dLat = toRads(other.latitude - this.latitude)
    val dLon = toRads(other.longitude - this.longitude)

    val a = sin(dLat / 2).pow(2) +
            cos(toRads(this.latitude)) * cos(toRads(other.latitude)) *
            sin(dLon / 2).pow(2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return R * c
}

data class Coordinate(val latitude: Double, val longitude: Double)

