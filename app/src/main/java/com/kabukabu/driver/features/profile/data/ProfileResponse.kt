package com.kabukabu.driver.features.profile.data


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProfileResponse(
    @Json(name = "status")
    val status: String?,
    @Json(name = "code")
    val code: Int?,
    @Json(name = "data")
    val data: ProfileData?
)

@JsonClass(generateAdapter = true)
data class ProfileData(
    @Json(name = "user")
    val user: User?=null,
    @Json(name = "car_details")
    val carDetails: CarDetails?=null,
    @Json(name = "documents")
    val documents: List<Document>?= emptyList(),

    @Json(name = "active_trip")
    val activeTrip: ActiveTrip?=null

)

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "_id")
    val id: String?,
    @Json(name = "full_name")
    val fullName: String?,
    @Json(name = "phone_number")
    val phoneNumber: String?,
    @Json(name = "email")
    val email: String?,
    @Json(name = "type")
    val type: String?,
    @Json(name = "isBlocked")
    val isBlocked: Boolean?,
    @Json(name = "reason_to_block")
    val reasonToBlock: String?,
    @Json(name = "is_onboarding_complete")
    val isOnboardingComplete: Boolean?,
    @Json(name = "onboarding_step")
    val onboardingStep: Int?,
    @Json(name = "online_status")
    val onlineStatus: String?,
    @Json(name = "guarantor_status")
    val guarantorStatus: String?,
    @Json(name = "guarantor_response")
    val guarantorResponse: Boolean?,
    @Json(name = "coordinate")
    val coordinate: List<Double>?,
    @Json(name = "current_trip_distance_left")
    val currentTripDistanceLeft: Int?,
    @Json(name = "total_trips")
    val totalTrips: Int?,
    @Json(name = "deleted")
    val deleted: Boolean?,
    @Json(name = "reason_for_delete")
    val reasonForDelete: String?,
    @Json(name = "is_rewarded")
    val isRewarded: Boolean?,
    @Json(name = "reward_signup_bonus")
    val rewardSignupBonus: Boolean?,
    @Json(name = "is_badge_rated")
    val isBadgeRated: Boolean?,
    @Json(name = "favourite_locations")
    val favouriteLocations: List<String>?,
    @Json(name = "created_at")
    val createdAt: String?,
    @Json(name = "updated_at")
    val updatedAt: String?,
    @Json(name = "accessTokens")
    val accessTokens: String?,
    @Json(name = "type_of_car")
    val typeOfCar: String?,
    @Json(name = "profile_image")
    val profileImage: String?,
    @Json(name = "guarantor_unique_code")
    val guarantorUniqueCode: String?,
    @Json(name = "referral_code")
    val referralCode: String?,
    @Json(name = "guarantor")
    val guarantor: Guarantor?,
    @Json(name = "preferred_destination")
    val preferredDestination: PreferredDestination?,
    @Json(name = "average_rating")
    val averageRating: AverageRating?,
    @Json(name = "auto_debit_dates")
    val autoDebitDates: AutoDebitDates?,
    @Json(name = "driver")
    val driver: Driver?
)

@JsonClass(generateAdapter = true)
data class Guarantor(
    @Json(name = "name")
    val name: String?,
    @Json(name = "relationship")
    val relationship: String?,
    @Json(name = "address")
    val address: String?,
    @Json(name = "city")
    val city: String?,
    @Json(name = "state")
    val state: String?,
    @Json(name = "phone_number")
    val phoneNumber: String?,
    @Json(name = "email")
    val email: String?,
    @Json(name = "image")
    val image: String?
)

@JsonClass(generateAdapter = true)
data class PreferredDestination(
    @Json(name = "is_active")
    val isActive: Boolean?,
    @Json(name = "coordinate")
    val coordinate: List<Double>?
)

@JsonClass(generateAdapter = true)
data class AverageRating(
    @Json(name = "value")
    val value: Int?,
    @Json(name = "count")
    val count: Int?
)

@JsonClass(generateAdapter = true)
data class AutoDebitDates(
    @Json(name = "trip_charges_date")
    val tripChargesDate: String?,
    @Json(name = "sharp_payment_date")
    val sharpPaymentDate: String?,
    @Json(name = "repair_loan_date")
    val repairLoanDate: String?,
    @Json(name = "is_NIN_verified")
    val isNINVerified: Boolean?
)

