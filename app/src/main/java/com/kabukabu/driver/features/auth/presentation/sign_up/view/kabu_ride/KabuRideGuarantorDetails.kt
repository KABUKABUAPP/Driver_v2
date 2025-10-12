package com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kabukabu.driver.R
import com.kabukabu.driver.components.ui.CustomLinearProgressIndicator
import com.kabukabu.driver.components.ui.FormTextfield
import com.kabukabu.driver.components.ui.FormTextfieldDropdown
import com.kabukabu.driver.components.ui.GrayBackgroundContainer
import com.kabukabu.driver.components.ui.KabuBottomButtonRowScope
import com.kabukabu.driver.components.ui.KabuTransparentBottomButtonRowScope
import com.kabukabu.driver.components.ui.RowScopeFormTextfield
import com.kabukabu.driver.components.ui.ScreenTitleText
import com.kabukabu.driver.components.ui.displayToastMessage
import com.kabukabu.driver.core.data.local.LocalDataSource
import com.kabukabu.driver.core.navigation.Navigator


@Composable
fun KabuRideGuarantorDetail(navigator: Navigator) {

    var showStateSheet by remember { mutableStateOf(false) }

    var guarantorImageUri by remember { mutableStateOf<Uri?>(null) }
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var houseAddress by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }

    if (showStateSheet) {
        SelectGuarantorRelationshipModal(
            onDismiss = { showStateSheet = false },
            onSelectRelationship = { selectedState ->
                state = selectedState
                showStateSheet = false
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
                progress = 1.0f,
                modifier = Modifier.padding(bottom = 50.dp)
            )

            ScreenTitleText(
                title = "Guarantor Details",
                subtitle = "Tell us about your guarantors",
                bottomPadding = 16
            )

            GrayBackgroundContainer {

                ProfileCard(
                    selectedImageUri = guarantorImageUri,
                    onImageSelected = { guarantorImageUri = it }
                )

                FormTextfield(
                    title = "Full Name",
                    value = fullName,
                    hintText = "John Doe",
                    onTextChanged = { fullName = it }
                )

                FormTextfieldDropdown(
                    "Relationship",
                    isCompulsory = false,
                    onClick = {}
                )

                FormTextfield(
                    title = "Email Address",
                    value = email,
                    hintText = "Email",
                    onTextChanged = { email = it }
                )

                FormTextfield(
                    title = "Phone number",
                    value = phoneNumber,
                    hintText = "Email",
                    onTextChanged = { phoneNumber = it }
                )

                FormTextfield(
                    title = "House address",
                    value = houseAddress,
                    hintText = "House address",
                    onTextChanged = { houseAddress = it }
                )


                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    RowScopeFormTextfield(
                        title = "City",
                        value = city,
                        hintText = "City here",
                        isDropdown = false,
                        onTextChanged = { city = it },
                        imeAction = ImeAction.Done
                    )
                    RowScopeFormTextfield(
                        title = "State",
                        value = state,
                        hintText = "Abia",
                        isDropdown = true,
                        onClick = { showStateSheet = true },
                        onTextChanged = {}
                    )
                }

                FormTextfield(
                    title = "Referral Code (Optional)",
                    value = "",
                    hintText = "Code here",
                    onTextChanged = {}
                )

            }

            Row(
                modifier = Modifier.padding(top = 30.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                KabuTransparentBottomButtonRowScope(
                    "Previous",
                    icon = R.drawable.arrow_left,
                    onClick = { navigator.navigateUp() }
                )

                KabuBottomButtonRowScope(
                    "Submit",
                    icon = R.drawable.arrow_right,
                    onClick = { navigator.navToKabuRidePendingAccountApprovalScreen() }
                )
            }

        }

    }
}

@OptIn( ExperimentalMaterial3Api::class)
@Composable
private fun SelectGuarantorRelationshipModal(
    onDismiss: () -> Unit,
    onSelectRelationship: (String) -> Unit
) {

    val relationship = LocalDataSource().guarantorRelationship

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
                items(relationship) { state ->
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
                                    onSelectRelationship(state)
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


@Composable
fun ProfileCard(
    selectedImageUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
//    showImageSelection: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> onImageSelected(uri) }
    )

    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(color = Color(0xFFF8C34A))
            .clickable {
//                if (showImageSelection) {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
//                } else {
                context.displayToastMessage("Please complete required fields before uploading.")
//                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (selectedImageUri == null || selectedImageUri == Uri.EMPTY) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Icon",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(selectedImageUri)
                    .size(800)
                    .crossfade(true)
                    .build(),
                contentDescription = "Selected profile image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}
