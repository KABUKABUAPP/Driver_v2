package com.kabukabu.driver.core.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Retrofit client for Transaction service endpoints (wallet, banks, pin, topup, withdraw).
 * NOTE: TRANSACTION_BASE_URL is set to the same as ride BASE_URL for now. Update if different.
 */
object TransactionApiClient {
    private const val TRANSACTION_BASE_URL = "https://rideservice-dev.up.railway.app/"
    private const val API_KEY = "3yBrArNb838bdyIPpLith6dpr0NHCcc66J4AR313"

    val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val headerInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("x-api-key", API_KEY)
            .build()
        chain.proceed(request)
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(headerInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(TRANSACTION_BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(httpClient)
        .build()

    val service: TransactionService by lazy {
        retrofit.create(TransactionService::class.java)
    }
}
