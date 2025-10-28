package com.kabukabu.driver.features.auth.presentation.sign_up.view

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.components.ui.CustomLinearProgressIndicator
import com.kabukabu.driver.components.ui.GrayBackgroundContainer
import com.kabukabu.driver.components.ui.ScreenTitleText
import com.kabukabu.driver.R
import com.kabukabu.driver.components.ui.CameraXCaptureImage
import com.kabukabu.driver.components.ui.FormTextfield
import com.kabukabu.driver.components.ui.FormTextfieldDropdown
import com.kabukabu.driver.components.ui.KabuBottomButtonRowScope
import com.kabukabu.driver.components.ui.KabuDivider
import com.kabukabu.driver.components.ui.KabuSpacer
import com.kabukabu.driver.components.ui.displayToastMessage
import com.kabukabu.driver.components.utils_functions.convertUrisToFiles
import com.kabukabu.driver.core.data.local.DataPersistenceViewModel
import com.kabukabu.driver.core.data.local.LocalDataSource
import com.kabukabu.driver.core.navigation.Navigator
import com.kabukabu.driver.features.auth.data.entity.req_body.UploadCarDetailsReqBody
import com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel.AuthViewModel
import com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel.UploadCarDetailsUiState
import com.kabukabu.driver.features.home.presentation.DriverViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import java.io.File

private enum class CarImageIndex { One, Two, Three, Four }

private enum class ImageSource { Camera, Gallery }

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun KabuRideCarDetailsScreen(
//    navigator: Navigator,
    authViewModel: AuthViewModel = koinViewModel(),
    dataPersistenceViewModel: DataPersistenceViewModel = koinViewModel()
) {

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        authViewModel.fetchCarBrands()
    }

    // Create DriverViewModel at Activity scope so it's shared across Splash and Home
    val activityOwner = context as ViewModelStoreOwner
    val driverViewModel: DriverViewModel = viewModel(viewModelStoreOwner = activityOwner)

    val uploadCarDetailsUiState = authViewModel.uploadCarDetailsUiState

    val imagesUriList = mutableListOf<Uri?>()
    var imagesFileList: List<File>
    val carBrands = dataPersistenceViewModel.carBrands.collectAsState().value

//    val context = LocalContext.current


    var launchCamera by remember { mutableStateOf(false) }

//    var imageUploadChannel by remember { mutableStateOf(null) }

    var showCarBrandModalSheet by remember { mutableStateOf(false) }
    var showCarColourModalSheet by remember { mutableStateOf(false) }
    var showImageSourceModalSheet by remember { mutableStateOf(false) }

    var imageSource by remember { mutableStateOf("") }


    var selectedCarBrand by remember { mutableStateOf("") }
    var carModel by remember { mutableStateOf("") }
    var carYear by remember { mutableStateOf("") }
    var carColour by remember { mutableStateOf("") }
    var plateNumber by remember { mutableStateOf("") }

    var selectedCarImageIndex by remember { mutableStateOf(CarImageIndex.One) }
    var selectedCarImageUriOne by remember { mutableStateOf<Uri?>(null) }
    var selectedCarImageUriTwo by remember { mutableStateOf<Uri?>(null) }
    var selectedCarImageUriThree by remember { mutableStateOf<Uri?>(null) }
    var selectedCarImageUriFour by remember { mutableStateOf<Uri?>(null) }

    val isInputValidated = remember { mutableStateOf(false) }
    val carBrandError = remember { mutableStateOf("") }
    val carModelError = remember { mutableStateOf("") }
    val carYearError = remember { mutableStateOf("") }
    val carColourError = remember { mutableStateOf("") }
    val plateNumberError = remember { mutableStateOf("") }

    val userPreferences = KabukabuDriverApp.getInstance().userPreferences
    val userDetails by userPreferences.userDetails.collectAsState(initial = null)


    if (showCarBrandModalSheet) {
        SelectCarBrandSheet(
            carBrands = carBrands,
            onDismiss = { showCarBrandModalSheet = false },
            onSelectCategory = { selectedBrand ->
                selectedCarBrand = selectedBrand
                showCarBrandModalSheet = false
            }
        )
    }

    if (showCarColourModalSheet) {
        SelectCarColourSheet(
            onDismiss = { showCarColourModalSheet = false },
            onSelectColour = { colour ->
                carColour = colour
                showCarColourModalSheet = false
            }
        )
    }


    if (showImageSourceModalSheet) {
        SelectImageSource(
            onDismiss = { showImageSourceModalSheet = false },
            onSelectCategory = { source ->
                imageSource = source
                showImageSourceModalSheet = false
            }
        )
    }


    LaunchedEffect(uploadCarDetailsUiState) {
        when (uploadCarDetailsUiState) {
            is UploadCarDetailsUiState.Success -> {
                context.displayToastMessage(uploadCarDetailsUiState.response.message)
                authViewModel.resetState()
//                navigator.navToKabuRideCarDocsUpload()
            }

            is UploadCarDetailsUiState.Error -> {
                context.displayToastMessage(uploadCarDetailsUiState.message)
                authViewModel.resetState()
            }

            else -> {}
        }
    }

    LaunchedEffect(userDetails) {
        when (userDetails?.user?.onboardingStep) {
//            3, 4 -> navigator.navToKabuRideCarDocsUpload()
        }
    }

