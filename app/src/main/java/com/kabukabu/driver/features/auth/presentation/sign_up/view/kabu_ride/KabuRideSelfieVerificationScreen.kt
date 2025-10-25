package com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride

import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.kabukabu.driver.components.ui.CameraXCaptureImage
import com.kabukabu.driver.components.ui.KabuBottomButton
import com.kabukabu.driver.components.ui.ScreenTitleText
import com.kabukabu.driver.components.ui.displayToastMessage
import com.kabukabu.driver.components.utils_functions.convertUriToFile
import com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel.AuthViewModel
import com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel.EditDriverProfileUiState
import com.kabukabu.driver.features.home.presentation.DriverViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun KabuRideSelfieVerificationScreen(
    onNavToKabuCarDetailsScreen: () -> Unit,
    authViewModel: AuthViewModel = koinViewModel()
    ) {

    val context = LocalContext.current
    // Create DriverViewModel at Activity scope so it's shared across Splash and Home
    val activityOwner = context as ViewModelStoreOwner
    val driverViewModel: DriverViewModel = viewModel(viewModelStoreOwner = activityOwner)

    val uiState = authViewModel.editDriverProfileUiState
//    val context = LocalContext.current

    var selfieUri by remember { mutableStateOf<Uri?>(null) }
    var photoError by remember { mutableStateOf<String?>(null) }
    var isCapturing by remember { mutableStateOf(true) }


    LaunchedEffect(uiState) {
        when (uiState) {
            is EditDriverProfileUiState.Success -> {
                context.displayToastMessage(uiState.response.message)
                authViewModel.resetState()
                onNavToKabuCarDetailsScreen()

            }

            is EditDriverProfileUiState.Error -> {
                context.displayToastMessage(uiState.message)
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
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScreenTitleText(
                title = "Verification",
                subtitle = "Let's put a face to your name",
                bottomPadding = 16
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (selfieUri == null) {
                    CameraXCaptureImage(
                        onImageCaptured = { uri ->
                            selfieUri = uri
                            isCapturing = false
                        },
                        onError = { error ->
                            photoError = error
                        },
                        cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                    )
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(selfieUri),
                        contentDescription = "Captured Selfie",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                photoError?.let { Text(it, color = Color.Red) }
            }

            Spacer(modifier = Modifier.height(16.dp))

            KabuBottomButton(
                text = "Continue",
                isLoading = uiState == EditDriverProfileUiState.Loading,
                onClick = {
//                    onNavToKabuCarDetailsScreen()

                    authViewModel.updateUserImage(convertUriToFile(context, selfieUri))
                },
                enabled = selfieUri != null
            )
        }
    }
}

