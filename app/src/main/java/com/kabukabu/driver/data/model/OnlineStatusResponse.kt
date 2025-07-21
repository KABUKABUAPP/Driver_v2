package com.kabukabu.driver.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OnlineStatusResponse(
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: DriverData?,
    @Json(name = "message") val message: String
)

@JsonClass(generateAdapter = true)
data class DriverData(
    @Json(name = "_id") val id: String,
    @Json(name = "user") val user: String,
    @Json(name = "online_status") val onlineStatus: String,
    @Json(name = "isVerified") val isVerified: Boolean,
    @Json(name = "total_trips") val totalTrips: Int,
    // Add other fields from the response as needed
) 