package com.kabukabu.driver.core.utils

import android.content.Context
import android.util.Log
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.scalebar.scalebar
import java.lang.ref.WeakReference

/**
 * Singleton manager that keeps the MapView alive across navigations.
 * This prevents the map from reloading when navigating away and back to HomeScreen.
 *
 * Usage:
 * - Call getOrCreateMapView() in your MapComponent to get/create the map
 * - Call saveState() before navigating away to preserve camera position
 * - Call restoreState() when returning to restore the camera position
 * - Call clearMapView() on logout to release resources
 */
object MapViewManager {
    private const val TAG = "MapViewManager"

    // Using WeakReference to allow garbage collection if absolutely needed
    private var mapViewRef: WeakReference<MapView>? = null

    // Cached camera state for restoration
    private var savedCameraOptions: CameraOptions? = null

    // Track if the map style is loaded
    private var isStyleLoaded: Boolean = false

    // Track the context the map was created with
    private var contextRef: WeakReference<Context>? = null

    // Track the last active trip ID to detect when trip ends (persists across MapComponent instances)
    private var lastActiveTripId: String? = null

    // Flag to indicate if there are stale annotations that need cleanup
    private var hasStaleAnnotations: Boolean = false

    // Store references to annotation managers (for selective cleanup)
    private var pointManagerRef: WeakReference<com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager>? = null
    private var polylineManagerRef: WeakReference<com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager>? = null

    /**
     * Get the existing MapView or create a new one if it doesn't exist.
     * The MapView is retained in memory to avoid recreating it on every navigation.
     */
    @Synchronized
    fun getOrCreateMapView(context: Context): MapView {
        val existingMapView = mapViewRef?.get()
        val existingContext = contextRef?.get()

        // Return existing map if valid and same context application
        if (existingMapView != null && existingContext?.applicationContext == context.applicationContext) {
            Log.d(TAG, "Returning existing MapView")
            return existingMapView
        }

        // Create new MapView
        Log.d(TAG, "Creating new MapView")
        val mapView = MapView(context)

        // Configure map view settings
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

        // Store references
        mapViewRef = WeakReference(mapView)
        contextRef = WeakReference(context)
        isStyleLoaded = false

        return mapView
    }

    /**
     * Get the cached MapView if it exists
     */
    fun getMapView(): MapView? = mapViewRef?.get()

    /**
     * Check if we have a valid cached MapView
     */
    fun hasValidMapView(): Boolean = mapViewRef?.get() != null

    /**
     * Check if the map style is already loaded
     */
    fun isMapStyleLoaded(): Boolean = isStyleLoaded && hasValidMapView()

    /**
     * Mark the map style as loaded
     */
    fun setStyleLoaded(loaded: Boolean) {
        isStyleLoaded = loaded
    }

    /**
     * Save the current camera position for later restoration
     */
    fun saveCameraState(cameraOptions: CameraOptions) {
        savedCameraOptions = cameraOptions
        Log.d(TAG, "Camera state saved: center=${cameraOptions.center}, zoom=${cameraOptions.zoom}")
    }

    /**
     * Save camera state from the current map
     */
    fun saveCameraStateFromMap() {
        mapViewRef?.get()?.let { mapView ->
            val cameraState = mapView.mapboxMap.cameraState
            savedCameraOptions = CameraOptions.Builder()
                .center(cameraState.center)
                .zoom(cameraState.zoom)
                .bearing(cameraState.bearing)
                .pitch(cameraState.pitch)
                .build()
            Log.d(TAG, "Camera state saved from map")
        }
    }

    /**
     * Get the saved camera position
     */
    fun getSavedCameraState(): CameraOptions? = savedCameraOptions

    /**
     * Restore the saved camera position to the map
     */
    fun restoreCameraState() {
        val mapView = mapViewRef?.get() ?: return
        val cameraOptions = savedCameraOptions ?: return

        try {
            mapView.mapboxMap.setCamera(cameraOptions)
            Log.d(TAG, "Camera state restored")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore camera state", e)
        }
    }

    /**
     * Clear the cached MapView - call on logout or when you want to fully reset the map
     */
    @Synchronized
    fun clearMapView() {
        Log.d(TAG, "Clearing MapView")
        mapViewRef?.get()?.let { mapView ->
            try {
                // Clean up annotations using the annotations plugin
                val annotationPlugin = mapView.annotations.cleanup()
            } catch (e: Exception) {
                Log.e(TAG, "Error cleaning up annotations", e)
            }
        }
        mapViewRef = null
        contextRef = null
        savedCameraOptions = null
        isStyleLoaded = false
    }

