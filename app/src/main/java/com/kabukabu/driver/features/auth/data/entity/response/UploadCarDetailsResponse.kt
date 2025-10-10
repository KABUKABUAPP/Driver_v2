package com.kabukabu.driver.features.auth.data.entity.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UploadCarDetailsResponse(
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: DriverData,
    @Json(name = "message") val message: String
)

@JsonClass(generateAdapter = true)
data class DriverData(
    @Json(name = "user") val user: User,
    @Json(name = "car_details") val carDetails: CarDetails
)

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "_id") val id: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "email") val email: String,
    @Json(name = "profile_image") val profileImage: String,
    @Json(name = "type") val type: String,
    @Json(name = "isBlocked") val isBlocked: Boolean,
    @Json(name = "reason_to_block") val reasonToBlock: String,
    @Json(name = "onboarding_step") val onboardingStep: Int,
    @Json(name = "is_onboarding_complete") val isOnboardingComplete: Boolean,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "__v") val version: Int,
    @Json(name = "accessTokens") val accessTokens: String,
    @Json(name = "driver") val driverId: String
)

@JsonClass(generateAdapter = true)
data class CarDetails(
    @Json(name = "_id") val id: String,
    @Json(name = "brand_name") val brandName: String,
    @Json(name = "user") val userId: String,
    @Json(name = "driver_owned") val driverOwned: Boolean,
    @Json(name = "model") val model: String,
    @Json(name = "year") val year: String,
    @Json(name = "color") val color: String,
    @Json(name = "images") val images: List<String>,
    @Json(name = "plate_number") val plateNumber: String,
    @Json(name = "driver") val driverId: String,
    @Json(name = "isVerified") val isVerified: Boolean,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "__v") val version: Int
)
