package com.kabukabu.driver.data.socket

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

sealed class SocketEvent {
    @JsonClass(generateAdapter = true)
    data class TripFound(
        @Json(name = "status") val status: String,
        @Json(name = "eventId") val eventId: String,
        @Json(name = "trip") val trip: TripDetails
    ) : SocketEvent()
}

@JsonClass(generateAdapter = true)
data class TripDetails(
    @Json(name = "_id") val id: String,
    @Json(name = "user") val user: User,
    @Json(name = "pickup_location") val pickupLocation: Location,
    @Json(name = "destination_location") val destinationLocation: Location,
    @Json(name = "fare") val fare: Double,
    @Json(name = "distance") val distance: String,
    @Json(name = "duration") val duration: String
)

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "fullname") val fullname: String,
    @Json(name = "profile_image") val profileImage: String?,
    @Json(name = "rating") val rating: Double
)

@JsonClass(generateAdapter = true)
data class Location(
    @Json(name = "name") val name: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double
) 