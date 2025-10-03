package com.kabukabu.driver.core.data.socket

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// The entire payload is the trip, so this is our top-level class for this event.
@JsonClass(generateAdapter = true)
data class TripFoundEvent(
    @Json(name = "_id") val eventId: String, // We use the trip's _id as the eventId for acknowledgement
    @Json(name = "status") val status: String,
    @Json(name = "user") val user: User,
    @Json(name = "start_address") val pickupLocation: Address,
    @Json(name = "end_address") val destinationLocation: Address,
    @Json(name = "start_point") val startPoint: List<Double>,
    @Json(name = "price") val fare: Double,
    @Json(name = "distance_in_km") val distance: Double,
    @Json(name = "duration_in_minutes") val duration: Int
)

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "full_name") val fullname: String,
    @Json(name = "profile_image") val profileImage: String? = null,
    @Json(name = "average_rating") val rating: Rating
)

@JsonClass(generateAdapter = true)
data class Rating(
    @Json(name = "value") val value: Double
)

@JsonClass(generateAdapter = true)
data class Address(
    @Json(name = "full_address") val name: String,
    @Json(name = "city") val city: String,
    @Json(name = "state") val state: String
) 