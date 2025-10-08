package com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kabukabu.driver.R
import com.kabukabu.driver.components.ui.CustomLinearProgressIndicator
import com.kabukabu.driver.components.ui.GreyBackgroundContainer
import com.kabukabu.driver.components.ui.KabuBottomButtonRowScope
import com.kabukabu.driver.components.ui.KabuDivider
import com.kabukabu.driver.components.ui.ScreenTitleText
import com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride.UploadCarImageBox

@Composable
fun KabuRideCarDocumentsUploadScreen(){
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = 30.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
//            verticalArrangement = Arrangement.SpaceBetween,
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

            GreyBackgroundContainer {

                ScreenTitleText(
                    title = "Car Images",
                    subtitle = "Upload at least 3 images of your car",
                    titleFontSize = 16,
                    subtitleFontSize = 13,
                    bottomPadding = 20
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                        .background(Color.White)
                ) {
                    UploadCarImageBox(onClick = {})
                    UploadCarImageBox(onClick = {})
                    UploadCarImageBox(onClick = {})
                    UploadCarImageBox(onClick = {})
                }
            }

            KabuDivider(height = 16.dp)

            GreyBackgroundContainer {

                ScreenTitleText(
                    title = "Car Details",
                    subtitle = "Give us more information about your car",
                    titleFontSize = 16,
                    subtitleFontSize = 13,
                    bottomPadding = 16
                )

                TextfieldSelectionDropdown("Car Brand", onClick = {})
                TextfieldSelectionDropdown("Car Model", onClick = {})
                TextfieldSelectionDropdown("Car Year", onClick = {})
                TextfieldSelectionDropdown("Car Colour", onClick = {})
                TextfieldSelection(title = "Plate Number", placeholderText = "ABC 123 CVGG")

            }

            Row {
                KabuBottomButtonRowScope("Next", icon = R.drawable.arrow_right)
            }

        }

    }

}