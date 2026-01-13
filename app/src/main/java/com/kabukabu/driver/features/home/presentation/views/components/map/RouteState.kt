package com.kabukabu.driver.features.home.presentation.views.components.map

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

