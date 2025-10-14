package com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride

import android.net.Uri
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kabukabu.driver.R
import com.kabukabu.driver.components.ui.CustomLinearProgressIndicator
import com.kabukabu.driver.components.ui.FormTextfield
import com.kabukabu.driver.components.ui.FormTextfieldDropdown
import com.kabukabu.driver.components.ui.GrayBackgroundContainer
import com.kabukabu.driver.components.ui.KabuBottomButtonRowScope
import com.kabukabu.driver.components.ui.KabuTransparentBottomButtonRowScope
import com.kabukabu.driver.components.ui.RowScopeFormTextfield
import com.kabukabu.driver.components.ui.ScreenTitleText
import com.kabukabu.driver.components.ui.displayToastMessage
import com.kabukabu.driver.components.utils_functions.convertUriToFile
import com.kabukabu.driver.core.data.local.LocalDataSource
import com.kabukabu.driver.core.navigation.Navigator
import com.kabukabu.driver.features.auth.data.entity.req_body.UploadGuarantorDetailsReqBody
import com.kabukabu.driver.features.auth.presentation.sign_up.view.SelectStateSheet
import com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel.AuthViewModel
import com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel.UploadGuarantorDetailsUiState
import java.util.regex.Pattern


@Composable
fun KabuRideGuarantorDetail(
    navigator: Navigator,
    authViewModel: AuthViewModel = viewModel()
) {

    val uploadGuarantorDetailsUiState = authViewModel.uploadGuarantorDetailsUiState

    val context = LocalContext.current

    var showStateSheet by remember { mutableStateOf(false) }
    var showGuarantorSheet by remember { mutableStateOf(false) }

    var guarantorImageUri by remember { mutableStateOf<Uri?>(null) }
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var houseAddress by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var guarantorRelationship by remember { mutableStateOf("") }
    var referralCode by remember { mutableStateOf("") }

    val isInputValidated = remember { mutableStateOf(false) }
    val fullNameError = remember { mutableStateOf("") }
    val phoneNumberError = remember { mutableStateOf("") }
    val emailError = remember { mutableStateOf("") }
    val houseAddressError = remember { mutableStateOf("") }
    val cityError = remember { mutableStateOf("") }
    val stateError = remember { mutableStateOf("") }
    val relationshipError = remember { mutableStateOf("") }


    if (showGuarantorSheet) {
        SelectGuarantorRelationshipModal(
            onDismiss = { showGuarantorSheet = false },
            onSelectRelationship = { guarantor ->
                guarantorRelationship = guarantor
                showStateSheet = false
            }
        )
    }

    if (showStateSheet) {
        SelectStateSheet(
            onDismiss = { showStateSheet = false },
            onSelectState = { selectedState ->
                state = selectedState
                showStateSheet = false
            }
        )
    }

    LaunchedEffect(uploadGuarantorDetailsUiState) {
        when (uploadGuarantorDetailsUiState) {
            is UploadGuarantorDetailsUiState.Success -> {
                context.displayToastMessage(uploadGuarantorDetailsUiState.response.message)
                authViewModel.resetState()
                navigator.navToKabuRidePendingAccountApprovalScreen()
            }

            is UploadGuarantorDetailsUiState.Error -> {
                context.displayToastMessage(uploadGuarantorDetailsUiState.message)
                authViewModel.resetState()
            }

            else -> {}
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = 30.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            CustomLinearProgressIndicator(
                progress = 1.0f,
                modifier = Modifier.padding(bottom = 50.dp)
            )

            ScreenTitleText(
                title = "Guarantor Details",
                subtitle = "Tell us about your guarantors",
                bottomPadding = 16
            )

            GrayBackgroundContainer {

                ProfileCard(
                    selectedImageUri = guarantorImageUri,
                    onImageSelected = { guarantorImageUri = it }
                )

                FormTextfield(
                    title = "Full Name",
                    value = fullName,
                    hintText = "John Doe",
                    onTextChanged = { fullName = it },
                    validationError = fullNameError.value.isNotEmpty(),
                    validationErrorMessage = fullNameError.value
                )

                FormTextfieldDropdown(
                    "Relationship",
                    value = guarantorRelationship,
                    isCompulsory = false,
                    onClick = {
                        showGuarantorSheet = true
                    },
                    validationError = relationshipError.value.isNotEmpty(),
                    validationErrorMessage = relationshipError.value
                )

                FormTextfield(
                    title = "Email Address",
                    value = email,
                    hintText = "Email",
                    onTextChanged = { email = it },
                    validationError = emailError.value.isNotEmpty(),
                    validationErrorMessage = emailError.value
                )

                FormTextfield(
                    title = "Phone number",
                    value = phoneNumber,
                    hintText = "Phone number",
                    onTextChanged = {
//                        if (phoneNumber.length <= 11) {
                            phoneNumber = it
//                        }
                    },
                    keyboardType = "number",
                    validationError = phoneNumberError.value.isNotEmpty(),
                    validationErrorMessage = phoneNumberError.value
                )

                FormTextfield(
                    title = "House address",
                    value = houseAddress,
                    hintText = "House address",
                    onTextChanged = { houseAddress = it },
                    validationError = houseAddressError.value.isNotEmpty(),
                    validationErrorMessage = houseAddressError.value
                )


                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    RowScopeFormTextfield(
                        title = "City",
                        value = city,
                        hintText = "City here",
                        isDropdown = false,
                        onTextChanged = { city = it },
                        imeAction = ImeAction.Done,
                        validationError = cityError.value.isNotEmpty(),
                        validationErrorMessage = cityError.value
                    )
                    RowScopeFormTextfield(
                        title = "State",
                        value = state,
                        hintText = "Abia",
                        isDropdown = true,
                        onClick = { showStateSheet = true },
                        onTextChanged = {},
                        validationError = stateError.value.isNotEmpty(),
                        validationErrorMessage = stateError.value
                    )
                }

                FormTextfield(
                    title = "Referral Code (Optional)",
                    value = referralCode,
                    hintText = "Code here",
                    isCompulsory = false,
                    onTextChanged = { referralCode = it },
                )

            }

            Row(
                modifier = Modifier.padding(top = 30.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                KabuTransparentBottomButtonRowScope(
                    "Previous",
                    icon = R.drawable.arrow_left,
                    onClick = { navigator.navigateUp() }
                )

                KabuBottomButtonRowScope(
                    "Submit",
                    icon = R.drawable.arrow_right,
                    isLoading = uploadGuarantorDetailsUiState == UploadGuarantorDetailsUiState.Loading,
                    onClick = {
//                        navigator.navToKabuRidePendingAccountApprovalScreen()
                        if (guarantorImageUri?.path.isNullOrEmpty()) {
                            context.displayToastMessage("Upload your Guarantor's image")
                            return@KabuBottomButtonRowScope
                        }
                        isInputValidated.value = validateGuarantorDetails(
                            fullName = fullName,
                            phoneNumber = phoneNumber,
                            email = email,
                            houseAddress = houseAddress,
                            relationship = guarantorRelationship,
                            city = city,
                            state = state,
                            fullNameError = fullNameError,
                            phoneNumberError = phoneNumberError,
                            emailError = emailError,
                            houseAddressError = houseAddressError,
                            cityError = cityError,
                            stateError = stateError,
                            relationshipError = relationshipError
                        )
                        if (isInputValidated.value) {
                            val uploadGuarantorDetailsReqBody = UploadGuarantorDetailsReqBody(
                                guarantorImage = convertUriToFile(context, guarantorImageUri),
                                guarantorFullName = fullName,
                                guarantorRelationship = guarantorRelationship,
                                guarantorHouseAddress = houseAddress,
                                guarantorCity = city,
                                guarantorState = state,
                                guarantorPhoneNumber = phoneNumber,
                                guarantorEmail = email,
                                referralCode = referralCode,
                                sharpProgramType = "HIRE_PURCHASE"
                                //RENTAL
                            )
                            authViewModel.uploadGuarantorDetails(uploadGuarantorDetailsReqBody)
                        }


                    }
                )
            }

        }

    }
}

