package com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kabukabu.driver.components.ui.CustomLinearProgressIndicator
import com.kabukabu.driver.components.ui.GreyBackgroundContainer
import com.kabukabu.driver.components.ui.ScreenTitleText
import com.kabukabu.driver.R

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
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    UploadCarImageBox(onClick = {})
                    UploadCarImageBox(onClick = {})
                    UploadCarImageBox(onClick = {})
                    UploadCarImageBox(onClick = {})
                }


            }

        }

    }
}


@Composable
fun UploadCarImageBox(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(80.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFF1F1F1))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.image_placeholder),
                contentDescription = "image placeholder icon",
                modifier = Modifier.size(80.dp)
            )
        }
    }
}


