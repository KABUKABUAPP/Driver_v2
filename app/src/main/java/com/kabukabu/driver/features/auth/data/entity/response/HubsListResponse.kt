package com.kabukabu.driver.features.auth.data.entity.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HubListResponse(
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: List<Hub>,
    @Json(name = "message") val message: String
)

@JsonClass(generateAdapter = true)
data class Hub(
    @Json(name = "_id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "address") val address: String,
    @Json(name = "city") val city: String,
    @Json(name = "state") val state: String,
    @Json(name = "country") val country: String,
    @Json(name = "hub_images") val hubImages: List<String>,
    @Json(name = "inspector") val inspector: String,
    @Json(name = "cars_processed") val carsProcessed: Int,
    @Json(name = "cars_approved") val carsApproved: Int,
    @Json(name = "cars_declined") val carsDeclined: Int,
    @Json(name = "deleted") val deleted: Boolean,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "__v") val v: Int
)
