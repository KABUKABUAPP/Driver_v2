package com.kabukabu.driver.features.home.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OnlineStatusRequest(
    @Json(name = "online_status") val onlineStatus: String
) 