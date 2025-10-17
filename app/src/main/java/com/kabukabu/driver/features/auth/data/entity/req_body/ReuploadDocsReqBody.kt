package com.kabukabu.driver.features.auth.data.entity.req_body

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.File

@JsonClass(generateAdapter = true)
data class ReuploadDocumentReqBody(
    @Json(name = "doc_number")
    val docNumber: String,
    @Json(name = "file")
    val file: File
)
