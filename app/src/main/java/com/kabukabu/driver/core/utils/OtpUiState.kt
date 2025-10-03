package com.kabukabu.driver.core.utils

import com.kabukabu.driver.features.auth.data.ConfirmOtpResponse

sealed class OtpUiState {
    object Idle : OtpUiState()
    object Loading : OtpUiState()
    data class Success(val response: ConfirmOtpResponse) : OtpUiState()
    data class Error(val message: String) : OtpUiState()
} 