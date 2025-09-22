package com.kabukabu.driver.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProfileResponse(
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: ProfileData?
)

@JsonClass(generateAdapter = true)
data class ProfileData(
    @Json(name = "user") val user: UserProfile?,
    @Json(name = "active_trip") val activeTrip: ActiveTrip?
)

@JsonClass(generateAdapter = true)
data class UserProfile(
    @Json(name = "_id") val id: String,
    @Json(name = "full_name") val fullName: String?,
    @Json(name = "email") val email: String?,
    @Json(name = "online_status") val onlineStatus: String?,
    @Json(name = "profile_image") val profileImage: String?,
    @Json(name = "average_rating") val averageRating: AverageRating?
)

@JsonClass(generateAdapter = true)
data class AverageRating(
    @Json(name = "value") val value: Double?,
    @Json(name = "count") val count: Int?
)

@JsonClass(generateAdapter = true)
data class ActiveTrip(
    @Json(name = "_id") val id: String,
    @Json(name = "status") val status: String?,
    @Json(name = "start_address") val startAddress: TripAddress?,
    @Json(name = "end_address") val endAddress: TripAddress?,
    @Json(name = "price") val price: Double?,
    @Json(name = "start_point") val startPoint: List<Double>?
)

@JsonClass(generateAdapter = true)
data class TripAddress(
    @Json(name = "full_address") val fullAddress: String?
) 