private fun validateGuarantorDetails(
    fullName: String,
    phoneNumber: String,
    email: String,
    houseAddress: String,
    relationship: String,
    city: String,
    state: String,
    fullNameError: MutableState<String>,
    phoneNumberError: MutableState<String>,
    emailError: MutableState<String>,
    houseAddressError: MutableState<String>,
    relationshipError: MutableState<String>,
    cityError: MutableState<String>,
    stateError: MutableState<String>,
): Boolean {

    var isValid = true
    fullNameError.value = ""
    phoneNumberError.value = ""
    emailError.value = ""
    houseAddressError.value = ""
    relationshipError.value = ""
    cityError.value = ""
    stateError.value = ""

    if (fullName.isEmpty() || fullName.length < 6) {
        fullNameError.value = "Full name is not valid"
        isValid = false
    }

    if (phoneNumber.isEmpty() || phoneNumber.length != 11) {
        phoneNumberError.value = "Enter a valid Phone number"
        isValid = false
    }

    if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email)
            .matches()
    ) {
        emailError.value = "Invalid email address"
        isValid = false
    }

    if (houseAddress.isEmpty() || houseAddress.length < 10) {
        houseAddressError.value = "Invalid house address"
        isValid = false
    }

    if (city.isEmpty() || city.length < 3) {
        cityError.value = "Invalid city"
        isValid = false
    }

    if (houseAddress.isEmpty() || houseAddress.length < 10) {
        houseAddressError.value = "Invalid house address"
        isValid = false
    }

   if (relationship.isEmpty()) {
        relationshipError.value = "Select a relationship"
        isValid = false
    }

    if (state.isEmpty()) {
        stateError.value = "Select a State"
        isValid = false
    }

    return isValid
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectGuarantorRelationshipModal(
    onDismiss: () -> Unit,
    onSelectRelationship: (String) -> Unit
) {

    val relationship = LocalDataSource().guarantorRelationship

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            Text(
                text = "Select State",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(relationship) { state ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                    ) {
                        Text(
                            text = state,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onSelectRelationship(state)
                                    onDismiss()
                                }
//                                .background(color = Color(0x2DD3D3D3))
                                .padding(vertical = 12.dp, horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ProfileCard(
    selectedImageUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
//    showImageSelection: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> onImageSelected(uri) }
    )

    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(color = Color(0xFFF8C34A))
            .clickable {
//                if (showImageSelection) {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
//                } else {
//                context.displayToastMessage("Please complete required fields before uploading.")
//                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (selectedImageUri == null || selectedImageUri == Uri.EMPTY) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Icon",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(selectedImageUri)
                    .size(800)
                    .crossfade(true)
                    .build(),
                contentDescription = "Selected profile image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}
