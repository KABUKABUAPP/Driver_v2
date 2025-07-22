package com.kabukabu.driver.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import com.kabukabu.driver.data.socket.TripFoundEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.res.painterResource
import com.kabukabu.driver.R
import android.location.Location
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import kotlin.math.roundToInt
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.CircularProgressIndicator

@Composable
fun TripRequestCard(
    isVisible: Boolean,
    tripDetails: TripFoundEvent,
    driverLocation: Location?,
    remainingTime: Int,
    isAccepting: Boolean,
    isDeclining: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onTimeout: () -> Unit
) {
    val progress = 1f - (remainingTime / 20f)
    val animatedProgress = animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000)
    )

    // Create a sharp gradient that moves with the progress
    val colorStops = arrayOf(
        0.0f to Color.White,
        animatedProgress.value to Color.White,
        animatedProgress.value + 0.001f to Color(0xFFFFF5D8), // rgba(255, 245, 216, 1)
        1.0f to Color(0xFFFFF5D8)
    )

    val backgroundBrush = Brush.horizontalGradient(colorStops = *colorStops)

    var distanceToRider by remember { mutableStateOf<Float?>(null) }
    var timeToRider by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(driverLocation, tripDetails) {
        if (driverLocation != null && tripDetails.startPoint.size == 2) {
            val riderLocation = Location("").apply {
                longitude = tripDetails.startPoint[0]
                latitude = tripDetails.startPoint[1]
            }
            val distance = driverLocation.distanceTo(riderLocation) / 1000 // in km
            distanceToRider = distance

            // Estimate time: average speed of 30 km/h in city
            val time = (distance / 30) * 60 // in minutes
            timeToRider = time.roundToInt()
        }
    }


    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent), // Set to transparent to use custom brush
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(backgroundBrush)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.taxi),
                        contentDescription = "Trip Request Icon",
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "You have a trip request",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W600,
                            color = Color(22, 22, 22)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (distanceToRider != null && timeToRider != null) {
                    Text(
                        text = "Rider is ${String.format("%.1f", distanceToRider)}km (${timeToRider} mins) away",
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.W500,
                            color = Color(22, 22, 22)
                        )
                    )
                }


                Spacer(modifier = Modifier.height(20.dp))

                // Destination Info Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(51.dp)
                        .background(
                            color = Color(0xFFF8F8F8), // rgba(248, 248, 248, 1)
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE6E6E6), // rgba(230, 230, 230, 1)
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.location_pin_grey),
                            contentDescription = "Destination",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Destination Area",
                                fontWeight = FontWeight.W400,
                                fontSize = 12.sp,
                                color = Color(0xFF9A9A9A) // rgba(154, 154, 154, 1)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${tripDetails.destinationLocation.city}, ${tripDetails.destinationLocation.state}",
                                fontWeight = FontWeight.W700,
                                fontSize = 14.sp,
                                color = Color(0xFF161616) // rgba(22, 22, 22, 1)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Fare Info Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(51.dp)
                        .background(
                            color = Color(0xFFF8F8F8),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE6E6E6),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.wallet_grey),
                                contentDescription = "Trip Fare",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Trip Fare",
                                fontWeight = FontWeight.W400,
                                fontSize = 12.sp,
                                color = Color(0xFF9A9A9A)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "₦${tripDetails.fare}",
                            fontWeight = FontWeight.W700,
                            fontSize = 14.sp,
                            color = Color(0xFF161616)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDecline,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFCD214B) // rgba(205, 33, 75, 1)
                        ),
                        enabled = !isAccepting && !isDeclining
                    ) {
                        if (isDeclining) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Decline",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.W600,
                                    color = Color.White
                                )
                            )
                        }
                    }
                    Button(
                        onClick = onAccept,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF26AE23) // rgba(38, 174, 35, 1)
                        ),
                        enabled = !isAccepting && !isDeclining
                    ) {
                        if (isAccepting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Accept",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.W600,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }
    }
} 