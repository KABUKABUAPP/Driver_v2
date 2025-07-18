package com.kabukabu.driver.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OtpLoginRequest(
    @Json(name = "number_or_email") val numberOrEmail: String
)

@JsonClass(generateAdapter = true)
data class OtpLoginResponse(
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: OtpLoginData?,
    @Json(name = "message") val message: String
)

@JsonClass(generateAdapter = true)
data class OtpLoginData(
    @Json(name = "otp") val otp: Int,
    @Json(name = "email") val email: String,
    @Json(name = "token") val token: String,
    @Json(name = "_id") val id: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "__v") val v: Int
) 