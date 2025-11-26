package com.kabukabu.driver.features.home.presentation.views.components

//import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kabukabu.driver.R
import com.kabukabu.driver.core.theme.KabukabuDriverTheme
import com.kabukabu.driver.features.profile.data.ActiveTrip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverRideScreenWithAnchoredControls(trip: ActiveTrip?) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    rememberCoroutineScope()
//    val coroutineScope = rememberCoroutineScope()

    // 1. Configuration for Min/Max Heights
    val configuration = LocalConfiguration.current
    configuration.screenHeightDp.dp
    LocalDensity.current

    // Define your heights
    300.dp // Min Height (Collapsed)
    val sheetMaxHeight = 600.dp  // Max Height (Expanded limit)

    // 2. State Tracking
    // Detect if sheet is fully expanded
    val isExpanded by remember {
        derivedStateOf {
            scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded
        }
    }

    // Debug print to track state changes
    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            println("📍 Bottom Sheet is at MAX Height")
        } else {
            println("📍 Bottom Sheet is Collapsed/Dragging")
        }
    }

    BottomSheetScaffold(

        scaffoldState = scaffoldState,
        sheetPeekHeight = 300.dp,

        sheetShape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        sheetContainerColor = Color(0xFFFDFDFD),
        containerColor = Color.White,
        sheetDragHandle = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    // This background will be clipped by the sheet's shape
                    .background(Color(0xFFFDFDFD)), // <-- CHANGE YOUR COLOR HERE
            ) {
                DragHandle()
            }
        },
        sheetContent = {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White)


                    .heightIn(max = sheetMaxHeight) // Limits the expansion
                    .padding(bottom = 20.dp) // Safe area
            ) {
                // Reuse the previous modal content here
                DriverTripModalContent()
            }
        }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Transparent) // Map Placeholder
        ) {
            // MAP GOES HERE
            Text("Google Map Area", modifier = Modifier.align(Alignment.Center), color = Color.Gray)

            // 4. Floating Anchored Controls
            // We use a custom layout modifier to anchor them to the sheet top
            AnchoredMapControls(
                sheetState = scaffoldState.bottomSheetState,
                isExpanded = isExpanded,
                offsetY = 16.dp // Gap between button and sheet
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnchoredMapControls(
    sheetState: SheetState, offsetY: androidx.compose.ui.unit.Dp, isExpanded: Boolean
) {
    val density = LocalDensity.current
    val offsetYPx = with(density) { offsetY.toPx() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                val sheetTop = try {
                    sheetState.requireOffset()
                } catch (e: Exception) {
                    // Fallback to bottom of screen if not ready
                    size.height
                }

                translationY = sheetTop - size.height - offsetYPx
            }) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)

                .padding(horizontal = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Google Maps Pill
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White,
                    shadowElevation = 4.dp,
                    onClick = { /* Open Maps */ }) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.gmlogo),
                            contentDescription = "Maps Logo",
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open in Google Maps", fontWeight = FontWeight.W700, fontSize = 14.sp)
                    }
                }

                // Compass/Location Button
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(60.dp),
                    onClick = { /* Recenter */ }) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.resetlocation),
                            contentDescription = "Maps Logo",
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
            }

            if (!isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(15.dp))
                    ArrivedButton()
                }
            }
        }
    }
}

// --- Content Placeholder from previous step ---
// --- Colors needed for this section ---
val ModalTextDark = Color(0xFF1A1A1A)
val ModalTextGray = Color(0xFF9A9A9A)
val ModalLightGrayBg = Color(0xF8F8F8)
val PrimaryYellow = Color(0xFFFBC02D)
val CancelRed = Color(0xFFEF2C5B)

