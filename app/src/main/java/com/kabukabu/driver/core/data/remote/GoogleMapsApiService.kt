package com.kabukabu.driver.core.data.remote

import com.kabukabu.driver.features.maps.data.DistanceMatrixResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API Service for Google Maps APIs
 */
interface GoogleMapsApiService {

    /**
     * Calculate distance and duration between origin and destination using Google Distance Matrix API
     *
     * @param origins Origin coordinates in format "lat,lng" (e.g., "6.5244,3.3792")
     * @param destinations Destination coordinates in format "lat,lng"
     * @param units Unit system to use for distance. Default is "metric"
     * @param mode Mode of transportation. Default is "driving"
     * @param key Google Maps API key
     * @return DistanceMatrixResponse containing distance and duration information
     */
    @GET("distancematrix/json")
    suspend fun getDistanceMatrix(
        @Query("origins") origins: String,
        @Query("destinations") destinations: String,
        @Query("units") units: String = "metric",
        @Query("mode") mode: String = "driving",
        @Query("key") key: String
    ): Response<DistanceMatrixResponse>
}

