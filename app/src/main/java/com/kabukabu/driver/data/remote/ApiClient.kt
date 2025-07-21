package com.kabukabu.driver.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ApiClient {
    private const val AUTH_BASE_URL = "https://rideservice-dev.up.railway.app/"
    private const val RIDE_SERVICE_BASE_URL = "https://rideservice-dev.up.railway.app/"
    private const val API_KEY = "3yBrArNb838bdyIPpLith6dpr0NHCcc66J4AR313"

    private val moshi = Moshi.Builder()
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

    private fun getRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(httpClient)
            .build()
    }

    // Client for Auth services
    val authService: ApiService by lazy {
        getRetrofit(AUTH_BASE_URL).create(ApiService::class.java)
    }

    // Client for Ride services
    val rideService: ApiService by lazy {
        getRetrofit(RIDE_SERVICE_BASE_URL).create(ApiService::class.java)
    }
} 