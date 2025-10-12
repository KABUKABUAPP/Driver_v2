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
    @Json(name = "new_user") val newUser: NewUser,
    @Json(name = "driver_details") val driverDetails: DriverDetails
    // access_tokens and drivers_licence removed as requested
)

@JsonClass(generateAdapter = true)
data class NewUser(
    @Json(name = "_id") val id: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "email") val email: String,
    @Json(name = "profile_image") val profileImage: String? = null,
    @Json(name = "type") val type: String,
    @Json(name = "isBlocked") val isBlocked: Boolean,
    @Json(name = "reason_to_block") val reasonToBlock: String?,
    @Json(name = "onboarding_step") val onboardingStep: Int,
    @Json(name = "is_onboarding_complete") val isOnboardingComplete: Boolean,
    @Json(name = "online_status") val onlineStatus: String,
    @Json(name = "guarantor_status") val guarantorStatus: String,
    @Json(name = "guarantor_response") val guarantorResponse: Boolean,
    @Json(name = "coordinate") val coordinate: List<Double>,
    @Json(name = "current_trip_distance_left") val currentTripDistanceLeft: Int,
    @Json(name = "total_trips") val totalTrips: Int,
    @Json(name = "deleted") val deleted: Boolean,
    @Json(name = "reason_for_delete") val reasonForDelete: String?,
    @Json(name = "is_rewarded") val isRewarded: Boolean,
    @Json(name = "reward_signup_bonus") val rewardSignupBonus: Boolean,
    @Json(name = "is_badge_rated") val isBadgeRated: Boolean,
    @Json(name = "favourite_locations") val favouriteLocations: List<String>,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "__v") val v: Int,
    @Json(name = "accessTokens") val accessTokens: String,
    @Json(name = "type_of_car") val typeOfCar: String,
    @Json(name = "driver") val driver: String,
    @Json(name = "preferred_destination") val preferredDestination: PreferredDestination,
    @Json(name = "average_rating") val averageRating: DriverDetailsAverageRating,
    @Json(name = "auto_debit_dates") val autoDebitDates: AutoDebitDates
)

@JsonClass(generateAdapter = true)
data class PreferredDestination(
    @Json(name = "is_active") val isActive: Boolean,
    @Json(name = "coordinate") val coordinate: List<Double>
)

@JsonClass(generateAdapter = true)
data class DriverDetailsAverageRating(
    @Json(name = "value") val value: Int,
    @Json(name = "count") val count: Int
)

@JsonClass(generateAdapter = true)
data class AutoDebitDates(
    @Json(name = "trip_charges_date") val tripChargesDate: String,
    @Json(name = "sharp_payment_date") val sharpPaymentDate: String,
    @Json(name = "repair_loan_date") val repairLoanDate: String,
    @Json(name = "is_NIN_verified") val isNINVerified: Boolean
)

@JsonClass(generateAdapter = true)
data class DriverDetails(
    @Json(name = "preferred_payment_methods")
    val preferredPaymentMethods: DriverDetailsPreferredPaymentMethods,
    @Json(name = "_id")
    val id: String,
    @Json(name = "user") val user: String,
    @Json(name = "car_owner") val carOwner: Boolean,
    @Json(name = "house_address") val houseAddress: String,
    @Json(name = "city") val city: String,
    @Json(name = "state") val state: String,
    @Json(name = "country") val country: String,
    @Json(name = "total_trips") val totalTrips: Int,
    @Json(name = "isVerified") val isVerified: Boolean,
    @Json(name = "isCameraSynched") val isCameraSynched: Boolean,
    @Json(name = "isLocationTrackerSynched") val isLocationTrackerSynched: Boolean,
    @Json(name = "online_status") val onlineStatus: String,
    @Json(name = "approval_status") val approvalStatus: String,
    @Json(name = "drivers_licence_status") val driversLicenceStatus: String,
    @Json(name = "admin_approval") val adminApproval: String,
    @Json(name = "admin_decline_count") val adminDeclineCount: Int,
    @Json(name = "status_remark") val statusRemark: String,
    @Json(name = "sharp_approval_status") val sharpApprovalStatus: String,
    @Json(name = "sharp_program_type") val sharpProgramType: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "__v") val v: Int
)

@JsonClass(generateAdapter = true)
data class DriverDetailsPreferredPaymentMethods(
    @Json(name = "cash") val cash: Boolean,
    @Json(name = "wallet") val wallet: Boolean,
    @Json(name = "card") val card: Boolean
)
