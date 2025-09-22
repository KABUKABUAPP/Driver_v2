package com.kabukabu.driver.data.model

import com.squareup.moshi.Json

// Mirrors subset of driver_app/lib/data/models/trip_history.dart for listing trips

data class TripHistoryResponse(
    val status: String?,
    val data: TripHistoryData?,
    val message: String?
)

data class TripHistoryData(
    val data: List<TripItem> = emptyList(),
    val pagination: Pagination? = null
)

data class Pagination(
    val pageSize: Int? = null,
    val totalCount: Int? = null,
    val pageCount: Int? = null,
    val currentPage: Int? = null,
    val hasNext: Boolean? = null
)

data class Address(
    val country: String? = null,
    val state: String? = null,
    val city: String? = null,
    val street: String? = null
)

data class TripItem(
    @Json(name = "_id") val id: String? = null,
    @Json(name = "start_address") val startAddress: Address? = null,
    @Json(name = "end_address") val endAddress: Address? = null,
    @Json(name = "distance_in_km") val distanceInKm: Double? = null,
    val price: Int? = null,
    @Json(name = "payment_type") val paymentType: String? = null,
    val status: String? = null,
    @Json(name = "start_time") val startTime: String? = null,
    @Json(name = "end_time") val endTime: String? = null
)
