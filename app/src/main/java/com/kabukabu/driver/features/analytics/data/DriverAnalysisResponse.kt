package com.kabukabu.driver.features.analytics.data

import com.squareup.moshi.Json

// Mirrors driver_app/lib/data/models/driver_analysis.dart (subset needed for UI)

data class DriverAnalysisResponse(
    val status: String?,
    val data: DriverAnalysisData?,
    val message: String?
)

data class DriverAnalysisData(
    @Json(name = "your_earnings") val yourEarnings: YourEarnings? = null,
    @Json(name = "wallet_balance") val walletBalance: Int? = null,
    @Json(name = "due_payments") val duePayments: Int? = null,
    @Json(name = "average_rating") val averageRating: Double? = null,
    @Json(name = "total_trips_data") val totalTripsData: TotalTripsData? = null,
    @Json(name = "trips_chart") val tripsChart: List<TripsChart>? = emptyList(),
    @Json(name = "payment_methods") val paymentMethods: PaymentMethods? = null,
)

data class YourEarnings(
    val current: Any? = null,
    val previous: Any? = null,
    val comment: String? = null
)

data class TotalTripsData(
    @Json(name = "total_distance_in_km") val totalDistanceInKm: Any? = null,
    @Json(name = "total_time_in_minutes") val totalTimeInMinutes: Int? = null,
    @Json(name = "total_trips") val totalTrips: Int? = null
)

data class TripsChart(
    val date: String? = null,
    @Json(name = "no_of_trips") val noOfTrips: Int? = null
)

data class PaymentMethods(
    val total: Int? = null,
    @Json(name = "cash_payment") val cashPayment: Payment? = null,
    @Json(name = "other_payment") val otherPayment: Payment? = null
)

data class Payment(
    val value: Int? = null,
    val percentage: Int? = null
)
