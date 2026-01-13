//package com.kabukabu.driver.features.home.presentation.views.components.map
//
//import android.util.Log
//import com.mapbox.geojson.Point
//import com.mapbox.maps.CameraOptions
//import com.mapbox.maps.MapView
//import com.mapbox.maps.dsl.cameraOptions
//import com.mapbox.maps.plugin.animation.MapAnimationOptions
//import com.mapbox.maps.plugin.animation.flyTo
//
///**
// * Utility class for managing map camera positioning and animations
// */
//object MapCameraManager {
//
//    /**
//     * Adjusts camera to show both driver and destination/rider in view
//     */
//    fun adjustCameraToShowRoute(mapView: MapView, driverPoint: Point, riderPoint: Point) {
//        try {
//            val lat1 = driverPoint.latitude()
//            val lon1 = driverPoint.longitude()
//            val lat2 = riderPoint.latitude()
//            val lon2 = riderPoint.longitude()
//
//            val centerLat = (lat1 + lat2) / 2
//            val centerLon = (lon1 + lon2) / 2
//
//            val latDiff = kotlin.math.abs(lat1 - lat2)
//            val lonDiff = kotlin.math.abs(lon1 - lon2)
//            val maxDiff = kotlin.math.max(latDiff, lonDiff)
//
//            val zoom = when {
//                maxDiff > 0.1 -> 11.0
//                maxDiff > 0.05 -> 12.0
//                maxDiff > 0.02 -> 13.0
//                maxDiff > 0.01 -> 14.0
//                else -> 15.0
//            }
//
//            val cameraOptions = cameraOptions {
//                center(Point.fromLngLat(centerLon, centerLat))
//                zoom(zoom)
//            }
//
//            mapView.flyTo(
//                cameraOptions, MapAnimationOptions.mapAnimationOptions {
//                    duration(1500L)
//                }
//            )
//
//            Log.d("MapCameraManager", "Camera adjusted to show route - center: ($centerLat, $centerLon), zoom: $zoom")
//        } catch (e: Exception) {
//            Log.e("MapCameraManager", "Failed to adjust camera", e)
//        }
//    }
//
//    /**
//     * Centers camera on a single point with specified zoom level
//     */
//    fun centerCameraOnPoint(
//        mapView: MapView,
//        point: Point,
//        zoom: Double = 15.0,
//        animated: Boolean = true
//    ) {
//        try {
//            val cameraOptions = CameraOptions.Builder()
//                .center(point)
//                .zoom(zoom)
//                .build()
//
//            if (animated) {
//                mapView.flyTo(
//                    cameraOptions,
//                    MapAnimationOptions.mapAnimationOptions { duration(1000L) }
//                )
//            } else {
//                mapView.mapboxMap.setCamera(cameraOptions)
//            }
//
//            Log.d("MapCameraManager", "Camera centered on point: (${point.latitude()}, ${point.longitude()})")
//        } catch (e: Exception) {
//            Log.e("MapCameraManager", "Failed to center camera", e)
//        }
//    }
//}
//
