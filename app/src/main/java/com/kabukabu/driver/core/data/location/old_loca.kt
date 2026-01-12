//package com.kabukabu.driver.core.data.location
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.content.Context
//import android.content.pm.PackageManager
//import android.location.Location
//import android.os.Looper
//import android.util.Log
//import androidx.core.content.ContextCompat
//import com.google.android.gms.location.*
//import com.google.android.gms.tasks.CancellationTokenSource
//import kotlinx.coroutines.flow.*
//
///**
// * Repository for managing driver location updates.
// * Provides a single source of truth for location data across the app.
// */
//class LocationRepository private constructor(
//    private val context: Context
//) {
//    private val fusedLocationClient: FusedLocationProviderClient =
//        LocationServices.getFusedLocationProviderClient(context)
//
//    private val _currentLocation = MutableStateFlow<Location?>(null)
//    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()
//
//    // New: expose whether we are still initializing (no cached/fresh location yet)
//    private val _isInitializing = MutableStateFlow(true)
//    val isInitializing: StateFlow<Boolean> = _isInitializing.asStateFlow()
//
//    private var locationCallback: LocationCallback? = null
//    private var isLocationUpdatesActive = false
//
//    // Constants for location filtering
//    private val MINIMUM_ACCEPTABLE_ACCURACY_METERS = 25f // prefer locations <= 25m accuracy
//    private val MINIMUM_DISTANCE_METERS = 5f // Only update if moved at least 5 meters
//
//    // Smoothing (exponential moving average) to reduce jitter
//    // alpha closer to 1 => more responsive, closer to 0 => smoother
//    private val SMOOTHING_ALPHA = 0.45f
//    private var smoothedLocation: Location? = null
//
//    // Recent raw samples buffer to do a small aggregation and reject outliers
//    private val RECENT_SAMPLES_SIZE = 5
//    private val recentRawSamples: ArrayDeque<Location> = ArrayDeque()
//
//    // SharedPreferences keys for cached location
//    private val PREFS_NAME = "location_repo_prefs"
//    private val KEY_LAT = "last_lat"
//    private val KEY_LON = "last_lon"
//    private val KEY_ACC = "last_acc"
//    private val KEY_TIME = "last_time"
//    private val KEY_SPEED = "last_speed"
//
//    init {
//        // Try to load a cached last-known location synchronously so UI can show something
//        loadCachedLocation()
//    }
//
//    companion object {
//        @SuppressLint("StaticFieldLeak")
//        @Volatile
//        private var INSTANCE: LocationRepository? = null
//
//        fun getInstance(context: Context): LocationRepository {
//            return INSTANCE ?: synchronized(this) {
//                INSTANCE ?: LocationRepository(context.applicationContext).also {
//                    INSTANCE = it
//                }
//            }
//        }
//    }
//
//    /**
//     * Check if location permissions are granted
//     */
//    fun hasLocationPermission(): Boolean {
//        return ContextCompat.checkSelfPermission(
//            context,
//            Manifest.permission.ACCESS_FINE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
//            context,
//            Manifest.permission.ACCESS_COARSE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED
//    }
//
//    /**
//     * Start receiving location updates
//     */
//    @SuppressLint("MissingPermission")
//    fun startLocationUpdates() {
//        if (!hasLocationPermission()) {
//            Log.w("LocationRepository", "Location permission not granted")
//            return
//        }
//
//        if (isLocationUpdatesActive) {
//            Log.d("LocationRepository", "Location updates already active")
//            return
//        }
//
//        // We're starting; if we already had a cached location, treat initialization as complete
//        if (smoothedLocation != null) {
//            _isInitializing.value = false
//        } else {
//            _isInitializing.value = true
//        }
//
//        val locationRequest = LocationRequest.Builder(
//            Priority.PRIORITY_HIGH_ACCURACY,
//            8000L // Target update ~8 seconds
//        ).apply {
//            setMinUpdateIntervalMillis(4000L) // Fastest update every 4 seconds
//            setWaitForAccurateLocation(true)
//        }.build()
//
//        locationCallback = object : LocationCallback() {
//            override fun onLocationResult(locationResult: LocationResult) {
//                // Sort incoming batch: prefer lower accuracy (smaller value) and newer timestamps
//                val sorted = locationResult.locations.sortedWith(compareBy<Location>({
//                    if (it.hasAccuracy()) it.accuracy else Float.MAX_VALUE
//                }, { -it.time }))
//
//                for (newLocation in sorted) {
//                    handleNewLocation(newLocation)
//                }
//            }
//        }
//
//        try {
//            // Try last known location first; if it's not good enough or stale, request a fresh one
//            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
//                val now = System.currentTimeMillis()
//                val isStale = location?.let { (now - it.time) > 15_000 } ?: true // older than 15s
//
//                if (location != null && !isStale && location.hasAccuracy() && location.accuracy <= MINIMUM_ACCEPTABLE_ACCURACY_METERS) {
//                    Log.d("LocationRepository", "Initial lastLocation acceptable: ${location.latitude}, ${location.longitude}, acc=${location.accuracy}m, age=${now - location.time}ms")
//                    smoothedLocation = location
//                    _currentLocation.value = location
//                    _isInitializing.value = false
//                    // persist
//                    saveLocationToPrefs(location)
//                } else {
//                    // Request a single high-accuracy fix as fallback
//                    Log.d("LocationRepository", "lastLocation missing, stale or inaccurate (stale=$isStale). Requesting fresh current location")
//                    val cts = CancellationTokenSource()
//                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
//                        .addOnSuccessListener { freshLoc ->
//                            if (freshLoc != null) {
//                                Log.d("LocationRepository", "Fresh initial location: ${freshLoc.latitude}, ${freshLoc.longitude}, acc=${freshLoc.accuracy}m")
//                                smoothedLocation = freshLoc
//                                _currentLocation.value = freshLoc
//                                _isInitializing.value = false
//                                saveLocationToPrefs(freshLoc)
//                            } else {
//                                Log.d("LocationRepository", "Fresh location request returned null")
//                                // keep initializing true until a later update arrives
//                            }
//                        }
//                        .addOnFailureListener { ex ->
//                            Log.w("LocationRepository", "Fresh location request failed: ${ex.message}")
//                        }
//                }
//            }.addOnFailureListener { e ->
//                Log.w("LocationRepository", "Failed to get lastLocation: ${e.message}")
//            }
//
//            // Start continuous updates
//            fusedLocationClient.requestLocationUpdates(
//                locationRequest,
//                locationCallback!!,
//                Looper.getMainLooper()
//            )
//            isLocationUpdatesActive = true
//            Log.d("LocationRepository", "Location updates started")
//        } catch (e: SecurityException) {
//            Log.e("LocationRepository", "Location permission error: ${e.message}")
//        }
//    }
//
//    /**
//     * Centralized processing for each new Location received
//     */
//    private fun handleNewLocation(newLocation: Location) {
//        // Reject obviously bad accuracy
//        if (newLocation.hasAccuracy() && newLocation.accuracy > 200f) {
//            Log.d("LocationRepository", "Discarding extremely poor accuracy location: ${newLocation.accuracy}m")
//            return
//        }
//
//        // Add to recent raw samples buffer
//        addToRecentSamples(newLocation)
//
//        // Build an aggregated candidate from recent samples to reduce impact of a single bad fix
//        val aggregated = aggregateRecentSamples() ?: newLocation
//
//        // If we don't have smoothedLocation yet, initialize it
//        if (smoothedLocation == null) {
//            smoothedLocation = aggregated
//            _currentLocation.value = aggregated
//            _isInitializing.value = false
//            saveLocationToPrefs(aggregated)
//            Log.d("LocationRepository", "Smoothed initial location set (agg): ${aggregated.latitude}, ${aggregated.longitude}, acc=${aggregated.accuracy}m")
//            return
//        }
//
//        val prev = smoothedLocation!!
//
//        // If new reading is much better accuracy, accept faster
//        val accuracyImproved = aggregated.hasAccuracy() && prev.hasAccuracy() &&
//                aggregated.accuracy < (prev.accuracy * 0.75f)
//
//        // Compute distance between smoothed location and aggregated sample
//        val distance = prev.distanceTo(aggregated)
//
//        // Adaptive alpha: be more responsive when moving faster or when accuracy improved
//        val speed = aggregated.speed.takeIf { it >= 0f } ?: 0f
//        val adaptiveAlpha = when {
//            accuracyImproved -> 0.7f
//            speed > 3f -> 0.65f // moving (> ~11 km/h) -> more responsive
//            else -> SMOOTHING_ALPHA
//        }
//
//        // If movement is significant or accuracy improved, update
//        if (distance >= MINIMUM_DISTANCE_METERS || accuracyImproved) {
//            // Apply exponential moving average to reduce jitter
//            val averaged = Location("fused")
//            val alpha = adaptiveAlpha
//
//            val lat = alpha * aggregated.latitude + (1 - alpha) * prev.latitude
//            val lon = alpha * aggregated.longitude + (1 - alpha) * prev.longitude
//            averaged.latitude = lat
//            averaged.longitude = lon
//
//            // For accuracy, take a conservative (better of the two)
//            val acc = if (aggregated.hasAccuracy() && prev.hasAccuracy()) {
//                Math.min(aggregated.accuracy, prev.accuracy)
//            } else if (aggregated.hasAccuracy()) {
//                aggregated.accuracy
//            } else if (prev.hasAccuracy()) {
//                prev.accuracy
//            } else {
//                MINIMUM_ACCEPTABLE_ACCURACY_METERS
//            }
//            averaged.accuracy = acc
//            averaged.time = aggregated.time
//            averaged.speed = aggregated.speed
//
//            smoothedLocation = averaged
//            _currentLocation.value = averaged
//            saveLocationToPrefs(averaged)
//
//            Log.d("LocationRepository", "Location updated (smoothed agg): ${averaged.latitude}, ${averaged.longitude}, acc=${averaged.accuracy}m, rawDist=${distance}m, alpha=${alpha}")
//        } else {
//            Log.d("LocationRepository", "New aggregated location ignored (too small move): ${distance}m < ${MINIMUM_DISTANCE_METERS}m")
//        }
//    }
//
//    private fun addToRecentSamples(loc: Location) {
//        // Keep only reasonably good samples; still store to buffer but avoid huge inaccuracies
//        recentRawSamples.addLast(loc)
//        while (recentRawSamples.size > RECENT_SAMPLES_SIZE) recentRawSamples.removeFirst()
//    }
//
//    private fun aggregateRecentSamples(): Location? {
//        if (recentRawSamples.isEmpty()) return null
//
//        // Filter out extremely poor accuracy samples
//        val filtered = recentRawSamples.filter { !it.hasAccuracy() || it.accuracy <= 100f }
//        if (filtered.isEmpty()) return recentRawSamples.lastOrNull()
//
//        // Weighted average by inverse accuracy (better accuracy -> higher weight)
//        var sumLat = 0.0
//        var sumLon = 0.0
//        var sumWeight = 0.0
//        var best: Location? = null
//        for (s in filtered) {
//            val weight = if (s.hasAccuracy() && s.accuracy > 0f) {
//                (1.0 / s.accuracy.toDouble())
//            } else {
//                1.0
//            }
//            sumLat += s.latitude * weight
//            sumLon += s.longitude * weight
//            sumWeight += weight
//            if (best == null || (s.hasAccuracy() && best.hasAccuracy() && s.accuracy < best.accuracy)) {
//                best = s
//            } else if (best == null) best = s
//        }
//
//        val centroid = Location("fused")
//        centroid.latitude = (sumLat / sumWeight)
//        centroid.longitude = (sumLon / sumWeight)
//        centroid.time = best?.time ?: System.currentTimeMillis()
//        centroid.accuracy = best?.accuracy ?: MINIMUM_ACCEPTABLE_ACCURACY_METERS
//        centroid.speed = best?.speed ?: 0f
//        return centroid
//    }
//
//    /**
//     * Stop receiving location updates
//     */
//    fun stopLocationUpdates() {
//        locationCallback?.let {
//            fusedLocationClient.removeLocationUpdates(it)
//            locationCallback = null
//            isLocationUpdatesActive = false
//            Log.d("LocationRepository", "Location updates stopped")
//        }
//    }
//
//    /**
//     * Get current location as a Flow (reactive updates)
//     */
//    fun getLocationFlow(): Flow<Location?> = currentLocation
//
//    /**
//     * Get current location value (snapshot)
//     */
//    fun getCurrentLocationValue(): Location? = _currentLocation.value
//
//    /**
//     * Request a single location update (useful for one-time operations)
//     */
//    @SuppressLint("MissingPermission")
//    suspend fun requestSingleLocationUpdate(): Location? {
//        if (!hasLocationPermission()) {
//            Log.w("LocationRepository", "Location permission not granted")
//            return null
//        }
//
//        return try {
//            kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
//                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
//                    continuation.resumeWith(Result.success(location))
//                }.addOnFailureListener { exception ->
//                    Log.e("LocationRepository", "Error getting single location: ${exception.message}")
//                    continuation.resumeWith(Result.success(null))
//                }
//            }
//        } catch (e: Exception) {
//            Log.e("LocationRepository", "Error getting single location: ${e.message}")
//            null
//        }
//    }
//
//    // --- Helpers to persist/load last known location so UI doesn't see 'unknown' ---
//    private fun saveLocationToPrefs(location: Location) {
//        try {
//            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//            prefs.edit()
//                .putLong(KEY_TIME, location.time)
//                .putFloat(KEY_LAT, location.latitude.toFloat())
//                .putFloat(KEY_LON, location.longitude.toFloat())
//                .putFloat(KEY_ACC, if (location.hasAccuracy()) location.accuracy else MINIMUM_ACCEPTABLE_ACCURACY_METERS)
//                .putFloat(KEY_SPEED, location.speed)
//                .apply()
//        } catch (e: Exception) {
//            Log.w("LocationRepository", "Failed to save location to prefs: ${e.message}")
//        }
//    }
//
//    private fun loadCachedLocation() {
//        try {
//            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//            if (!prefs.contains(KEY_TIME)) return
//
//            val time = prefs.getLong(KEY_TIME, 0L)
//            if (time <= 0L) return
//
//            val lat = prefs.getFloat(KEY_LAT, 0f).toDouble()
//            val lon = prefs.getFloat(KEY_LON, 0f).toDouble()
//            val acc = prefs.getFloat(KEY_ACC, MINIMUM_ACCEPTABLE_ACCURACY_METERS)
//            val speed = prefs.getFloat(KEY_SPEED, 0f)
//
//            val cached = Location("fused")
//            cached.latitude = lat
//            cached.longitude = lon
//            cached.time = time
//            cached.accuracy = acc
//            cached.speed = speed
//
//            smoothedLocation = cached
//            _currentLocation.value = cached
//            _isInitializing.value = false
//
//            Log.d("LocationRepository", "Loaded cached location: $lat, $lon, acc=$acc, time=$time")
//        } catch (e: Exception) {
//            Log.w("LocationRepository", "Failed to load cached location: ${e.message}")
//        }
//    }
//
//}
