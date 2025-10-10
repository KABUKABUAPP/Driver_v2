package com.kabukabu.driver.features.auth.data.entity.req_body

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DriverPersonalDetailsReqBody(
    @Json(name = "full_name") val fullName: String,
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "email") val email: String,
    @Json(name = "house_address") val houseAddress: String,
    @Json(name = "city") val city: String,
    @Json(name = "state") val state: String,
    @Json(name = "car_owner") val carOwner: Boolean = false,
    @Json(name = "car_category") val carCategory: String,

//    @Json(name = "live_picture") val livePicture: String = "",
//    @Json(name = "drivers_licence") val driversLicence: String = ""
)
