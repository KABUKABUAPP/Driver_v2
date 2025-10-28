package com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.R
import com.kabukabu.driver.components.ui.KabuDivider
import com.kabukabu.driver.components.ui.ScreenTitleText
import com.kabukabu.driver.components.ui.TitleText
import com.kabukabu.driver.core.data.local.DataPersistenceViewModel
import com.kabukabu.driver.features.auth.data.entity.response.InspectionHub
import com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel.AuthViewModel
import com.kabukabu.driver.features.home.presentation.DriverViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun InspectionHubsScreen(
    onNavToHome: () -> Unit,
//    dataPersistenceViewModel: DataPersistenceViewModel = koinViewModel(),
//    authViewModel: AuthViewModel = koinViewModel()
) {

    val context = LocalContext.current
    // Create DriverViewModel at Activity scope so it's shared across Splash and Home
    val activityOwner = context as ViewModelStoreOwner
    val driverViewModel: DriverViewModel = viewModel(viewModelStoreOwner = activityOwner)
    val dataPersistenceViewModel: DataPersistenceViewModel = koinViewModel()
    val authViewModel: AuthViewModel = koinViewModel()

    val hubsList =
        dataPersistenceViewModel.inspectionsHubs.collectAsState().value?.data ?: emptyList()

    val userPreferences = KabukabuDriverApp.getInstance().userPreferences
    val userDetails by userPreferences.userDetails.collectAsState(initial = null)
    val inspectionCode = userDetails?.user?.driver?.inspectionCode

    LaunchedEffect(userDetails?.user?.isOnboardingComplete) {
        if (userDetails?.user?.isOnboardingComplete == true) {
            onNavToHome()
        }
    }


    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = 30.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Column {
                ScreenTitleText(
                    title = "Inspection",
                    titleFontSize = 22,
                    subtitle = "Please take your car to our nearest \n" +
                            "inspection hub",
                    bottomPadding = 20
                )

                ScreenTitleText(
                    title = userDetails?.user?.driver?.inspectionCode ?: "Not available yet",
                    subtitle = "Driver code",
                    titleFontSize = 24,
                    bottomPadding = 16
                )

                InspectionInstruction(
                    text = "The inspection agent will require your driver code to verify your account."
                )

                InspectionInstruction(
                    text = "Visit the nearest Hub in the next 72 hours to validate and verify your car."
                )

                InspectionInstruction(
                    text = "Ensure you go to the inspection hubs with ALL your car documents"
                )

                KabuDivider(height = 30.dp)

                TitleText(
                    text = "Kabu Inspection Hub closest to you",
                    fontSize = 16,
                    fontWeight = FontWeight.W500,
                    bottomPadding = 16
                )
                InspectionHubsList(hubsList)
            }

            TitleText(
                "This screen will proceed when agent approves your inspection",
                fontSize = 13,
                color = Color.Gray
            )

        }

    }
}

@Composable
private fun InspectionHubsList(hubs: List<InspectionHub>) {

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        itemsIndexed(hubs) { index, data ->
            InspectionHubsListItem(hubData = data)
        }
    }
}


@Composable
private fun InspectionHubsListItem(hubData: InspectionHub) {
    Box(
        modifier = Modifier
            .background(
                color = Color(0xFFFFF5D8),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(end = 16.dp, top = 16.dp, bottom = 16.dp)
            .fillMaxWidth()
    ) {
        Column {
            HubInspectionLazyRowItem(
                hubData.name ?: "",
                icon = Icons.Default.Person
            )
            HubInspectionLazyRowItem(
                hubData.address ?: "",
                icon = Icons.Default.Home
            )
            HubInspectionLazyRowItem(
                "Closes 5pm" ?: "",
                icon = Icons.Default.DateRange
            )
            HubInspectionLazyRowItem(
                "0811111112" ?: "",
                icon = Icons.Default.Call
            )
        }
    }
}


@Composable
private fun HubInspectionLazyRowItem(text: String?, icon: ImageVector) {
    Row(
        modifier = Modifier.padding(bottom = 6.dp, start =
        12.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Icon(
            imageVector = icon,
            contentDescription = "Icon",
            modifier = Modifier.size(18.dp)
        )
        TitleText(
            text = text ?: "",
            fontSize = 15,
            fontWeight = FontWeight.W500,
            lineHeight = 20,
            bottomPadding = 0
        )
    }
}


@Composable
private fun InspectionInstruction(text: String) {
    Row(
        modifier = Modifier.padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.diamond),
            contentDescription = "image placeholder icon",
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        TitleText(
            text = text,
            fontSize = 14,
            fontWeight = FontWeight.W500,
            lineHeight = 20
        )
    }
}
