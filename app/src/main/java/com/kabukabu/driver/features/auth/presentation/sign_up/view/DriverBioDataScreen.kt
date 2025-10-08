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
import com.kabukabu.driver.components.ui.KabuSpacer
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
//
//                TitleText(
//                    text = "Welcome",
//                    fontSize = 20,
//                    fontWeight = FontWeight.W600,
//                    bottomPadding = 8,
//                )
//                TitleText(
//                    text = "Tell us about you",
//                    fontSize = 15,
//                    bottomPadding = 12,
//                )

                FormTextfield(
                    title = "Full Name",
                    text = "",
                    hintText = "John Doe",
                    onTextChanged = {}
                )

                FormTextfield(
                    title = "Email Address",
                    text = "",
                    hintText = "example@gmail.com",
                    onTextChanged = {}
                )

                FormTextfield(
                    title = "House Address",
                    text = "",
                    hintText = "House address here",
                    onTextChanged = {}
                )

                Row {
                    RowScopeFormTextfield(
                        title = "City",
                        text = "",
                        hintText = "City here",
                        isDropdown = false,
                        onTextChanged = {}
                    )
                    KabuSpacer(16)
                    RowScopeFormTextfield(
                        title = "State",
                        text = "",
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


