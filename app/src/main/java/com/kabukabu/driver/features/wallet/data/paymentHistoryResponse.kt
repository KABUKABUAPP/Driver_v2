package com.kabukabu.driver.features.wallet.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PaymentHistoryResponse(
    @Json(name = "status")
    val status: String? = null,

    @Json(name = "data")
    val data: PaymentHistoryData? = null,

    @Json(name = "message")
    val message: String? = null
)

@JsonClass(generateAdapter = true)
data class PaymentHistoryData(
    @Json(name = "data")
    val data: TransactionData? = null,

    @Json(name = "total")
    val total: Int? = null,

    @Json(name = "currentPage")
    val currentPage: Int? = null,

    @Json(name = "hasNext")
    val hasNext: Boolean? = null,

    @Json(name = "hasPrevious")
    val hasPrevious: Boolean? = null,

    @Json(name = "perPage")
    val perPage: Int? = null,

    @Json(name = "totalPages")
    val totalPages: Int? = null
)

@JsonClass(generateAdapter = true)
data class TransactionData(
    @Json(name = "count")
    val count: Int? = null,

    @Json(name = "rows")
    val rows: List<TransactionRow>? = null
)

@JsonClass(generateAdapter = true)
data class TransactionRow(
    @Json(name = "id")
    val id: Int? = null,

    @Json(name = "amount")
    val amount: Any? = null, // Can be Int, Double, or String

    @Json(name = "createdAt")
    val createdAt: String? = null, // ISO 8601 date string

    @Json(name = "user_id")
    val userId: String? = null,

    @Json(name = "narration")
    val narration: String? = null,

    @Json(name = "type")
    val type: String? = null,

    @Json(name = "status")
    val status: String? = null
)