//    LaunchedEffect(imageSource) {
//        launchCamera = true
//    }

    BackHandler(enabled = true) {
        if (launchCamera) {
            launchCamera = false
        }
    }


    Scaffold { paddingValues ->
        if (launchCamera) {
            // Show the camera view
            if (imageSource == "Take a Picture") {
            CameraXCaptureImage(
                onImageCaptured = { uri ->
                    if (uri != null) {
                        when (selectedCarImageIndex) {
                            CarImageIndex.One -> {
                                selectedCarImageUriOne = uri
                            }

                            CarImageIndex.Two -> {
                                selectedCarImageUriTwo = uri
                            }

                            CarImageIndex.Three -> {
                                selectedCarImageUriThree = uri
                            }

                            CarImageIndex.Four -> {
                                selectedCarImageUriFour = uri
                            }

                        }
                    }
                    launchCamera = false
                },
                onError = { error ->
                    launchCamera = false
                },
            )
            } else if (imageSource == "Choose from Gallery") {
                val coroutineScope = rememberCoroutineScope()

                val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia(),
                    onResult = { uri ->
                        coroutineScope.launch(Dispatchers.IO) {
                            uri?.let {
                                // Optionally downscale or validate image here
                                withContext(Dispatchers.Main) {
                                    when (selectedCarImageIndex) {
                                        CarImageIndex.One -> selectedCarImageUriOne = uri
                                        CarImageIndex.Two -> selectedCarImageUriTwo = uri
                                        CarImageIndex.Three -> selectedCarImageUriThree = uri
                                        CarImageIndex.Four -> selectedCarImageUriFour = uri
                                    }
                                    launchCamera = false
                                }
                            }
                        }
                    }
                )

                LaunchedEffect(Unit) {
                    singlePhotoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
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

                        UploadCarImageBoxCamera(
                            selectedImageUri = selectedCarImageUriOne,
                            onClick = {
                                if (imageSource.isEmpty()) {
                                    showImageSourceModalSheet = true
                                    return@UploadCarImageBoxCamera
                                }
                                selectedCarImageIndex = CarImageIndex.One
                                launchCamera = true
                            }
                        )


                        UploadCarImageBoxCamera(
                            selectedImageUri = selectedCarImageUriTwo,
                            onClick = {
                                if (imageSource.isEmpty()) {
                                    showImageSourceModalSheet = true
                                    return@UploadCarImageBoxCamera
                                }
                                selectedCarImageIndex = CarImageIndex.Two
                                launchCamera = true
                            }
                        )
                        UploadCarImageBoxCamera(
                            selectedImageUri = selectedCarImageUriThree,
                            onClick = {
                                if (imageSource.isEmpty()) {
                                    showImageSourceModalSheet = true
                                    return@UploadCarImageBoxCamera
                                }
                                selectedCarImageIndex = CarImageIndex.Three
                                launchCamera = true
                            }
                        )
                        UploadCarImageBoxCamera(
                            selectedImageUri = selectedCarImageUriFour,
                            onClick = {
                                if (imageSource.isEmpty()) {
                                    showImageSourceModalSheet = true
                                    return@UploadCarImageBoxCamera
                                }
                                selectedCarImageIndex = CarImageIndex.Four
                                launchCamera = true
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
                            showCarBrandModalSheet = true
                        },
                        validationError = carBrandError.value.isNotEmpty(),
                        validationErrorMessage = carBrandError.value
                    )

                    FormTextfield(
                        value = carModel,
                        title = "Car Model",
                        hintText = "e.g Corolla",
                        onTextChanged = { carModel = it },
                        validationError = carModelError.value.isNotEmpty(),
                        validationErrorMessage = carModelError.value
                    )

                    FormTextfield(
                        value = carYear,
                        title = "Car Year",
                        hintText = "e.g 2009",
                        onTextChanged = { carYear = it },
                        validationError = carYearError.value.isNotEmpty(),
                        validationErrorMessage = carYearError.value
                    )

                    FormTextfieldDropdown(
                        value = carColour,
                        title = "Car Colour",
                        onClick = {
                            showCarColourModalSheet = true
                        },
                        validationError = carColourError.value.isNotEmpty(),
                        validationErrorMessage = carColourError.value
                    )

                    FormTextfield(
                        value = plateNumber,
                        title = "Plate Number",
                        hintText = "e.g ABC 123 CVGG",
                        imeAction = ImeAction.Done,
                        onTextChanged = { input ->
                            if (!input.contains(" ")) {
                                plateNumber = input
                            }
                        },
                        validationError = plateNumberError.value.isNotEmpty(),
                        validationErrorMessage = plateNumberError.value

                    )

                }

                Row {
                    Spacer(modifier = Modifier.weight(1f))
                    KabuBottomButtonRowScope(
                        "Next", icon = R.drawable.arrow_right,
                        isLoading = uploadCarDetailsUiState == UploadCarDetailsUiState.Loading,
                        onClick = {
//                        navigator.navToKabuRideCarDocsUpload()

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
                            if (imagesUriList.size < 3) {
                                context.displayToastMessage("Select atleast 3 images")
                                return@KabuBottomButtonRowScope
                            }
                            isInputValidated.value = validateCarDetails(
                                carBrand = selectedCarBrand,
                                carModel = carModel,
                                carYear = carYear,
                                carColour = carColour,
                                plateNumber = plateNumber,
                                carBrandError = carBrandError,
                                carModelError = carModelError,
                                carYearError = carYearError,
                                carColourError = carColourError,
                                plateNumberError = plateNumberError
                            )

                            if (isInputValidated.value) {
                                imagesFileList =
                                    convertUrisToFiles(
                                        context = context,
                                        uris = imagesUriList as List<Uri>
                                    )

                                val uploadCarDetails = UploadCarDetailsReqBody(
                                    carBrand = selectedCarBrand,
                                    carModel = carModel,
                                    carYear = carYear,
                                    carColor = carColour,
                                    carPlateNumber = plateNumber,
                                    carImages = imagesFileList
                                )
                                authViewModel.uploadCarDetails(uploadCarDetails)

                            }
                        }
                    )
                }

            }

        }

    }
}

