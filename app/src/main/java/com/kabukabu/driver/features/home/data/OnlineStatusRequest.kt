package com.kabukabu.driver.features.home.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OnlineStatusRequest(
    @Json(name = "status") val onlineStatus: String
)

@JsonClass(generateAdapter = true)
data class RatingRequest(
    @Json(name = "rating") val rating: Double,
    @Json(name = "comment") val comment: String? = null
)


