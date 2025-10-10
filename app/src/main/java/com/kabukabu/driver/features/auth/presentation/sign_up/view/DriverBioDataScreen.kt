package com.kabukabu.driver.features.auth.presentation.sign_up.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kabukabu.driver.components.ui.FormTextfield
import com.kabukabu.driver.components.ui.KabuBottomButton
import com.kabukabu.driver.components.ui.KabuDivider
import com.kabukabu.driver.components.ui.RowScopeFormTextfield
import com.kabukabu.driver.components.ui.ScreenTitleText
import com.kabukabu.driver.components.ui.displayToastMessage
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


    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
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
                    value = "",
                    hintText = "John Doe",
                    onTextChanged = {}
                )

                FormTextfield(
                    title = "Email Address",
                    value = "",
                    hintText = "example@gmail.com",
                    onTextChanged = {}
                )

                FormTextfield(
                    title = "House Address",
                    value = "",
                    hintText = "House address here",
                    onTextChanged = {}
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    RowScopeFormTextfield(
                        title = "City",
                        value = "",
                        hintText = "City here",
                        isDropdown = false,
                        onTextChanged = {}
                    )
                    RowScopeFormTextfield(
                        title = "State",
                        value = "",
                        hintText = "Abia State",
                        isDropdown = true,
                        onTextChanged = {}
                    )
                }
            }

            KabuBottomButton(
                text = "Continue",
                isLoading = authViewModel.onboardDriverBiodataUiState ==
                    OnboardDriverPersonalDetailsUiState.Loading,
                onClick = {
                    val driverBiodata = DriverPersonalDetailsReqBody(
                        fullName = ,
                        phoneNumber = ,
                        email = ,
                        houseAddress = ,
                        city = ,
                        state = ,
                        carOwner = false,
                    )
                    authViewModel.sendDriverBioData(driverPersonalDetailsReqBody = driverBiodata)
                }
            )
        }
    }
}


