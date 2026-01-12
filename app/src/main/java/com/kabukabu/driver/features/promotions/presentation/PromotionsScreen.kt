package com.kabukabu.driver.features.promotions.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kabukabu.driver.core.theme.KabukabuDriverTheme
import kotlin.math.max
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.kabukabu.driver.core.theme.KabukabuYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import com.kabukabu.driver.core.utils.composableSafeClickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionsScreen(onBack: () -> Unit, vm: PromotionsViewModel = viewModel()) {
    val ui by vm.uiState.collectAsState()
    val selectedTab = remember { mutableIntStateOf(0) } // 0: Ongoing, 1: Completed

    Scaffold(

        // Use a center-aligned top app bar to display the back arrow and title in the topBar
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "Promotions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
//            Spacer(modifier = Modifier.height(24.dp))

            // Pill tab bar (re-implements TabButton style from newPromotion.kt)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Color(0xFFF2F2F2), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                PillTabButton(text = "Ongoing", isSelected = selectedTab.intValue == 0, modifier = Modifier.weight(1f)) { selectedTab.intValue = 0 }
                PillTabButton(text = "Completed", isSelected = selectedTab.intValue == 1, modifier = Modifier.weight(1f)) { selectedTab.intValue = 1 }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content lists with loading / error / pagination handling
            if (selectedTab.intValue == 0) {
                when {
                    // Centered loading indicator when initial load is happening and no items yet
                    ui.isLoadingOngoing && ui.ongoing.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = KabukabuYellow)
                        }
                    }

                    // Centered error state with retry when there is an error and no items
                    ui.errorOngoing != null && ui.ongoing.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "Error: ${ui.errorOngoing}", color = Color(0xFFB00020))
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { vm.refresh() }) {
                                Text(text = "Retry")
                            }
                        }
                    }

                    // Empty state when loading finished but no items
                    !ui.isLoadingOngoing && ui.ongoing.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "No ongoing promotions")
                        }
                    }

                    else -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            itemsIndexed(ui.ongoing) { index, item ->
                                val promo = mapToUiPromotion(item)
                                PromotionCard(promo = promo, isCompletedTab = false)
                                if (index == ui.ongoing.lastIndex && !ui.noMoreOngoing && !ui.isLoadingMoreOngoing) {
                                    vm.loadMore("ongoing")
                                }
                            }

                            item {
                                if (ui.isLoadingMoreOngoing) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Loading more…")
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    }
                }
            } else {
                when {
                    // Centered loading indicator for completed
                    ui.isLoadingCompleted && ui.completed.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = KabukabuYellow)
                        }
                    }

                    // Centered error state with retry for completed
                    ui.errorCompleted != null && ui.completed.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "Error: ${ui.errorCompleted}", color = Color(0xFFB00020))
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { vm.refresh() }) {
                                Text(text = "Retry")
                            }
                        }
                    }

                    // Empty completed state
                    !ui.isLoadingCompleted && ui.completed.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "No completed promotions")
                        }
                    }

                    else -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            itemsIndexed(ui.completed) { index, item ->
                                val promo = mapToUiPromotion(item)
                                PromotionCard(promo = promo, isCompletedTab = true)
                                if (index == ui.completed.lastIndex && !ui.noMoreCompleted && !ui.isLoadingMoreCompleted) {
                                    vm.loadMore("completed")
                                }
                            }

                            item {
                                if (ui.isLoadingMoreCompleted) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Loading more…")
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// Simple pill-style tab button similar to newPromotion.kt's TabButton
@Composable
private fun PillTabButton(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(if (isSelected) KabukabuYellow else Color.Transparent, RoundedCornerShape(8.dp))
            .composableSafeClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontWeight = FontWeight.Medium, color = Color.Black, fontSize = 14.sp)
    }
}

// Helper to capitalize the first letter of promotion titles
private fun capitalizeFirstLetter(input: String?): String {
    if (input.isNullOrBlank()) return input ?: ""
    return input.trim().replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}

// Map the ViewModel's PromotionItem to the UI Promotion model used by PromotionCard
private fun mapToUiPromotion(item: PromotionItem): Promotion {
    val current = item.count ?: 0
    val total = item.targetValue ?: max(1, current)
    val footer: String = when {
        !item.expiryDate.isNullOrBlank() -> formatExpiryRelative(item.expiryDate)
        !item.comment.isNullOrBlank() -> item.comment
        else -> ""
    }

    return Promotion(
        title = capitalizeFirstLetter(item.description ?: "Promotion"),
        currentTrips = current,
        totalTrips = total,
        footerText = footer,
        isRedeemed = false
    )
}

// Helper to format expiry date into "X days left" / "Y hours left" etc.
private fun formatExpiryRelative(expiryIso: String?): String {
    if (expiryIso.isNullOrBlank()) return ""

    return try {
        // Try parsing ISO 8601 with milliseconds first (UTC)
        val sdfMillis = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdfMillis.timeZone = TimeZone.getTimeZone("UTC")
        val expiryDate: Date = try {
            sdfMillis.parse(expiryIso)
        } catch (_: Exception) {
            // Fallback to seconds precision
            val sdfSeconds = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            sdfSeconds.timeZone = TimeZone.getTimeZone("UTC")
            sdfSeconds.parse(expiryIso)
        }

        val nowMillis = System.currentTimeMillis()
        val durationMillis = expiryDate.time - nowMillis

        if (durationMillis <= 0L) {
            "Expired"
        } else {
            val days = TimeUnit.MILLISECONDS.toDays(durationMillis)
            if (days >= 1L) {
                if (days == 1L) "1 day left" else "$days days left"
            } else {
                val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
                if (hours >= 1L) {
                    if (hours == 1L) "1 hour left" else "$hours hours left"
                } else {
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis)
                    if (minutes <= 1L) "1 minute left" else "$minutes minutes left"
                }
            }
        }
    } catch (_: Exception) {
        // If parsing fails, fall back to the raw string to avoid losing data
        expiryIso
    }
}

// Preview wrapper to allow quick previewing in IDE
@Suppress("unused")
@Composable
fun PromotionsScreenPreviewWrapper(onBack: () -> Unit = {}) {
    KabukabuDriverTheme {
        PromotionsScreen(onBack = onBack, vm = PromotionsViewModel())
    }
}
