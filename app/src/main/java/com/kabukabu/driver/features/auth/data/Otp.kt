package com.kabukabu.driver.features.auth.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConfirmOtpRequest(
    @Json(name = "otp") val otp: Int,
    @Json(name = "user_type") val userType: String = "driver",
    @Json(name = "email") val email: String? = null
)
//
//@JsonClass(generateAdapter = true)
//data class ConfirmOtpResponse(
//    @Json(name = "status") val status: String,
//    @Json(name = "code") val code: Int?,
//    @Json(name = "data") val data: LoginSuccessData?,
//    @Json(name = "message") val message: String
//)
//
//@JsonClass(generateAdapter = true)
//data class LoginSuccessData(
//    @Json(name = "logged_in_user") val loggedInUser: Driver?,
//    @Json(name = "access_tokens") val accessTokens: String? = "",
//    @Json(name = "pendingUpdates") val pendingUpdates: PendingUpdates?
//)
//
//@JsonClass(generateAdapter = true)
//data class Driver(
//    @Json(name = "_id") val id: String,
//    @Json(name = "full_name") val fullName: String?,
//    @Json(name = "phone_number") val phoneNumber: String?,
//    @Json(name = "email") val email: String?,
//    @Json(name = "type") val type: String?,
//    @Json(name = "isBlocked") val isBlocked: Boolean?,
//    @Json(name = "is_onboarding_complete") val isOnboardingComplete: Boolean?,
//    @Json(name = "online_status") val onlineStatus: String?,
//    @Json(name = "accessTokens") val accessTokens: String?
//)
//
//@JsonClass(generateAdapter = true)
//data class PendingUpdates(
//    @Json(name = "_id") val id: String?,
//    @Json(name = "title") val title: String?,
//    @Json(name = "content") val content: String?,
//    @Json(name = "images") val images: List<String>?,
//    @Json(name = "audience") val audience: String?,
//    @Json(name = "is_active") val isActive: Boolean?,
//    @Json(name = "link") val link: String?,
//    @Json(name = "app_path") val appPath: String?
//)
//



@JsonClass(generateAdapter = true)
data class ConfirmOtpResponse(
    @Json(name = "status") val status: String,
    @Json(name = "code") val code: Int?,
    @Json(name = "data") val data: LoginSuccessData?,
    @Json(name = "message") val message: String
)

@JsonClass(generateAdapter = true)
data class LoginSuccessData(
    @Json(name = "logged_in_user") private val _loggedInUser: Driver?,
    @Json(name = "driver") private val _driver: Driver?,
    @Json(name = "token") private val _token: String?,
    @Json(name = "access_tokens") private val _accessTokens: String?,  // maps to "token"
    @Json(name = "pendingUpdates") val pendingUpdates: PendingUpdates? = null
) {
    val loggedInUser: Driver?
        get() = _loggedInUser ?: _driver

    val accessTokens: String?
        get() = _accessTokens ?: _token
}




@JsonClass(generateAdapter = true)
data class Driver(
    @Json(name = "_id") val id: String?,
    @Json(name = "full_name") val fullName: String?,
    @Json(name = "phone_number") val phoneNumber: String?,
    @Json(name = "email") val email: String?,
    @Json(name = "type") val type: String?,
    @Json(name = "isBlocked") val isBlocked: Boolean?,
    @Json(name = "is_onboarding_complete") val isOnboardingComplete: Boolean?,
    @Json(name = "online_status") val onlineStatus: String?,
    @Json(name = "accessTokens") val accessTokens: String?,
    @Json(name = "preferred_destination") val preferredDestination: PreferredDestination? = null,
    @Json(name = "average_rating") val averageRating: AverageRating? = null,
    @Json(name = "auto_debit_dates") val autoDebitDates: AutoDebitDates? = null,
    @Json(name = "is_NIN_verified") val isNinVerified: Boolean? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class PreferredDestination(
    @Json(name = "is_active") val isActive: Boolean?,
    @Json(name = "coordinate") val coordinate: List<Double>?
)

@JsonClass(generateAdapter = true)
data class AverageRating(
    @Json(name = "value") val value: Double?,
    @Json(name = "count") val count: Int?
)

@JsonClass(generateAdapter = true)
data class AutoDebitDates(
    @Json(name = "trip_charges_date") val tripChargesDate: String?,
    @Json(name = "sharp_payment_date") val sharpPaymentDate: String?,
    @Json(name = "repair_loan_date") val repairLoanDate: String?
)

@JsonClass(generateAdapter = true)
data class PendingUpdates(
    @Json(name = "_id") val id: String?,
    @Json(name = "title") val title: String?,
    @Json(name = "content") val content: String?,
    @Json(name = "images") val images: List<String>?,
    @Json(name = "audience") val audience: String?,
    @Json(name = "is_active") val isActive: Boolean?,
    @Json(name = "link") val link: String?,
    @Json(name = "app_path") val appPath: String?
)
