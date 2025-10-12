package com.kabukabu.driver.features.auth.data.entity.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UploadCarDetailsResponse(
    @Json(name = "status") val status: String,
    @Json(name = "message") val message: String,
    @Json(name = "data") val data: DriverData
)

@JsonClass(generateAdapter = true)
data class DriverData(
    @Json(name = "user") val user: DriverGuarantorUser,
    @Json(name = "car_details") val carDetails: DriverCarDetails
)

@JsonClass(generateAdapter = true)
data class DriverGuarantorUser(
    @Json(name = "_id") val id: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "email") val email: String,
    @Json(name = "type") val type: String,
    @Json(name = "isBlocked") val isBlocked: Boolean,
    @Json(name = "reason_to_block") val reasonToBlock: String? = null,
    @Json(name = "onboarding_step") val onboardingStep: Int,
    @Json(name = "is_onboarding_complete") val isOnboardingComplete: Boolean,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "__v") val version: Int,
    @Json(name = "accessTokens") val accessTokens: String? = null,
    @Json(name = "driver") val driverId: String? = null,

    // Nested / optional fields
    @Json(name = "preferred_destination") val preferredDestination: DriverPreferredDestination? = null,
    @Json(name = "average_rating") val averageRating: DriverAverageRating? = null,
    @Json(name = "auto_debit_dates") val autoDebitDates: DriverAutoDebitDates? = null,
    @Json(name = "coordinate") val coordinate: List<Double>? = null,
    @Json(name = "current_trip_distance_left") val currentTripDistanceLeft: Int? = null,
    @Json(name = "total_trips") val totalTrips: Int? = null,
    @Json(name = "deleted") val deleted: Boolean? = null,
    @Json(name = "reason_for_delete") val reasonForDelete: String? = null,
    @Json(name = "is_rewarded") val isRewarded: Boolean? = null,
    @Json(name = "reward_signup_bonus") val rewardSignupBonus: Boolean? = null,
    @Json(name = "is_badge_rated") val isBadgeRated: Boolean? = null,
    @Json(name = "favourite_locations") val favouriteLocations: List<Any>? = null,
    @Json(name = "type_of_car") val typeOfCar: String? = null,
    @Json(name = "guarantor_status") val guarantorStatus: String? = null,
    @Json(name = "guarantor_response") val guarantorResponse: Boolean? = null,
    @Json(name = "online_status") val onlineStatus: String? = null
)

@JsonClass(generateAdapter = true)
data class DriverPreferredDestination(
    @Json(name = "is_active") val isActive: Boolean?,
    @Json(name = "coordinate") val coordinate: List<Double>?
)

@JsonClass(generateAdapter = true)
data class DriverAverageRating(
    @Json(name = "value") val value: Double?,
    @Json(name = "count") val count: Int?
)

@JsonClass(generateAdapter = true)
data class DriverAutoDebitDates(
    @Json(name = "trip_charges_date") val tripChargesDate: String?,
    @Json(name = "sharp_payment_date") val sharpPaymentDate: String?,
    @Json(name = "repair_loan_date") val repairLoanDate: String?,
    @Json(name = "is_NIN_verified") val isNinVerified: Boolean?
)

@JsonClass(generateAdapter = true)
data class DriverCarDetails(
    @Json(name = "_id") val id: String,
    @Json(name = "brand_name") val brandName: String,
    @Json(name = "user") val userId: String,
    @Json(name = "driver_owned") val driverOwned: Boolean,
    @Json(name = "model") val model: String,
    @Json(name = "year") val year: String,
    @Json(name = "color") val color: String,
    @Json(name = "images") val images: List<String>,
    @Json(name = "plate_number") val plateNumber: String,
    @Json(name = "isVerified") val isVerified: Boolean,
    @Json(name = "isAvailable") val isAvailable: Boolean? = null,
    @Json(name = "on_a_trip") val onATrip: Boolean? = null,
    @Json(name = "assigned") val assigned: Boolean? = null,
    @Json(name = "active") val active: Boolean? = null,
    @Json(name = "driver") val driverId: String? = null,
    @Json(name = "camera") val camera: DriverCamera? = null,
    @Json(name = "location_tracker") val locationTracker: DriverLocationTracker? = null,
    @Json(name = "pick_up_status") val pickUpStatus: String? = null,
    @Json(name = "sharp_status") val sharpStatus: String? = null,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "__v") val version: Int
)

@JsonClass(generateAdapter = true)
data class DriverCamera(
    @Json(name = "isSynched") val isSynched: Boolean?
)

@JsonClass(generateAdapter = true)
data class DriverLocationTracker(
    @Json(name = "isSynched") val isSynched: Boolean?
)
