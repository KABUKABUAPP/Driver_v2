package com.kabukabu.driver.features.auth.data.entity.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DriverPersonalDetailsResponse(
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: DriverPersonalDetailsData,
    @Json(name = "message") val message: String
)

@JsonClass(generateAdapter = true)
data class DriverPersonalDetailsData(
    @Json(name = "access_tokens") val accessTokens: String,
    @Json(name = "new_user") val newUser: NewUser,
    @Json(name = "driver_details") val driverDetails: DriverDetails,
    @Json(name = "drivers_licence") val driversLicence: DriversLicence
)

@JsonClass(generateAdapter = true)
data class NewUser(
    @Json(name = "_id") val id: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "email") val email: String,
    @Json(name = "profile_image") val profileImage: String?,
    @Json(name = "type") val type: String,
    @Json(name = "isBlocked") val isBlocked: Boolean,
    @Json(name = "reason_to_block") val reasonToBlock: String?,
    @Json(name = "onboarding_step") val onboardingStep: Int,
    @Json(name = "is_onboarding_complete") val isOnboardingComplete: Boolean,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "__v") val v: Int,
    @Json(name = "accessTokens") val accessTokens: String,
    @Json(name = "driver") val driver: String
)

@JsonClass(generateAdapter = true)
data class DriverDetails(
    @Json(name = "user") val user: String,
    @Json(name = "car_owner") val carOwner: Boolean,
    @Json(name = "house_address") val houseAddress: String,
    @Json(name = "city") val city: String,
    @Json(name = "state") val state: String,
    @Json(name = "country") val country: String,
    @Json(name = "isVerified") val isVerified: Boolean,
    @Json(name = "status") val status: String,
    @Json(name = "reason_for_decline") val reasonForDecline: String?,
    @Json(name = "_id") val id: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "__v") val v: Int
)

@JsonClass(generateAdapter = true)
data class DriversLicence(
    @Json(name = "title") val title: String,
    @Json(name = "url") val url: String,
    @Json(name = "owner") val owner: String,
    @Json(name = "_id") val id: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "__v") val v: Int
)
