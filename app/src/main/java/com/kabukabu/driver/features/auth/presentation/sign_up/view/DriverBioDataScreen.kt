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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
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
import org.koin.androidx.compose.koinViewModel
import java.util.regex.Pattern


@Composable
fun DriverBioDataScreen(
    navToSelectVehicleScreen: () -> Unit,
    authViewModel: AuthViewModel = koinViewModel()
) {
//    val driverUiState = authViewModel.onboardDriverBiodataUiState
//    val context = LocalContext.current

    var showStateSheet by remember { mutableStateOf(false) }
    var showCarCategorySheet by remember { mutableStateOf(false) }

    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var houseAddress by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var carCategory by remember { mutableStateOf("") }


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
            }
        )
    }

    if (showCarCategorySheet) {
        SelectCarCategorySheet(
            onDismiss = { showCarCategorySheet = false },
            onSelectCategory = { carCat ->
                carCategory = carCat
                showCarCategorySheet = false
            }
        )
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {

                KabuDivider(height = 24.0.dp)

                ScreenTitleText(
                    title = "Welcome",
                    subtitle = "Tell us about you"
                )

                FormTextfield(
                    title = "Full Name",
                    value = fullName,
                    hintText = "John Doe",
                    onTextChanged = { fullName = it },
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
                    onTextChanged = {
                        if (phoneNumber.length <= 11) {
                            phoneNumber = it
                        }
                    },
                    keyboardType = "number",
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

                FormTextfieldDropdown(
                    value = carCategory,
                    title = "Car Category",
                    onClick = { showCarCategorySheet = true },
                    validationError = carCategoryError.value.isNotEmpty(),
                    validationErrorMessage = carCategoryError.value

                )
            }


            KabuBottomButton(
                text = "Continue",
                isLoading = authViewModel.onboardDriverBiodataUiState ==
                        OnboardDriverPersonalDetailsUiState.Loading,
                onClick = {
                    isInputValidated.value = validateDriverDetails(
                        fullName = fullName,
                        phoneNumber = phoneNumber,
                        email = email,
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
                            fullName = "Olad djei",
                            phoneNumber = phoneNumber,
                            email = email,
                            houseAddress = "Isolo",
                            city = "Leventis",
                            state = "Lagos",
                            carOwner = false,
                            carCategory = "REGULAR"
                        )
                        //                    val driverBiodata = UploadPersonalDetailsReqBody(
//                        fullName = fullName,
//                        phoneNumber = phoneNumber,
//                        email = email,
//                        houseAddress = houseAddress,
//                        city = city,
//                        state = state,
//                        carOwner = false,
//                        carCategory = carCategory
//                    )


                        //persist values locally before navigating
                        authViewModel.setUploadUserDetailsReqBody(driverBiodata)
                        navToSelectVehicleScreen()
                    }
                }
            )
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
        fullNameError.value = "Fullname is not valid"
        isValid = false
    }

    //validate inputted phone number
    if (phoneNumber.isBlank()) {
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
