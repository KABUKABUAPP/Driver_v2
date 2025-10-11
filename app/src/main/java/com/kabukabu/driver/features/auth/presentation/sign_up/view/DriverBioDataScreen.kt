package com.kabukabu.driver.features.auth.presentation.sign_up.view

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
import com.kabukabu.driver.features.auth.data.entity.req_body.DriverPersonalDetailsReqBody
import com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel.AuthViewModel
import com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel.OnboardDriverPersonalDetailsUiState


@Composable
fun DriverBioDataScreen(
    navToSelectVehicleScreen: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {

    val driverUiState = authViewModel.onboardDriverBiodataUiState
    val context = LocalContext.current

    var showStateSheet by remember { mutableStateOf(false) }
    var showCarCategorySheet by remember { mutableStateOf(false) }

    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var houseAddress by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var carCategory by remember { mutableStateOf("") }

    LaunchedEffect(driverUiState) {
        when (driverUiState) {
            is OnboardDriverPersonalDetailsUiState.Success -> {
                context.displayToastMessage(driverUiState.response.message)
              authViewModel.resetState()
                navToSelectVehicleScreen()
            }

            is OnboardDriverPersonalDetailsUiState.Error -> {
                context.displayToastMessage(driverUiState.message)
                authViewModel.resetState()
            }
            else -> {}
        }
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
                    onClick = { showCarCategorySheet = true}
                )
            }


            KabuBottomButton(
                text = "Continue",
                isLoading = authViewModel.onboardDriverBiodataUiState ==
                    OnboardDriverPersonalDetailsUiState.Loading,
                onClick = {
//                    val driverBiodata = DriverPersonalDetailsReqBody(
//                        fullName = fullName,
//                        phoneNumber = phoneNumber,
//                        email = email,
//                        houseAddress = houseAddress,
//                        city = city,
//                        state = state,
//                        carOwner = false,
//                        carCategory = carCategory
//                    )
                    val driverBiodata = DriverPersonalDetailsReqBody(
                        fullName = "Olad djei",
                        phoneNumber = "0911111112",
                        email = email,
                        houseAddress = "Isolo",
                        city = "Leventis",
                        state = "Lagos",
                        carOwner = false,
                        carCategory = "REGULAR"
                    )
                    authViewModel.sendDriverBioData(driverPersonalDetailsReqBody = driverBiodata)
//                    navToSelectVehicleScreen()

                }
            )
        }
    }
}


@OptIn( ExperimentalMaterial3Api::class)
@Composable
private fun SelectStateSheet(
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


@OptIn( ExperimentalMaterial3Api::class)
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
