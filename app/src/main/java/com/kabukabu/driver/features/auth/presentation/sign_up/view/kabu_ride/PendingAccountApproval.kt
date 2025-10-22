package com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kabukabu.driver.components.ui.KabuBottomButton
import com.kabukabu.driver.components.ui.TitleText
import com.kabukabu.driver.features.home.presentation.DriverViewModel

@Composable
fun KabuRidePendingAccountApprovalScreen(onNavigateToLogin: ()-> Unit) {

    val context = LocalContext.current
    // Create DriverViewModel at Activity scope so it's shared across Splash and Home
    val activityOwner = context as ViewModelStoreOwner
    val driverViewModel: DriverViewModel = viewModel(viewModelStoreOwner = activityOwner)

    Scaffold(
        bottomBar = {
            KabuBottomButton(
                text = "Okay",
                onClick = { onNavigateToLogin },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TitleText(
                text = "Your account is pending \napproval",
                fontSize = 25,
                fontWeight = FontWeight.W500,
                bottomPadding = 12,
                topPadding = 24
            )

            TitleText(
                text = "We are doing background check. We will notify you immediately afterwards",
                bottomPadding = 16,
                topPadding = 8,
                fontWeight = FontWeight.W500
            )
        }
    }
}