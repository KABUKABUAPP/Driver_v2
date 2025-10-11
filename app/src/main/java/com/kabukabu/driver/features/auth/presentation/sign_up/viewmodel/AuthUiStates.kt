package com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel

import com.kabukabu.driver.features.auth.data.entity.response.DriverPersonalDetailsResponse
import com.kabukabu.driver.features.auth.data.entity.response.UploadCarDetailsResponse
import com.kabukabu.driver.features.auth.data.entity.response.UploadCarDocsResponse


sealed class OnboardDriverPersonalDetailsUiState {
    object Idle : OnboardDriverPersonalDetailsUiState()
    object Loading : OnboardDriverPersonalDetailsUiState()
    data class Success(val response: DriverPersonalDetailsResponse) : OnboardDriverPersonalDetailsUiState()
    data class Error(val message: String) : OnboardDriverPersonalDetailsUiState()
}


sealed class UploadCarDetailsUiState {
    object Idle : UploadCarDetailsUiState()
    object Loading : UploadCarDetailsUiState()
    data class Success(val response: UploadCarDetailsResponse) : UploadCarDetailsUiState()
    data class Error(val message: String) : UploadCarDetailsUiState()
}

sealed class UploadCarDocsUiState {
    object Idle : UploadCarDocsUiState()
    object Loading : UploadCarDocsUiState()
    data class Success(val response: UploadCarDocsResponse) : UploadCarDocsUiState()
    data class Error(val message: String) : UploadCarDocsUiState()
}

