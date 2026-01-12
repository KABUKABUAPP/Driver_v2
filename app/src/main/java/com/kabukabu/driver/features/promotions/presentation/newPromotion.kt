package com.kabukabu.driver.features.promotions.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.kabukabu.driver.core.theme.KabukabuDriverTheme
import com.kabukabu.driver.core.theme.KabukabuYellow
import com.kabukabu.driver.core.utils.composableSafeClickable

// --- Color Palette ---
val ThemeYellow = KabukabuYellow
val ThemeLightGray = Color(0xFFF2F2F2)
val OngoingProgressFill = Color(0xFFFFF9C4) // Light yellow for ongoing progress
val CompletedProgressFill = Color(0xFFE8F5E9) // Light green for completed progress

// --- Data Models ---
data class Promotion(
    val title: String,
    val currentTrips: Int,
    val totalTrips: Int,
    val footerText: String,
    val isRedeemed: Boolean = false
)

@Composable
fun PromotionsApp() {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Ongoing, 1: Completed

    val ongoingPromos = listOf(
        Promotion("Complete 10 trips to earn \n₦3,000 daily", 3, 10, "23 hrs left"),
        Promotion("Complete 50 trips to earn \nbag of rice at month end", 3, 50, "10 days left")
    )

    val completedPromos = listOf(
        Promotion("Complete 5 trips to win 12 litres of \nfuel daily", 5, 5, "Completed Yesterday"),
        Promotion("Complete 5 trips to win 12 litres of \nfuel daily", 5, 5, "Completed Monday", isRedeemed = true),
        Promotion("Complete 5 trips to win 12 litres of \nfuel daily", 5, 5, "Completed Sunday")
    )

    Scaffold(
        topBar = {
            Column(Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier
                            .size(20.dp)
                            .composableSafeClickable { /* Handle Back */ }
                    )
                    Text(
                        text = "Promotions",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Custom Pill Tab Bar ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(ThemeLightGray, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    TabButton(
                        text = "Ongoing",
                        isSelected = selectedTab == 0,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedTab = 0 }
                    )
                    TabButton(
                        text = "Completed",
                        isSelected = selectedTab == 1,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedTab = 1 }
                    )
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            if (selectedTab == 0) {
                PromotionList(ongoingPromos, isCompletedTab = false)
            } else {
                PromotionList(completedPromos, isCompletedTab = true)
            }
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(
                if (isSelected) ThemeYellow else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .composableSafeClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            fontSize = 14.sp
        )
    }
}

@Composable
fun PromotionList(promotions: List<Promotion>, isCompletedTab: Boolean) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(promotions) { promo ->
            PromotionCard(promo, isCompletedTab)
        }
    }
}

@Composable
fun PromotionCard(promo: Promotion, isCompletedTab: Boolean) {
    // Calculate width percentage for the background progress
    val progressFraction = promo.currentTrips.toFloat() / promo.totalTrips.toFloat()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min), // Matches height of the progress fill to text content
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // --- Background Layer (The Progress Indicator) ---
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progressFraction)
                    .background(if (isCompletedTab) CompletedProgressFill else OngoingProgressFill)
            )

            // --- Content Layer ---
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text(
                    text = promo.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${promo.currentTrips}/${promo.totalTrips}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.Black
                    )
                    Text(
                        text = " trips completed",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 4.dp),
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(promo.footerText, color = Color.Gray, fontSize = 12.sp)
                        if (isCompletedTab) {
                            Text("Tap to open", color = Color.DarkGray, fontSize = 12.sp)
                        }
                    }

                    if (promo.isRedeemed) {
                        Surface(
                            color = ThemeYellow,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "Redeemed",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

// Preview for the full Promotions screen
@Preview(showBackground = true, name = "PromotionsApp Preview")
@Composable
fun PromotionsAppPreview() {
    KabukabuDriverTheme {
        PromotionsApp()
    }
}

// Preview for an individual Promotion card
@Preview(showBackground = true, name = "PromotionCard Preview")
@Composable
fun PromotionCardPreview() {
    KabukabuDriverTheme {
        PromotionCard(
            promo = Promotion(
                title = "Complete 10 trips to earn \n₦3,000 daily",
                currentTrips = 3,
                totalTrips = 10,
                footerText = "23 hrs left",
                isRedeemed = false
            ),
            isCompletedTab = false
        )
    }
}
