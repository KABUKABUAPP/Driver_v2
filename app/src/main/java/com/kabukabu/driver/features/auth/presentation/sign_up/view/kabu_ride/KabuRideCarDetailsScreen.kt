package com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kabukabu.driver.components.ui.CustomLinearProgressIndicator
import com.kabukabu.driver.components.ui.GrayBackgroundContainer
import com.kabukabu.driver.components.ui.ScreenTitleText
import com.kabukabu.driver.R
import com.kabukabu.driver.components.ui.FormTextfield
import com.kabukabu.driver.components.ui.FormTextfieldDropdown
import com.kabukabu.driver.components.ui.KabuBottomButtonRowScope
import com.kabukabu.driver.components.ui.KabuDivider
import com.kabukabu.driver.components.ui.KabuOutlinedTextField
import com.kabukabu.driver.components.ui.TitleText
import com.kabukabu.driver.components.ui.displayToastMessage
import com.kabukabu.driver.components.utils_functions.convertUrisToFiles
import com.kabukabu.driver.core.data.local.LocalDataSource
import com.kabukabu.driver.core.navigation.Navigator
import com.kabukabu.driver.features.auth.data.entity.req_body.UploadCarDetailsReqBody
import com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel.AuthViewModel
import java.io.File

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun KabuRideCarDetailsScreen(
    navigator: Navigator,
    authViewModel: AuthViewModel = viewModel()
) {

    LaunchedEffect(Unit) {
        authViewModel.fetchCarBrands()
    }

    val imagesUriList = mutableListOf<Uri?>()
    var imagesFileList = listOf<File>()
    val carBrands = authViewModel.carBrands.collectAsState().value

    val context = LocalContext.current

    var showCarBrandSheet by remember { mutableStateOf(false) }
    var showCarColourSheet by remember { mutableStateOf(false) }

    var selectedCarBrand by remember { mutableStateOf("") }
    var carModel by remember { mutableStateOf("") }
    var carYear by remember { mutableStateOf("") }
    var carColour by remember { mutableStateOf("") }
    var plateNumber by remember { mutableStateOf("") }


    var selectedCarImageUriOne by remember { mutableStateOf<Uri?>(null) }
    var selectedCarImageUriTwo by remember { mutableStateOf<Uri?>(null) }
    var selectedCarImageUriThree by remember { mutableStateOf<Uri?>(null) }
    var selectedCarImageUriFour by remember { mutableStateOf<Uri?>(null) }



    if (showCarBrandSheet) {
        SelectCarBrandSheet(
            carBrands = carBrands,
            onDismiss = { showCarBrandSheet = false },
            onSelectCategory = { selectedBrand ->
                selectedCarBrand = selectedBrand
                showCarBrandSheet = false
            }
        )
    }

    if (showCarColourSheet) {
        SelectCarColourSheet(
            onDismiss = { showCarColourSheet = false },
            onSelectColour = { colour ->
                carColour = colour
                showCarColourSheet = false
            }
        )
    }

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
                progress = 0.34f,
                modifier = Modifier.padding(bottom = 50.dp)
            )

            ScreenTitleText(
                title = "Car Details",
                subtitle = "Give us more information about your car",
                bottomPadding = 16
            )

            GrayBackgroundContainer {

                ScreenTitleText(
                    title = "Car Images",
                    subtitle = "Upload at least 3 images of your car",
                    titleFontSize = 16,
                    subtitleFontSize = 13,
                    bottomPadding = 20
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    UploadCarImageBox(
                        selectedImageUri = selectedCarImageUriOne,
                        onImageSelected = { uri ->
                            selectedCarImageUriOne = uri
//                            imagesList.add(uri)
                        }
                    )
                    UploadCarImageBox(
                        selectedImageUri = selectedCarImageUriTwo,
                        onImageSelected = {
                            selectedCarImageUriTwo = it
//                            imagesList.add(it)
                        }
                    )
                    UploadCarImageBox(
                        selectedImageUri = selectedCarImageUriThree,
                        onImageSelected = {
                            selectedCarImageUriThree = it
//                            imagesList.add(it)

                        }
                    )
                    UploadCarImageBox(
                        selectedImageUri = selectedCarImageUriFour,
                        onImageSelected = {
                            selectedCarImageUriFour = it
//                            imagesList.add(it)
                        }
                    )

                }
            }

            KabuDivider(height = 16.dp)

            GrayBackgroundContainer {

                ScreenTitleText(
                    title = "Car Details",
                    subtitle = "Give us more information about your car",
                    titleFontSize = 16,
                    subtitleFontSize = 13,
                    bottomPadding = 16
                )

                FormTextfieldDropdown(
                    value = selectedCarBrand,
                    title = "Car Brand",
                    onClick = {
                        showCarBrandSheet = true
                    }
                )

                FormTextfield(
                    value = carModel,
                    title = "Car Model",
                    hintText = "e.g Corolla",
                    onTextChanged = { carModel = it }
                )

                FormTextfield(
                    value = carYear,
                    title = "Car Year",
                    hintText = "e.g 2009",
                    onTextChanged = { carYear = it }
                )

                FormTextfieldDropdown(
                    value = carColour,
                    title = "Car Colour",
                    onClick = {
                        showCarColourSheet = true
                    }
                )

                FormTextfield(
                    value = plateNumber,
                    title = "Plate Number",
                    hintText = "e.g ABC 123 CVGG",
                    onTextChanged = { plateNumber = it }
                )

            }

            Row {
                Spacer(modifier = Modifier.weight(1f))
                KabuBottomButtonRowScope(
                    "Next", icon = R.drawable.arrow_right,
                    onClick = {
                        navigator.navToKabuDocumentsUpload()

                        listOf(
                            selectedCarImageUriOne,
                            selectedCarImageUriTwo,
                            selectedCarImageUriThree,
                            selectedCarImageUriFour
                        ).forEach { uri ->
                            if (uri != null) {
                                imagesUriList.add(uri)
                            }
                        }

                        imagesFileList = convertUrisToFiles(context = context, uris = imagesUriList as List<Uri>)

                        val uploadCarDetails = UploadCarDetailsReqBody()
                    }
                )
            }

        }

    }
}





