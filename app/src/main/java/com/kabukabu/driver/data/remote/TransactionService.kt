package com.kabukabu.driver.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface TransactionService {
    // Wallet
    @GET("transaction/fetch-wallet")
    suspend fun fetchWallet(
        @Header("Authorization") bearerToken: String,
        @Header("authid") userId: String
    ): Response<ResponseBody>

    // Topup
    @POST("transaction/generate-payment-link")
    suspend fun generateTopupLink(
        @Header("Authorization") bearerToken: String,
        @Header("authid") userId: String,
        @Body payload: Map<String, @JvmSuppressWildcards Any>
    ): Response<ResponseBody>

    // Withdraw
    @POST("transaction/withdraw-from-wallet")
    suspend fun withdrawFromWallet(
        @Header("Authorization") bearerToken: String,
        @Header("authid") userId: String,
        @Body payload: Map<String, @JvmSuppressWildcards Any>
    ): Response<ResponseBody>

    // Banks
    @GET("transaction/fetch-banks")
    suspend fun fetchBanks(
        @Header("Authorization") bearerToken: String,
        @Header("authid") userId: String
    ): Response<ResponseBody>

    @POST("transaction/confirm-bank-account")
    suspend fun confirmBankAccount(
        @Header("Authorization") bearerToken: String,
        @Header("authid") userId: String,
        @Body payload: Map<String, @JvmSuppressWildcards Any>
    ): Response<ResponseBody>

    // Pin
    @GET("pin/has-pin")
    suspend fun hasPin(
        @Header("Authorization") bearerToken: String,
        @Header("authid") userId: String
    ): Response<ResponseBody>

    @POST("pin/create")
    suspend fun createPin(
        @Header("Authorization") bearerToken: String,
        @Header("authid") userId: String,
        @Body payload: Map<String, @JvmSuppressWildcards Any>
    ): Response<ResponseBody>

    @POST("pin/change")
    suspend fun changePin(
        @Header("Authorization") bearerToken: String,
        @Header("authid") userId: String,
        @Body payload: Map<String, @JvmSuppressWildcards Any>
    ): Response<ResponseBody>

    @GET("pin/reset-pin")
    suspend fun requestPinReset(
        @Header("Authorization") bearerToken: String,
        @Header("authid") userId: String
    ): Response<ResponseBody>

    @POST("pin/validate-pin-reset")
    suspend fun validatePinReset(
        @Header("Authorization") bearerToken: String,
        @Header("authid") userId: String,
        @Body payload: Map<String, @JvmSuppressWildcards Any>
    ): Response<ResponseBody>
}
