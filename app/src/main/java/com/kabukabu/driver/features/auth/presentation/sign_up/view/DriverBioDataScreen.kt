package com.kabukabu.driver.features.auth.presentation.sign_up.view

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kabukabu.driver.components.ui.FormTextfield
import com.kabukabu.driver.components.ui.FormTextfieldDropdown
import com.kabukabu.driver.components.ui.KabuBottomButton
import com.kabukabu.driver.components.ui.KabuDivider
import com.kabukabu.driver.components.ui.RowScopeFormTextfield
import com.kabukabu.driver.components.ui.ScreenTitleText
import com.kabukabu.driver.core.data.local.LocalDataSource
import com.kabukabu.driver.features.auth.data.entity.req_body.UploadPersonalDetailsReqBody
import com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel.AuthViewModel
import com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel.OnboardDriverPersonalDetailsUiState
import com.kabukabu.driver.features.home.presentation.DriverViewModel
import org.koin.androidx.compose.koinViewModel
import java.util.regex.Pattern


@Composable
fun DriverBioDataScreen(
//    navToSelectVehicleScreen: () -> Unit,
    authViewModel: AuthViewModel = koinViewModel()
) {

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    // Create DriverViewModel at Activity scope so it's shared across Splash and Home
    val activityOwner = context as ViewModelStoreOwner
    val driverViewModel: DriverViewModel = viewModel(viewModelStoreOwner = activityOwner)

    var showStateSheet by remember { mutableStateOf(false) }
    var showCarCategorySheet by remember { mutableStateOf(false) }

    val uiState = authViewModel.uploadPersonalDetailsReqBody.collectAsState().value

    var fullName by remember { mutableStateOf(uiState?.fullName ?: "") }
    var phoneNumber by remember { mutableStateOf(uiState?.phoneNumber ?: "") }
    var email by remember { mutableStateOf(uiState?.email ?: "") }
    var houseAddress by remember { mutableStateOf(uiState?.houseAddress ?: "") }
    var city by remember { mutableStateOf(uiState?.city ?: "") }
    var state by remember { mutableStateOf(uiState?.state ?: "") }
    var carCategory by remember { mutableStateOf(uiState?.carCategory ?: "") }

    val isInputValidated = remember { mutableStateOf(false) }
    val fullNameError = remember { mutableStateOf("") }
    val phoneNumberError = remember { mutableStateOf("") }
    val emailError = remember { mutableStateOf("") }
    val houseAddressError = remember { mutableStateOf("") }
    val cityError = remember { mutableStateOf("") }
    val stateError = remember { mutableStateOf("") }
    val carCategoryError = remember { mutableStateOf("") }

    if (showStateSheet) {
        SelectStateSheet(
            onDismiss = { showStateSheet = false },
            onSelectState = { selectedState ->
                state = selectedState
                showStateSheet = false
                focusManager.clearFocus()
            }
        )
    }

    if (showCarCategorySheet) {
        SelectCarCategorySheet(
            onDismiss = { showCarCategorySheet = false },
            onSelectCategory = { carCat ->
                carCategory = carCat
                showCarCategorySheet = false
                focusManager.clearFocus()
            }
        )
    }

    Scaffold(
        bottomBar = {
            KabuBottomButton(
                modifier = Modifier.padding(16.dp),
                text = "Continue",
                isLoading = authViewModel.onboardDriverBiodataUiState ==
                        OnboardDriverPersonalDetailsUiState.Loading,
                onClick = {
                    isInputValidated.value = validateDriverDetails(
                        fullName = fullName,
                        phoneNumber = phoneNumber,
                        email = email.trim(),
                        houseAddress = houseAddress,
                        city = city,
                        state = state,
                        carCategory = carCategory,
                        fullNameError = fullNameError,
                        phoneNumberError = phoneNumberError,
                        emailError = emailError,
                        houseAddressError = houseAddressError,
                        cityError = cityError,
                        stateError = stateError,
                        carCategoryError = carCategoryError,
                    )

                    if (isInputValidated.value) {
                        val driverBiodata = UploadPersonalDetailsReqBody(
                            fullName = fullName,
                            phoneNumber = phoneNumber,
                            email = email.trim(),
                            houseAddress = houseAddress,
                            city = city,
                            state = state,
                            carOwner = false,
                            carCategory = carCategory
                        )

                        // persist values locally before navigating
                        authViewModel.setUploadUserDetailsReqBody(driverBiodata)
//                        navToSelectVehicleScreen()
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            KabuDivider(height = 24.0.dp)

            ScreenTitleText(
                title = "Welcome",
                subtitle = "Tell us about you",
                bottomPadding = 16
            )

            FormTextfield(
                title = "Full Name",
                value = fullName,
                hintText = "John Doe",
                onTextChanged = { newText ->
                    val filtered = newText.filter { it.isLetter() }
                    fullName = filtered
                },
                validationError = fullNameError.value.isNotEmpty(),
                validationErrorMessage = fullNameError.value
            )

            FormTextfield(
                title = "Email Address",
                value = email,
                hintText = "example@gmail.com",
                onTextChanged = { email = it },
                validationError = emailError.value.isNotEmpty(),
                validationErrorMessage = emailError.value
            )

            FormTextfield(
                title = "Phone number",
                value = phoneNumber,
                hintText = "08012345678",
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
                title = "House Address",
                value = houseAddress,
                hintText = "House address here",
                onTextChanged = { houseAddress = it },
                validationError = houseAddressError.value.isNotEmpty(),
                validationErrorMessage = houseAddressError.value
            )

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
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

            FormTextfieldDropdown(
                value = carCategory,
                title = "Car Category",
                onClick = { showCarCategorySheet = true },
                validationError = carCategoryError.value.isNotEmpty(),
                validationErrorMessage = carCategoryError.value
            )

            // Add space so content isn't hidden behind the button
//            KabuDivider(height = 100.dp)
        }
    }
}


private fun validateDriverDetails(
    fullName: String,
    phoneNumber: String,
    email: String,
    houseAddress: String,
    city: String,
    state: String,
    carCategory: String,
    fullNameError: MutableState<String>,
    phoneNumberError: MutableState<String>,
    emailError: MutableState<String>,
    houseAddressError: MutableState<String>,
    cityError: MutableState<String>,
    stateError: MutableState<String>,
    carCategoryError: MutableState<String>,

    ): Boolean {

    var isValid = true
    fullNameError.value = ""
    phoneNumberError.value = ""
    emailError.value = ""
    houseAddressError.value = ""
    cityError.value = ""
    stateError.value = ""
    carCategoryError.value = ""

    if (fullName.isEmpty() || fullName.length < 6) {
        fullNameError.value = "Full name is too short"
        isValid = false
    } else if (!fullName.matches(Regex("^[A-Za-z\\s]+\$"))) {
        fullNameError.value = "Full name must contain only letters"
        isValid = false
    }


    //validate inputted phone number
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
        houseAddressError.value = "House address is too short"
        isValid = false
    }

    if (city.isEmpty() || city.length < 3) {
        cityError.value = "Invalid city"
        isValid = false
    }


    if (state.isEmpty()) {
        stateError.value = "Select a State"
        isValid = false
    }

    if (carCategory.isEmpty()) {
        carCategoryError.value = "Choose car category"
        isValid = false
    }

    return isValid
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectStateSheet(
    onDismiss: () -> Unit,
    onSelectState: (String) -> Unit
) {

    val nigeriaStates = LocalDataSource().nigeriaStates

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
                items(nigeriaStates) { state ->
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
                                    onSelectState(state)
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectCarCategorySheet(
    onDismiss: () -> Unit,
    onSelectCategory: (String) -> Unit
) {

    val carCategories = LocalDataSource().carCategories

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
                text = "Select Car Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(carCategories) { state ->
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
                                    onSelectCategory(state)
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