@Composable
fun DriverTripModalContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        // 1. HEADER: Title + Time Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "You are driving to rider",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.W700,
                    color = ModalTextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Est. 10 minutes", fontSize = 12.sp, color = ModalTextGray, fontWeight = FontWeight.W500
                )
            }

            // The "10 Mins away" Badge
            Surface(
                color = ModalLightGrayBg,
                border = BorderStroke(1.dp, Color(0xFFF1F1F1)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.width(72.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "10",
                        fontWeight = FontWeight.W700,
                        fontSize = 16.sp,
                        color = ModalTextDark
                    )
                    Text(
                        text = "Mins away", fontSize = 12.sp, color = ModalTextDark,
                        fontWeight = FontWeight.W600
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. PICKUP LOCATION CARD
        DetailCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.pin_1),
                    contentDescription = "Maps Logo",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "Pickup Point", fontSize = 12.sp, color = ModalTextGray, fontWeight = FontWeight.W400)
                    Text(
                        text = "2 Ebinpejo Lane, Idumota, Lagos",
                        fontWeight = FontWeight.W700,
                        fontSize = 14.sp,
                        color = ModalTextDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. RIDER PROFILE CARD (Name, Trips, Buttons, Avatar)
        DetailCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Side: Text + Buttons
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Solomon Mafoluku",
                        fontWeight = FontWeight.W700,
                        fontSize = 18.sp,
                        color = ModalTextDark
                    )
                    Text(
                        text = "20 total trips", fontSize = 12.sp, color = ModalTextGray, fontWeight = FontWeight.W600
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Chat & Call Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProfileIconButton(R.drawable.message_1)
                        ProfileIconButton(R.drawable.phone_1)
                    }
                }

                // Right Side: Avatar Placeholder
                Surface(
                    shape = CircleShape, color = Color(0xFFE0E0E0), // Light gray placeholder
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Rider Avatar",
                            tint = Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    // Note: Use Image(...) here to load a real photo
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. PRICE SECTION (Wallet, Strikethrough price)
        DetailCard {
            Column {
                // Top Row: "Estimated Price" --- "Wallet Payment"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Estimated Price", fontSize = 12.sp, color = ModalTextGray, fontWeight = FontWeight.W400)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.wallet_1),
                            contentDescription = "Maps Logo",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Wallet Payment", fontSize = 12.sp, color = ModalTextDark, fontWeight = FontWeight.W400)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Price Text
                Text(
                    buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                textDecoration = TextDecoration.LineThrough, color = ModalTextGray,fontWeight = FontWeight.W700,
                                fontSize = 14.sp,
                            )
                        ) {
                            append("₦4500 ")
                        }
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.W700,
                                fontSize = 16.sp,
                                color = ModalTextDark
                            )
                        ) {
                            append("₦2,000")
                        }
                    })

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = ModalTextGray,fontWeight = FontWeight.W400,
                                fontSize = 12.sp,
                            )
                        ) {
                            append("Kabukabu will pay driver ")
                        }
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.W500,
                                fontSize = 12.sp,
                                color = ModalTextDark
                            )
                        ) {
                            append("₦4500")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = ModalTextGray,fontWeight = FontWeight.W400,
                                fontSize = 12.sp,
                            )
                        ) {
                            append(" balance")
                        }
                    })

            }
        }

        Spacer(modifier = Modifier.height(36.dp))




        // 6. BUTTONS ("I have arrived" & "Cancel")
        ArrivedButton()


        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = { /* TODO */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0XFFF1F1F1)),
            shape = RoundedCornerShape(8.dp), // Slightly rounded rectangle
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Text(
                text = "Cancel Trip",
                color = CancelRed,
                fontWeight = FontWeight.W500,
                fontSize = 14.sp
            )
        }

        // Extra space at the bottom for better scrolling experience
        Spacer(modifier = Modifier.height(24.dp))
    }
}



@Composable
fun DetailCard(content: @Composable () -> Unit) {
    Surface(
        color = ModalLightGrayBg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE6E6E6)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            content()
        }
    }
}

@Composable
fun ProfileIconButton(id: Int) {
    Surface(
        color = Color(0xFFEBEBEB), // Slightly darker gray for buttons
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(42.dp),
        onClick = { /* TODO */ }) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = id),
                contentDescription = "Maps Logo",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ArrivedButton() {
    Button(
        onClick = { /* TODO */ },
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow),
        shape = RoundedCornerShape(40), // Pill shape
        border = BorderStroke(1.dp, Color.Black),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(
            text = "I have arrived at pickup location",
            color = ModalTextDark,
            fontWeight = FontWeight.W700,
            fontSize = 14.sp
        )
    }

}

@Preview
@Composable
fun PreviewAnchored() {
    KabukabuDriverTheme {
        DriverRideScreenWithAnchoredControls(null)
    }
}