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
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.components.utils_functions.toMultipartPart
import com.kabukabu.driver.core.data.local.UserPreferences
import com.kabukabu.driver.core.data.remote.ApiClient
import com.kabukabu.driver.core.data.remote.HttpExceptionUtil
import com.kabukabu.driver.core.data.remote.hub.HubApiClient
import com.kabukabu.driver.features.auth.data.entity.req_body.ReUploadDocumentReqBody
import com.kabukabu.driver.features.auth.data.entity.req_body.ReUploadGuarantorDetailsReqBody
import com.kabukabu.driver.features.auth.data.entity.req_body.UploadCarDetailsReqBody
import com.kabukabu.driver.features.auth.data.entity.req_body.UploadPersonalDetailsReqBody
import com.kabukabu.driver.features.auth.data.entity.req_body.UploadCarDocsReqBody
import com.kabukabu.driver.features.auth.data.entity.req_body.UploadGuarantorDetailsReqBody
import com.kabukabu.driver.features.auth.data.entity.response.InspectionHubsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File

class AuthViewModel : ViewModel() {

//    private val userPreferences = UserPreferences(application)
    val userPreferences = KabukabuDriverApp.getInstance().userPreferences

    private val _carBrands = MutableStateFlow<List<String>>(emptyList())
    val carBrands: StateFlow<List<String>> = _carBrands.asStateFlow()

    private val _inspectionsHubs = MutableStateFlow<InspectionHubsResponse?>(null)
    val inspectionsHubs: StateFlow<InspectionHubsResponse?> = _inspectionsHubs.asStateFlow()

    private val _uploadPersonalDetailsReqBody = MutableStateFlow<UploadPersonalDetailsReqBody?>(null)
    val uploadPersonalDetailsReqBody: StateFlow<UploadPersonalDetailsReqBody?> = _uploadPersonalDetailsReqBody.asStateFlow()

    var onboardDriverBiodataUiState: OnboardDriverPersonalDetailsUiState by mutableStateOf(
        OnboardDriverPersonalDetailsUiState.Idle
    )
        private set

    var editDriverProfileUiState: EditDriverProfileUiState by mutableStateOf(
        EditDriverProfileUiState.Idle
    )
        private set

    var uploadCarDetailsUiState: UploadCarDetailsUiState by mutableStateOf(UploadCarDetailsUiState.Idle)
        private set

    var uploadCarDocsUiState: UploadCarDocsUiState by mutableStateOf(UploadCarDocsUiState.Idle)
        private set

    var uploadGuarantorDetailsUiState: UploadGuarantorDetailsUiState by mutableStateOf(
        UploadGuarantorDetailsUiState.Idle
    )
        private set

    var reUploadGuarantorDetailsUiState: ReUploadGuarantorDetailsUiState by mutableStateOf(ReUploadGuarantorDetailsUiState.Idle)
        private set

    var reUploadDocUiState: ReUploadDocUiState by mutableStateOf(ReUploadDocUiState.Idle)
        private set


    init {
        fetchCarBrands()
        fetchHubs("Lagos")
    }

    fun setUploadUserDetailsReqBody(uploadPersonalDetailsReqBody: UploadPersonalDetailsReqBody) {
        _uploadPersonalDetailsReqBody.value = uploadPersonalDetailsReqBody.copy()
    }