@JsonClass(generateAdapter = true)
data class Driver(
    @Json(name = "preferred_payment_methods")
    val preferredPaymentMethods: PreferredPaymentMethods?,
    @Json(name = "_id")
    val id: String?,
    @Json(name = "user")
    val user: String?,
    @Json(name = "car_owner")
    val carOwner: Boolean?,
    @Json(name = "house_address")
    val houseAddress: String?,
    @Json(name = "city")
    val city: String?,
    @Json(name = "state")
    val state: String?,
    @Json(name = "country")
    val country: String?,
    @Json(name = "total_trips")
    val totalTrips: Int?,
    @Json(name = "isVerified")
    val isVerified: Boolean?,
    @Json(name = "isCameraSynched")
    val isCameraSynched: Boolean?,
    @Json(name = "isLocationTrackerSynched")
    val isLocationTrackerSynched: Boolean?,
    @Json(name = "online_status")
    val onlineStatus: String?,
    @Json(name = "approval_status")
    val approvalStatus: String?,
    @Json(name = "drivers_licence_status")
    val driversLicenceStatus: String?,
    @Json(name = "admin_approval")
    val adminApproval: String?,
    @Json(name = "admin_decline_count")
    val adminDeclineCount: Int?,
    @Json(name = "status_remark")
    val statusRemark: String?,
    @Json(name = "sharp_approval_status")
    val sharpApprovalStatus: String?,
    @Json(name = "sharp_program_type")
    val sharpProgramType: String?,
    @Json(name = "created_at")
    val createdAt: String?,
    @Json(name = "updated_at")
    val updatedAt: String?,
    @Json(name = "current_car")
    val currentCar: String?,
    @Json(name = "assigned_hub_id")
    val assignedHubId: String?,
    @Json(name = "inspector_id")
    val inspectorId: String?,
    @Json(name = "admin_approval_remark")
    val adminApprovalRemark: String?,
    @Json(name = "inspection_code")
    val inspectionCode: String?

)

@JsonClass(generateAdapter = true)
data class PreferredPaymentMethods(
    @Json(name = "cash")
    val cash: Boolean?,
    @Json(name = "wallet")
    val wallet: Boolean?,
    @Json(name = "card")
    val card: Boolean?
)

@JsonClass(generateAdapter = true)
data class PreferredPaymentMethodsRequest(
    @Json(name = "cash")
    val cash: String?,
    @Json(name = "wallet")
    val wallet: String?,
    @Json(name = "card")
    val card: String?
)

@JsonClass(generateAdapter = true)
data class CarDetails(
    @Json(name = "camera")
    val camera: Camera?,
    @Json(name = "location_tracker")
    val locationTracker: LocationTracker?,
    @Json(name = "_id")
    val id: String?,
    @Json(name = "brand_name")
    val brandName: String?,
    @Json(name = "user")
    val user: String?,
    @Json(name = "driver_owned")
    val driverOwned: Boolean?,
    @Json(name = "model")
    val model: String?,
    @Json(name = "year")
    val year: String?,
    @Json(name = "color")
    val color: String?,
    @Json(name = "images")
    val images: List<String>?,
    @Json(name = "plate_number")
    val plateNumber: String?,
    @Json(name = "isVerified")
    val isVerified: Boolean?,
    @Json(name = "isAvailable")
    val isAvailable: Boolean?,
    @Json(name = "on_a_trip")
    val onATrip: Boolean?,
    @Json(name = "assigned")
    val assigned: Boolean?,
    @Json(name = "active")
    val active: Boolean?,
    @Json(name = "coordinate")
    val coordinate: List<Double>?,
    @Json(name = "pick_up_status")
    val pickUpStatus: String?,
    @Json(name = "sharp_status")
    val sharpStatus: String?,
    @Json(name = "created_at")
    val createdAt: String?,
    @Json(name = "updated_at")
    val updatedAt: String?
)

@JsonClass(generateAdapter = true)
data class Camera(
    @Json(name = "isSynched")
    val isSynched: Boolean?
)

@JsonClass(generateAdapter = true)
data class LocationTracker(
    @Json(name = "isSynched")
    val isSynched: Boolean?
)

@JsonClass(generateAdapter = true)
data class Document(
    @Json(name = "_id")
    val id: String?,
    @Json(name = "title")
    val title: String?,
    @Json(name = "url")
    val url: String?,
    @Json(name = "status")
    val status: String?,
    @Json(name = "owner")
    val owner: String?,
    @Json(name = "created_at")
    val createdAt: String?,
    @Json(name = "updated_at")
    val updatedAt: String?
)

