package com.kabukabu.driver.data.remote

import com.kabukabu.driver.data.model.ConfirmOtpRequest
import com.kabukabu.driver.data.model.ConfirmOtpResponse
import com.kabukabu.driver.data.model.OtpLoginRequest
import com.kabukabu.driver.data.model.OtpLoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/otp-login")
    suspend fun otpLogin(@Body request: OtpLoginRequest): OtpLoginResponse

    @POST("auth/confrim-otp")
    suspend fun confirmOtp(@Body request: ConfirmOtpRequest): ConfirmOtpResponse
} 