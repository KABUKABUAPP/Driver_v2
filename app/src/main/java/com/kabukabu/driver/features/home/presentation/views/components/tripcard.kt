//import androidx.compose.material.icons.filled.DirectionsCar
//import androidx.compose.material.icons.filled.Wallet
import android.location.Location
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kabukabu.driver.R
import com.kabukabu.driver.core.data.socket.TripFoundEvent
import com.kabukabu.driver.core.theme.KabukabuDriverTheme
import com.kabukabu.driver.features.home.presentation.viewmodel.DistanceInfo
import com.kabukabu.driver.features.home.presentation.views.components.ModalTextDark
import com.kabukabu.driver.features.home.presentation.views.components.ModalTextGray

// Define colors
val CabRed = Color(0xFFCD214B)
val CabGreen = Color(0xFF26AE23)
val CabGrayBg = Color(0xFFF8F8F8)
val CabBorder = Color(0xFFE6E6E6)
val TextDark = Color(0xFF1A1A1A)
val TextGray = Color(0xFF757575)

// The new fill color
val LightOrangeFill = Color(0xFFFFF5D8) // Bisque/Light Orange

@Composable
fun TripRequestModalAnimated(
    distanceInfo: DistanceInfo?,
    fillDurationMillis: Int = 15000, // Parameter to control fill time (default 3s)
    isVisible: Boolean,
    tripDetails: TripFoundEvent?,
    driverLocation: Location?,
    remainingTime: Int,
    isAccepting: Boolean,
    isDeclining: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onTimeout: () -> Unit
) {

    val fillProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        fillProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = fillDurationMillis, easing = LinearEasing)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(color = Color(0xffFDFDFD))
                    val currentFillHeight = size.height * fillProgress.value
                    drawRect(
                        color = LightOrangeFill,
                        topLeft = Offset(x = 0f, y = size.height - currentFillHeight),
                        size = Size(width = size.width, height = currentFillHeight)
                    )
                }) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- Header Section ---
                Image(
                    painter = painterResource(id = R.drawable.taxi_2),
                    contentDescription = "Maps Logo",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You have a trip request",
                    fontWeight = FontWeight.W600,
                    fontSize = 14.sp,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = if (distanceInfo != null) "Rider is ${distanceInfo.distanceText} (${distanceInfo.durationText}) away" else "Rider is 3km (15 mins) away",
//                    text = distanceInfo ?.distanceText ?: "Rider is 3km (15 mins) away",
                    fontWeight = FontWeight.W500, fontSize = 10.sp, color = TextDark
                )

                Spacer(modifier = Modifier.height(20.dp))

                // --- Destination Section ---
                DetailBox {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.pin_1),
                            contentDescription = "Maps Logo",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Destination Area",
                                fontSize = 12.sp,
                                color = TextGray,
                                fontWeight = FontWeight.W400,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = tripDetails?.pickupLocation?.name ?: "Ikeja, Lagos",
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                color = TextDark,
                                fontWeight = FontWeight.W700
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- Price Section ---
                DetailBox {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.wallet_2),
                                contentDescription = "Maps Logo",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Estimated Price",
                                fontSize = 12.sp,
                                color = TextGray,
                                fontWeight = FontWeight.W400
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            buildAnnotatedString {
//                                withStyle(
//                                    style = SpanStyle(
//                                        textDecoration = TextDecoration.LineThrough,
//                                        color = ModalTextGray,
//                                        fontWeight = FontWeight.W700,
//                                        fontSize = 14.sp,
//                                    )
//                                ) {
//                                    append("₦4500 ")
//                                }
                                withStyle(
                                    style = SpanStyle(
                                        fontWeight = FontWeight.W700,
                                        fontSize = 16.sp,
                                        color = ModalTextDark
                                    )
                                ) {
                                    append(if (tripDetails != null) "₦${tripDetails.priceRange.last()}" else "₦2,000")

                                }
                            })

//                        Spacer(modifier = Modifier.height(4.dp))
//                        Text(
//                            buildAnnotatedString {
//                                withStyle(
//                                    style = SpanStyle(
//                                        color = ModalTextGray, fontWeight = FontWeight.W400,
//                                        fontSize = 12.sp,
//                                    )
//                                ) {
//                                    append("Kabukabu will pay driver ")
//                                }
//                                withStyle(
//                                    style = SpanStyle(
//                                        fontWeight = FontWeight.W500,
//                                        fontSize = 12.sp,
//                                        color = ModalTextDark
//                                    )
//                                ) {
//                                    append("₦4500")
//                                }
//                                withStyle(
//                                    style = SpanStyle(
//                                        color = ModalTextGray, fontWeight = FontWeight.W400,
//                                        fontSize = 12.sp,
//                                    )
//                                ) {
//                                    append(" balance")
//                                }
//                            })


                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Spacer(modifier = Modifier.height(24.dp))

                // --- Action Buttons ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDecline,
                        enabled = !isDeclining && !isAccepting,
                        colors = ButtonDefaults.buttonColors(containerColor = CabRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {

                        if (isDeclining) {
                            Text(
                                "Declining...",
                                fontWeight = FontWeight.W500,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                "Decline",
                                fontWeight = FontWeight.W500,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }

                    }

                    Button(
                        onClick = onAccept,
                        enabled = !isAccepting && !isDeclining,
                        colors = ButtonDefaults.buttonColors(containerColor = CabGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        if (isAccepting) {
                            Text(
                                "Accepting...",
                                fontWeight = FontWeight.W500,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                "Accept",
                                fontWeight = FontWeight.W500,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// Reusable container for the inner gray boxes (Unchanged)
@Composable
fun DetailBox(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CabBorder),
        color = CabGrayBg
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF666666)
@Composable
fun PreviewAnimatedTripRequest() {
    KabukabuDriverTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // You can set the duration here, e.g., 5000ms for 5 seconds
            TripRequestModalAnimated(
                distanceInfo = null,
                fillDurationMillis = 20000,
                isVisible = true,
                tripDetails = null,
                driverLocation = null,
                remainingTime = 25,
                isAccepting = false,
                isDeclining = false,
                onAccept = {},
                onDecline = {},
                onTimeout = {}

            )
        }
    }
}