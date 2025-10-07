package com.kabukabu.driver.features.auth.presentation.sign_up.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kabukabu.driver.components.ui.KabuBottomButton
import com.kabukabu.driver.components.ui.KabuDivider
import com.kabukabu.driver.components.ui.KabuSpacer
import com.kabukabu.driver.components.ui.TitleText
import com.kabukabu.driver.components.ui.getThirtyPercentOfScreenWidth

@Composable
fun SelectVehicleScreen() {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
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
                    bottomPadding = 8,
                )

                TitleText(
                    text = "This will enable us know the kind of service to offer",
                    fontSize = 14,
                    bottomPadding = 8,
                    maxLines = 2
                )


            }

            KabuBottomButton(
                text = "Continue",
                onClick = {}
            )
        }
    }

}


@Composable
fun TaxiOwnershipSelector(
    modifier: Modifier = Modifier,
    onSelectionChanged: (Boolean) -> Unit = {}
) {
    var hasTaxi by remember { mutableStateOf<Boolean?>(null) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SelectionCard(
            text = "Yes, i do",
            isSelected = hasTaxi == true,
            icon = ,
            onClick = {
                hasTaxi = true
                onSelectionChanged(true)
            }
        )

        SelectionCard(
            text = "No, i don’t",
            isSelected = hasTaxi == false,
            icon = painterResource(id = R.drawable.ic_taxi),
            onClick = {
                hasTaxi = false
                onSelectionChanged(false)
            }
        )
    }
}

@Composable
fun RowScope.SelectionCard(
    text: String,
    icon: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor =
        if (isSelected) Color(0xFFF9C543) else Color(0xFFF9F9F9)
    val borderColor =
        if (isSelected) Color.Transparent else Color(0xFFE6E6E6)

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1.2f)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = "vehicle icon",
                tint = Color.Black,
                modifier = Modifier.size(28.dp)
            )
            KabuDivider(height = 8.dp)
            TitleText(
                text = text,
                color = Color.Black,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14
            )
        }
    }
}


