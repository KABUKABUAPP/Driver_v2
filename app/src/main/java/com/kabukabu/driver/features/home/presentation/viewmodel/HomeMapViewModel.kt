package com.kabukabu.driver.features.home.presentation.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import com.mapbox.maps.CameraOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel scoped to the navigation graph that preserves map state across navigations.
 * This ensures that when the user navigates to another screen and comes back,
 * the map doesn't reload from scratch.
 */
class HomeMapViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * Last known camera position to restore when returning to the map
     */
    private val _lastCameraPosition = MutableStateFlow<CameraOptions?>(null)
    val lastCameraPosition: StateFlow<CameraOptions?> = _lastCameraPosition.asStateFlow()

    /**
     * Last known location for quick restoration
     */
    private val _lastKnownLocation = MutableStateFlow<Location?>(null)
    val lastKnownLocation: StateFlow<Location?> = _lastKnownLocation.asStateFlow()

    /**
     * Whether the map has been initialized at least once
     */
    private val _isMapInitialized = MutableStateFlow(false)
    val isMapInitialized: StateFlow<Boolean> = _isMapInitialized.asStateFlow()

    /**
     * Current zoom level
     */
    private val _zoomLevel = MutableStateFlow(15.0)
    val zoomLevel: StateFlow<Double> = _zoomLevel.asStateFlow()

    /**
     * Save the current camera position for later restoration
     */
    fun saveCameraPosition(cameraOptions: CameraOptions) {
        _lastCameraPosition.value = cameraOptions
    }

    /**
     * Save the last known location
     */
    fun saveLastLocation(location: Location) {
        _lastKnownLocation.value = location
    }

    /**
     * Mark the map as initialized
     */
    fun setMapInitialized() {
        _isMapInitialized.value = true
    }

    /**
     * Save the current zoom level
     */
    fun saveZoomLevel(zoom: Double) {
        _zoomLevel.value = zoom
    }

    /**
     * Clear all saved state (e.g., on logout)
     */
    fun clearState() {
        _lastCameraPosition.value = null
        _lastKnownLocation.value = null
        _isMapInitialized.value = false
        _zoomLevel.value = 15.0
    }
}

