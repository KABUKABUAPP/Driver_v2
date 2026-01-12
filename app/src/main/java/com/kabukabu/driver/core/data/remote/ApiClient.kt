package com.kabukabu.driver.core.data.remote

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "https://rideservice-dev.up.railway.app/"
    private const val GOOGLE_MAPS_BASE_URL = "https://maps.googleapis.com/maps/api/"
//    private const val HUB_BASE_URL = "https://hubservice.up.railway.app/"
    private const val API_KEY = "3yBrArNb838bdyIPpLith6dpr0NHCcc66J4AR313"

    // TODO: Replace with your actual Google Maps API key
    const val GOOGLE_MAPS_API_KEY = "AIzaSyBKw_APHMTRn37FXj0dd7_CptLColGP4Gc"

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

    /**
     * Interceptor that checks for 401 Unauthorized responses
     * and triggers a global auth event to redirect to login
     */
    private val authInterceptor = Interceptor { chain ->
        val request = chain.request()
        val response: Response = chain.proceed(request)

        if (response.code == 401) {
            Log.w("ApiClient", "401 Unauthorized received - triggering logout")
            AuthEventManager.onUnauthorized()
        }

        response
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(3600, TimeUnit.SECONDS)
        .readTimeout(3600, TimeUnit.SECONDS)
        .writeTimeout(3600, TimeUnit.SECONDS)
        .addInterceptor(headerInterceptor)
        .addInterceptor(authInterceptor) // Add auth interceptor to catch 401s
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(httpClient)
        .build()

//    private val hubRetrofit = Retrofit.Builder()
//        .baseUrl(HUB_BASE_URL)
//        .addConverterFactory(MoshiConverterFactory.create(moshi))
//        .client(httpClient)
//        .build()

    val authService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    val rideService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    // Google Maps API client (no authentication headers needed)
    private val googleMapsHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    private val googleMapsRetrofit = Retrofit.Builder()
        .baseUrl(GOOGLE_MAPS_BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(googleMapsHttpClient)
        .build()

    val googleMapsService: GoogleMapsApiService by lazy {
        googleMapsRetrofit.create(GoogleMapsApiService::class.java)
    }

//    val hubService: ApiService by lazy {
//        hubRetrofit.create(ApiService::class.java)
//    }

} 