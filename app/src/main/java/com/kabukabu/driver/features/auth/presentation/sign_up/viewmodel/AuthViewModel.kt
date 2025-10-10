package com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.kabukabu.driver.core.data.remote.ApiClient
import com.kabukabu.driver.core.utils.LoginUiState
import com.kabukabu.driver.features.auth.data.entity.req_body.DriverPersonalDetailsReqBody
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _carBrands = MutableStateFlow<List<String>>(emptyList())
    val carBrands: StateFlow<List<String>> = _carBrands

    var onboardDriverBiodataUiState: OnboardDriverPersonalDetailsUiState by mutableStateOf(OnboardDriverPersonalDetailsUiState.Idle)
        private set

    init {
        fetchCarBrands()
    }


    fun fetchCarBrands() {
        val db = FirebaseFirestore.getInstance()
        val reference = db.collection("carbrands").document("t9MZDmH3FWTg3KoKuXs3")

        reference.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val brands = document.get("branditems") as? List<String>
                    brands?.let {
                        _carBrands.value = it
                        println("car brands are $carBrands")
                    }
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }


    fun sendDriverBioData(driverPersonalDetailsReqBody: DriverPersonalDetailsReqBody) {
        viewModelScope.launch {
            onboardDriverBiodataUiState = OnboardDriverPersonalDetailsUiState.Loading
            try {
                val response = ApiClient.authService.onboardDriverPersonalDetails(driverPersonalDetailsReqBody)
                if (response.status == "success") {
                    onboardDriverBiodataUiState = OnboardDriverPersonalDetailsUiState.Success(response)
                } else {
                    onboardDriverBiodataUiState = OnboardDriverPersonalDetailsUiState.Error(response.message)
                }
            } catch (e: Exception) {
                onboardDriverBiodataUiState = OnboardDriverPersonalDetailsUiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }


    fun resetState() {
        onboardDriverBiodataUiState = OnboardDriverPersonalDetailsUiState.Idle
    }


}
