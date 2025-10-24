package com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kabukabu.driver.components.ui.CameraXCaptureImage
import com.kabukabu.driver.components.ui.GrayBackgroundContainer
import com.kabukabu.driver.components.ui.KabuBottomButton
import com.kabukabu.driver.components.ui.ScreenTitleText
import com.kabukabu.driver.components.ui.displayToastMessage
import com.kabukabu.driver.components.utils_functions.convertUriToFile
import com.kabukabu.driver.core.navigation.Navigator
import com.kabukabu.driver.features.auth.data.entity.req_body.ReUploadDocumentReqBody
import com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel.AuthViewModel
import com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel.ReUploadDocUiState
import com.kabukabu.driver.features.home.presentation.DriverViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun KabuRideDocumentsReUploadScreen(
    id: String,
    title: String,
    navigator: Navigator,
    authViewModel: AuthViewModel = koinViewModel()
) {
//    println("document id.........$id")
    val context = LocalContext.current
    // Create DriverViewModel at Activity scope so it's shared across Splash and Home
    val activityOwner = context as ViewModelStoreOwner
    val driverViewModel: DriverViewModel = viewModel(viewModelStoreOwner = activityOwner)

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val photoError = remember { mutableStateOf("") }
    var launchCamera by remember { mutableStateOf(false) }
    val uiState = authViewModel.reUploadDocUiState

    LaunchedEffect(uiState) {
        when (uiState) {

            is ReUploadDocUiState.Success -> {
                context.displayToastMessage(uiState.response.message)
                authViewModel.resetState()
                navigator.navToKabuRideAccountDeclinedScreen()
            }

            is ReUploadDocUiState.Error -> {
                context.displayToastMessage(uiState.message)
                authViewModel.resetState()
            }

            else -> {}
        }
    }
    Scaffold { paddingValues ->

        if (launchCamera) {
            // Show the camera view
            CameraXCaptureImage(
                onImageCaptured = { uri ->
                    selectedImageUri = uri
                    launchCamera = false
                },
                onError = { error ->
                    launchCamera = false
                }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(top = 30.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),

//                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Column {

                    ScreenTitleText(
                        title = "Documents",
                        subtitle = "Re-upload Document",
                        bottomPadding = 16
                    )

                    GrayBackgroundContainer {

                        CaptureDocumentItem(
                            title = formatDeclinedDocumentName(title),
                            label = "Tap here to capture",
                            imageUri = selectedImageUri,
                            onClick = { launchCamera = true }
                        )
                    }
                }


                KabuBottomButton(
                    "Submit",
                    isLoading = uiState == ReUploadDocUiState.Loading,
                    onClick = {
                        if (selectedImageUri?.path.isNullOrEmpty()){
                            return@KabuBottomButton
                        }
                        val reqBody = ReUploadDocumentReqBody(
                            docNumber = id,
                            file = convertUriToFile(context, selectedImageUri)
                        )
                        authViewModel.reUploadDocument(
                            reqBody, id
                        )

                    }
                )

            }
        }

    }

}
