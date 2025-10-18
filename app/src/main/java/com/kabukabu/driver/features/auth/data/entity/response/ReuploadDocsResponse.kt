package com.kabukabu.driver.features.auth.data.entity.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReUploadDocumentResponse(
    @Json(name = "status") val status: String,
    @Json(name = "code") val code: Int,
    @Json(name = "data") val data: DocumentData,
    @Json(name = "message") val message: String
)

@JsonClass(generateAdapter = true)
data class DocumentData(
    @Json(name = "_id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "url") val url: String,
    @Json(name = "status") val status: String,
    @Json(name = "owner") val owner: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "approved_by") val approvedBy: String,
    @Json(name = "doc_number") val docNumber: String
)

