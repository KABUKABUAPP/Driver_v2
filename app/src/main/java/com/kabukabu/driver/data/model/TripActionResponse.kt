package com.kabukabu.driver.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AcceptTripResponse(
    @Json(name = "status") val status: String,
    @Json(name = "code") val code: Int?,
    @Json(name = "data") val data: AcceptTripData,
    @Json(name = "message") val message: String
)

@JsonClass(generateAdapter = true)
data class AcceptTripData(
    @Json(name = "accepted_order") val acceptedOrder: AcceptedOrder,
    @Json(name = "new_trip") val newTrip: NewTrip
)

@JsonClass(generateAdapter = true)
data class AcceptedOrder(
    @Json(name = "_id") val id: String,
    @Json(name = "status") val status: String
    // Add other fields from the accepted_order object if needed
)

@JsonClass(generateAdapter = true)
data class NewTrip(
    @Json(name = "_id") val id: String,
    @Json(name = "status") val status: String
    // Add other fields from the new_trip object if needed
)

@JsonClass(generateAdapter = true)
data class DeclineTripRequest(
    @Json(name = "reason_for_cancel") val reasonForCancel: String
)

@JsonClass(generateAdapter = true)
data class DeclineTripResponse(
    @Json(name = "status") val status: String,
    @Json(name = "message") val message: String
) 