    //fetch car brands from firebase
    fun fetchCarBrands() {
        viewModelScope.launch(Dispatchers.IO) {
            val db = FirebaseFirestore.getInstance()
            val reference = db.collection("carbrands").document("t9MZDmH3FWTg3KoKuXs3")

            reference.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val brands = document.get("branditems") as? List<String>
                        brands?.let {
                            _carBrands.value = it
                        }
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }

        }

    }

    fun fetchHubs(state: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = HubApiClient.hubService.fetchHubs(state)
                _inspectionsHubs.value = response
            } catch (e: Exception) {
                Log.e("HubViewModel", "Error fetching hubs", e)
            }
        }
    }


    fun uploadDriverBioData(driverPersonalDetailsReqBody: UploadPersonalDetailsReqBody) {
        Log.i("AuthViewModel", "uploadDriverBioData called..........")
        viewModelScope.launch(Dispatchers.Main) {
            val token = userPreferences.authToken.firstOrNull()
            if (token.isNullOrBlank()) {
                Log.e("AuthViewModel", "Cannot fetch profile, token is missing.")
                return@launch
            }
            try {
                onboardDriverBiodataUiState = OnboardDriverPersonalDetailsUiState.Loading
                val bearerToken = "Bearer $token"
                //add log statement for driverPersonalDetailsReqBody here
                Log.i("AuthViewModel", "DriverPersonalDetailsReqBody:..... $driverPersonalDetailsReqBody")
                val textPlain = "text/plain".toMediaTypeOrNull()
                Log.i("req-body", "$driverPersonalDetailsReqBody.")
                val response = ApiClient.authService.onboardDriverPersonalDetails(
                    bearerToken = bearerToken,
                    fullName = driverPersonalDetailsReqBody.fullName.toRequestBody(textPlain),
                    phoneNumber = driverPersonalDetailsReqBody.phoneNumber.toRequestBody(textPlain),
                    email = driverPersonalDetailsReqBody.email.toRequestBody(textPlain),
                    houseAddress = driverPersonalDetailsReqBody.houseAddress.toRequestBody(textPlain),
                    city = driverPersonalDetailsReqBody.city.toRequestBody(textPlain),
                    state = driverPersonalDetailsReqBody.state.toRequestBody(textPlain),
                    carOwner = driverPersonalDetailsReqBody.carOwner.toString()
                        .toRequestBody(textPlain),
                    carCategory = driverPersonalDetailsReqBody.carCategory.toRequestBody(textPlain)
                )

                if (response.status == "success") {
                    onboardDriverBiodataUiState =
                        OnboardDriverPersonalDetailsUiState.Success(response)
//                    userPreferences.saveOnboardingStep(response.data.newUser.onboardingStep)
                    userPreferences.saveOnboardingStep(2)
                } else {
                    onboardDriverBiodataUiState =
                        OnboardDriverPersonalDetailsUiState.Error(response.message)
                }

            } catch (e: Exception) {
                onboardDriverBiodataUiState = OnboardDriverPersonalDetailsUiState.Error(
                    e.message ?: "An unknown error occurred"
                )
                Log.e("AuthViewModel", "Error sending biodata", e)
            }
        }
    }


    fun updateUserImage(imageFile: File?) {
        if (imageFile == null) {
            Log.e("ProfileViewModel", "Image file is null, skipping upload.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val token = userPreferences.authToken.firstOrNull()
            if (token.isNullOrBlank()) {
                Log.e("ProfileViewModel", "Cannot upload image, token missing.")
                return@launch
            }
            try {
                editDriverProfileUiState = EditDriverProfileUiState.Loading
                val bearerToken = "Bearer $token"

                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val profileImagePart = MultipartBody.Part.createFormData(
                    "profile_image",
                    imageFile.name,
                    requestFile
                )

                val response = ApiClient.authService.editUserProfile(
                    bearerToken = bearerToken,
                    fullName = null,
                    phoneNumber = null,
                    email = null,
                    profile_image = profileImagePart,
                    nextOfKinFullName = null,
                    nextOfKinRelationship = null,
                    nextOfKinPhoneNumber = null,
                    guarantorName = null,
                    guarantorAddress = null,
                    guarantorPhoneNumber = null,
                    guarantor_image = null,
                    gender = null
                )

                if (response.status == "success") {
                    editDriverProfileUiState = EditDriverProfileUiState.Success(response)
                    userPreferences.saveOnboardingStep(3)
//                    userPreferences.saveOnboardingStep(response.data.editedUser.onboardingStep)
                } else {
                    editDriverProfileUiState = EditDriverProfileUiState.Error(response.message)
                }

            } catch (e: Exception) {
                editDriverProfileUiState = EditDriverProfileUiState.Error(
                    e.message ?: "Unexpected error occurred"
                )
                Log.e("ProfileViewModel", "Error uploading image", e)
            }
        }
    }


    fun uploadCarDetails(uploadCarDetailsReqBody: UploadCarDetailsReqBody) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = userPreferences.authToken.firstOrNull()
            if (token.isNullOrBlank()) {
                Log.e("AuthViewModel", "Cannot upload car details, token is missing.")
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
                    userPreferences.saveOnboardingStep(4)
//                    userPreferences.saveOnboardingStep(response.data.user.onboardingStep)
                } else {
                    uploadCarDetailsUiState = UploadCarDetailsUiState.Error(response.message)
                }

            } catch (e: Exception) {
                uploadCarDetailsUiState = UploadCarDetailsUiState.Error(
                    e.message ?: "An unknown error occurred"
                )
                Log.e("AuthViewModel", "Error uploading car details", e)
            }
        }
    }

    fun uploadCarDocs(uploadCarDocsReqBody: UploadCarDocsReqBody) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = userPreferences.authToken.firstOrNull()
            if (token.isNullOrBlank()) {
                Log.e("AuthViewModel", "Cannot upload car docs, token is missing.")
                return@launch
            }

            try {
                uploadCarDocsUiState = UploadCarDocsUiState.Loading
                val bearerToken = "Bearer $token"
                val textPlain = "text/plain".toMediaTypeOrNull()

                // Prepare text parts
                val driverLicenceNumber =
                    uploadCarDocsReqBody.driverLicenceNumber?.toRequestBody(textPlain)
                val carInsuranceNumber =
                    uploadCarDocsReqBody.carInsuranceNumber?.toRequestBody(textPlain)
                val vehicleLicenceNumber =
                    uploadCarDocsReqBody.vehicleLicenceNumber?.toRequestBody(textPlain)
                val proofOfOwnershipNumber =
                    uploadCarDocsReqBody.proofOfOwnershipNumber?.toRequestBody(textPlain)
                val roadWorthinessCertificationNumber =
                    uploadCarDocsReqBody.roadWorthinessCertificationNumber?.toRequestBody(textPlain)
                val hackneyPermitNumber =
                    uploadCarDocsReqBody.hackneyPermitNumber?.toRequestBody(textPlain)

                // Prepare file parts using the helper
                val driverLicence =
                    uploadCarDocsReqBody.driverLicence.toMultipartPart("driver_licence")
                val vehicleLicence =
                    uploadCarDocsReqBody.vehicleLicence.toMultipartPart("vehicle_licence")
                val insuranceCertificate =
                    uploadCarDocsReqBody.insuranceCertificate.toMultipartPart("insurance_certificate")
                val proofOfOwnership =
                    uploadCarDocsReqBody.proofOfOwnership.toMultipartPart("proof_of_ownership")
                val roadWorthinessCertification =
                    uploadCarDocsReqBody.roadWorthinessCertification.toMultipartPart("road_worthiness_certificate")
                val hackneyPermit =
                    uploadCarDocsReqBody.hackneyPermit.toMultipartPart("hackney_permit")

                // Make network call
                val response = ApiClient.authService.uploadCarDocs(
                    bearerToken = bearerToken,
                    driverLicenceNumber = driverLicenceNumber,
                    carInsuranceNumber = carInsuranceNumber,
                    vehicleLicenceNumber = vehicleLicenceNumber,
                    proofOfOwnershipNumber = proofOfOwnershipNumber,
                    roadWorthinessCertificationNumber = roadWorthinessCertificationNumber,
                    hackneyPermitNumber = hackneyPermitNumber,
                    driverLicence = driverLicence!!, // required
                    vehicleLicence = vehicleLicence!!, // required
                    insuranceCertificate = insuranceCertificate!!, // required
                    proofOfOwnership = proofOfOwnership, // optional
                    roadWorthinessCertification = roadWorthinessCertification!!, // required
                    hackneyPermit = hackneyPermit // optional
                )

                if (response.status == "success") {
                    uploadCarDocsUiState = UploadCarDocsUiState.Success(response)
                    userPreferences.saveOnboardingStep(5)
//                    userPreferences.saveOnboardingStep(response.data.user.onboardingStep)

                } else {
                    uploadCarDocsUiState = UploadCarDocsUiState.Error(response.status)
                }

            } catch (e: Exception) {
                uploadCarDocsUiState = UploadCarDocsUiState.Error(
                    e.message ?: "An unknown error occurred"
                )
                Log.e("AuthViewModel", "Error uploading car docs", e)
            }
        }
    }

    fun uploadGuarantorDetails(uploadGuarantorDetailsReqBody: UploadGuarantorDetailsReqBody) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = userPreferences.authToken.firstOrNull()
            if (token.isNullOrBlank()) {
                Log.e("AuthViewModel", "Cannot upload guarantor details, token is missing.")
                return@launch
            }

            try {
                uploadGuarantorDetailsUiState = UploadGuarantorDetailsUiState.Loading
                val bearerToken = "Bearer $token"
                val textPlain = "text/plain".toMediaTypeOrNull()

                // Prepare text parts
                val fullName =
                    uploadGuarantorDetailsReqBody.guarantorFullName.toRequestBody(textPlain)
                val relationship =
                    uploadGuarantorDetailsReqBody.guarantorRelationship.toRequestBody(textPlain)
                val houseAddress =
                    uploadGuarantorDetailsReqBody.guarantorHouseAddress.toRequestBody(textPlain)
                val city = uploadGuarantorDetailsReqBody.guarantorCity.toRequestBody(textPlain)
                val state = uploadGuarantorDetailsReqBody.guarantorState.toRequestBody(textPlain)
                val phoneNumber =
                    uploadGuarantorDetailsReqBody.guarantorPhoneNumber.toRequestBody(textPlain)
                val email = uploadGuarantorDetailsReqBody.guarantorEmail.toRequestBody(textPlain)
                val referralCode =
                    uploadGuarantorDetailsReqBody.referralCode?.toRequestBody(textPlain)
                val sharpProgramType =
                    uploadGuarantorDetailsReqBody.sharpProgramType?.toRequestBody(textPlain)

                // Prepare file part
                val guarantorImage =
                    uploadGuarantorDetailsReqBody.guarantorImage.toMultipartPart("guarantor_image")!!

                // Make network call
                val response = ApiClient.authService.uploadGuarantorDetails(
                    bearerToken = bearerToken,
                    guarantorFullName = fullName,
                    guarantorRelationship = relationship,
                    guarantorHouseAddress = houseAddress,
                    guarantorCity = city,
                    guarantorState = state,
                    guarantorPhoneNumber = phoneNumber,
                    guarantorEmail = email,
                    referralCode = referralCode,
                    sharpProgramType = sharpProgramType,
                    guarantorImage = guarantorImage
                )

                if (response.status == "success") {
                    uploadGuarantorDetailsUiState = UploadGuarantorDetailsUiState.Success(response)
                    userPreferences.saveOnboardingStep(6)
//                    userPreferences.saveOnboardingStep(response.data.user.onboardingStep)
                } else {
                    uploadGuarantorDetailsUiState =
                        UploadGuarantorDetailsUiState.Error(response.status)
                }

            } catch (e: Exception) {
                uploadGuarantorDetailsUiState = UploadGuarantorDetailsUiState.Error(
                    e.message ?: "An unknown error occurred"
                )
                Log.e("AuthViewModel", "Error uploading guarantor details", e)
            }
        }
    }



    fun reUploadGuarantorDetails(uploadGuarantorDetailsReqBody: ReUploadGuarantorDetailsReqBody) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = userPreferences.authToken.firstOrNull()
            if (token.isNullOrBlank()) {
                Log.e("AuthViewModel", "Cannot re-upload guarantor details, token is missing.")
                return@launch
            }

            try {
                reUploadGuarantorDetailsUiState = ReUploadGuarantorDetailsUiState.Loading
                val bearerToken = "Bearer $token"
                val textPlain = "text/plain".toMediaTypeOrNull()

                // Prepare text fields
                val guarantorFullName =
                    uploadGuarantorDetailsReqBody.guarantorFullName.toRequestBody(textPlain)
                val guarantorRelationship =
                    uploadGuarantorDetailsReqBody.guarantorRelationship.toRequestBody(textPlain)
                val guarantorHouseAddress =
                    uploadGuarantorDetailsReqBody.guarantorHouseAddress.toRequestBody(textPlain)
                val guarantorCity =
                    uploadGuarantorDetailsReqBody.guarantorCity.toRequestBody(textPlain)
                val guarantorState =
                    uploadGuarantorDetailsReqBody.guarantorState.toRequestBody(textPlain)
                val guarantorPhoneNumber =
                    uploadGuarantorDetailsReqBody.guarantorPhoneNumber.toRequestBody(textPlain)
                val guarantorEmail =
                    uploadGuarantorDetailsReqBody.guarantorEmail.toRequestBody(textPlain)
//                val referralCode =
//                    uploadGuarantorDetailsReqBody.referralCode?.toRequestBody(textPlain)
//                val sharpProgramType =
//                    uploadGuarantorDetailsReqBody.sharpProgramType?.toRequestBody(textPlain)

                // Prepare file part
                val guarantorImage = uploadGuarantorDetailsReqBody.guarantorImage?.let {
                    MultipartBody.Part.createFormData(
                        "guarantor_image",
                        it.name,
                        it.asRequestBody("image/*".toMediaType())
                    )
                }

                // Make the network call
                val response = ApiClient.authService.reUploadGuarantorDetails(
                    bearerToken = bearerToken,
                    guarantorFullName = guarantorFullName,
                    guarantorRelationship = guarantorRelationship,
                    guarantorHouseAddress = guarantorHouseAddress,
                    guarantorCity = guarantorCity,
                    guarantorState = guarantorState,
                    guarantorPhoneNumber = guarantorPhoneNumber,
                    guarantorEmail = guarantorEmail,
                    guarantorImage = guarantorImage!!
                )

                if (response.status == "success") {
                    reUploadGuarantorDetailsUiState =
                        ReUploadGuarantorDetailsUiState.Success(response)
                    Log.d("AuthViewModel", "Guarantor re-upload successful.")
                } else {
                    reUploadGuarantorDetailsUiState =
                        ReUploadGuarantorDetailsUiState.Error(response.status)
                    Log.e("AuthViewModel", "Guarantor re-upload failed: ${response.status}")
                }

            } catch (e: Exception) {
                reUploadGuarantorDetailsUiState = ReUploadGuarantorDetailsUiState.Error(
                    e.message ?: "An unknown error occurred"
                )
                Log.e("AuthViewModel", "Error re-uploading guarantor details", e)
            }
        }
    }


    fun reUploadDocument(reuploadDocumentReqBody: ReUploadDocumentReqBody, id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = userPreferences.authToken.firstOrNull()
            if (token.isNullOrBlank()) {
                Log.e("AuthViewModel", "Cannot re-upload document, token is missing.")
                return@launch
            }

            try {
                reUploadDocUiState = ReUploadDocUiState.Loading
                val bearerToken = "Bearer $token"
                val textPlain = "text/plain".toMediaTypeOrNull()

                // Prepare text part
                val docNumberBody = reuploadDocumentReqBody.docNumber.toRequestBody(textPlain)

                // Validate file
                val file = reuploadDocumentReqBody.file
                if (file == null || !file.exists()) {
                    reUploadDocUiState = ReUploadDocUiState.Error("File not found or invalid")
                    return@launch
                }

                val filePart = MultipartBody.Part.createFormData(
                    "file",
                    file.name,
                    file.asRequestBody("image/*".toMediaType())
                )

                // Make network call
                val response = ApiClient.authService.reUploadDocument(
                    bearerToken = bearerToken,
                    id = id,
                    docNumber = docNumberBody,
                    file = filePart
                )

                if (response.status == "success") {
                    reUploadDocUiState = ReUploadDocUiState.Success(response)
                    Log.d("AuthViewModel", "Document re-upload successful.")
                } else {
                    reUploadDocUiState = ReUploadDocUiState.Error(response.message ?: "Unknown error")
                    Log.e("AuthViewModel", "Document re-upload failed: ${response.message}")
                }

            } catch (e: HttpException) {
                val errorMessage = HttpExceptionUtil.parseHttpError(e)
                reUploadDocUiState = ReUploadDocUiState.Error(errorMessage)
                Log.e("AuthViewModel", "HTTP error re-uploading document: $errorMessage")

            } catch (e: Exception) {
                reUploadDocUiState = ReUploadDocUiState.Error(
                    e.message ?: "An unknown error occurred"
                )
                Log.e("AuthViewModel", "Error re-uploading document", e)
            }
        }
    }

    fun resetState() {
        onboardDriverBiodataUiState = OnboardDriverPersonalDetailsUiState.Idle
        uploadCarDetailsUiState = UploadCarDetailsUiState.Idle
        uploadCarDocsUiState = UploadCarDocsUiState.Idle
        uploadGuarantorDetailsUiState = UploadGuarantorDetailsUiState.Idle
        editDriverProfileUiState = EditDriverProfileUiState.Idle
    }


}
