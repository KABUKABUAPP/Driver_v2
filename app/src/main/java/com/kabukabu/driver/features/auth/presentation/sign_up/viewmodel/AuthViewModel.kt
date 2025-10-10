package com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.kabukabu.driver.core.data.local.UserPreferences
import com.kabukabu.driver.core.data.remote.ApiClient
import com.kabukabu.driver.core.utils.LoginUiState
import com.kabukabu.driver.features.auth.data.entity.req_body.DriverPersonalDetailsReqBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)

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

//    fun sendDriverBioData(driverPersonalDetailsReqBody: DriverPersonalDetailsReqBody) {
//        viewModelScope.launch(Dispatchers.IO) {
//            val token = userPreferences.authToken.firstOrNull()
//            if (token.isNullOrBlank()) {
//                Log.e("DriverViewModel", "Cannot fetch profile, token is missing.")
//                return@launch
//            }
//
//            try {
//                onboardDriverBiodataUiState = OnboardDriverPersonalDetailsUiState.Loading
//                val bearerToken = "Bearer $token"
//
//                val response = ApiClient.authService.onboardDriverPersonalDetails(
//                    bearerToken = bearerToken,
//                    request = driverPersonalDetailsReqBody
//                )
//                if (response.status == "success") {
//                    onboardDriverBiodataUiState = OnboardDriverPersonalDetailsUiState.Success(response)
//                } else {
//                    onboardDriverBiodataUiState = OnboardDriverPersonalDetailsUiState.Error(response.message)
//                }
//
//            } catch (e: Exception) {
//                onboardDriverBiodataUiState = OnboardDriverPersonalDetailsUiState.Error(
//                    e.message ?: "An unknown error occurred"
//                )
//                Log.e("DriverViewModel", "Error sending biodata", e)
//            }
//        }
//    }


    fun sendDriverBioData(driverPersonalDetailsReqBody: DriverPersonalDetailsReqBody) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = userPreferences.authToken.firstOrNull()
            if (token.isNullOrBlank()) {
                Log.e("DriverViewModel", "Cannot fetch profile, token is missing.")
                return@launch
            }

            try {
                onboardDriverBiodataUiState = OnboardDriverPersonalDetailsUiState.Loading
                val bearerToken = "Bearer $token"

                val textPlain = "text/plain".toMediaTypeOrNull()

                val response = ApiClient.authService.onboardDriverPersonalDetails(
                    bearerToken = bearerToken,
                    fullName = driverPersonalDetailsReqBody.fullName.toRequestBody(textPlain),
                    phoneNumber = driverPersonalDetailsReqBody.phoneNumber.toRequestBody(textPlain),
                    email = driverPersonalDetailsReqBody.email.toRequestBody(textPlain),
                    houseAddress = driverPersonalDetailsReqBody.houseAddress.toRequestBody(textPlain),
                    city = driverPersonalDetailsReqBody.city.toRequestBody(textPlain),
                    state = driverPersonalDetailsReqBody.state.toRequestBody(textPlain),
                    carOwner = driverPersonalDetailsReqBody.carOwner.toString().toRequestBody(textPlain),
                    carCategory = driverPersonalDetailsReqBody.carCategory.toRequestBody(textPlain)
                )

                if (response.status == "success") {
                    onboardDriverBiodataUiState = OnboardDriverPersonalDetailsUiState.Success(response)
                } else {
                    onboardDriverBiodataUiState = OnboardDriverPersonalDetailsUiState.Error(response.message)
                }

            } catch (e: Exception) {
                onboardDriverBiodataUiState = OnboardDriverPersonalDetailsUiState.Error(
                    e.message ?: "An unknown error occurred"
                )
                Log.e("DriverViewModel", "Error sending biodata", e)
            }
        }
    }



    fun resetState() {
        onboardDriverBiodataUiState = OnboardDriverPersonalDetailsUiState.Idle
    }


}
