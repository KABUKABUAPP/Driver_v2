package com.kabukabu.driver.data.remote

import com.kabukabu.driver.data.model.ConfirmOtpRequest
import com.kabukabu.driver.data.model.ConfirmOtpResponse
import com.kabukabu.driver.data.model.OtpLoginRequest
import com.kabukabu.driver.data.model.OtpLoginResponse
import com.kabukabu.driver.data.model.OnlineStatusRequest
import com.kabukabu.driver.data.model.OnlineStatusResponse
import com.kabukabu.driver.data.model.AcceptTripResponse
import com.kabukabu.driver.data.model.DeclineTripRequest
import com.kabukabu.driver.data.model.DeclineTripResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import okhttp3.ResponseBody
import retrofit2.Response

interface ApiService {
    @POST("auth/otp-login")
    suspend fun otpLogin(@Body request: OtpLoginRequest): OtpLoginResponse

    @POST("auth/confrim-otp")
    suspend fun confirmOtp(@Body request: ConfirmOtpRequest): ConfirmOtpResponse

    @GET("user/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<ResponseBody>

    @PUT("driver/online_status")
    suspend fun updateOnlineStatus(
        @Header("Authorization") token: String,
        @Header("authid") userId: String,
        @Body request: OnlineStatusRequest
    ): OnlineStatusResponse

    @PUT("order/match-order/{orderId}")
    suspend fun acceptTrip(
        @Header("Authorization") bearerToken: String,
        @Header("authorizationToken") rawToken: String,
        @Header("authid") userId: String,
        @Path("orderId") orderId: String
    ): AcceptTripResponse

    @PUT("order/decline-order/{orderId}")
    suspend fun declineTrip(
        @Header("Authorization") bearerToken: String,
        @Header("authorizationToken") rawToken: String,
        @Header("authid") userId: String,
        @Path("orderId") orderId: String,
        @Body request: DeclineTripRequest
    ): DeclineTripResponse
} 