package com.kabukabu.driver.features.auth.data.entity.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class ErrorResponse(
    @Json(name = "status")
    val status: String? = null,
    @Json(name = "code")
    val code: Int? = null,
    @Json(name = "message")
    val message: String? = null,
    @Json(name = "request_id")
    val requestId: String? = null
)
