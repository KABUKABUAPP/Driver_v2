package com.kabukabu.driver.features.auth.presentation.sign_up.view

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.kabukabu.driver.R
import com.kabukabu.driver.components.ui.KabuBottomButton
import com.kabukabu.driver.components.ui.KabuDivider
import com.kabukabu.driver.components.ui.TitleText
import com.kabukabu.driver.components.ui.getThirtyPercentOfScreenWidth

@Composable
fun SelectVehicleScreen() {

    var hasVehicle by remember { mutableStateOf<Boolean?>(null) }

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
                    bottomPadding = 30,
                )

                TitleText(
                    text = "This will enable us know the kind of service to offer",
                    fontSize = 14,
                    bottomPadding = 8,
                    maxLines = 2
                )

                TaxiOwnershipSelector(
                    onSelectionChanged = {
                        hasVehicle = it
                    },
                    hasTaxi = hasVehicle
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
fun VehicleTypeSelector(){

}

@Composable
fun TaxiOwnershipSelector(
    modifier: Modifier = Modifier,
    hasTaxi: Boolean?,
    onSelectionChanged: (Boolean) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SelectionCard(
            text = "Yes, I do",
            isSelected = hasTaxi == true,
            icon = R.drawable.taxi_outlined,
            userHasTaxi = hasTaxi == true,
            onClick = { onSelectionChanged(true) }
        )

        SelectionCard(
            text = "No, I don’t",
            isSelected = hasTaxi == false,
            icon = R.drawable.taxi_outlined,
            userHasTaxi = hasTaxi == true,
            onClick = { onSelectionChanged(false) }
        )
    }
}


@Composable
fun RowScope.SelectionCard(
    text: String,
    icon: Int,
    isSelected: Boolean,
    userHasTaxi: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor =
        if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF9F9F9)
    val borderColor =
        if (isSelected) Color.Black else Color.Transparent

    val targetHeight = if (userHasTaxi) 120.dp else 160.dp
    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "cardHeightAnimation"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .height(animatedHeight)
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
            Image(
                painter = painterResource(icon),
                contentDescription = "vehicle icon",
                modifier = Modifier.size(22.dp)
            )

            KabuDivider(height = 12.dp)
            TitleText(
                text = text,
                color = Color.Black,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14
            )
        }
    }
}


