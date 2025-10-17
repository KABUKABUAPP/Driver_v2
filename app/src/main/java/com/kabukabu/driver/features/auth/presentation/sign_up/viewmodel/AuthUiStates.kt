package com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel

import com.kabukabu.driver.features.auth.data.entity.response.UploadGuarantorDetailsResponse
import com.kabukabu.driver.features.auth.data.entity.response.DriverPersonalDetailsResponse
import com.kabukabu.driver.features.auth.data.entity.response.EditDriverProfileResponse
import com.kabukabu.driver.features.auth.data.entity.response.ReUploadDocumentResponse
import com.kabukabu.driver.features.auth.data.entity.response.ReuploadGuarantorDetailsResponse
import com.kabukabu.driver.features.auth.data.entity.response.UploadCarDetailsResponse
import com.kabukabu.driver.features.auth.data.entity.response.UploadCarDocsResponse


sealed class OnboardDriverPersonalDetailsUiState {
    object Idle : OnboardDriverPersonalDetailsUiState()
    object Loading : OnboardDriverPersonalDetailsUiState()
    data class Success(val response: DriverPersonalDetailsResponse) : OnboardDriverPersonalDetailsUiState()
    data class Error(val message: String) : OnboardDriverPersonalDetailsUiState()
}

sealed class EditDriverProfileUiState {
    object Idle : EditDriverProfileUiState()
    object Loading : EditDriverProfileUiState()
    data class Success(val response: EditDriverProfileResponse) : EditDriverProfileUiState()
    data class Error(val message: String) : EditDriverProfileUiState()
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

sealed class UploadGuarantorDetailsUiState {
    object Idle : UploadGuarantorDetailsUiState()
    object Loading : UploadGuarantorDetailsUiState()
    data class Success(val response: UploadGuarantorDetailsResponse) : UploadGuarantorDetailsUiState()
    data class Error(val message: String) : UploadGuarantorDetailsUiState()
}

sealed class ReUploadGuarantorDetailsUiState {
    object Idle : ReUploadGuarantorDetailsUiState()
    object Loading : ReUploadGuarantorDetailsUiState()
    data class Success(val response: ReuploadGuarantorDetailsResponse) : ReUploadGuarantorDetailsUiState()
    data class Error(val message: String) : ReUploadGuarantorDetailsUiState()
}

sealed class ReUploadDocUiState {
    object Idle : ReUploadDocUiState()
    object Loading : ReUploadDocUiState()
    data class Success(val response: ReUploadDocumentResponse) : ReUploadDocUiState()
    data class Error(val message: String) : ReUploadDocUiState()
}

