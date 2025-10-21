package com.kabukabu.driver.core.data.remote

import android.util.Log
import com.kabukabu.driver.features.auth.data.entity.response.ErrorResponse
import com.squareup.moshi.Moshi
import retrofit2.HttpException

object HttpExceptionUtil {

    fun parseHttpError(e: HttpException): String {
        return try {
            // Extract raw error
            val errorBody = e.response()?.errorBody()?.string()

            if (errorBody.isNullOrBlank()) {
                return "An unexpected server error occurred."
            }

            val moshi = Moshi.Builder()
                .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()
            val adapter = moshi.adapter(ErrorResponse::class.java)
            val errorResponse = try {
                adapter.fromJson(errorBody)
            } catch (parseEx: Exception) {
                Log.e("HttpExceptionUtil", "Failed to parse error JSON, fallback to regex", parseEx)
                null
            }

            val messageFromRaw = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"")
                .find(errorBody)?.groupValues?.getOrNull(1)

            errorResponse?.message ?: messageFromRaw ?: "An unexpected server error occurred."

        } catch (parseError: Exception) {
            Log.e("HttpExceptionUtil", "Error parsing HttpException", parseError)
            "An unexpected error occurred."
        }
    }


    fun parseExceptionError(e: Exception): String {
        return try {
            if (e is HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val moshi = Moshi.Builder().build()
                val adapter = moshi.adapter(ErrorResponse::class.java)
                val errorResponse = adapter.fromJson(errorBody ?: "")
                errorResponse?.message ?: "An unexpected server error occurred."
            } else {
                e.message ?: "An unexpected error occurred."
            }
        } catch (parseError: Exception) {
            Log.e("HttpExceptionUtil", "Failed to parse error body", parseError)
            "An unexpected error occurred."
        }
    }


}
