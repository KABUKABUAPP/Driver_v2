package com.kabukabu.driver.core.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface NotificationService {

    @POST("notification/fcm-token")
    suspend fun updateFcmToken(
        @Header("Authorization") bearerToken: String,
        @Header("authid") userId: String,
        @Body payload: FcmTokenPayload
    ): Response<ResponseBody>
}

/**
 * Payload for updating FCM/OneSignal player ID
 */
data class FcmTokenPayload(
    val playerId: String,
    val platform: String,
    val userId: String,
    val type: String = "driver"
)
