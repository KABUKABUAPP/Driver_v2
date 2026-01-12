//package com.kabukabu.driver.features.analytics.data
//
//data class AnalyticsResponse(
//    val data: AnalyticsData
//)
//
//data class AnalyticsData(
//    val your_earnings: Earnings2,
//    val wallet_balance: Double,
//    val due_payments: Double,
//    val average_rating: Double,
//    val total_trips_data: TripStats,
//    val trips_chart: List<TripDay>,
//    val payment_methods: PaymentMethods2
//)
//
//data class Earnings2(val current: Double, val previous: Double, val comment: String)
//data class TripStats(val total_distance_in_km: Double, val total_time_in_minutes: Int, val total_trips: Int)
//data class TripDay(val date: String, val no_of_trips: Int)
//data class PaymentMethods2(
//    val total: Int,
//    val cash_payment: PaymentDetail,
//    val other_payment: PaymentDetail
//)
//data class PaymentDetail(val value: Int, val percentage: Double)