package com.kabukabu.driver.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SetOnlineStatusRequest(
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class SetOnlineStatusResponse(
    @Json(name = "status") val status: String,
    @Json(name = "message") val message: String,
    @Json(name = "data") val data: OnlineStatusData?
)

@JsonClass(generateAdapter = true)
data class OnlineStatusData(
    @Json(name = "online_status") val onlineStatus: String
) 