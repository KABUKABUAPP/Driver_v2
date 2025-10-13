package com.kabukabu.driver.core.data.remote

import com.kabukabu.driver.features.auth.data.ConfirmOtpRequest
import com.kabukabu.driver.features.auth.data.ConfirmOtpResponse
import com.kabukabu.driver.features.auth.data.OtpLoginRequest
import com.kabukabu.driver.features.auth.data.OtpLoginResponse
import com.kabukabu.driver.features.home.data.OnlineStatusRequest
import com.kabukabu.driver.features.home.data.OnlineStatusResponse
import com.kabukabu.driver.features.home.data.AcceptTripResponse
import com.kabukabu.driver.features.home.data.DeclineTripRequest
import com.kabukabu.driver.features.home.data.DeclineTripResponse
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
import com.kabukabu.driver.features.wallet.data.DuePaymentResponse
import com.kabukabu.driver.features.analytics.data.DriverAnalysisResponse
import com.kabukabu.driver.features.auth.data.entity.response.UploadGuarantorDetailsResponse
import com.kabukabu.driver.features.auth.data.entity.response.DriverPersonalDetailsResponse
import com.kabukabu.driver.features.auth.data.entity.response.EditDriverProfileResponse
import com.kabukabu.driver.features.auth.data.entity.response.UploadCarDetailsResponse
import com.kabukabu.driver.features.auth.data.entity.response.UploadCarDocsResponse
import com.kabukabu.driver.features.trips.data.TripHistoryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.Part

interface ApiService {
    @POST("auth/otp-login")
    suspend fun otpLogin(@Body request: OtpLoginRequest): OtpLoginResponse

    @POST("auth/confrim-otp")
    suspend fun confirmOtp(@Body request: ConfirmOtpRequest): ConfirmOtpResponse


    @Multipart
    @POST("auth/driver-onboard-one")
    suspend fun onboardDriverPersonalDetails(
        @Header("Authorization") bearerToken: String,
        @Part("full_name") fullName: RequestBody,
        @Part("phone_number") phoneNumber: RequestBody,
        @Part("email") email: RequestBody,
        @Part("house_address") houseAddress: RequestBody,
        @Part("city") city: RequestBody,
        @Part("state") state: RequestBody,
        @Part("car_owner") carOwner: RequestBody,
        @Part("car_category") carCategory: RequestBody
    ): DriverPersonalDetailsResponse


    @Multipart
    @PUT("user/edit-profile")
    suspend fun editUserProfile(
        @Header("Authorization") bearerToken: String,

        @Part("full_name") fullName: RequestBody?,
        @Part("phone_number") phoneNumber: RequestBody?,
        @Part("email") email: RequestBody?,

        @Part profile_image: MultipartBody.Part?, // file

        @Part("next_of_kin_full_name") nextOfKinFullName: RequestBody?,
        @Part("next_of_kin_relationship") nextOfKinRelationship: RequestBody?,
        @Part("next_of_kin_phone_number") nextOfKinPhoneNumber: RequestBody?,

        @Part("guarantor_name") guarantorName: RequestBody?,
        @Part("guarantor_address") guarantorAddress: RequestBody?,
        @Part("guarantor_phone_number") guarantorPhoneNumber: RequestBody?,
        @Part guarantor_image: MultipartBody.Part?, // file

        @Part("gender") gender: RequestBody?
    ): EditDriverProfileResponse

    @Multipart
    @POST("auth/driver-onboard-two")
    suspend fun uploadCarDetails(
        @Header("Authorization") bearerToken: String,
        @Part("car_brand") carBrand: RequestBody,
        @Part("car_model") carModel: RequestBody,
        @Part("car_year") carYear: RequestBody,
        @Part("car_color") carColor: RequestBody,
        @Part("car_plate_number") carPlateNumber: RequestBody,
        @Part carImages: List<MultipartBody.Part>
    ): UploadCarDetailsResponse


    @Multipart
    @POST("auth/driver-onboard-three")
    suspend fun uploadCarDocs(
        @Header("Authorization") bearerToken: String,
        @Part("driver_licence_number") driverLicenceNumber: RequestBody,
        @Part("car_insurance_number") carInsuranceNumber: RequestBody,
        @Part("vehicle_licence_number") vehicleLicenceNumber: RequestBody,
        @Part("proof_of_ownership_number") proofOfOwnershipNumber: RequestBody,
        @Part("road_worthiness_certification_number") roadWorthinessCertificationNumber: RequestBody,
        @Part("hackney_permit_number") hackneyPermitNumber: RequestBody,
        @Part driverLicence: MultipartBody.Part,
        @Part vehicleLicence: MultipartBody.Part,
        @Part insuranceCertificate: MultipartBody.Part,
        @Part proofOfOwnership: MultipartBody.Part?,
        @Part roadWorthinessCertification: MultipartBody.Part,
        @Part hackneyPermit: MultipartBody.Part?
    ): UploadCarDocsResponse


    @Multipart
    @POST("auth/driver-onboard-four")
    suspend fun uploadGuarantorDetails(
        @Header("Authorization") bearerToken: String,
        @Part("guarantor_full_name") guarantorFullName: RequestBody,
        @Part("guarantor_relationship") guarantorRelationship: RequestBody,
        @Part("guarantor_house_address") guarantorHouseAddress: RequestBody,
        @Part("guarantor_city") guarantorCity: RequestBody,
        @Part("guarantor_state") guarantorState: RequestBody,
        @Part("guarantor_phone_number") guarantorPhoneNumber: RequestBody,
        @Part("guarantor_email") guarantorEmail: RequestBody,
        @Part("referral_code") referralCode: RequestBody?,
        @Part("sharp_program_type") sharpProgramType: RequestBody?,
        @Part guarantorImage: MultipartBody.Part
    ): UploadGuarantorDetailsResponse



    @GET("user/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<ResponseBody>


    @PUT("driver/online_status")
    suspend fun updateOnlineStatus(
        @Header("Authorization") token: String,
        @Header("authid") userId: String,
        @Body request: OnlineStatusRequest
    ): Response<OnlineStatusResponse>


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