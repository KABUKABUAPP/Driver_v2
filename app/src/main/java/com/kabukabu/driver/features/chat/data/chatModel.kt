package com.kabukabu.driver.features.chat.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Minimal chat models matching the Flutter shapes used in the repo.
 * Adjust fields if your server returns additional properties.
 */

@JsonClass(generateAdapter = true)
data class ChatResponse(
    @Json(name = "data")
    val data: List<ChatData> = emptyList(),

    // Optional: pagination/meta fields if present:
    @Json(name = "meta")
    val meta: Meta? = null
)

@JsonClass(generateAdapter = true)
data class Meta(
    @Json(name = "page") val page: Int? = null,
    @Json(name = "limit") val limit: Int? = null,
    @Json(name = "total") val total: Int? = null
)

@JsonClass(generateAdapter = true)
data class ChatData(
    @Json(name = "_id")
    val _id: String? = null,

    // some servers use "id" rather than "_id"
    @Json(name = "id")
    val id: String? = null,

    @Json(name = "room")
    val room: String? = null,

    @Json(name = "room_id")
    val roomId: String? = null,

    @Json(name = "content")
    val content: String? = null,

    @Json(name = "createdAt")
    val createdAt: String? = null, // ISO 8601 date string from backend

    @Json(name = "user")
    val user: ChatUser? = null,

    // Add files/attachments if your server includes them
    @Json(name = "files")
    val files: List<Any>? = null
) {
    // convenience id getter
    fun idOrLocal(): String = _id ?: id ?: java.util.UUID.randomUUID().toString()

    // Convert ISO 8601 date string to timestamp in milliseconds
    fun getCreatedAtTimestamp(): Long {
        return try {
            if (createdAt != null) {
                // Parse ISO 8601 date string (e.g., "2025-12-03T19:31:17.720Z")
                val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                format.timeZone = java.util.TimeZone.getTimeZone("UTC")
                val date = format.parse(createdAt)
                date?.time ?: System.currentTimeMillis()
            } else {
                System.currentTimeMillis()
            }
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }
}

@JsonClass(generateAdapter = true)
data class ChatUser(
    @Json(name = "_id")
    val _id: String? = null,

    @Json(name = "id")
    val id: String? = null,

    @Json(name = "name")
    val name: String? = null,

    @Json(name = "profileImage")
    val profileImage: String? = null,

    @Json(name = "type")
    val type: String? = null  // "driver" or "rider"
)
