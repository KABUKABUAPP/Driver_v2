package com.kabukabu.driver.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabukabu.driver.data.model.ConfirmOtpRequest
import com.kabukabu.driver.data.model.ConfirmOtpResponse
import com.kabukabu.driver.data.remote.ApiClient
import com.kabukabu.driver.utils.OtpUiState
import kotlinx.coroutines.launch

class OtpViewModel : ViewModel() {
    var uiState: OtpUiState by mutableStateOf(OtpUiState.Idle)
        private set

    fun isValidOtp(otp: String): Boolean {
        return otp.length == 4 && otp.all { it.isDigit() }
    }

    fun verifyOtp(otp: String) {
        viewModelScope.launch {
            uiState = OtpUiState.Loading
            try {
                val response = ApiClient.apiService.confirmOtp(ConfirmOtpRequest(otp = otp.toInt()))
                if (response.status == "success" && response.data?.loggedInUser != null) {
                    uiState = OtpUiState.Success(response)
                } else {
                    uiState = OtpUiState.Error(response.message)
                }
            } catch (e: Exception) {
                uiState = OtpUiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun resetState() {
        uiState = OtpUiState.Idle
    }
} 