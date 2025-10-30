package com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.R
import com.kabukabu.driver.components.ui.CustomLinearProgressIndicator
import com.kabukabu.driver.components.ui.FormTextfield
import com.kabukabu.driver.components.ui.FormTextfieldDropdown
import com.kabukabu.driver.components.ui.GrayBackgroundContainer
import com.kabukabu.driver.components.ui.KabuBottomButton
import com.kabukabu.driver.components.ui.RowScopeFormTextfield
import com.kabukabu.driver.components.ui.ScreenTitleText
import com.kabukabu.driver.components.ui.displayToastMessage
import com.kabukabu.driver.components.utils_functions.convertUriToFile
import com.kabukabu.driver.core.navigation.Navigator
import com.kabukabu.driver.features.auth.data.entity.req_body.ReUploadGuarantorDetailsReqBody
import com.kabukabu.driver.features.auth.presentation.sign_up.view.SelectStateSheet
import com.kabukabu.driver.features.auth.presentation.sign_up.view.validateFullName
import com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel.AuthViewModel
import com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel.ReUploadGuarantorDetailsUiState
import com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel.UploadGuarantorDetailsUiState
import com.kabukabu.driver.features.home.presentation.DriverViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReuploadGuarantorDetail(
    navigator: Navigator,
    authViewModel: AuthViewModel = koinViewModel()
) {
    val context = LocalContext.current


    val activityOwner = context as ViewModelStoreOwner

    val driverViewModel: DriverViewModel = viewModel(viewModelStoreOwner = activityOwner)
    val userPreferences = KabukabuDriverApp.getInstance().userPreferences


    val reuploadGuarantorDetailsUiState = authViewModel.reUploadGuarantorDetailsUiState


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

    LaunchedEffect(reuploadGuarantorDetailsUiState) {
        when (reuploadGuarantorDetailsUiState) {
            is ReUploadGuarantorDetailsUiState.Success -> {
                context.displayToastMessage(reuploadGuarantorDetailsUiState.response.message)
                authViewModel.resetState()
                navigator.navToKabuRidePendingAccountApprovalScreen()
            }

            is ReUploadGuarantorDetailsUiState.Error -> {
                context.displayToastMessage(reuploadGuarantorDetailsUiState.message)
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
                subtitle = "Reupload your guarantors details",
                bottomPadding = 16,
                modifier = Modifier.clickable{

                }
            )

            GrayBackgroundContainer {

                ProfileCard(
                    selectedImageUri = guarantorImageUri,
                    onImageSelected = { guarantorImageUri = it },
                    modifier = Modifier.padding(6.dp)
                )

                FormTextfield(
                    title = "Full Name",
                    value = fullName,
                    hintText = "John Doe",
                    onTextChanged = { newText ->
                        val isValid = validateFullName(
                            newText,
                            fullNameError
                        )
                        if (isValid) {
                            fullName = newText
                        } else {
                            return@FormTextfield
                        }
                    },
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
                    keyboardType = "phone number",
                    onTextChanged = { newText ->
                        val filtered = newText.filter { it.isDigit() }
                        if (filtered.length <= 11) {
                            phoneNumber = filtered
                        }
                    },
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
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
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

//                FormTextfield(
//                    title = "Referral Code (Optional)",
//                    value = referralCode,
//                    hintText = "Code here",
//                    isCompulsory = false,
//                    onTextChanged = { referralCode = it },
//                )

            }


            KabuBottomButton(
                text = "Submit",
                isLoading = reuploadGuarantorDetailsUiState == ReUploadGuarantorDetailsUiState.Loading,
                onClick = {
                    if (guarantorImageUri?.path.isNullOrEmpty()) {
                        context.displayToastMessage("Upload your Guarantor's image")
                        return@KabuBottomButton
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
                        val uploadGuarantorDetailsReqBody = ReUploadGuarantorDetailsReqBody(
                            guarantorImage = convertUriToFile(context, guarantorImageUri),
                            guarantorFullName = fullName,
                            guarantorRelationship = guarantorRelationship,
                            guarantorHouseAddress = houseAddress,
                            guarantorCity = city,
                            guarantorState = state,
                            guarantorPhoneNumber = phoneNumber,
                            guarantorEmail = email,
                        )
                        authViewModel.reUploadGuarantorDetails(uploadGuarantorDetailsReqBody)
                    }

                }
            )

        }

    }
}