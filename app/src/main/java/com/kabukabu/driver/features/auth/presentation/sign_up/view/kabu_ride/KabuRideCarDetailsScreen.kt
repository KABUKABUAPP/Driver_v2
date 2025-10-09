package com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kabukabu.driver.components.ui.CustomLinearProgressIndicator
import com.kabukabu.driver.components.ui.GrayBackgroundContainer
import com.kabukabu.driver.components.ui.ScreenTitleText
import com.kabukabu.driver.R
import com.kabukabu.driver.components.ui.FormTextfieldDropdown
import com.kabukabu.driver.components.ui.KabuBottomButtonRowScope
import com.kabukabu.driver.components.ui.KabuDivider
import com.kabukabu.driver.components.ui.KabuOutlinedTextField
import com.kabukabu.driver.components.ui.KabuOutlinedTextFieldWithTrailingIconButton
import com.kabukabu.driver.components.ui.TitleText

@Composable
fun KabuRideCarDetailsScreen() {
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

            GrayBackgroundContainer {

                ScreenTitleText(
                    title = "Car Details",
                    subtitle = "Give us more information about your car",
                    titleFontSize = 16,
                    subtitleFontSize = 13,
                    bottomPadding = 16
                )

                FormTextfieldDropdown("Car Brand", onClick = {})
                FormTextfieldDropdown("Car Model", onClick = {})
                FormTextfieldDropdown("Car Year", onClick = {})
                FormTextfieldDropdown("Car Colour", onClick = {})
                TextfieldSelection(title = "Plate Number", placeholderText = "ABC 123 CVGG")

            }

           Row {
               KabuBottomButtonRowScope("Next", icon = R.drawable.arrow_right)
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
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(75.dp)
            .weight(1f)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFF1F1F1))
//            .padding(8.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
            Image(
                painter = painterResource(R.drawable.image_placeholder),
                contentDescription = "image placeholder icon",
                modifier = Modifier.size(22.dp),
            )
        }

}


