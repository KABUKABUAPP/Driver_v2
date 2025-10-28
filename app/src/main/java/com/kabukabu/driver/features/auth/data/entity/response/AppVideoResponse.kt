package com.kabukabu.driver.features.auth.data.entity.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VideoClipsResponse(
    @Json(name = "clip")
    val clip: String,
    @Json(name = "duration")
    val duration: String,
    @Json(name = "thumbnail")
    val thumbnail: String,
    @Json(name = "title")
    val title: String,

)
