package com.kabukabu.driver.features.auth.presentation.sign_up.view

import android.content.Context
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kabukabu.driver.components.ui.FormTextfield
import com.kabukabu.driver.components.ui.FormTextfieldDropdown
import com.kabukabu.driver.components.ui.KabuBottomButton
import com.kabukabu.driver.components.ui.KabuDivider
import com.kabukabu.driver.components.ui.RowScopeFormTextfield
import com.kabukabu.driver.components.ui.ScreenTitleText
import com.kabukabu.driver.components.ui.displayToastMessage
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
                    onTextChanged = { fullName = it }
                )

                FormTextfield(
                    title = "Email Address",
                    value = email,
                    hintText = "example@gmail.com",
                    onTextChanged = { email = it }
                )

                FormTextfield(
                    title = "Phone number",
                    value = phoneNumber,
                    hintText = "08012345678",
                    onTextChanged = { phoneNumber = it }
                )

                FormTextfield(
                    title = "House Address",
                    value = houseAddress,
                    hintText = "House address here",
                    onTextChanged = { houseAddress = it }
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
                        imeAction = ImeAction.Done
                    )
                    RowScopeFormTextfield(
                        title = "State",
                        value = state,
                        hintText = "Abia",
                        isDropdown = true,
                        onClick = { showStateSheet = true },
                        onTextChanged = {}
                    )
                }

                FormTextfieldDropdown(
                    value = carCategory,
                    title = "Car Category",
                    onClick = { showCarCategorySheet = true }
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
                        phoneNumberError = TODO(),
                        emailError = TODO(),
                        houseAddressError = TODO(),
                        cityError = TODO(),
                        stateError = TODO(),
                        carCategoryError = TODO(),
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
//    context: Context,
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
    } else if (!Pattern.matches("(0|234)[7-9][01][0-9]{8}", phoneNumber)) {
        phoneNumberError.value = "Enter a valid phone number"
        isValid = false
    }

    if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email)
            .matches()
    ) {
        phoneNumberError.value = "Invalid email address"
        isValid = false
    }

    if (houseAddress.isEmpty() || houseAddress.length < 10) {
        houseAddressError.value = "Invalid house address"
        isValid = false
    }

    if (city.isEmpty() || city.length < 3) {
        houseAddressError.value = "Invalid city"
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


//fun validateAirtimeInput(
//    context: Context,
//    phoneNumberInput: String,
//    networkInput: String,
//    amountInput: String,
//    phoneNumberError: MutableState<String>,
//    networkError: MutableState<String>,
//    amountError: MutableState<String>
//): Boolean {
//    var isValid = true
//    phoneNumberError.value = ""
//    networkError.value = ""
//    amountError.value = ""
//
//    //validate inputted phone number
//    if (phoneNumberInput.isBlank()) {
//        phoneNumberError.value = context.getString(R.string.please_enter_a_phone_number)
//        isValid = false
//    } else if (!Pattern.matches("(0|234)[7-9][01][0-9]{8}", phoneNumberInput)) {
//        phoneNumberError.value = context.getString(R.string.enter_a_valid_phone_number)
//        isValid = false
//    }
//
//    //validate network input
//    if (networkInput.isBlank()) {
//        networkError.value = context.getString(R.string.please_select_a_network)
//        isValid = false
//    }
//    //validate amount
//    if (amountInput.isEmpty()) {
//        amountError.value = context.getString(R.string.invalid_amount)
//        isValid = false
//    } else if (amountInput.toDouble() > 1000000) {
//        amountError.value = context.getString(R.string.amount_cannot_be_greater_than_1_000_000)
//        isValid = false
//    } else if (amountInput.toDouble() < 50) {
//        amountError.value = context.getString(R.string.minimum_recharge_is_50)
//        isValid = false
//    }
//    return isValid
//}

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
