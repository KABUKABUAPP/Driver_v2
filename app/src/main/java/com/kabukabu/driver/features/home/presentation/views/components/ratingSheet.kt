package com.kabukabu.driver.features.home.presentation.views.components
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kabukabu.driver.core.theme.KabukabuYellow

// --- 1. Color Constants ---
val BrandBlue = Color(0xFF2962FF) // The blue border color
val BrandYellow = KabukabuYellow // The "Continue" button color
val TextDark = Color(0xFF1F1F1F)
val TextGray = Color(0xFF757575)
val StarGreen = Color(0xFF66BB6A)
val RouteRed = Color(0xFFEF5350)
val LightGrayBg = Color(0xFFF9F9F9)
val BorderGray = Color(0xFFE0E0E0)

@Composable
fun RideSummaryScreen() {
    // Root container simulating the Map View
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray) // Placeholder for your Map
    ) {

        // --- STACKED BOTTOM SHEET IMPLEMENTATION ---

        Box(
            modifier = Modifier.align(Alignment.BottomCenter),
            contentAlignment = Alignment.BottomCenter
        ) {
            // LAYER 1: The "Back" Sheet (Peeking out from behind)
            // It is narrower (0.94f) and taller (0.90f) to show at the top
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .fillMaxHeight(0.885f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = Color(0xFFD8D8D8), // A very light blue/white to distinguish it
                shadowElevation = 4.dp
            ) {
                // Decorative empty layer
            }

            // LAYER 2: The "Front" Main Sheet
            // It is full width, slightly shorter, and has the Blue Border
            Surface(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .fillMaxHeight(0.88f) // Slightly shorter so the back sheet shows
                    .padding(top = 12.dp), // Pushes it down to reveal the back sheet
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = Color.White,
//                border = BorderStroke(2.dp, BrandBlue), // The Blue Border from your image
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

                    // --- Content Start ---

                    Text(
                        text = "Arrived at destination",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Please rate the Jame Pike",
                        fontSize = 14.sp,
                        color = TextDark
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Price Section
                    PriceCard(price = "1,300")

                    Spacer(modifier = Modifier.height(16.dp))

                    // Route Section
                    RouteCard(
                        pickup = "22, Kuvuki Land",
                        dropoff = "Filmhouse Cinemas IMAX Lekki"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Rating Section
                    RatingSection()

                    Spacer(modifier = Modifier.height(24.dp))

                    // Continue Button
                    Button(
                        onClick = { /* TODO: Handle Click */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandYellow),
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

// --- 2. Components ---

@Composable
fun PriceCard(price: String) {
    ContainerBox {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Total Price", color = TextDark, fontSize = 14.sp)
            Text(
                text = price,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        }
    }
}

@Composable
fun RouteCard(pickup: String, dropoff: String) {
    ContainerBox {
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
                        color = RouteRed,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // Dashed Line
                Canvas(modifier = Modifier
                    .width(1.dp)
                    .height(30.dp) // Gap between dots
                    .padding(vertical = 4.dp)
                ) {
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Hollow Blue Circle
                Canvas(modifier = Modifier.size(10.dp)) {
                    drawCircle(
                        color = BrandBlue,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            // Address Text
            Column {
                Text(text = pickup, color = TextDark, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(24.dp)) // Matches visual height of line
                Text(text = dropoff, color = TextDark, fontSize = 14.sp)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RatingSection() {
    ContainerBox {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Rate your Rider", color = TextDark, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Good", fontWeight = FontWeight.SemiBold, color = TextDark)

            Spacer(modifier = Modifier.height(12.dp))

            // Stars
            Row {
                repeat(3) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = StarGreen,
                        modifier = Modifier.size(32.dp)
                    )
                }
                repeat(2) {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "Feedback (optional)", fontSize = 12.sp, color = TextGray)

            Spacer(modifier = Modifier.height(12.dp))

            // Chips
            FlowRow(
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 3
            ) {
                val chips = listOf(
                    "Rude rider/driver", "Abusive rider/driver",
                    "Long waiting time", "Bad driving", "Excellent service",
                    "Dirty rider/driver", "Bad communication"
                )

                chips.forEach { label ->
                    FeedbackChip(label)
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}

@Composable
fun FeedbackChip(text: String) {
    Surface(
        color = Color(0xFFF5F5F5),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextDark
        )
    }
}

@Composable
fun ContainerBox(content: @Composable () -> Unit) {
    // Outer surface for clean white background
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White
    ) {
        // Inner box for the gray border
        Box(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = BorderGray.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRideScreen() {
    RideSummaryScreen()
}