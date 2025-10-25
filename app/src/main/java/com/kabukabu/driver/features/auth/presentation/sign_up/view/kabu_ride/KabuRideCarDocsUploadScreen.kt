package com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kabukabu.driver.R
import com.kabukabu.driver.components.ui.AnnotatedTextfieldTitle
import com.kabukabu.driver.components.ui.CameraXCaptureImage
import com.kabukabu.driver.components.ui.CustomLinearProgressIndicator
import com.kabukabu.driver.components.ui.FormTextfield
import com.kabukabu.driver.components.ui.GrayBackgroundContainer
import com.kabukabu.driver.components.ui.KabuBottomButtonRowScope
import com.kabukabu.driver.components.ui.KabuTransparentBottomButtonRowScope
import com.kabukabu.driver.components.ui.ScreenTitleText
import com.kabukabu.driver.components.ui.TitleText
import com.kabukabu.driver.components.ui.displayToastMessage
import com.kabukabu.driver.components.utils_functions.convertUriToFile
import com.kabukabu.driver.core.navigation.Navigator
import com.kabukabu.driver.features.auth.data.entity.req_body.UploadCarDocsReqBody
import com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel.AuthViewModel
import com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel.UploadCarDocsUiState
import com.kabukabu.driver.features.home.presentation.DriverViewModel
import org.koin.androidx.compose.koinViewModel

private enum class SelectedDoc { VehicleLicense, DriverLicense, Insurance, ProofOfOwnership, RoadWorthiness, HackneyPermit }

