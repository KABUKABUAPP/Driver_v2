package com.kabukabu.driver.features.auth.data.entity.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EditDriverProfileResponse(
    @Json(name = "status")
    val status: String,
    @Json(name = "code")
    val code: Int?,
    @Json(name = "data")
    val data: EditUserDataWrapper,
    @Json(name = "message")
    val message: String
)

@JsonClass(generateAdapter = true)
data class EditUserDataWrapper(
    @Json(name = "editedUser")
    val editedUser: EditedUser
)

@JsonClass(generateAdapter = true)
data class EditedUser(
    @Json(name = "_id")
    val id: String,
    @Json(name = "full_name")
    val fullName: String,
    @Json(name = "phone_number")
    val phoneNumber: String,
    @Json(name = "email")
    val email: String,
    @Json(name = "profile_image")
    val profileImage: String?,
    @Json(name = "type")
    val type: String,
    @Json(name = "isBlocked")
    val isBlocked: Boolean,
    @Json(name = "is_onboarding_complete")
    val isOnboardingComplete: Boolean,
    @Json(name = "onboarding_step")
    val onboardingStep: Int?,
    @Json(name = "created_at")
    val createdAt: String,
    @Json(name = "updated_at")
    val updatedAt: String,
    @Json(name = "__v")
    val v: Int?,
    @Json(name = "accessTokens")
    val accessTokens: String?,
    @Json(name = "type_of_car")
    val typeOfCar: String?,
    @Json(name = "driver")
    val driverId: String?,
    @Json(name = "preferred_destination")
    val preferredDestination: EditUserPreferredDestination?,
    @Json(name = "average_rating")
    val averageRating: EditUserAverageRating?,
    @Json(name = "auto_debit_dates")
    val autoDebitDates: EditUserAutoDebitDates?
)

@JsonClass(generateAdapter = true)
data class EditUserPreferredDestination(
    @Json(name = "is_active")
    val isActive: Boolean?,
    @Json(name = "coordinate")
    val coordinate: List<Double>?
)

@JsonClass(generateAdapter = true)
data class EditUserAverageRating(
    @Json(name = "value")
    val value: Double?,
    @Json(name = "count")
    val count: Int?
)

@JsonClass(generateAdapter = true)
data class EditUserAutoDebitDates(
    @Json(name = "trip_charges_date")
    val tripChargesDate: String?,
    @Json(name = "sharp_payment_date")
    val sharpPaymentDate: String?,
    @Json(name = "repair_loan_date")
    val repairLoanDate: String?,
    @Json(name = "is_NIN_verified")
    val isNinVerified: Boolean?
)
