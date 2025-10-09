package com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kabukabu.driver.components.ui.KabuBottomButton
import com.kabukabu.driver.components.ui.ScreenTitleText

@Composable
fun KabuRideSelfieVerificationScreen(onNavToTermsAndCondition: () -> Unit) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = 30.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            ScreenTitleText(
                title = "Verification",
                subtitle = "Let's put a face to your name",
                bottomPadding = 16
            )

            KabuBottomButton("Continue",
                onClick = onNavToTermsAndCondition
            )

        }

    }
}