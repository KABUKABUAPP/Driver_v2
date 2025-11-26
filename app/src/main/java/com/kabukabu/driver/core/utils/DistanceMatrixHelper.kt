package com.kabukabu.driver.core.utils

import android.util.Log
import com.kabukabu.driver.core.data.remote.ApiClient
import com.kabukabu.driver.features.maps.data.DistanceMatrixResponse

/**
 * Helper class for calculating distance and time between two coordinates
 * using Google Distance Matrix API
 */
object DistanceMatrixHelper {

    /**
     * Calculate distance and duration between origin and destination
     *
     * @param originLat Origin latitude
     * @param originLng Origin longitude
     * @param destLat Destination latitude
     * @param destLng Destination longitude
     * @return DistanceMatrixResponse or null if request fails
     */
    suspend fun calculateDistanceAndTime(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double
    ): DistanceMatrixResponse? {
        return try {
            val origins = "$originLat,$originLng"
            val destinations = "$destLat,$destLng"

            Log.d("DistanceMatrix", "Calculating distance from $origins to $destinations")

            val response = ApiClient.googleMapsService.getDistanceMatrix(
                origins = origins,
                destinations = destinations,
                units = "metric",
                mode = "driving",
                key = ApiClient.GOOGLE_MAPS_API_KEY
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "OK") {
                    Log.d("DistanceMatrix", "Success: ${body.rows.firstOrNull()?.elements?.firstOrNull()?.distance?.text}")
                    body
                } else {
                    Log.e("DistanceMatrix", "API returned status: ${body?.status}")
                    null
                }
            } else {
                Log.e("DistanceMatrix", "Request failed: ${response.code()} - ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e("DistanceMatrix", "Error calculating distance: ${e.message}", e)
            null
        }
    }

    /**
     * Calculate distance and duration between origin and destination using coordinate arrays
     *
     * @param origin Array of [longitude, latitude]
     * @param destination Array of [longitude, latitude]
     * @return DistanceMatrixResponse or null if request fails
     */
    suspend fun calculateDistanceAndTime(
        origin: List<Double>,
        destination: List<Double>
    ): DistanceMatrixResponse? {
        require(origin.size >= 2) { "Origin must have at least 2 elements [lng, lat]" }
        require(destination.size >= 2) { "Destination must have at least 2 elements [lng, lat]" }

        // Note: Google Maps API expects lat,lng but arrays are usually [lng, lat]
        return calculateDistanceAndTime(
            originLat = origin[1],
            originLng = origin[0],
            destLat = destination[1],
            destLng = destination[0]
        )
    }

    /**
     * Extract distance in meters from response
     */
    fun getDistanceInMeters(response: DistanceMatrixResponse?): Int? {
        return response?.rows?.firstOrNull()?.elements?.firstOrNull()?.distance?.value
    }

    /**
     * Extract distance text (e.g., "5.2 km") from response
     */
    fun getDistanceText(response: DistanceMatrixResponse?): String? {
        return response?.rows?.firstOrNull()?.elements?.firstOrNull()?.distance?.text
    }

    /**
     * Extract duration in seconds from response
     */
    fun getDurationInSeconds(response: DistanceMatrixResponse?): Int? {
        return response?.rows?.firstOrNull()?.elements?.firstOrNull()?.duration?.value
    }

    /**
     * Extract duration text (e.g., "15 mins") from response
     */
    fun getDurationText(response: DistanceMatrixResponse?): String? {
        return response?.rows?.firstOrNull()?.elements?.firstOrNull()?.duration?.text
    }
}

