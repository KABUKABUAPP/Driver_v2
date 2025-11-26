package com.kabukabu.driver.features.auth.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.features.auth.data.ConfirmOtpRequest
import com.kabukabu.driver.features.auth.data.ConfirmOtpResponse
import com.kabukabu.driver.core.data.remote.ApiClient
import com.kabukabu.driver.core.utils.OtpUiState
import kotlinx.coroutines.launch
import android.util.Log

class OtpViewModel : ViewModel() {
    var uiState: OtpUiState by mutableStateOf(OtpUiState.Idle)
        private set
    
    private val userPreferences = KabukabuDriverApp.getInstance().userPreferences

    fun isValidOtp(otp: String): Boolean {
        return otp.length == 4 && otp.all { it.isDigit() }
    }

    fun verifyOtp(otp: String, email: String) {
        viewModelScope.launch {
            uiState = OtpUiState.Loading
            try {
                Log.d("OtpViewModel", "Verifying OTP: $otp for email: $email")
                val response = ApiClient.authService.confirmOtp(
                    ConfirmOtpRequest(
                        otp = otp.toInt(),
                        email = email
                    )
                )
                Log.d("OtpViewModel", "Response: ${response}")

                if (response.status == "success" && response.data?.loggedInUser != null) {
                    val user = response.data.loggedInUser
                    // Save auth token
                    response.data.accessTokens?.let { token ->
                        Log.d("OtpViewModel", "Saving token: $token")
                        userPreferences.saveAuthToken(token)
                    }
                    
                    // Save user email
                    user?.email?.let { email ->
                        Log.d("OtpViewModel", "Saving email: $email")
                        userPreferences.saveUserEmail(email)
                    }

                    // Save user ID
                    user?.id?.let { id ->
                        Log.d("OtpViewModel", "Saving user ID: $id")
                        userPreferences.saveUserId(id)
                    }
                    
                    uiState = OtpUiState.Success(response)
                    Log.d("OtpViewModel", "Success state set")

                }
                else if (response.status == "success" && response.data?.loggedInUser == null) {
//                     Save auth token for user of user not yet created
                    response.data?.accessTokens?.let { token ->
                        Log.d("OtpViewModel", "Saving token: $token")
                        userPreferences.saveAuthToken(token)
                    }
                    uiState = OtpUiState.Success(response)

                    Log.d("OtpViewModel", "Unregistered user")

                } else {
                    Log.e("OtpViewModel", "Error: ${response.message}")
                    uiState = OtpUiState.Error(response.message)
                }

            } catch (e: Exception) {
                Log.e("OtpViewModel", "Exception: ${e.message}", e)
                uiState = OtpUiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun resetState() {
        uiState = OtpUiState.Idle
    }
} 