@Composable
internal fun TextfieldSelection(title: String, placeholderText: String) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        TitleText(title)
        KabuOutlinedTextField(
            placeholderText = placeholderText
        )
    }
}


@Composable
fun RowScope.UploadCarImageBox(
    selectedImageUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
    showImageSelection: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            onImageSelected(uri)
        }
    )

    if (selectedImageUri == null || selectedImageUri == Uri.EMPTY) {
        Box(
            modifier = modifier
                .height(75.dp)
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF1F1F1))
                .clickable {
                    if (showImageSelection) {
                        singlePhotoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    } else {
                        context.displayToastMessage("Please fill all required fields before uploading.")
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.image_placeholder),
                contentDescription = "image placeholder icon",
                modifier = Modifier.size(24.dp)
            )
        }
    } else {
        Box(
            modifier = modifier
                .height(75.dp)
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .clickable {
                    singlePhotoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(selectedImageUri)
                    .size(800)
                    .crossfade(true)
                    .build(),
                contentDescription = "Car image preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectCarBrandSheet(
    carBrands: List<String?>,
    onDismiss: () -> Unit,
    onSelectCategory: (String) -> Unit
) {


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
                text = "Select Car Brand",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(carBrands) { state ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                    ) {
                        Text(
                            text = state ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onSelectCategory(state ?: "")
                                    onDismiss()
                                }
                                .padding(vertical = 12.dp, horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectCarColourSheet(
    onDismiss: () -> Unit,
    onSelectColour: (String) -> Unit
) {

    val nigeriaStates = LocalDataSource().carColours

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
                                    onSelectColour(state)
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
