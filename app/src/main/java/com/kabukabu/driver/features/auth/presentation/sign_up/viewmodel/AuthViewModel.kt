package com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.components.utils_functions.toMultipartPart
import com.kabukabu.driver.core.data.local.DataPersistenceViewModel
import com.kabukabu.driver.core.data.remote.ApiClient
import com.kabukabu.driver.core.data.remote.HttpExceptionUtil
import com.kabukabu.driver.core.data.remote.hub.HubApiClient
import com.kabukabu.driver.features.auth.data.entity.req_body.ReUploadDocumentReqBody
import com.kabukabu.driver.features.auth.data.entity.req_body.ReUploadGuarantorDetailsReqBody
import com.kabukabu.driver.features.auth.data.entity.req_body.UploadCarDetailsReqBody
import com.kabukabu.driver.features.auth.data.entity.req_body.DriverDetailsReqBody
import com.kabukabu.driver.features.auth.data.entity.req_body.UploadCarDocsReqBody
import com.kabukabu.driver.features.auth.data.entity.req_body.UploadGuarantorDetailsReqBody
import com.kabukabu.driver.features.auth.data.entity.response.VideoClipsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File

class AuthViewModel(private val dataPersistenceViewModel: DataPersistenceViewModel
) : ViewModel() {

//    private val userPreferences = UserPreferences(application)
    val userPreferences = KabukabuDriverApp.getInstance().userPreferences

//    private val _carBrands = MutableStateFlow<List<String>>(emptyList())
//    val carBrands: StateFlow<List<String>> = _carBrands.asStateFlow()

//    private val _inspectionsHubs = MutableStateFlow<InspectionHubsResponse?>(null)
//    val inspectionsHubs: StateFlow<InspectionHubsResponse?> = _inspectionsHubs.asStateFlow()

//    private val _driverDetailsReqBody = MutableStateFlow<DriverDetailsReqBody?>(null)
//    val driverDetailsReqBody: StateFlow<DriverDetailsReqBody?> = _driverDetailsReqBody.asStateFlow()

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
//        fetchCarBrands()
//        fetchHubs("Lagos")
//        fetchVideoClips()
    }

    fun setUploadUserDetailsReqBody(driverDetailsReqBody: DriverDetailsReqBody) {
        dataPersistenceViewModel.setDriverDetailsReqBody(driverDetailsReqBody.copy())
//        _driverDetailsReqBody.value = driverDetailsReqBody.copy()
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
                            dataPersistenceViewModel.setCarBrands(it)
//                            _carBrands.value = it
//                            println("car brands are ${_carBrands.value}")
                        }
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }

        }

    }

    fun fetchVideoClips() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                val reference = db.collection("app_video").document("info")

                reference.get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val clipsList = document.get("clips") as? List<Map<String, Any>>
                            clipsList?.let { maps ->
                                val clips = maps.mapNotNull { map ->
                                    try {
                                        VideoClipsResponse(
                                            clip = map["clip"] as? String ?: "",
                                            duration = map["duration"] as? String ?: "",
                                            thumbnail = map["thumbnail"] as? String ?: "",
                                            title = map["title"] as? String ?: ""
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        null
                                    }
                                }

                                if (clips.isNotEmpty()) {
                                    dataPersistenceViewModel.setVideoClips(clips)
                                }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                    }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchHubs(state: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = HubApiClient.hubService.fetchHubs(state)
                dataPersistenceViewModel.setInspectionHubs(response)
//                _inspectionsHubs.value = response
            } catch (e: Exception) {
                Log.e("HubViewModel", "Error fetching hubs", e)
            }
        }
    }

    fun uploadDriverBioData(driverPersonalDetailsReqBody: DriverDetailsReqBody) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = userPreferences.authToken.firstOrNull()
            if (token.isNullOrBlank()) {
                Log.e("AuthViewModel", "Cannot fetch profile, token is missing.")
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
//                    email = driverPersonalDetailsReqBody.email.toRequestBody(textPlain),
                    houseAddress = driverPersonalDetailsReqBody.houseAddress.toRequestBody(textPlain),
                    city = driverPersonalDetailsReqBody.city.toRequestBody(textPlain),
                    state = driverPersonalDetailsReqBody.state.toRequestBody(textPlain),
                    carOwner = driverPersonalDetailsReqBody.carOwner.toString().toRequestBody(textPlain),
//                    carCategory = driverPersonalDetailsReqBody.carCategory.toRequestBody(textPlain)
                )

                if (response.status == "success") {
                    onboardDriverBiodataUiState = OnboardDriverPersonalDetailsUiState.Success(response)
                } else {
                    onboardDriverBiodataUiState = OnboardDriverPersonalDetailsUiState.Error(response.message)
                }

            } catch (e: HttpException) {
                val errorMessage = HttpExceptionUtil.parseHttpError(e)
                onboardDriverBiodataUiState = OnboardDriverPersonalDetailsUiState.Error(errorMessage)
                Log.e("AuthViewModel", "HTTP error uploading driver biodata: $errorMessage")

            } catch (e: Exception) {
                onboardDriverBiodataUiState = OnboardDriverPersonalDetailsUiState.Error(e.message ?: "An unknown error occurred")
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
                val profileImagePart = MultipartBody.Part.createFormData("profile_image", imageFile.name, requestFile)

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
                } else {
                    editDriverProfileUiState = EditDriverProfileUiState.Error(response.message)
                }

            } catch (e: HttpException) {
                val errorMessage = HttpExceptionUtil.parseHttpError(e)
                editDriverProfileUiState = EditDriverProfileUiState.Error(errorMessage)
                Log.e("AuthViewModel", "HTTP error uploading profile image: $errorMessage")

            } catch (e: Exception) {
                editDriverProfileUiState = EditDriverProfileUiState.Error(e.message ?: "Unexpected error occurred")
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

                val carBrand = uploadCarDetailsReqBody.carBrand.toRequestBody(textPlain)
                val carModel = uploadCarDetailsReqBody.carModel.toRequestBody(textPlain)
                val carYear = uploadCarDetailsReqBody.carYear.toRequestBody(textPlain)
                val carColor = uploadCarDetailsReqBody.carColor.toRequestBody(textPlain)
                val carPlateNumber = uploadCarDetailsReqBody.carPlateNumber.toRequestBody(textPlain)

                val carImages = uploadCarDetailsReqBody.carImages.map { file ->
                    MultipartBody.Part.createFormData("car_images", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
                }

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
                } else {
                    uploadCarDetailsUiState = UploadCarDetailsUiState.Error(response.message)
                }

            } catch (e: HttpException) {
                val errorMessage = HttpExceptionUtil.parseHttpError(e)
                uploadCarDetailsUiState = UploadCarDetailsUiState.Error(errorMessage)
                Log.e("AuthViewModel", "HTTP error uploading car details: $errorMessage")

            } catch (e: Exception) {
                uploadCarDetailsUiState = UploadCarDetailsUiState.Error(e.message ?: "An unknown error occurred")
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

                val driverLicence = uploadCarDocsReqBody.driverLicence.toMultipartPart("driver_licence")!!
                val vehicleLicence = uploadCarDocsReqBody.vehicleLicence.toMultipartPart("vehicle_licence")!!
                val insuranceCertificate = uploadCarDocsReqBody.insuranceCertificate.toMultipartPart("insurance_certificate")!!
                val roadWorthinessCertification = uploadCarDocsReqBody.roadWorthinessCertification.toMultipartPart("road_worthiness_certificate")!!

                val response = ApiClient.authService.uploadCarDocs(
                    bearerToken = bearerToken,
                    driverLicenceNumber = uploadCarDocsReqBody.driverLicenceNumber?.toRequestBody(textPlain),
                    carInsuranceNumber = uploadCarDocsReqBody.carInsuranceNumber?.toRequestBody(textPlain),
                    vehicleLicenceNumber = uploadCarDocsReqBody.vehicleLicenceNumber?.toRequestBody(textPlain),
                    proofOfOwnershipNumber = uploadCarDocsReqBody.proofOfOwnershipNumber?.toRequestBody(textPlain),
                    roadWorthinessCertificationNumber = uploadCarDocsReqBody.roadWorthinessCertificationNumber?.toRequestBody(textPlain),
                    hackneyPermitNumber = uploadCarDocsReqBody.hackneyPermitNumber?.toRequestBody(textPlain),
                    driverLicence = driverLicence,
                    vehicleLicence = vehicleLicence,
                    insuranceCertificate = insuranceCertificate,
                    proofOfOwnership = uploadCarDocsReqBody.proofOfOwnership?.toMultipartPart("proof_of_ownership"),
                    roadWorthinessCertification = roadWorthinessCertification,
                    hackneyPermit = uploadCarDocsReqBody.hackneyPermit?.toMultipartPart("hackney_permit")
                )

                if (response.status == "success") {
                    uploadCarDocsUiState = UploadCarDocsUiState.Success(response)
                    userPreferences.saveOnboardingStep(5)
                } else {
                    uploadCarDocsUiState = UploadCarDocsUiState.Error(response.status)
                }

            } catch (e: HttpException) {
                val errorMessage = HttpExceptionUtil.parseHttpError(e)
                uploadCarDocsUiState = UploadCarDocsUiState.Error(errorMessage)
                Log.e("AuthViewModel", "HTTP error uploading car docs: $errorMessage")

            } catch (e: Exception) {
                uploadCarDocsUiState = UploadCarDocsUiState.Error(e.message ?: "An unknown error occurred")
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

                val guarantorImage = uploadGuarantorDetailsReqBody.guarantorImage.toMultipartPart("guarantor_image")!!

                val response = ApiClient.authService.uploadGuarantorDetails(
                    bearerToken = bearerToken,
                    guarantorFullName = uploadGuarantorDetailsReqBody.guarantorFullName.toRequestBody(textPlain),
                    guarantorRelationship = uploadGuarantorDetailsReqBody.guarantorRelationship.toRequestBody(textPlain),
                    guarantorHouseAddress = uploadGuarantorDetailsReqBody.guarantorHouseAddress.toRequestBody(textPlain),
                    guarantorCity = uploadGuarantorDetailsReqBody.guarantorCity.toRequestBody(textPlain),
                    guarantorState = uploadGuarantorDetailsReqBody.guarantorState.toRequestBody(textPlain),
                    guarantorPhoneNumber = uploadGuarantorDetailsReqBody.guarantorPhoneNumber.toRequestBody(textPlain),
                    guarantorEmail = uploadGuarantorDetailsReqBody.guarantorEmail.toRequestBody(textPlain),
                    referralCode = uploadGuarantorDetailsReqBody.referralCode?.toRequestBody(textPlain),
                    sharpProgramType = uploadGuarantorDetailsReqBody.sharpProgramType?.toRequestBody(textPlain),
                    guarantorImage = guarantorImage
                )

                if (response.status == "success") {
                    uploadGuarantorDetailsUiState = UploadGuarantorDetailsUiState.Success(response)
                    userPreferences.saveOnboardingStep(6)
                } else {
                    uploadGuarantorDetailsUiState = UploadGuarantorDetailsUiState.Error(response.status)
                }

            } catch (e: HttpException) {
                val errorMessage = HttpExceptionUtil.parseHttpError(e)
                uploadGuarantorDetailsUiState = UploadGuarantorDetailsUiState.Error(errorMessage)
                Log.e("AuthViewModel", "HTTP error uploading guarantor details: $errorMessage")

            } catch (e: Exception) {
                uploadGuarantorDetailsUiState = UploadGuarantorDetailsUiState.Error(e.message ?: "An unknown error occurred")
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

                val guarantorImage = uploadGuarantorDetailsReqBody.guarantorImage?.let {
                    MultipartBody.Part.createFormData("guarantor_image", it.name, it.asRequestBody("image/*".toMediaType()))
                }

                val response = ApiClient.authService.reUploadGuarantorDetails(
                    bearerToken = bearerToken,
                    guarantorFullName = uploadGuarantorDetailsReqBody.guarantorFullName.toRequestBody(textPlain),
                    guarantorRelationship = uploadGuarantorDetailsReqBody.guarantorRelationship.toRequestBody(textPlain),
                    guarantorHouseAddress = uploadGuarantorDetailsReqBody.guarantorHouseAddress.toRequestBody(textPlain),
                    guarantorCity = uploadGuarantorDetailsReqBody.guarantorCity.toRequestBody(textPlain),
                    guarantorState = uploadGuarantorDetailsReqBody.guarantorState.toRequestBody(textPlain),
                    guarantorPhoneNumber = uploadGuarantorDetailsReqBody.guarantorPhoneNumber.toRequestBody(textPlain),
                    guarantorEmail = uploadGuarantorDetailsReqBody.guarantorEmail.toRequestBody(textPlain),
                    guarantorImage = guarantorImage!!
                )

                if (response.status == "success") {
                    reUploadGuarantorDetailsUiState = ReUploadGuarantorDetailsUiState.Success(response)
                    Log.d("AuthViewModel", "Guarantor re-upload successful.")
                } else {
                    reUploadGuarantorDetailsUiState = ReUploadGuarantorDetailsUiState.Error(response.status)
                    Log.e("AuthViewModel", "Guarantor re-upload failed: ${response.status}")
                }

            } catch (e: HttpException) {
                val errorMessage = HttpExceptionUtil.parseHttpError(e)
                reUploadGuarantorDetailsUiState = ReUploadGuarantorDetailsUiState.Error(errorMessage)
                Log.e("AuthViewModel", "HTTP error re-uploading guarantor: $errorMessage")

            } catch (e: Exception) {
                reUploadGuarantorDetailsUiState = ReUploadGuarantorDetailsUiState.Error(e.message ?: "An unknown error occurred")
                Log.e("AuthViewModel", "Error re-uploading guarantor details", e)
            }
        }
    }

    fun reUploadDocument(reuploadDocumentReqBody: ReUploadDocumentReqBody, id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = userPreferences.authToken.firstOrNull()
            if (token.isNullOrBlank()) {
                Log.e("AuthViewModel", "Cannot re-upload document, token is missing.")
                reUploadDocUiState = ReUploadDocUiState.Error("Authentication token is missing.")
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
                    reUploadDocUiState = ReUploadDocUiState.Error("File not found or invalid.")
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
                    reUploadDocUiState = ReUploadDocUiState.Error(response.message ?: "Unknown error.")
                    Log.e("AuthViewModel", "Document re-upload failed: ${response.message}")
                }

            } catch (e: HttpException) {
                // Parse and show server error message
                val errorMessage = HttpExceptionUtil.parseHttpError(e)
                reUploadDocUiState = ReUploadDocUiState.Error(errorMessage)
                Log.e("AuthViewModel", "HTTP error re-uploading document: $errorMessage")

            } catch (e: Exception) {
                reUploadDocUiState = ReUploadDocUiState.Error(
                    e.message ?: "An unknown error occurred."
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
        reUploadDocUiState = ReUploadDocUiState.Idle
        reUploadGuarantorDetailsUiState = ReUploadGuarantorDetailsUiState.Idle
    }


}
