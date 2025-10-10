package com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kabukabu.driver.R
import com.kabukabu.driver.components.ui.CustomLinearProgressIndicator
import com.kabukabu.driver.components.ui.FormTextfield
import com.kabukabu.driver.components.ui.GrayBackgroundContainer
import com.kabukabu.driver.components.ui.KabuBottomButtonRowScope
import com.kabukabu.driver.components.ui.KabuTransparentBottomButtonRowScope
import com.kabukabu.driver.components.ui.ScreenTitleText
import com.kabukabu.driver.core.navigation.Navigator

@Composable
fun KabuRideDocumentsReUploadScreen(navigator: Navigator) {

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val photoError = remember { mutableStateOf("") }

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
                progress = 0.67f,
                modifier = Modifier.padding(bottom = 50.dp)
            )

            ScreenTitleText(
                title = "Documents",
                subtitle = "Upload your car documents",
                bottomPadding = 16
            )

            GrayBackgroundContainer {

                CaptureDocumentItem(
                    title = "Vehicle License",
                    label = "Tap here to capture",
                    selectedImageUri = selectedImageUri,
                    onImageSelected = { uri ->
                        selectedImageUri = uri
                    },
                    photoBoxError = photoError
                )

                FormTextfield(
                    title = "Vehicle License Number",
                    value = "",
                    hintText = "ABC1234567",
                    onTextChanged = {}
                )

                CaptureDocumentItem(
                    title = "Driver’s License",
                    label = "Tap here to capture",
                    selectedImageUri = selectedImageUri,
                    onImageSelected = { uri ->
                        selectedImageUri = uri
                    },
                    photoBoxError = photoError
                )

                FormTextfield(
                    title = "Driver's License Number",
                    value = "",
                    hintText = "ABC1234567",
                    onTextChanged = {}
                )

            }

            Row(
                modifier = Modifier.padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                KabuTransparentBottomButtonRowScope(
                    "Previous",
                    icon = R.drawable.arrow_left,
                    onClick = { navigator.navigateUp() }
                )

                KabuBottomButtonRowScope(
                    "Next",
                    icon = R.drawable.arrow_right
                )
            }

        }

    }

}
