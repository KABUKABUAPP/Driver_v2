package com.kabukabu.driver.data.remote

import com.kabukabu.driver.data.model.ConfirmOtpRequest
import com.kabukabu.driver.data.model.ConfirmOtpResponse
import com.kabukabu.driver.data.model.OtpLoginRequest
import com.kabukabu.driver.data.model.OtpLoginResponse
import com.kabukabu.driver.data.model.OnlineStatusRequest
import com.kabukabu.driver.data.model.OnlineStatusResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface ApiService {
    @POST("auth/otp-login")
    suspend fun otpLogin(@Body request: OtpLoginRequest): OtpLoginResponse

    @POST("auth/confrim-otp")
    suspend fun confirmOtp(@Body request: ConfirmOtpRequest): ConfirmOtpResponse

    @PUT("driver/online_status")
    suspend fun updateOnlineStatus(
        @Header("Authorization") token: String,
        @Header("authid") userId: String,
        @Body request: OnlineStatusRequest
    ): OnlineStatusResponse
} 