package com.kabukabu.driver.features.auth.data.entity.req_body

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.File

@JsonClass(generateAdapter = true)
data class UploadCarDetailsReqBody(
    @Json(name = "car_brand") val carBrand: String,
    @Json(name = "car_model") val carModel: String,
    @Json(name = "car_year") val carYear: String,
    @Json(name = "car_color") val carColor: String,
    @Json(name = "car_plate_number") val carPlateNumber: String,
    @Json(name = "car_images") val carImages: List<File>
)
