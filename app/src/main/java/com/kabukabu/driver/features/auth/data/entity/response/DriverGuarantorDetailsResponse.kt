package com.kabukabu.driver.features.auth.data.entity.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DriverGuarantorDetailsResponse(
    @Json(name = "status") val status: String,
    @Json(name = "code") val code: Int,
    @Json(name = "data") val data: DriverGuarantorData,
    @Json(name = "message") val message: String
)

@JsonClass(generateAdapter = true)
data class DriverGuarantorData(
    @Json(name = "user") val user: DriverUser
)

@JsonClass(generateAdapter = true)
data class DriverUser(
    @Json(name = "guarantor") val guarantor: GuarantorDetails,
    @Json(name = "average_rating") val averageRating: AverageRating,
    @Json(name = "_id") val id: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "type") val type: String,
    @Json(name = "isBlocked") val isBlocked: Boolean,
    @Json(name = "reason_to_block") val reasonToBlock: String,
    @Json(name = "is_onboarding_complete") val isOnboardingComplete: Boolean,
    @Json(name = "onboarding_step") val onboardingStep: Int,
    @Json(name = "online_status") val onlineStatus: String,
    @Json(name = "guarantor_status") val guarantorStatus: String,
    @Json(name = "guarantor_response") val guarantorResponse: Boolean,
    @Json(name = "coordinate") val coordinate: List<Double>,
    @Json(name = "total_trips") val totalTrips: Int,
    @Json(name = "favourite_locations") val favouriteLocations: List<String>,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "__v") val v: Int,
    @Json(name = "email") val email: String,
    @Json(name = "driver") val driver: String,
    @Json(name = "profile_image") val profileImage: String,
    @Json(name = "accessTokens") val accessTokens: String
)

@JsonClass(generateAdapter = true)
data class GuarantorDetails(
    @Json(name = "name") val name: String,
    @Json(name = "relationship") val relationship: String,
    @Json(name = "address") val address: String,
    @Json(name = "city") val city: String,
    @Json(name = "state") val state: String,
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "email") val email: String,
    @Json(name = "image") val image: String
)

@JsonClass(generateAdapter = true)
data class AverageRating(
    @Json(name = "value") val value: Int,
    @Json(name = "count") val count: Int
)
