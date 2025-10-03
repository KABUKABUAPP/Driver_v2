package com.kabukabu.driver.features.wallet.data

import com.squareup.moshi.Json

// Mirrors driver_app/lib/data/models/due_payment.dart

data class DuePaymentResponse(
    val status: String? = null,
    val data: DuePaymentData? = null,
    val message: String? = null
)

data class DuePaymentData(
    @Json(name = "wallet_balance") val walletBalance: Int? = 0,
    @Json(name = "subscription_payment") val subscriptionPayment: Any? = null,
    @Json(name = "sub_due_date") val subDueDate: String? = null,
    @Json(name = "sharp_payment") val sharpPayment: Any? = null,
    @Json(name = "trip_charge_cap") val tripChargeCap: Any? = null,
    @Json(name = "sharp_due_date") val sharpDueDate: String? = null,
    @Json(name = "ongoing_sharp_program") val ongoingSharpProgram: OngoingSharpProgram? = null,
    val vat: Int? = null,
    @Json(name = "total_payment") val totalPayment: Any? = null,
    @Json(name = "loan_repayment") val loanRepayment: Any? = null,
)

data class OngoingSharpProgram(
    val type: String? = null,
    @Json(name = "no_of_payments_made") val noOfPaymentsMade: Int? = null,
    @Json(name = "total_no_of_payments_to_be_made") val totalNoOfPaymentsToBeMade: Any? = null,
    @Json(name = "due_sharp_payment") val dueSharpPayment: Any? = null,
    @Json(name = "next_sharp_due_date") val nextSharpDueDate: String? = null,
    @Json(name = "percentage_paid") val percentagePaid: Int? = null,
    @Json(name = "total_amount_to_be_paid") val totalAmountToBePaid: Any? = null,
)
