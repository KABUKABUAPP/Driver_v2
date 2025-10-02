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
import retrofit2.http.Query
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import okhttp3.ResponseBody
import retrofit2.Response
import com.kabukabu.driver.data.model.DuePaymentResponse
import com.kabukabu.driver.data.model.DriverAnalysisResponse
import com.kabukabu.driver.data.model.TripHistoryResponse

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

    // Wallet / Payments
    @GET("payment/my-due-payments")
    suspend fun getDuePayment(
        @Header("Authorization") bearerToken: String,
        @Header("authid") userId: String
    ): DuePaymentResponse

    // Analytics (Flutter uses 'driver/analytcs')
    @GET("driver/analytcs")
    suspend fun getDriverAnalysis(
        @Header("Authorization") bearerToken: String,
        @Header("authid") userId: String,
        @Query("time_frame") timeFrame: String? = null,
        @Query("custom_date") customDate: String? = null
    ): DriverAnalysisResponse

    // Trips (support pagination like Flutter models)
    @GET("trip/my-trips")
    suspend fun getMyTrips(
        @Header("Authorization") bearerToken: String,
        @Header("authid") userId: String,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): TripHistoryResponse

    // Promotions
    @GET("promotions/all-promo")
    suspend fun getPromotions(
        @Header("Authorization") bearerToken: String,
        @Header("authid") userId: String,
        @Query("status") status: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<ResponseBody>

    // Repair Loan
    @GET("repair-loan/get-all")
    suspend fun getRepairLoans(
        @Header("Authorization") bearerToken: String,
        @Header("authid") userId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("repair-loan/request-new")
    suspend fun requestRepairLoan(
        @Header("Authorization") bearerToken: String,
        @Header("authid") userId: String,
        @Field("amount") amount: Int,
        @Field("reason") reason: String
    ): Response<ResponseBody>

    @GET("repair-loan/view")
    suspend fun viewRepairLoan(
        @Header("Authorization") bearerToken: String,
        @Header("authid") userId: String,
        @Query("id") id: String
    ): Response<ResponseBody>

    // Support
    @GET("support/all")
    suspend fun getSupportTickets(
        @Header("Authorization") bearerToken: String,
        @Header("authid") userId: String,
        @Query("status") status: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<ResponseBody>

    @GET("support/view")
    suspend fun viewSupportTicket(
        @Header("Authorization") bearerToken: String,
        @Header("authid") userId: String,
        @Query("support_id") supportId: String
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("support/send_reply")
    suspend fun replySupportTicket(
        @Header("Authorization") bearerToken: String,
        @Header("authid") userId: String,
        @Field("support_id") supportId: String,
        @Field("message") message: String
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("support/open_new_ticket")
    suspend fun openNewSupportTicket(
        @Header("Authorization") bearerToken: String,
        @Header("authid") userId: String,
        @Field("subject") subject: String,
        @Field("message") message: String
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("support/open_ticket_by_trip")
    suspend fun openSupportTicketByTrip(
        @Header("Authorization") bearerToken: String,
        @Header("authid") userId: String,
        @Field("trip_id") tripId: String,
        @Field("subject") subject: String,
        @Field("message") message: String
    ): Response<ResponseBody>
}