package com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride

import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.kabukabu.driver.components.ui.CameraXCaptureImage
import com.kabukabu.driver.components.ui.KabuBottomButton
import com.kabukabu.driver.components.ui.ScreenTitleText
@Composable
fun KabuRideSelfieVerificationScreen(
    onNavToTermsAndCondition: () -> Unit
) {
    var selfieUri by remember { mutableStateOf<Uri?>(null) }
    var photoError by remember { mutableStateOf<String?>(null) }
    var isCapturing by remember { mutableStateOf(true) }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Top Section: Titles ---
            ScreenTitleText(
                title = "Verification",
                subtitle = "Let's put a face to your name",
                bottomPadding = 16
            )

            // --- Camera / Preview Section ---
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
                    // Show captured image
                    Image(
                        painter = rememberAsyncImagePainter(selfieUri),
                        contentDescription = "Captured Selfie",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Optional: show error
                photoError?.let { Text(it, color = Color.Red) }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Capture / Retake Button ---
//            Button(
//                onClick = {
//                    if (selfieUri != null) {
//                        selfieUri = null // Retake
//                        isCapturing = true
//                    } else {
//                        // No-op, capture happens automatically in CameraXCaptureImage
//                    }
//                },
//                modifier = Modifier.padding(top = 8.dp)
//            ) {
//                Text(if (selfieUri != null) "Retake Selfie" else "Capture Selfie")
//            }

            // --- Continue Button ---
            KabuBottomButton(
                text = "Continue",
                onClick = onNavToTermsAndCondition,
                enabled = selfieUri != null
            )
        }
    }
}

