package com.kabukabu.driver.data.remote

import com.kabukabu.driver.KabukabuDriverApp
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ApiClient {
    private const val BASE_URL = "https://rideservice-dev.up.railway.app/" // Base endpoint
    private const val API_KEY = "3yBrArNb838bdyIPpLith6dpr0NHCcc66J4AR313" // TODO: Replace with your actual API key

    private val userPreferences = KabukabuDriverApp.getInstance().userPreferences

    private val authInterceptor = Interceptor { chain ->
        val token = runBlocking { userPreferences.authToken.first() }
        val userId = runBlocking { userPreferences.userId.first() }

        val requestBuilder = chain.request().newBuilder()

        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }
        userId?.let {
            requestBuilder.addHeader("authid", it)
        }
        requestBuilder.addHeader("x-api-key", API_KEY)

        chain.proceed(requestBuilder.build())
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor(authInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(httpClient)
        .build()

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
} 