package com.kabukabu.driver.features.auth.data.entity.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EditUserProfileResponse(
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: EditUserData,
    @Json(name = "message") val message: String
)

@JsonClass(generateAdapter = true)
data class EditUserData(
    @Json(name = "next_of_kin") val nextOfKin: NextOfKin?,
    @Json(name = "guarantor") val guarantor: Guarantor?,
    @Json(name = "_id") val id: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "email") val email: String,
    @Json(name = "profile_image") val profileImage: String,
    @Json(name = "type") val type: String,
    @Json(name = "isBlocked") val isBlocked: Boolean,
    @Json(name = "is_onboarding_complete") val isOnboardingComplete: Boolean,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "__v") val v: Int,
    @Json(name = "driver_id") val driverId: String,
    @Json(name = "onboarding_step") val onboardingStep: Int,
    @Json(name = "accessTokens") val accessTokens: String
)

@JsonClass(generateAdapter = true)
data class NextOfKin(
    @Json(name = "relationship") val relationship: String,
    @Json(name = "phone_number") val phoneNumber: String
)

@JsonClass(generateAdapter = true)
data class Guarantor(
    @Json(name = "name") val name: String,
    @Json(name = "address") val address: String,
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "image") val image: String
)