    /**
     * Detach the MapView from its parent without destroying it.
     * Call this when the composable is disposed but you want to keep the map.
     */
    fun detachFromParent() {
        mapViewRef?.get()?.let { mapView ->
            try {
                val parent = mapView.parent
                if (parent is android.view.ViewGroup) {
                    parent.removeView(mapView)
                    Log.d(TAG, "MapView detached from parent")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error detaching MapView from parent", e)
            }
        }
    }

    /**
     * Update the last active trip ID and detect if trip ended.
     * Returns true if the trip ended (was not null and now is null), meaning cleanup is needed.
     */
    @Synchronized
    fun updateTripId(newTripId: String?): Boolean {
        val tripEnded = lastActiveTripId != null && newTripId == null
        if (tripEnded) {
            hasStaleAnnotations = true
            Log.d(TAG, "Trip ended detected: $lastActiveTripId -> null, marking for cleanup")
        }
        lastActiveTripId = newTripId
        return tripEnded
    }

    /**
     * Get the last known active trip ID (persists across MapComponent instances)
     */
    fun getLastActiveTripId(): String? = lastActiveTripId

    /**
     * Check if there are stale annotations that need to be cleaned up
     */
    fun needsAnnotationCleanup(): Boolean = hasStaleAnnotations

    /**
     * Clear the stale annotations flag after cleanup has been performed
     */
    fun markAnnotationsCleanedUp() {
        hasStaleAnnotations = false
        Log.d(TAG, "Annotations cleanup completed")
    }

    /**
     * Register annotation managers so we can selectively clear them
     */
    fun registerPointAnnotationManager(manager: com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager) {
        pointManagerRef = WeakReference(manager)
        Log.d(TAG, "Point annotation manager registered")
    }

    fun registerPolylineAnnotationManager(manager: com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager) {
        polylineManagerRef = WeakReference(manager)
        Log.d(TAG, "Polyline annotation manager registered")
    }

    // Constants for dashed line layer (same as in mapComponent.kt)
    private const val DASHED_LINE_SOURCE_ID = "dashed-connector-source"
    private const val DASHED_LINE_LAYER_ID = "dashed-connector-layer"

    /**
     * Clear trip-related annotations from the cached MapView.
     * Call this when trip ends to ensure route polylines and destination markers are removed.
     * This method clears ALL annotations including the driver marker, which will be recreated
     * on the next location update.
     * Also clears the dashed line layer which is drawn separately from annotations.
     */
    fun clearTripAnnotations() {
        mapViewRef?.get()?.let { mapView ->
            try {
                Log.d(TAG, "Clearing trip annotations...")

                // 1. Clear the dashed line layer (uses style layer, not annotation manager)
                clearDashedLineLayer(mapView)

                // 2. Clear all polylines using registered manager
                polylineManagerRef?.get()?.let { polylineManager ->
                    try {
                        val count = polylineManager.annotations.size
                        polylineManager.deleteAll()
                        Log.d(TAG, "Deleted $count polylines from registered manager")
                    } catch (e: Exception) {
                        Log.w(TAG, "Error clearing polylines: ${e.message}")
                    }
                } ?: Log.w(TAG, "Polyline manager not registered or already cleared")

                // 3. Clear all point annotations (destination marker + driver marker)
                // The driver marker will be recreated on next location update
                pointManagerRef?.get()?.let { pointManager ->
                    try {
                        val count = pointManager.annotations.size
                        pointManager.deleteAll()
                        Log.d(TAG, "Deleted $count point annotations from registered manager")
                    } catch (e: Exception) {
                        Log.w(TAG, "Error clearing point annotations: ${e.message}")
                    }
                } ?: Log.w(TAG, "Point manager not registered or already cleared")

                // Reset the last trip ID since trip has ended
                lastActiveTripId = null

                // Clear the stale annotations flag since we just did cleanup
                hasStaleAnnotations = false
                Log.d(TAG, "Trip annotations cleared successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing trip annotations", e)
            }
        } ?: Log.w(TAG, "MapView not available for clearing trip annotations")
    }

    /**
     * Clear all annotations from the cached MapView including driver marker.
     * Call this only when completely resetting the map (e.g., logout).
     */
    fun clearAllAnnotations() {
        mapViewRef?.get()?.let { mapView ->
            try {
                // Clear all annotation managers (polylines, points including driver marker)
                mapView.annotations.cleanup()
                Log.d(TAG, "All annotations cleared from MapView")

                // Also clear the dashed line layer (uses style layer, not annotation manager)
                clearDashedLineLayer(mapView)

                // Reset the last trip ID
                lastActiveTripId = null

                // Set flag for verification
                hasStaleAnnotations = true
                Log.d(TAG, "Full cleanup completed, stale flag set for verification")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing annotations", e)
            }
        }
    }

    /**
     * Clear the dashed line layer from the map style
     */
    private fun clearDashedLineLayer(mapView: MapView) {
        try {
            val style = mapView.mapboxMap.style
            if (style != null) {
                if (style.styleLayerExists(DASHED_LINE_LAYER_ID)) {
                    style.removeStyleLayer(DASHED_LINE_LAYER_ID)
                    Log.d(TAG, "Removed dashed line layer")
                }
                if (style.styleSourceExists(DASHED_LINE_SOURCE_ID)) {
                    style.removeStyleSource(DASHED_LINE_SOURCE_ID)
                    Log.d(TAG, "Removed dashed line source")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error clearing dashed line layer: ${e.message}")
        }
    }
}

