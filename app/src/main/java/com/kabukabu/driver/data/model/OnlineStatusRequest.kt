package com.kabukabu.driver.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OnlineStatusRequest(
    @Json(name = "online_status") val onlineStatus: String
) 