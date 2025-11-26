package com.kabukabu.driver.core.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

/**
 * Repository for managing driver location updates.
 * Provides a single source of truth for location data across the app.
 */
class LocationRepository private constructor(
    private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private var locationCallback: LocationCallback? = null
    private var isLocationUpdatesActive = false

    companion object {
        @Volatile
        private var INSTANCE: LocationRepository? = null

        fun getInstance(context: Context): LocationRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocationRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    /**
     * Check if location permissions are granted
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Start receiving location updates
     */
    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (!hasLocationPermission()) {
            Log.w("LocationRepository", "Location permission not granted")
            return
        }

        if (isLocationUpdatesActive) {
            Log.d("LocationRepository", "Location updates already active")
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L // Update every 10 seconds
        ).apply {
            setMinUpdateIntervalMillis(5000L) // Fastest update every 5 seconds
            setWaitForAccurateLocation(false)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    Log.d("LocationRepository", "Location updated: ${location.latitude}, ${location.longitude}")
                    _currentLocation.value = location
                }
            }
        }

        try {
            // Get initial location immediately
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    Log.d("LocationRepository", "Initial location: ${it.latitude}, ${it.longitude}")
                    _currentLocation.value = it
                }
            }

            // Start continuous updates
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
            isLocationUpdatesActive = true
            Log.d("LocationRepository", "Location updates started")
        } catch (e: SecurityException) {
            Log.e("LocationRepository", "Location permission error: ${e.message}")
        }
    }

    /**
     * Stop receiving location updates
     */
    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            isLocationUpdatesActive = false
            Log.d("LocationRepository", "Location updates stopped")
        }
    }

    /**
     * Get current location as a Flow (reactive updates)
     */
    fun getLocationFlow(): Flow<Location?> = currentLocation

    /**
     * Get current location value (snapshot)
     */
    fun getCurrentLocationValue(): Location? = _currentLocation.value

    /**
     * Request a single location update (useful for one-time operations)
     */
    @SuppressLint("MissingPermission")
    suspend fun requestSingleLocationUpdate(): Location? {
        if (!hasLocationPermission()) {
            Log.w("LocationRepository", "Location permission not granted")
            return null
        }

        return try {
            kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    continuation.resume(location) {}
                }.addOnFailureListener { exception ->
                    Log.e("LocationRepository", "Error getting single location: ${exception.message}")
                    continuation.resume(null) {}
                }
            }
        } catch (e: Exception) {
            Log.e("LocationRepository", "Error getting single location: ${e.message}")
            null
        }
    }

}