//adds on
@JsonClass(generateAdapter = true)
data class ActiveTrip(
    @Json(name = "start_address") val startAddress: AddressDetail,
    @Json(name = "end_address") val endAddress: AddressDetail,
    @Json(name = "rating") val rating: Rating,
    @Json(name = "_id") val id: String,
    @Json(name = "start_point") val startPoint: List<Double>,
    @Json(name = "end_point") val endPoint: List<Double>,
    @Json(name = "start_city") val startCity: String,
    @Json(name = "start_state") val startState: String,
    @Json(name = "distance_in_km") val distanceInKm: Double,
    @Json(name = "duration_in_minutes") val durationInMinutes: Int,
    @Json(name = "kabu_type") val kabuType: String,
    @Json(name = "user") val user: RiderUser, // Rider Details
//    @Json(name = "driver") val driver: DriverUser, // Driver Details
    @Json(name = "car") val car: CarDetails,
    @Json(name = "stop_points") val stopPoints: List<Any>, // Empty list in JSON, using Any
    @Json(name = "number_of_stop_points") val numberOfStopPoints: Int,
    @Json(name = "order") val order: String,
    @Json(name = "price") val price: Int,
    @Json(name = "price_range") val priceRange: List<Int>,
    @Json(name = "surge") val surge: Int,
    @Json(name = "payment_type") val paymentType: String,
    @Json(name = "status") val status: String,
    @Json(name = "is_coupon_applied") val isCouponApplied: Boolean,
    @Json(name = "is_bonus_applied") val isBonusApplied: Boolean,
    @Json(name = "bonus_amount") val bonusAmount: Int,
    @Json(name = "is_rated") val isRated: Boolean,
    @Json(name = "rate_by_driver") val rateByDriver: Boolean,
    @Json(name = "rate_by_rider") val rateByRider: Boolean,
    @Json(name = "trip_type") val tripType: String,
    @Json(name = "price_calculation_type") val priceCalculationType: String,
    @Json(name = "category") val category: String,
    @Json(name = "incoming_passengers") val incomingPassengers: Int,
    @Json(name = "is_main_in_shared") val isMainInShared: Boolean,
    @Json(name = "connecting_trips") val connectingTrips: List<Any>, // Empty list in JSON, using Any
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "updatedAt") val updatedAt: String,
    @Json(name = "__v") val v: Int
)

@JsonClass(generateAdapter = true)
data class TripAddress(
    @Json(name = "full_address") val fullAddress: String?
)

@JsonClass(generateAdapter = true)
data class UserProfile(
    @Json(name = "_id") val id: String,
    @Json(name = "full_name") val fullName: String?,
    @Json(name = "email") val email: String?,
    @Json(name = "online_status") val onlineStatus: String?,
    @Json(name = "profile_image") val profileImage: String?,
    @Json(name = "average_rating") val averageRating: AverageRating?
)

@JsonClass(generateAdapter = true)
data class AddressDetail(
    @Json(name = "country") val country: String,
    @Json(name = "state") val state: String,
    @Json(name = "city") val city: String,
    @Json(name = "street") val street: String,
    @Json(name = "full_address") val fullAddress: String
)


@JsonClass(generateAdapter = true)
data class RiderUser(
    // Nested objects
//    @Json(name = "next_of_kin") val nextOfKin: NextOfKin?,
    @Json(name = "preferred_destination") val preferredDestination: PreferredDestination,
//    @Json(name = "average_rating") val averageRating: Rating,
//    @Json(name = "rating_badge") val ratingBadge: RatingBadge,
    @Json(name = "auto_debit_dates") val autoDebitDates: AutoDebitDates,

    // Top-level fields
    @Json(name = "_id") val id: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "email") val email: String,
    @Json(name = "type") val type: String,
    @Json(name = "isBlocked") val isBlocked: Boolean,
    @Json(name = "reason_to_block") val reasonToBlock: String,
    @Json(name = "is_onboarding_complete") val isOnboardingComplete: Boolean,
    @Json(name = "onboarding_step") val onboardingStep: Int,
    @Json(name = "online_status") val onlineStatus: String,
    @Json(name = "guarantor_status") val guarantorStatus: String,
    @Json(name = "guarantor_response") val guarantorResponse: Boolean,
    @Json(name = "state") val state: String,
    @Json(name = "coordinate") val coordinate: List<Double>,
    @Json(name = "current_trip_distance_left") val currentTripDistanceLeft: Int,
    @Json(name = "total_trips") val totalTrips: Int,
    @Json(name = "deleted") val deleted: Boolean,
    @Json(name = "reason_for_delete") val reasonForDelete: String,
    @Json(name = "is_rewarded") val isRewarded: Boolean,
    @Json(name = "reward_signup_bonus") val rewardSignupBonus: Boolean,
    @Json(name = "is_badge_rated") val isBadgeRated: Boolean,
//    @Json(name = "favourite_locations") val favouriteLocations: List<FavouriteLocation>,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "__v") val v: Int,
    @Json(name = "accessTokens") val accessTokens: String,
    @Json(name = "referral_code") val referralCode: String,
    @Json(name = "location_update_date") val locationUpdateDate: String
)

@JsonClass(generateAdapter = true)
data class Rating(
    @Json(name = "value") val value: Double,
    @Json(name = "count") val count: Int
)