private fun validateCarDetails(
    carBrand: String,
    carModel: String,
    carYear: String,
    carColour: String,
    plateNumber: String,
    carBrandError: MutableState<String>,
    carModelError: MutableState<String>,
    carYearError: MutableState<String>,
    carColourError: MutableState<String>,
    plateNumberError: MutableState<String>,
): Boolean {

    var isValid = true

    carBrandError.value = ""
    carModelError.value = ""
    carYearError.value = ""
    carColourError.value = ""
    plateNumberError.value = ""

    if (carBrand.isEmpty()) {
        carBrandError.value = "Select a car brand"
        isValid = false
    }

    if (carModel.length < 3) {
        carModelError.value = "Invalid car model"
        isValid = false
    }

    if (carYear.isEmpty() || carYear.length < 4) {
        carYearError.value = "Invalid car year"
        isValid = false
    }

    if (carColour.isEmpty()) {
        carColourError.value = "Select a colour"
        isValid = false
    }

    if (plateNumber.isEmpty() || plateNumber.length < 5) {
        plateNumberError.value = "Invalid plate number"
        isValid = false
    }


    return isValid


}


@Composable
fun RowScope.UploadCarImageBoxImagePicker(
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
//                    if (showImageSelection) {
                        singlePhotoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
//                    } else {
//                        context.displayToastMessage("Please fill all required fields before uploading.")
//                    }
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

@Composable
fun RowScope.UploadCarImageBoxCamera(
    modifier: Modifier = Modifier,
    selectedImageUri: Uri?,
    onClick: () -> Unit,
) {

    if (selectedImageUri == null || selectedImageUri == Uri.EMPTY) {
        Box(
            modifier = modifier
                .height(75.dp)
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF1F1F1))
                .clickable { onClick() },
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
                .clickable { onClick() }
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
private fun SelectImageSource(
    onDismiss: () -> Unit,
    onSelectCategory: (String) -> Unit
) {

    val imageSource = listOf("Take a Picture", "Choose from Gallery")
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
                text = "Select Image Source",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(imageSource) { state ->
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
                                    onSelectCategory(state)
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

    val colours = LocalDataSource().carColourModal

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
                text = "Select Colour",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(colours) { modalItem ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF9F9F9), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .clickable{
                                    onSelectColour(modalItem.colorName)
                                    onDismiss()
                                },

                            verticalAlignment = Alignment.CenterVertically,

                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(modalItem.hexCode))
                            )

                            KabuSpacer(12)

//                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = modalItem.colorName,
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }

//                        Text(
//                            text = state,
//                            style = MaterialTheme.typography.bodyLarge,
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .clip(RoundedCornerShape(12.dp))
//                                .clickable {
//                                    onSelectColour(state)
//                                    onDismiss()
//                                }
////                                .background(color = Color(0x2DD3D3D3))
//                                .padding(vertical = 12.dp, horizontal = 16.dp)
//                        )
                    }
                }
            }
        }
    }
}