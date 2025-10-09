package com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kabukabu.driver.R
import com.kabukabu.driver.components.ui.CustomLinearProgressIndicator
import com.kabukabu.driver.components.ui.GrayBackgroundContainer
import com.kabukabu.driver.components.ui.KabuBottomButtonRowScope
import com.kabukabu.driver.components.ui.KabuTransparentBottomButtonRowScope
import com.kabukabu.driver.components.ui.ScreenTitleText
import com.kabukabu.driver.components.ui.TitleText


@Composable
fun KabuRideGuarantorDetail() {
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
                ProfileCard()
            }

            Row(
                modifier = Modifier.padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                KabuTransparentBottomButtonRowScope(
                    "Previous",
                    icon = R.drawable.arrow_left
                )

                KabuBottomButtonRowScope(
                    "Next",
                    icon = R.drawable.arrow_right
                )
            }

        }

    }

}

@Composable
fun ProfileCard() {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(color = Color(0xFFF8C34A), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Icon",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        // Optional — You can add text or details next to it
        Spacer(modifier = Modifier.width(12.dp))


    }
}
