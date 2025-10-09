package com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kabukabu.driver.R
import com.kabukabu.driver.components.ui.GrayBackgroundContainer
import com.kabukabu.driver.components.ui.ScreenTitleText
import com.kabukabu.driver.components.ui.TitleText

@Composable
fun InspectionScreen() {
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

            ScreenTitleText(
                title = "Inspection",
                titleFontSize = 22,
                subtitle = "Please take your car to our nearest \n" +
                        "inspection hub",
                bottomPadding = 20
            )

            ScreenTitleText(
                title = "KAB-1234",
                subtitle = "Driver code",
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

        }

    }
}

@Composable
fun InspectionInstruction(text: String) {
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
