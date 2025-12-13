package com.kabukabu.driver.features.home.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TripTodayResponse(
    @Json(name = "status")
    val status: String? = null,

    @Json(name = "data")
    val data: TripTodayData? = null,

    @Json(name = "message")
    val message: String? = null
)

@JsonClass(generateAdapter = true)
data class TripTodayData(
    @Json(name = "trips_for_today")
    val totalTrips: Int? = 0,

    @Json(name = "total_km_for_today")
    val totalKMToday: Double? = 0.0,

    @Json(name = "total_earned_today")
    val totalEarnedToday: Double? = 0.0
)

