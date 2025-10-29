package com.kabukabu.driver.features.auth.presentation.sign_up.view

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kabukabu.driver.R
import com.kabukabu.driver.components.ui.KabuBottomButton
import com.kabukabu.driver.components.ui.KabuDivider
import com.kabukabu.driver.components.ui.TitleText
import com.kabukabu.driver.components.ui.displayToastMessage
import com.kabukabu.driver.components.ui.getThirtyPercentOfScreenWidth
import com.kabukabu.driver.core.data.local.DataPersistenceViewModel
import com.kabukabu.driver.features.auth.data.entity.req_body.DriverDetailsReqBody
import com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel.AuthViewModel
import com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel.OnboardDriverPersonalDetailsUiState
import com.kabukabu.driver.features.home.presentation.DriverViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SelectVehicleScreen(
    onNavToTermsAndCondition: () -> Unit,
    authViewModel: AuthViewModel = koinViewModel(),
    dataPersistenceViewModel: DataPersistenceViewModel = koinViewModel()

) {

    val context = LocalContext.current
    // Create DriverViewModel at Activity scope so it's shared across Splash and Home
    val activityOwner = context as ViewModelStoreOwner
    val driverViewModel: DriverViewModel = viewModel(viewModelStoreOwner = activityOwner)
    val coroutineScope = rememberCoroutineScope()
    val driverUiState = authViewModel.onboardDriverBiodataUiState
    val userDetails = dataPersistenceViewModel.driverDetailsReqBody.collectAsState().value
    val currentUserDetails by rememberUpdatedState(userDetails)

    var hasVehicle by remember { mutableStateOf<Boolean?>(null) }
    var selectedCar by remember { mutableStateOf<Boolean?>(null) }


    LaunchedEffect(driverUiState) {
        when (driverUiState) {
            is OnboardDriverPersonalDetailsUiState.Success -> {
                context.displayToastMessage(driverUiState.response.message)
                authViewModel.resetState()
                onNavToTermsAndCondition()
            }

            is OnboardDriverPersonalDetailsUiState.Error -> {
                context.displayToastMessage(driverUiState.message)
                authViewModel.resetState()
            }

            else -> {}
        }
    }

    Scaffold(
        bottomBar = {
            KabuBottomButton(
                modifier = Modifier.padding(16.dp),
                text = "Submit",
                isLoading = driverUiState == OnboardDriverPersonalDetailsUiState.Loading,
                onClick = {

                    if (hasVehicle == null) {
                        context.displayToastMessage("No selection made")
                        return@KabuBottomButton
                    }

                    val driverBiodata = DriverDetailsReqBody(
                        fullName = currentUserDetails?.fullName ?: "",
                        phoneNumber = currentUserDetails?.phoneNumber ?: "",
                        email = currentUserDetails?.email ?: "",
                        houseAddress = currentUserDetails?.houseAddress ?: "",
                        city = currentUserDetails?.city ?: "",
                        state = currentUserDetails?.state ?: "",
                        carOwner = hasVehicle,
                        carCategory = currentUserDetails?.carCategory ?: "REGULAR"
                    )

                    coroutineScope.launch {
                        authViewModel.uploadDriverBioData(driverBiodata)
                    }


                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            KabuDivider(height = 30.0.dp)
            KabuDivider(
                height = 6.0.dp,
                width = getThirtyPercentOfScreenWidth(),
                color = MaterialTheme.colorScheme.primary,
            )

            TitleText(
                text = "Do you have a vehicle",
                fontSize = 24,
                fontWeight = FontWeight.W600,
                topPadding = 60,
                bottomPadding = 30,
            )

            TitleText(
                text = "This will enable us know the kind of service to offer",
                fontSize = 14,
                bottomPadding = 16,
                maxLines = 2
            )

            TaxiOwnershipSelector(
                onSelectionChanged = { hasVehicle = it },
                hasTaxi = hasVehicle
            )

            if (hasVehicle == true) {
                VehicleTypeSelector(
                    isSelectedCar = selectedCar,
                    onSelectionChanged = { selectedCar = it },
                )
            }

            if (hasVehicle == false && selectedCar == false) {
                TitleText(
                    "You will be enrolled in the Sharp application, once \n you qualify, a car will be presented to you",
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    fontSize = 15,
                    fontWeight = FontWeight.W400,
                    topPadding = 30
                )
            }

            if (selectedCar == true) {
                TitleText(
                    "Driving your car on Kabukabu enrols you \n to the KabuDrive family",
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    fontSize = 15,
                    fontWeight = FontWeight.W400,
                    topPadding = 30
                )
            } else if (selectedCar == false && hasVehicle == false) {
                TitleText(
                    "Driving your keke on Kabukabu enrols you \n to the KabuKeke family",
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    fontSize = 15,
                    fontWeight = FontWeight.W400,
                    topPadding = 30
                )
            }

            Spacer(Modifier.height(100.dp)) // small gap above button area
        }
    }
}


@Composable
fun VehicleTypeSelector(
    isSelectedCar: Boolean?,
    onSelectionChanged: (Boolean) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TitleText(
            text = "Vehicle type",
            fontSize = 18,
            fontWeight = FontWeight.W500,
            topPadding = 50,
            bottomPadding = 16,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VehicleTypeSelectionCard(
                text = "A car",
                isSelected = isSelectedCar == true,
                icon = R.drawable.taxi,
                onClick = { onSelectionChanged(true) }
            )

            VehicleTypeSelectionCard(
                text = "A Keke",
                isSelected = isSelectedCar == false,
                icon = R.drawable.keke_napep,
                onClick = { onSelectionChanged(false) }
            )
        }
    }
}


@Composable
fun RowScope.VehicleTypeSelectionCard(
    text: String,
    icon: Int,
    isSelected: Boolean,
//    userHasTaxi: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor =
        if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF9F9F9)
    val borderColor =
        if (isSelected) Color.Black else Color.Transparent

    Box(
        modifier = Modifier
            .weight(1f)
            .height(120.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(backgroundColor, RoundedCornerShape(32.dp))
            .border(1.dp, borderColor, RoundedCornerShape(32.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = "vehicle icon",
                modifier = Modifier.size(22.dp)
            )

            KabuDivider(height = 12.dp)
            TitleText(
                text = text,
                color = Color.Black,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15
            )
        }
    }
}

@Composable
fun TaxiOwnershipSelector(
    modifier: Modifier = Modifier,
    hasTaxi: Boolean?,
    onSelectionChanged: (Boolean) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HasCarSelectionCard(
            text = "Yes, I do",
            isSelected = hasTaxi == true,
            icon = R.drawable.taxi_outlined,
            userHasTaxi = hasTaxi == true,
            onClick = { onSelectionChanged(true) }
        )

        HasCarSelectionCard(
            text = "No, I don’t",
            isSelected = hasTaxi == false,
            icon = R.drawable.taxi_outlined,
            userHasTaxi = hasTaxi == true,
            onClick = { onSelectionChanged(false) }
        )
    }
}


@Composable
fun RowScope.HasCarSelectionCard(
    text: String,
    icon: Int,
    isSelected: Boolean,
    userHasTaxi: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor =
        if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF9F9F9)
    val borderColor =
        if (isSelected) Color.Black else Color.Transparent

    val targetHeight = if (userHasTaxi) 90.dp else 130.dp
    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "cardHeightAnimation"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .height(animatedHeight)
            .clip(RoundedCornerShape(32.dp))
            .background(backgroundColor, RoundedCornerShape(32))
            .border(1.dp, borderColor, RoundedCornerShape(32.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = "vehicle icon",
                modifier = Modifier.size(22.dp)
            )

            KabuDivider(height = 12.dp)
            TitleText(
                text = text,
                color = Color.Black,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14
            )
        }
    }
}


