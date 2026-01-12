package com.kabukabu.driver.features.trips.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Mirrors subset of driver_app/lib/data/models/trip_history.dart for listing trips

@JsonClass(generateAdapter = true)
data class TripHistoryResponse(
    val status: String?,
    val data: TripHistoryData?,
    val message: String?
)

@JsonClass(generateAdapter = true)
data class TripHistoryData(
    val data: List<TripItem> = emptyList(),
    val pagination: Pagination? = null
)

@JsonClass(generateAdapter = true)
data class Pagination(
    val pageSize: Int? = null,
    val totalCount: Int? = null,
    val pageCount: Int? = null,
    val currentPage: Int? = null,
    val hasNext: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class Address(
    val country: String? = null,
    val state: String? = null,
    val city: String? = null,
    val street: String? = null
    ,
    @Json(name = "full_address") val fullAddress: String? = null
)

@JsonClass(generateAdapter = true)
data class TripItem(
    @Json(name = "_id") val id: String? = null,
    @Json(name = "start_address") val startAddress: Address? = null,
    @Json(name = "end_address") val endAddress: Address? = null,
    val rating: Rating? = null,
    @Json(name = "start_point") val startPoint: List<Double>? = null,
    @Json(name = "end_point") val endPoint: List<Double>? = null,
    @Json(name = "distance_in_km") val distanceInKm: Double? = null,
    @Json(name = "duration_in_minutes") val durationInMinutes: Int? = null,
    @Json(name = "kabu_type") val kabuType: String? = null,
    val user: UserClass? = null,
    val driver: UserClass? = null,
    val car: Car? = null,
    @Json(name = "stop_points") val stopPoints: List<Any>? = null,
    @Json(name = "number_of_stop_points") val numberOfStopPoints: Int? = null,
    // `order` can be a nested object or just an order id string depending on API surface.
    // Use a flexible Any? type to avoid parse errors; callers can handle string vs object.
    val order: String? = null,
    val price: Int? = null,
    @Json(name = "price_range") val priceRange: List<Int>? = null,
    @Json(name = "payment_type") val paymentType: String? = null,
    val status: String? = null,
    @Json(name = "start_city") val startCity: String? = null,
    @Json(name = "start_state") val startState: String? = null,
    @Json(name = "createdAt") val createdAt: String? = null,
    @Json(name = "updatedAt") val updatedAt: String? = null,
    @Json(name = "start_time") val startTime: String? = null,
    @Json(name = "end_time") val endTime: String? = null,
    @Json(name = "price_details") val priceDetails: PriceDetails? = null,
    @Json(name = "is_rated") val isRated: Boolean? = null,
    @Json(name = "trip_type") val tripType: String? = null,
    @Json(name = "incoming_passengers") val incomingPassengers: Int? = null,
    val category: String? = null,
    @Json(name = "id") val datumId: String? = null,
    @Json(name = "rating_comment") val ratingComment: String? = null
)

@JsonClass(generateAdapter = true)
data class Car(
    val camera: Camera? = null,
    @Json(name = "location_tracker") val locationTracker: Camera? = null,
    @Json(name = "_id") val id: String? = null,
    @Json(name = "brand_name") val brandName: String? = null,
    val user: String? = null,
    @Json(name = "driver_owned") val driverOwned: Boolean? = null,
    val model: String? = null,
    val year: String? = null,
    val color: String? = null,
    val images: List<String>? = null,
    @Json(name = "plate_number") val plateNumber: String? = null,
    @Json(name = "isVerified") val isVerified: Boolean? = null,
    @Json(name = "on_a_trip") val onATrip: Boolean? = null,
    val assigned: Boolean? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null,
    @Json(name = "__v") val v: Int? = null
)

@JsonClass(generateAdapter = true)
data class Camera(
    @Json(name = "isSynched") val isSynched: Boolean? = null,
    @Json(name = "serial_number") val serialNumber: String? = null
)

@JsonClass(generateAdapter = true)
data class UserClass(
    val guarantor: Guarantor? = null,
    @Json(name = "average_rating") val averageRating: Double? = null,
    @Json(name = "_id") val id: String? = null,
    @Json(name = "full_name") val fullName: String? = null,
    @Json(name = "phone_number") val phoneNumber: String? = null,
    val type: String? = null,
    @Json(name = "isBlocked") val isBlocked: Boolean? = null,
    @Json(name = "reason_to_block") val reasonToBlock: String? = null,
    @Json(name = "onboarding_step") val onboardingStep: Int? = null,
    @Json(name = "is_onboarding_complete") val isOnboardingComplete: Boolean? = null,
    @Json(name = "online_status") val onlineStatus: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null,
    @Json(name = "__v") val v: Int? = null,
    val email: String? = null,
    val driver: DriverDriver? = null,
    val accessTokens: String? = null,
    val coordinate: List<Double>? = null,
    @Json(name = "total_trips") val totalTrips: Int? = null,
    @Json(name = "next_of_kin") val nextOfKin: NextOfKin? = null,
    @Json(name = "profile_image") val profileImage: String? = null
)

@JsonClass(generateAdapter = true)
data class Rating(
    // value can be int or float depending on API; use Double to cover both
    val value: Double? = null,
    val count: Int? = null
)

@JsonClass(generateAdapter = true)
data class DriverDriver(
    @Json(name = "preferred_payment_methods") val preferredPaymentMethods: PreferredPaymentMethods? = null,
    @Json(name = "online_status") val onlineStatus: String? = null,
    @Json(name = "_id") val id: String? = null,
    val user: String? = null,
    @Json(name = "car_owner") val carOwner: Boolean? = null,
    @Json(name = "house_address") val houseAddress: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    @Json(name = "total_trips") val totalTrips: Int? = null,
    @Json(name = "isVerified") val isVerified: Boolean? = null,
    @Json(name = "approval_status") val approvalStatus: String? = null,
    @Json(name = "isCameraSynched") val isCameraSynched: Boolean? = null,
    @Json(name = "isLocationTrackerSynched") val isLocationTrackerSynched: Boolean? = null,
    @Json(name = "drivers_licence_status") val driversLicenceStatus: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null,
    @Json(name = "__v") val v: Int? = null,
    @Json(name = "current_car") val currentCar: String? = null,
    @Json(name = "inspection_code") val inspectionCode: String? = null
)

@JsonClass(generateAdapter = true)
data class PreferredPaymentMethods(
    val cash: Boolean? = null,
    val wallet: Boolean? = null,
    val card: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class Guarantor(
    val name: String? = null,
    val relationship: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    @Json(name = "phone_number") val phoneNumber: String? = null,
    val image: String? = null
)

@JsonClass(generateAdapter = true)
data class NextOfKin(
    @Json(name = "full_name") val fullName: String? = null,
    val relationship: String? = null,
    @Json(name = "phone_number") val phoneNumber: String? = null
)

@JsonClass(generateAdapter = true)
data class Order(
    @Json(name = "start_address") val startAddress: Address? = null,
    @Json(name = "end_address") val endAddress: Address? = null,
    @Json(name = "_id") val id: String? = null,
    @Json(name = "start_point") val startPoint: List<Double>? = null,
    @Json(name = "end_point") val endPoint: List<Double>? = null,
    @Json(name = "distance_in_km") val distanceInKm: Double? = null,
    @Json(name = "stop_points") val stopPoints: List<Any>? = null,
    @Json(name = "number_of_stop_points") val numberOfStopPoints: Int? = null,
    @Json(name = "kabu_type") val kabuType: String? = null,
    @Json(name = "payment_type") val paymentType: String? = null,
    val user: String? = null,
    @Json(name = "price_range") val priceRange: List<Int>? = null,
    val currency: String? = null,
    val status: String? = null,
    @Json(name = "card_id") val cardId: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null,
    @Json(name = "__v") val v: Int? = null,
    val driver: String? = null
)

@JsonClass(generateAdapter = true)
data class PriceDetails(
    @Json(name = "booking_fee") val bookingFee: Double? = null,
    @Json(name = "base_fare") val baseFare: Double? = null,
    @Json(name = "distace") val distace: Double? = null,
    val time: Double? = null,
    @Json(name = "wait_time") val waitTime: Double? = null,
    @Json(name = "driver_earned") val driverEarned: Double? = null,
    @Json(name = "kabu_split") val kabuSplit: Double? = null,
    @Json(name = "state_levy") val stateLevy: Double? = null,
    @Json(name = "total_charge") val totalCharge: Double? = null,
    @Json(name = "original_price") val originalPrice: Double? = null,
    @Json(name = "is_coupon_applied") val isCouponApplied: Boolean? = null,
    @Json(name = "bonus_amount") val bonusAmount: Double? = null,
    @Json(name = "is_shared_trip") val isSharedTrip: Boolean? = null,
    @Json(name = "shared_trip_percentage_cut") val sharedTripPercentageCut: Double? = null,
    @Json(name = "tripTime") val tripTime: Double? = null
)
