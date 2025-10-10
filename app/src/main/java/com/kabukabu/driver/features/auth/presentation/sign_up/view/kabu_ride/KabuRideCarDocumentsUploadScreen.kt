package com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kabukabu.driver.R
import com.kabukabu.driver.components.ui.AnnotatedTextfieldTitle
import com.kabukabu.driver.components.ui.CameraXCaptureImage
import com.kabukabu.driver.components.ui.CustomLinearProgressIndicator
import com.kabukabu.driver.components.ui.FormTextfield
import com.kabukabu.driver.components.ui.GrayBackgroundContainer
import com.kabukabu.driver.components.ui.KabuBottomButtonRowScope
import com.kabukabu.driver.components.ui.KabuTransparentBottomButtonRowScope
import com.kabukabu.driver.components.ui.ScreenTitleText
import com.kabukabu.driver.components.ui.TitleText
import com.kabukabu.driver.components.ui.displayToastMessage
import com.kabukabu.driver.core.navigation.Navigator

@Composable
fun KabuRideCarDocumentsUploadScreen(navigation: Navigator) {

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var vehicleLicenseUri by remember { mutableStateOf<Uri?>(null) }
    var photoError by remember { mutableStateOf("") }
    var launchCamera by remember { mutableStateOf(false) }
    var isPreviewVisible by remember { mutableStateOf(true) }

    Scaffold { paddingValues ->
        if (launchCamera) {
            // Show the camera view
            CameraXCaptureImage(
                onImageCaptured = { uri ->
                    if (uri != null) {
                        vehicleLicenseUri = uri
                    }
                    launchCamera = false
                },
                onError = { error ->
                    photoError = error
                    launchCamera = false
                }
            )
        } else {
            // Show the main form
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(top = 30.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
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
                        imageUri = vehicleLicenseUri,
                        onClick = { launchCamera = true }
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
                        imageUri = selectedImageUri,
                        onClick = { launchCamera = true }
                    )

                    FormTextfield(
                        title = "Driver's License Number",
                        value = "",
                        hintText = "ABC1234567",
                        onTextChanged = {}
                    )

                    CaptureDocumentItem(
                        title = "Issuance Certificate",
                        label = "Tap here to capture",
                        imageUri = selectedImageUri,
                        onClick = { launchCamera = true }
                    )

                    FormTextfield(
                        title = "Issuance Certificate Number",
                        value = "",
                        hintText = "ABC1234567",
                        onTextChanged = {}
                    )

                    CaptureDocumentItem(
                        title = "Proof of Ownership",
                        label = "Tap here to capture",
                        imageUri = vehicleLicenseUri,
                        onClick = { launchCamera = true }
                    )

                    FormTextfield(
                        title = "Proof of Ownership Number",
                        value = "",
                        hintText = "ABC1234567",
                        isCompulsory = false,
                        onTextChanged = {}
                    )

                    CaptureDocumentItem(
                        title = "Road Worthiness Certificate",
                        label = "Tap here to capture",
                        imageUri = vehicleLicenseUri,
                        onClick = { launchCamera = true }
                    )

                    FormTextfield(
                        title = "Road Worthiness Certificate",
                        value = "",
                        hintText = "ABC1234567",
                        isCompulsory = false,
                        onTextChanged = {}
                    )

                    CaptureDocumentItem(
                        title = "Hackney Permit",
                        label = "Tap here to capture",
                        isCompulsoryField = false,
                        imageUri = vehicleLicenseUri,
                        onClick = { launchCamera = true }
                    )

                    TitleText(
                        "Tap to reupload",
                        color = Color.Gray,
                        fontWeight = FontWeight.W500
                    )

                    FormTextfield(
                        title = "Hackney Permit Number",
                        value = "",
                        hintText = "Doc-IMHG-0088",
                        isCompulsory = false,
                        onTextChanged = {}
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    KabuTransparentBottomButtonRowScope(
                        "Previous",
                        icon = R.drawable.arrow_left
                    )

                    KabuBottomButtonRowScope(
                        "Next",
                        icon = R.drawable.arrow_right,
                        onClick = { navigation.navToKabuRideGuarantorDetailsScreen() }
                    )
                }
            }
        }
    }
}




@Composable
fun CaptureDocumentItem(
    title: String,
    label: String,
    imageUri: Uri?,
    isCompulsoryField: Boolean = true,
    photoBoxError: String = "",
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        AnnotatedTextfieldTitle(title, isCompulsoryField)

        if (imageUri == null || imageUri == Uri.EMPTY) {
            CaptureDocumentBox(
                label = label,
                onClick = onClick
            )
            if (photoBoxError.isNotEmpty()) {
                TitleText(
                    text = photoBoxError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12
                )
            }

        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onClick() }, // allow recapture/reupload
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUri)
                        .crossfade(true)
                        .size(800)
                        .build(),
                    contentDescription = "Captured document preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Overlay a semi-transparent layer with a “Tap to change” text
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f))
                        .align(Alignment.Center)
                ) {
                    TitleText(
                        text = "Tap to reupload",
                        color = Color.White,
                        fontWeight = FontWeight.W500,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun CaptureDocumentBox(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
//            .height(100.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFF1F1F1))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.doc_upload),
                contentDescription = "image placeholder icon",
                modifier = Modifier.size(22.dp),
                tint = Color.Gray
            )
            TitleText(
                label,
                topPadding = 12
            )


        }
    }

}



//@Composable
//internal fun CaptureDocumentItem(
//    title: String,
//    label: String,
//    isCompulsoryField: Boolean = true,
//    showImageSelection: Boolean = true,
//    selectedImageUri: Uri?,
//    onImageSelected: (Uri?) -> Unit,
//    photoBoxError: MutableState<String>,
//    modifier: Modifier = Modifier
//) {
//    val context = LocalContext.current
//
//    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.PickVisualMedia(),
//        onResult = { uri ->
//            onImageSelected(uri)
//        }
//    )
//
//    Column(modifier = modifier) {
//        AnnotatedTextfieldTitle(title, isCompulsoryField)
//
//        if (selectedImageUri == null || selectedImageUri == Uri.EMPTY) {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(200.dp)
//                    .clip(RoundedCornerShape(6.dp))
//                    .background(Color(0xFFF1F1F1))
//                    .clickable {
//                        if (showImageSelection) {
//                            singlePhotoPickerLauncher.launch(
//                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
//                            )
//                        } else {
//                            context.displayToastMessage("Please fill all required fields before uploading.")
//                        }
//                    },
//                contentAlignment = Alignment.Center
//            ) {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.Center
//                ) {
//                    Icon(
//                        painter = painterResource(R.drawable.doc_upload),
//                        contentDescription = "upload icon",
//                        modifier = Modifier.size(24.dp),
//                        tint = Color.Gray
//                    )
//                    TitleText(
//                        label,
//                        topPadding = 12
//                    )
//                }
//            }
//
//            if (photoBoxError.value.isNotEmpty()) {
//                TitleText(
//                    text = photoBoxError.value,
//                    color = MaterialTheme.colorScheme.error,
//                    fontSize = 12
//                )
//            }
//
//        } else {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clip(RoundedCornerShape(8.dp))
//            ) {
//                AsyncImage(
//                    model = ImageRequest.Builder(LocalContext.current)
//                        .data(selectedImageUri)
//                        .size(800)
//                        .crossfade(true)
//                        .build(),
//                    contentDescription = "Captured document",
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(200.dp)
//                        .clickable {
//                            singlePhotoPickerLauncher.launch(
//                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
//                            )
//                        },
//                    contentScale = ContentScale.Crop
//                )
//
//            }
//        }
//    }
//}

