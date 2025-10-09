package com.kabukabu.driver.features.auth.presentation.sign_up.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kabukabu.driver.components.ui.FormTextfield
import com.kabukabu.driver.components.ui.KabuBottomButton
import com.kabukabu.driver.components.ui.KabuDivider
import com.kabukabu.driver.components.ui.RowScopeFormTextfield
import com.kabukabu.driver.components.ui.ScreenTitleText


@Composable
fun DriverBioDataScreen() {
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
                onClick = {}
            )
        }
    }
}


