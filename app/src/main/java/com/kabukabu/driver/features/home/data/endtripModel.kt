package com.kabukabu.driver.features.home.data
import com.kabukabu.driver.features.profile.data.AddressDetail
import com.kabukabu.driver.features.profile.data.Rating
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class EndTripResponse(
    @Json(name = "status") val status: String?,
    @Json(name = "code") val code: Int?,
    @Json(name = "message") val message: String?,
    @Json(name = "data") val data: EndTripData?
)

@JsonClass(generateAdapter = true)
data class EndTripData(
    @Json(name = "endedTrip") val endedTrip: EndedTrip?,
    @Json(name = "updatedDriver") val updatedDriver: UpdatedDriver?,
    @Json(name = "updatedRider") val updatedRider: UpdatedRider?,
    @Json(name = "paymentDetails") val paymentDetails: PaymentDetails?
)

@JsonClass(generateAdapter = true)
data class EndedTrip(
    @Json(name = "start_address") val startAddress: AddressDetail?,
    @Json(name = "end_address") val endAddress: AddressDetail?,
    @Json(name = "rating") val rating: Rating?,
    @Json(name = "_id") val id: String?,
    @Json(name = "start_point") val startPoint: List<Double>?,
    @Json(name = "end_point") val endPoint: List<Double>?,
    @Json(name = "start_city") val startCity: String?,
    @Json(name = "start_state") val startState: String?,
    @Json(name = "distance_in_km") val distanceInKm: Double?,
    @Json(name = "duration_in_minutes") val durationInMinutes: Int?,
    @Json(name = "kabu_type") val kabuType: String?,
    @Json(name = "user") val user: String?,
    @Json(name = "driver") val driver: String?,
    @Json(name = "car") val car: String?,
    @Json(name = "stop_points") val stopPoints: List<Any>?,
    @Json(name = "number_of_stop_points") val numberOfStopPoints: Int?,
    @Json(name = "order") val order: String?,
    @Json(name = "price") val price: Int?,
    @Json(name = "price_range") val priceRange: List<Int>?,
    @Json(name = "surge") val surge: Int?,
    @Json(name = "payment_type") val paymentType: String?,
    @Json(name = "status") val status: String?,
    @Json(name = "is_coupon_applied") val isCouponApplied: Boolean?,
    @Json(name = "is_bonus_applied") val isBonusApplied: Boolean?,
    @Json(name = "bonus_amount") val bonusAmount: Int?,
    @Json(name = "is_rated") val isRated: Boolean?,
    @Json(name = "rate_by_driver") val rateByDriver: Boolean?,
    @Json(name = "rate_by_rider") val rateByRider: Boolean?,
    @Json(name = "trip_type") val tripType: String?,
    @Json(name = "price_calculation_type") val priceCalculationType: String?,
    @Json(name = "category") val category: String?,
    @Json(name = "incoming_passengers") val incomingPassengers: Int?,
    @Json(name = "is_main_in_shared") val isMainInShared: Boolean?,
    @Json(name = "connecting_trips") val connectingTrips: List<Any>?,
    @Json(name = "createdAt") val createdAt: String?,
    @Json(name = "updatedAt") val updatedAt: String?,
    @Json(name = "__v") val v: Int?,
    @Json(name = "start_time") val startTime: String?,
    @Json(name = "end_time") val endTime: String?,
    @Json(name = "price_details") val priceDetails: PriceDetails?
)

@JsonClass(generateAdapter = true)
data class PriceDetails(
    @Json(name = "booking_fee") val bookingFee: Int?,
    @Json(name = "base_fare") val baseFare: Int?,
    @Json(name = "distace") val distance: Int?, // Handles the "distace" typo in the JSON
    @Json(name = "time") val time: Int?,
    @Json(name = "wait_time") val waitTime: Int?,
    @Json(name = "driver_earned") val driverEarned: Int?,
    @Json(name = "kabu_split") val kabuSplit: Int?,
    @Json(name = "state_levy") val stateLevy: Int?,
    @Json(name = "total_charge") val totalCharge: Int?,
    @Json(name = "original_price") val originalPrice: Int?,
    @Json(name = "is_coupon_applied") val isCouponApplied: Boolean?,
    @Json(name = "bonus_amount") val bonusAmount: Int?,
    @Json(name = "is_shared_trip") val isSharedTrip: Boolean?,
    @Json(name = "shared_trip_percentage_cut") val sharedTripPercentageCut: Int?,
    @Json(name = "tripTime") val tripTime: Int?
)

@JsonClass(generateAdapter = true)
data class UpdatedDriver(
    @Json(name = "_id") val id: String?,
    @Json(name = "total_trips") val totalTrips: Int?
)

@JsonClass(generateAdapter = true)
data class UpdatedRider(
    @Json(name = "_id") val id: String?,
    @Json(name = "total_trips") val totalTrips: Int?
)

@JsonClass(generateAdapter = true)
data class PaymentDetails(
    @Json(name = "order") val order: String?,
    @Json(name = "trip") val trip: String?,
    @Json(name = "type") val type: String?,
    @Json(name = "user") val user: String?,
    @Json(name = "driver") val driver: String?,
    @Json(name = "driver_earned") val driverEarned: Int?,
    @Json(name = "kabu_split") val kabuSplit: Int?,
    @Json(name = "trip_price") val tripPrice: Int?,
    @Json(name = "price_details") val priceDetails: PriceDetails?,
    @Json(name = "status") val status: String?,
    @Json(name = "_id") val id: String?,
    @Json(name = "created_at") val createdAt: String?,
    @Json(name = "updated_at") val updatedAt: String?,
    @Json(name = "__v") val v: Int?
)