package com.kabukabu.driver.features.maps.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DistanceMatrixResponse(
    @Json(name = "destination_addresses") val destinationAddresses: List<String>,
    @Json(name = "origin_addresses") val originAddresses: List<String>,
    @Json(name = "rows") val rows: List<Row>,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class Row(
    @Json(name = "elements") val elements: List<Element>
)

@JsonClass(generateAdapter = true)
data class Element(
    @Json(name = "distance") val distance: Distance?,
    @Json(name = "duration") val duration: Duration?,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class Distance(
    @Json(name = "text") val text: String,
    @Json(name = "value") val value: Int // in meters
)

@JsonClass(generateAdapter = true)
data class Duration(
    @Json(name = "text") val text: String,
    @Json(name = "value") val value: Int // in seconds
)

