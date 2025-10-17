package com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kabukabu.driver.R
import com.kabukabu.driver.components.ui.KabuBottomButton
import com.kabukabu.driver.components.ui.TitleText
import com.kabukabu.driver.features.home.presentation.DriverViewModel

@Composable
fun KabuRideAccountDeclinedScreen() {

    val context = LocalContext.current
    // Create DriverViewModel at Activity scope so it's shared across Splash and Home
    val activityOwner = context as ViewModelStoreOwner
    val driverViewModel: DriverViewModel = viewModel(viewModelStoreOwner = activityOwner)

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {

                TitleText(
                    text = "❌",
                    color = Color.Red,
                    fontSize = 25,
                    fontWeight = FontWeight.W500,
                    bottomPadding = 12,
                    topPadding = 30
                )

                TitleText(
                    text = "Your account was \ndeclined",
                    fontSize = 25,
                    fontWeight = FontWeight.W500,
                    bottomPadding = 12,
                    topPadding = 12
                )

                TitleText(
                    text = "Here’s why your account was declined",
                    fontSize = 16,
                    bottomPadding = 16,
                    topPadding = 8,
                    fontWeight = FontWeight.W400,
                )

                ReasonForApprovalDeclineCard(
                    title = "Hackney Permit",
                    subtitle = "Document has expired"
                )


            }

            KabuBottomButton(
                text = "Resubmit",
                onClick = {}
            )
        }
    }

}

@Composable
fun ReasonForApprovalDeclineCard (
    title: String,
    subtitle: String,
) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFFF8F8F8), RoundedCornerShape(12.dp)),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

                Icon(
                    painter = painterResource(id = R.drawable.document),
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .size(30.dp)
                )


            Column (
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(all = 16.dp)
            ) {
                TitleText(
                    text = title,
                    fontWeight = FontWeight.W500,
                    fontSize = 16, topPadding = 0,
                    bottomPadding = 0
                )

                TitleText(
                    text = subtitle,
                    color = Color(0xFF686868),
                    bottomPadding = 0,
                    fontSize = 13
                )
            }



    }

}