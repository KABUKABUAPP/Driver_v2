package com.kabukabu.driver.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabukabu.driver.data.model.OtpLoginRequest
import com.kabukabu.driver.data.model.OtpLoginResponse
import com.kabukabu.driver.data.remote.ApiClient
import com.kabukabu.driver.utils.LoginUiState
import kotlinx.coroutines.launch
import android.util.Patterns

class LoginViewModel : ViewModel() {
    var uiState: LoginUiState by mutableStateOf(LoginUiState.Idle)
        private set

    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun login(email: String) {
        viewModelScope.launch {
            uiState = LoginUiState.Loading
            try {
                val response = ApiClient.authService.otpLogin(OtpLoginRequest(numberOrEmail = email))
                if (response.status == "success") {
                    uiState = LoginUiState.Success(response)
                } else {
                    uiState = LoginUiState.Error(response.message)
                }
            } catch (e: Exception) {
                uiState = LoginUiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun resetState() {
        uiState = LoginUiState.Idle
    }
} 