package com.kabukabu.driver.features.auth.presentation.sign_up.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kabukabu.driver.components.ui.KabuBottomButton
import com.kabukabu.driver.components.ui.KabuDivider
import com.kabukabu.driver.components.ui.KabuOutlinedTextField
import com.kabukabu.driver.components.ui.KabuOutlinedTextFieldWithTrailingIconButton
import com.kabukabu.driver.components.ui.KabuSpacer
import com.kabukabu.driver.components.ui.ScreenTitleText
import com.kabukabu.driver.components.ui.TitleText

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


@Composable
internal fun FormTextfield(
    title: String,
    text: String,
    hintText: String,
    onTextChanged: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .background(color = Color(0x4DF1F1F1))
            .padding(vertical = 12.dp)
            .fillMaxWidth()
    ) {
        Column {
            AnnotatedTextfieldTitle(title = title)
            KabuOutlinedTextField(
                value = text,
                onTextChanged = onTextChanged,
                placeholderText = hintText,
                textFieldColors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF1F1F1),
                    focusedContainerColor = Color(0xFFF1F1F1),
                )
            )
        }
    }
}

@Composable
internal fun RowScope.RowScopeFormTextfield(
    title: String,
    text: String,
    hintText: String,
    isDropdown: Boolean,
    onTextChanged: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .background(color = Color(0x4DF1F1F1))
            .padding(vertical = 12.dp)
            .weight(1f),
//            .fillMaxWidth()
    ) {
        Column {
            AnnotatedTextfieldTitle(title = title)
            if (isDropdown) {
                KabuOutlinedTextFieldWithTrailingIconButton(
                    value = text,
                    onTextChanged = onTextChanged,
                    placeholderText = hintText,
                    textFieldColors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF1F1F1),
                        focusedContainerColor = Color(0xFFF1F1F1),
                    )
                )
            } else {
                KabuOutlinedTextField(
                    value = text,
                    onTextChanged = onTextChanged,
                    placeholderText = hintText,
                    textFieldColors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF1F1F1),
                        focusedContainerColor = Color(0xFFF1F1F1),
                    )
                )
            }

        }
    }
}

@Composable
internal fun AnnotatedTextfieldTitle(title: String) {
    Row(
        modifier = Modifier.padding()
    ) {
        TitleText(title, endPadding = 3, fontSize = 15)
        TitleText(
            "*",
            fontSize = 15,
            color = MaterialTheme.colorScheme.error
        )

    }
}
