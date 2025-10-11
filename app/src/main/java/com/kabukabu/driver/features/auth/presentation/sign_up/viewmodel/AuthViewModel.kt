package com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.kabukabu.driver.core.data.local.UserPreferences
import com.kabukabu.driver.core.data.remote.ApiClient
import com.kabukabu.driver.features.auth.data.entity.req_body.UploadCarDetailsReqBody
import com.kabukabu.driver.features.auth.data.entity.req_body.DriverPersonalDetailsReqBody
import com.kabukabu.driver.features.auth.data.entity.req_body.UploadCarDocsReqBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)

    private val _carBrands = MutableStateFlow<List<String>>(emptyList())
    val carBrands: StateFlow<List<String>> = _carBrands.asStateFlow()

    var onboardDriverBiodataUiState: OnboardDriverPersonalDetailsUiState by mutableStateOf(OnboardDriverPersonalDetailsUiState.Idle)
        private set

  var uploadCarDetailsUiState: UploadCarDetailsUiState by mutableStateOf(UploadCarDetailsUiState.Idle)
        private set

  var uploadCarDocsUiState: UploadCarDocsUiState by mutableStateOf(UploadCarDocsUiState.Idle)
        private set

    init {
        fetchCarBrands()
    }


    fun fetchCarBrands() {
        println("fetching car brands")
        val db = FirebaseFirestore.getInstance()
        val reference = db.collection("carbrands").document("t9MZDmH3FWTg3KoKuXs3")

        reference.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val brands = document.get("branditems") as? List<String>
                    brands?.let {
                        _carBrands.value = it
                        println("car brands are ${_carBrands.value}")
                    }
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }


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


    fun uploadCarDetails(uploadCarDetailsReqBody: UploadCarDetailsReqBody) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = userPreferences.authToken.firstOrNull()
            if (token.isNullOrBlank()) {
                Log.e("DriverViewModel", "Cannot upload car details, token is missing.")
                return@launch
            }

            try {
                uploadCarDetailsUiState = UploadCarDetailsUiState.Loading
                val bearerToken = "Bearer $token"
                val textPlain = "text/plain".toMediaTypeOrNull()

                // Prepare text parts
                val carBrand = uploadCarDetailsReqBody.carBrand.toRequestBody(textPlain)
                val carModel = uploadCarDetailsReqBody.carModel.toRequestBody(textPlain)
                val carYear = uploadCarDetailsReqBody.carYear.toRequestBody(textPlain)
                val carColor = uploadCarDetailsReqBody.carColor.toRequestBody(textPlain)
                val carPlateNumber = uploadCarDetailsReqBody.carPlateNumber.toRequestBody(textPlain)

                // Prepare image parts
                val carImages = uploadCarDetailsReqBody.carImages.mapIndexed { index, file ->
                    val imageRequestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData(
                        name = "car_images",
                        filename = file.name,
                        body = imageRequestBody
                    )
                }

                // Make network call
                val response = ApiClient.authService.uploadCarDetails(
                    bearerToken = bearerToken,
                    carBrand = carBrand,
                    carModel = carModel,
                    carYear = carYear,
                    carColor = carColor,
                    carPlateNumber = carPlateNumber,
                    carImages = carImages
                )

                if (response.status == "success") {
                    uploadCarDetailsUiState = UploadCarDetailsUiState.Success(response)
                } else {
                    uploadCarDetailsUiState = UploadCarDetailsUiState.Error(response.message)
                }

            } catch (e: Exception) {
                uploadCarDetailsUiState = UploadCarDetailsUiState.Error(
                    e.message ?: "An unknown error occurred"
                )
                Log.e("DriverViewModel", "Error uploading car details", e)
            }
        }
    }

    fun uploadCarDocs(uploadDriverAndCarDocsReqBody: UploadCarDocsReqBody) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = userPreferences.authToken.firstOrNull()
            if (token.isNullOrBlank()) {
                Log.e("DriverViewModel", "Cannot upload car docs, token is missing.")
                return@launch
            }

            try {
                uploadCarDocsUiState = UploadCarDocsUiState.Loading
                val bearerToken = "Bearer $token"
                val textPlain = "text/plain".toMediaTypeOrNull()

                // Prepare text parts
                val driverLicenceNumber = uploadDriverAndCarDocsReqBody.driverLicenceNumber.toRequestBody(textPlain)
                val carInsuranceNumber = uploadDriverAndCarDocsReqBody.carInsuranceNumber.toRequestBody(textPlain)
                val vehicleLicenceNumber = uploadDriverAndCarDocsReqBody.vehicleLicenceNumber.toRequestBody(textPlain)
                val proofOfOwnershipNumber = uploadDriverAndCarDocsReqBody.proofOfOwnershipNumber.toRequestBody(textPlain)
                val roadWorthinessCertificationNumber = uploadDriverAndCarDocsReqBody.roadWorthinessCertificationNumber.toRequestBody(textPlain)
                val hackneyPermitNumber = uploadDriverAndCarDocsReqBody.hackneyPermitNumber.toRequestBody(textPlain)

                // Prepare file parts
                val driverLicence = MultipartBody.Part.createFormData(
                    name = "driver_licence",
                    filename = uploadDriverAndCarDocsReqBody.driverLicence.name,
                    body = uploadDriverAndCarDocsReqBody.driverLicence.asRequestBody("image/*".toMediaTypeOrNull())
                )

                val vehicleLicence = MultipartBody.Part.createFormData(
                    name = "vehicle_licence",
                    filename = uploadDriverAndCarDocsReqBody.vehicleLicence.name,
                    body = uploadDriverAndCarDocsReqBody.vehicleLicence.asRequestBody("image/*".toMediaTypeOrNull())
                )

                val insuranceCertificate = MultipartBody.Part.createFormData(
                    name = "insurance_certificate",
                    filename = uploadDriverAndCarDocsReqBody.insuranceCertificate.name,
                    body = uploadDriverAndCarDocsReqBody.insuranceCertificate.asRequestBody("image/*".toMediaTypeOrNull())
                )

                val proofOfOwnership = uploadDriverAndCarDocsReqBody.proofOfOwnership?.let { file ->
                    MultipartBody.Part.createFormData(
                        name = "proof_of_ownership",
                        filename = file.name,
                        body = file.asRequestBody("image/*".toMediaTypeOrNull())
                    )
                }

                val roadWorthinessCertification = MultipartBody.Part.createFormData(
                    name = "road_worthiness_certification",
                    filename = uploadDriverAndCarDocsReqBody.roadWorthinessCertification.name,
                    body = uploadDriverAndCarDocsReqBody.roadWorthinessCertification.asRequestBody("image/*".toMediaTypeOrNull())
                )

                val hackneyPermit = uploadDriverAndCarDocsReqBody.hackneyPermit?.let { file ->
                    MultipartBody.Part.createFormData(
                        name = "hackney_permit",
                        filename = file.name,
                        body = file.asRequestBody("image/*".toMediaTypeOrNull())
                    )
                }

                // Make network call
                val response = ApiClient.authService.uploadCarDocs(
                    bearerToken = bearerToken,
                    driverLicenceNumber = driverLicenceNumber,
                    carInsuranceNumber = carInsuranceNumber,
                    vehicleLicenceNumber = vehicleLicenceNumber,
                    proofOfOwnershipNumber = proofOfOwnershipNumber,
                    roadWorthinessCertificationNumber = roadWorthinessCertificationNumber,
                    hackneyPermitNumber = hackneyPermitNumber,
                    driverLicence = driverLicence,
                    vehicleLicence = vehicleLicence,
                    insuranceCertificate = insuranceCertificate,
                    proofOfOwnership = proofOfOwnership,
                    roadWorthinessCertification = roadWorthinessCertification,
                    hackneyPermit = hackneyPermit
                )

                if (response.status == "success") {
                    uploadCarDocsUiState = UploadCarDocsUiState.Success(response)
                } else {
                    uploadCarDocsUiState = UploadCarDocsUiState.Error(response.message)
                }

            } catch (e: Exception) {
                uploadCarDocsUiState = UploadCarDocsUiState.Error(
                    e.message ?: "An unknown error occurred"
                )
                Log.e("DriverViewModel", "Error uploading car docs", e)
            }
        }
    }



    fun resetState() {
        onboardDriverBiodataUiState = OnboardDriverPersonalDetailsUiState.Idle
        uploadCarDetailsUiState = UploadCarDetailsUiState.Idle
    }


}
