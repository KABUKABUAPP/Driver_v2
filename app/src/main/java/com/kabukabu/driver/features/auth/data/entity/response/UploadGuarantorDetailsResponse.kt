package com.kabukabu.driver.features.auth.data.entity.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UploadGuarantorDetailsResponse(
    @Json(name = "status") val status: String,
    @Json(name = "code") val code: Int,
    @Json(name = "data") val data: DriverGuarantorData,
    @Json(name = "message") val message: String
)

@JsonClass(generateAdapter = true)
data class DriverGuarantorData(
    @Json(name = "user") val user: DriverGuarantorUserResponse
)

@JsonClass(generateAdapter = true)
data class DriverGuarantorUserResponse(
    @Json(name = "guarantor") val guarantor: GuarantorDetails,
    @Json(name = "preferred_destination") val preferredDestination: GuarantorDetailsPreferredDestination?,
    @Json(name = "average_rating") val averageRating: GuarantorResponseAverageRating,
    @Json(name = "auto_debit_dates") val autoDebitDates: GuarantorDetailsAutoDebitDates?,
    @Json(name = "_id") val id: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "email") val email: String,
    @Json(name = "type") val type: String,
    @Json(name = "isBlocked") val isBlocked: Boolean,
    @Json(name = "reason_to_block") val reasonToBlock: String?,
    @Json(name = "is_onboarding_complete") val isOnboardingComplete: Boolean,
    @Json(name = "onboarding_step") val onboardingStep: Int,
    @Json(name = "online_status") val onlineStatus: String,
    @Json(name = "guarantor_status") val guarantorStatus: String,
    @Json(name = "guarantor_response") val guarantorResponse: Boolean,
    @Json(name = "coordinate") val coordinate: List<Double>?,
    @Json(name = "current_trip_distance_left") val currentTripDistanceLeft: Int?,
    @Json(name = "total_trips") val totalTrips: Int,
    @Json(name = "deleted") val deleted: Boolean?,
    @Json(name = "reason_for_delete") val reasonForDelete: String?,
    @Json(name = "is_rewarded") val isRewarded: Boolean?,
    @Json(name = "reward_signup_bonus") val rewardSignupBonus: Boolean?,
    @Json(name = "is_badge_rated") val isBadgeRated: Boolean?,
    @Json(name = "favourite_locations") val favouriteLocations: List<String>?,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "__v") val v: Int,
    @Json(name = "accessTokens") val accessTokens: String?,
    @Json(name = "type_of_car") val typeOfCar: String?,
    @Json(name = "driver") val driver: String?,
    @Json(name = "guarantor_unique_code") val guarantorUniqueCode: String?,
    @Json(name = "referral_code") val referralCode: String?,
    @Json(name = "profile_image") val profileImage: String? = null // not in response but kept safe
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
data class GuarantorResponseAverageRating(
    @Json(name = "value") val value: Int,
    @Json(name = "count") val count: Int
)

@JsonClass(generateAdapter = true)
data class GuarantorDetailsPreferredDestination(
    @Json(name = "is_active") val isActive: Boolean,
    @Json(name = "coordinate") val coordinate: List<Double>?
)

@JsonClass(generateAdapter = true)
data class GuarantorDetailsAutoDebitDates(
    @Json(name = "trip_charges_date") val tripChargesDate: String?,
    @Json(name = "sharp_payment_date") val sharpPaymentDate: String?,
    @Json(name = "repair_loan_date") val repairLoanDate: String?,
    @Json(name = "is_NIN_verified") val isNinVerified: Boolean?
)