@Composable
fun KabuRideCarDocumentsUploadScreen(
    navigation: Navigator,
    authViewModel: AuthViewModel = koinViewModel()
) {

    val context = LocalContext.current
    // Create DriverViewModel at Activity scope so it's shared across Splash and Home
    val activityOwner = context as ViewModelStoreOwner
    val driverViewModel: DriverViewModel = viewModel(viewModelStoreOwner = activityOwner)

    val uploadCarDocsUiState = authViewModel.uploadCarDocsUiState
//    val context = LocalContext.current

    var selectedDocType by remember { mutableStateOf(SelectedDoc.VehicleLicense) }
    var vehicleLicenseUri by remember { mutableStateOf<Uri?>(null) }
    var driverLicenseUri by remember { mutableStateOf<Uri?>(null) }
    var insuranceUri by remember { mutableStateOf<Uri?>(null) }
    var proofOfOwnershipUri by remember { mutableStateOf<Uri?>(null) }
    var roadWorthinessUri by remember { mutableStateOf<Uri?>(null) }
    var hackneyPermitUri by remember { mutableStateOf<Uri?>(null) }


    var launchCamera by remember { mutableStateOf(false) }

    var vehicleLicense by remember { mutableStateOf(null) }
    var driverLicense by remember { mutableStateOf(null) }
    var insuranceNumber by remember { mutableStateOf(null) }
    var proofOfOwnershipNumber by remember { mutableStateOf(null) }
    var roadWorthinessNumber by remember { mutableStateOf(null) }
    var hackneyPermitNumber by remember { mutableStateOf(null) }

    val isInputValidated = remember { mutableStateOf(false) }
    val driverLicenseError = remember { mutableStateOf("") }
    val carInsuranceError = remember { mutableStateOf("") }
    val vehicleLicenseError = remember { mutableStateOf("") }
    val proofOfOwnershipError = remember { mutableStateOf("") }
    val roadWorthinessError = remember { mutableStateOf("") }

    BackHandler { true }


    LaunchedEffect(uploadCarDocsUiState) {
        when (uploadCarDocsUiState) {

            is UploadCarDocsUiState.Success -> {
                context.displayToastMessage(uploadCarDocsUiState.response.message)
                authViewModel.resetState()
                navigation.navToKabuRideGuarantorDetailsScreen()
            }

            is UploadCarDocsUiState.Error -> {
                context.displayToastMessage(uploadCarDocsUiState.message)
                authViewModel.resetState()
            }

            else -> {}
        }
    }


    Scaffold { paddingValues ->
        if (launchCamera) {
            // Show the camera view
            CameraXCaptureImage(
                onImageCaptured = { uri ->
                    if (uri != null) {
                        when (selectedDocType) {
                            SelectedDoc.VehicleLicense -> {
                                vehicleLicenseUri = uri
                            }

                            SelectedDoc.DriverLicense -> {
                                driverLicenseUri = uri
                            }

                            SelectedDoc.Insurance -> {
                                insuranceUri = uri
                            }

                            SelectedDoc.ProofOfOwnership -> {
                                proofOfOwnershipUri = uri
                            }

                            SelectedDoc.RoadWorthiness -> {
                                roadWorthinessUri = uri
                            }

                            SelectedDoc.HackneyPermit -> {
                                hackneyPermitUri = uri
                            }
                        }
                    }
                    launchCamera = false
                },
                onError = { error ->
//                    photoError = error
                    launchCamera = false
                }
            )
        } else {
            // Show the main form
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
                    .padding(top = 30.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                CustomLinearProgressIndicator(
                    progress = 0.67f,
                    modifier = Modifier.padding(bottom = 50.dp)
                )

                ScreenTitleText(
                    title = "Documents",
                    subtitle = "Upload your car documents",
                    bottomPadding = 16
                )

                GrayBackgroundContainer {


                    CaptureDocumentItem(
                        title = "Vehicle License",
                        label = "Tap here to capture",
                        imageUri = vehicleLicenseUri,
                        onClick = {
                            selectedDocType = SelectedDoc.VehicleLicense
                            launchCamera = true
                        },
                        validationError = vehicleLicenseError.value.isNotEmpty(),
                        validationErrorMessage = vehicleLicenseError.value
                    )


//                    FormTextfield(
//                        title = "Vehicle License Number",
//                        value = vehicleLicense,
//                        hintText = "ABC1234567",
//                        onTextChanged = { vehicleLicense = it}
//                    )

                    CaptureDocumentItem(
                        title = "Driver’s License",
                        label = "Tap here to capture",
                        imageUri = driverLicenseUri,
                        onClick = {
                            selectedDocType = SelectedDoc.DriverLicense
                            launchCamera = true
                        },
                        validationError = driverLicenseError.value.isNotEmpty(),
                        validationErrorMessage = driverLicenseError.value
                    )

//                    FormTextfield(
//                        title = "Driver's License Number",
//                        value = driverLicense,
//                        hintText = "ABC1234567",
//                        onTextChanged = { driverLicense = it }
//                    )

                    CaptureDocumentItem(
                        title = "Insurance Certificate",
                        label = "Tap here to capture",
                        imageUri = insuranceUri,
                        onClick = {
                            selectedDocType = SelectedDoc.Insurance
                            launchCamera = true
                        },
                        validationError = carInsuranceError.value.isNotEmpty(),
                        validationErrorMessage = carInsuranceError.value
                    )

//                    FormTextfield(
//                        title = "Insurance Certificate Number",
//                        value = insuranceNumber,
//                        hintText = "ABC1234567",
//                        onTextChanged = { insuranceNumber = it }
//                    )

                    CaptureDocumentItem(
                        title = "Proof of Ownership",
                        label = "Tap here to capture",
                        imageUri = proofOfOwnershipUri,
                        onClick = {
                            selectedDocType = SelectedDoc.ProofOfOwnership
                            launchCamera = true
                        },
                        validationError = proofOfOwnershipError.value.isNotEmpty(),
                        validationErrorMessage = proofOfOwnershipError.value
                    )

//                    FormTextfield(
//                        title = "Proof of Ownership Number",
//                        value = proofOfOwnershipNumber,
//                        hintText = "ABC1234567",
//                        isCompulsory = false,
//                        onTextChanged = { proofOfOwnershipNumber = it }
//                    )

                    CaptureDocumentItem(
                        title = "Road Worthiness Certificate",
                        label = "Tap here to capture",
                        imageUri = roadWorthinessUri,
                        onClick = {
                            selectedDocType = SelectedDoc.RoadWorthiness
                            launchCamera = true
                        },
                        validationError = roadWorthinessError.value.isNotEmpty(),
                        validationErrorMessage = roadWorthinessError.value
                    )

//                    FormTextfield(
//                        title = "Road Worthiness Certificate",
//                        value = roadWorthinessNumber,
//                        hintText = "ABC1234567",
//                        isCompulsory = false,
//                        onTextChanged = { roadWorthinessNumber = it }
//                    )

                    CaptureDocumentItem(
                        title = "Hackney Permit",
                        label = "Tap here to capture",
                        isCompulsoryField = false,
                        imageUri = hackneyPermitUri,
                        onClick = {
                            selectedDocType = SelectedDoc.HackneyPermit
                            launchCamera = true
                        }
                    )

//                    TitleText(
//                        "Tap to reupload",
//                        color = Color.Gray,
//                        fontWeight = FontWeight.W500
//                    )

//                    FormTextfield(
//                        title = "Hackney Permit Number",
//                        value = hackneyPermitNumber,
//                        hintText = "Doc-IMHG-0088",
//                        isCompulsory = false,
//                        imeAction = ImeAction.Done,
//                        onTextChanged = { hackneyPermitNumber = it }
//                    )
                }

                Row(
                    modifier = Modifier.padding(top = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    KabuTransparentBottomButtonRowScope(
                        "Previous",
                        icon = R.drawable.arrow_left,
                        onClick = { navigation.navigateUp() }
                    )

                    KabuBottomButtonRowScope(
                        "Next",
                        isLoading = uploadCarDocsUiState == UploadCarDocsUiState.Loading,
                        icon = R.drawable.arrow_right,
                        onClick = {
//                            navigation.navToKabuRideGuarantorDetailsScreen()

                            isInputValidated.value = validateCarDocs(
                                driverLicenseUri = driverLicenseUri,
                                carInsuranceUri = insuranceUri,
                                vehicleLicenseUri = vehicleLicenseUri,
                                proofOfOwnership = proofOfOwnershipUri,
                                roadWorthinessUri = roadWorthinessUri,
                                driverLicenseError = driverLicenseError,
                                carInsuranceError = carInsuranceError,
                                vehicleLicenseError = vehicleLicenseError,
                                proofOfOwnershipError = proofOfOwnershipError,
                                roadWorthinessError = roadWorthinessError
                            )

                            if (isInputValidated.value) {
                                val uploadCarDocsReqBody = UploadCarDocsReqBody(
                                    driverLicenceNumber = driverLicense,
                                    carInsuranceNumber = insuranceNumber,
                                    vehicleLicenceNumber = vehicleLicense,
                                    proofOfOwnershipNumber = proofOfOwnershipNumber,
                                    roadWorthinessCertificationNumber = roadWorthinessNumber,
                                    hackneyPermitNumber = hackneyPermitNumber,
                                    driverLicence = convertUriToFile(context, driverLicenseUri),
                                    vehicleLicence = convertUriToFile(context, vehicleLicenseUri),
                                    insuranceCertificate = convertUriToFile(context, insuranceUri),
                                    proofOfOwnership = convertUriToFile(
                                        context,
                                        proofOfOwnershipUri
                                    ),
                                    roadWorthinessCertification = convertUriToFile(
                                        context,
                                        roadWorthinessUri
                                    ),
                                    hackneyPermit = convertUriToFile(context, hackneyPermitUri),
                                )
                                authViewModel.uploadCarDocs(uploadCarDocsReqBody = uploadCarDocsReqBody)

                            }

                        }
                    )
                }
            }
        }
    }
}


private fun validateCarDocs(
    driverLicenseUri: Uri?,
    carInsuranceUri: Uri?,
    vehicleLicenseUri: Uri?,
    proofOfOwnership: Uri?,
    roadWorthinessUri: Uri?,
    driverLicenseError: MutableState<String>,
    carInsuranceError: MutableState<String>,
    vehicleLicenseError: MutableState<String>,
    proofOfOwnershipError: MutableState<String>,
    roadWorthinessError: MutableState<String>,
): Boolean {
    var isValid = true

    driverLicenseError.value = ""
    carInsuranceError.value = ""
    vehicleLicenseError.value = ""
    proofOfOwnershipError.value = ""
    roadWorthinessError.value = ""

    if (driverLicenseUri?.path.isNullOrEmpty()) {
        driverLicenseError.value = "Driver license not selected"
        isValid = false
    }

    if (carInsuranceUri?.path.isNullOrEmpty()) {
        carInsuranceError.value = "Car insurance certificate not selected"
        isValid = false
    }

    if (vehicleLicenseUri?.path.isNullOrEmpty()) {
        vehicleLicenseError.value = "Vehicle license not selected"
        isValid = false
    }

    if (proofOfOwnership?.path.isNullOrEmpty()) {
        proofOfOwnershipError.value = "Proof of ownership not selected"
        isValid = false
    }

    if (roadWorthinessUri?.path.isNullOrEmpty()) {
        roadWorthinessError.value = "Road worthiness certificate not selected"
        isValid = false
    }
    return isValid
}

@Composable
fun CaptureDocumentItem(
    title: String,
    label: String,
    imageUri: Uri?,
    isCompulsoryField: Boolean = true,
    photoBoxError: String = "",
    onClick: () -> Unit,
    validationError: Boolean = false,
    validationErrorMessage: String = "",
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(bottom = 20.dp)
    ) {
        AnnotatedTextfieldTitle(title, isCompulsoryField)

        if (imageUri == null || imageUri == Uri.EMPTY) {
            CaptureDocumentBox(
                label = label,
                onClick = onClick
            )
            if (photoBoxError.isNotEmpty()) {
                TitleText(
                    text = photoBoxError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12
                )
            }

        } else {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onClick() }, // allow recapture/reupload
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUri)
                            .crossfade(true)
                            .size(800)
                            .build(),
                        contentDescription = "Captured document preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Overlay a semi-transparent layer with a “Tap to change” text
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.2f))
                            .align(Alignment.Center)
                    ) {
                        TitleText(
                            text = "Tap to reupload",
                            color = Color.White,
                            fontWeight = FontWeight.W500,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(8.dp)
                        )
                    }
                }
            }
        }
        if (validationError) {
            TitleText(
                validationErrorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12,
                topPadding = 4
            )
        }

    }
}


@Composable
fun CaptureDocumentBox(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
//            .height(100.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFF1F1F1))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.doc_upload),
                contentDescription = "image placeholder icon",
                modifier = Modifier.size(22.dp),
                tint = Color.Gray
            )
            TitleText(
                label,
                topPadding = 12
            )


        }
    }

}