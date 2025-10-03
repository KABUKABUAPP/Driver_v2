package com.kabukabu.driver.core.utils

import com.kabukabu.driver.features.auth.data.OtpLoginResponse

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val response: OtpLoginResponse) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
} 