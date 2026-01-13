package com.kabukabu.driver.features.home.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kabukabu.driver.core.theme.KabukabuYellow
import com.kabukabu.driver.features.home.data.EndTripData
import com.kabukabu.driver.features.home.presentation.viewmodel.DriverViewModel
import com.kabukabu.driver.core.utils.composableSafeClickable
import androidx.compose.foundation.background

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TripCompletionBottomSheet(
    endTripData: EndTripData?,
    driverViewModel: DriverViewModel,
    onDismiss: () -> Unit
) {
    // Extract trip details from endTripData
    val endedTrip = endTripData?.endedTrip
    val paymentDetails = endTripData?.paymentDetails
    val driverEarned =
        paymentDetails?.tripPrice ?: endedTrip?.priceDetails?.driverEarned ?: endedTrip?.price ?: 0
    endedTrip?.paymentType ?: "cash"
    endedTrip?.distanceInKm ?: 0.0
    endedTrip?.durationInMinutes ?: 0
    val pickupAddress = endedTrip?.startAddress?.fullAddress ?: "Pickup location"
    val dropoffAddress = endedTrip?.endAddress?.fullAddress ?: "Destination"
    val orderId = endedTrip?.id ?: ""

    // Rating state - initially null (no rating given)
    var rating by remember { mutableStateOf<Int?>(null) }

    // Selected feedback chips
    var selectedFeedback by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Define chip groups once so they can be referenced when rating changes
    val negativeChips = listOf(
        "Rude rider",
        "Abusive rider",
        "Long waiting time",
        "Bad driving",
        "Dirty rider",
        "Bad communication"
    )
    val positiveChips = listOf(
        "Smooth ride",
        "Good rider",
        "Easy Navigation",
        "Excellent service",
        "Good communication"
    )

    // Ensure selectedFeedback stays relevant when rating changes
    LaunchedEffect(rating) {
        val allowed = if ((rating ?: 0) <= 2) negativeChips else positiveChips
        selectedFeedback = selectedFeedback.filter { it in allowed }.toSet()
    }

    // Root container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        // --- STACKED BOTTOM SHEET IMPLEMENTATION ---
        Box(
            modifier = Modifier.align(Alignment.BottomCenter),
            contentAlignment = Alignment.BottomCenter
        ) {
            // LAYER 1: The "Back" Sheet (Peeking out from behind)
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .fillMaxHeight(0.885f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = Color(0xFFD8D8D8),
                shadowElevation = 4.dp
            ) {
                // Decorative empty layer
            }

            // LAYER 2: The "Front" Main Sheet
            Surface(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .fillMaxHeight(0.88f)
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                // Content Scroll View
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(top = 24.dp, bottom = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Text(
                        text = "Arrived at destination",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F1F1F)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Please rate your rider", fontSize = 14.sp, color = Color(0xFF1F1F1F)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Price Card
                    TripCompletionContainerBox {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "You Earned", color = Color(0xFF1F1F1F), fontSize = 14.sp
                            )
                            Text(
                                text = "₦${driverEarned}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F1F1F)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Route Card
                    TripCompletionContainerBox {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Custom drawn route indicator
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(top = 4.dp, end = 12.dp)
                            ) {
                                // Hollow Red Circle
                                Canvas(modifier = Modifier.size(10.dp)) {
                                    drawCircle(
                                        color = Color(0xFFEF5350),
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                }

                                // Dashed Line
                                Canvas(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(30.dp)
                                        .padding(vertical = 4.dp)
                                ) {
                                    drawLine(
                                        color = Color.LightGray,
                                        start = Offset(0f, 0f),
                                        end = Offset(0f, size.height),
                                        pathEffect = PathEffect.dashPathEffect(
                                            floatArrayOf(10f, 10f), 0f
                                        ),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }

                                // Hollow Blue Circle
                                Canvas(modifier = Modifier.size(10.dp)) {
                                    drawCircle(
                                        color = Color(0xFF2962FF),
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                }
                            }

                            // Address Text
                            Column {
                                Text(
                                    text = pickupAddress,
                                    color = Color(0xFF1F1F1F),
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = dropoffAddress,
                                    color = Color(0xFF1F1F1F),
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Rating Section
                    TripCompletionContainerBox {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Rate your Rider",
                                color = Color(0xFF1F1F1F),
                                fontSize = 14.sp
                            )

                            // Show rating text only when rating is given
                            if (rating != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = when (rating) {
                                        1 -> "Poor"
                                        2 -> "Fair"
                                        3 -> "Good"
                                        4 -> "Very Good"
                                        5 -> "Excellent"
                                        else -> "Good"
                                    },
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1F1F1F)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Stars
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(5) { index ->
                                    Icon(
                                        imageVector = if (rating != null && index < rating!!) Icons.Filled.Star else Icons.Outlined.Star,
                                        contentDescription = "Star ${index + 1}",
                                        tint = if (rating != null && index < rating!!) Color(0xFF66BB6A) else Color.LightGray,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .composableSafeClickable(onClick = {
                                                val newRating = index + 1
                                                rating = newRating
                                            })
                                    )
                                }
                            }

                            // Show feedback section only after rating is given
                            if (rating != null) {
                                Spacer(modifier = Modifier.height(20.dp))

                                Text(
                                    text = "Feedback (optional)",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Chips (dynamic by rating)
                                FlowRow(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    maxItemsInEachRow = 3
                                ) {
                                    val chipsToShow = if ((rating ?: 0) <= 2) negativeChips else positiveChips

                                    chipsToShow.forEach { label ->
                                        TripCompletionFeedbackChip(
                                            text = label,
                                            isSelected = selectedFeedback.contains(label),
                                            onClick = {
                                                selectedFeedback = if (selectedFeedback.contains(label)) {
                                                    selectedFeedback - label
                                                } else {
                                                    selectedFeedback + label
                                                }
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Continue Button
                    Button(
                        onClick = {
                            // If rating was given and feedback chips selected, submit with comment
                            if (rating != null && selectedFeedback.isNotEmpty()) {
                                val feedbackComment = selectedFeedback.joinToString(", ")
                                driverViewModel.rateRider(
                                    orderId = orderId,
                                    rating = rating!!.toDouble(),
                                    comment = feedbackComment
                                )
                            }
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KabukabuYellow
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Continue",
                            fontSize = 18.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

