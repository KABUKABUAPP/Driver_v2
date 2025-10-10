package com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel

import com.kabukabu.driver.features.auth.data.entity.response.DriverPersonalDetailsResponse


sealed class OnboardDriverPersonalDetailsUiState {
    object Idle : OnboardDriverPersonalDetailsUiState()
    object Loading : OnboardDriverPersonalDetailsUiState()
    data class Success(val response: DriverPersonalDetailsResponse) : OnboardDriverPersonalDetailsUiState()
    data class Error(val message: String) : OnboardDriverPersonalDetailsUiState()
}