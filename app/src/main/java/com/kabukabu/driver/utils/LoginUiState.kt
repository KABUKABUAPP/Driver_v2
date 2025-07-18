package com.kabukabu.driver.utils

import com.kabukabu.driver.data.model.OtpLoginResponse

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val response: OtpLoginResponse) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
} 