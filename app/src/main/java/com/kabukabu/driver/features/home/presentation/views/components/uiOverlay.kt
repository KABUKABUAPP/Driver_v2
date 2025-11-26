//package com.kabukabu.driver.features.home.presentation.views.components
//
//import android.location.Geocoder
//import android.location.Location
//import androidx.compose.animation.animateContentSize
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.offset
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.layout.wrapContentWidth
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Check
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.Divider
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.ColorFilter
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.kabukabu.driver.R
//import com.kabukabu.driver.core.components.SwipeButton
//import com.kabukabu.driver.core.theme.BorderSubtle
//import com.kabukabu.driver.core.theme.Success
//import com.kabukabu.driver.core.theme.TextSecondary
//import com.kabukabu.driver.features.home.presentation.viewmodel.DriverViewModel
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//
//
//@Composable
//fun UIOverlay(
//    hideDriverStatusCard: Boolean = false,
//    currentLocation: Location?,
//    onLogout: () -> Unit,
//    isOnline: Boolean,
//    onIsOnlineChange: (Boolean) -> Unit,
//    onMenuClick: () -> Unit,
//    driverViewModel: DriverViewModel
//) {
//    val context = LocalContext.current
//    var locationName by remember { mutableStateOf("Loading location...") }
//
//    // This effect runs when `currentLocation` changes.
//    LaunchedEffect(currentLocation) {
//        if (currentLocation != null) {
//            // Use a coroutine to avoid blocking the main thread
//            launch(Dispatchers.IO) {
//                try {
//                    val geocoder = Geocoder(context)
//                    val addresses = geocoder.getFromLocation(
//                        currentLocation.latitude, currentLocation.longitude, 1
//                    )
//                    if (addresses?.isNotEmpty() == true) {
//                        locationName = addresses[0].thoroughfare ?: "Unknown Location"
//                    }
//                } catch (e: Exception) {
//                    locationName = "Location not found"
//                }
//            }
//        }
//    }
//
//    Box(modifier = Modifier.fillMaxSize()) {
//        // Top UI Bar
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .align(Alignment.TopCenter)
//                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // Left Menu Button
//            Box(
//                modifier = Modifier
//                    .size(44.dp)
//                    .background(
//                        MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp)
//                    )
//                    .clickable { onMenuClick() }, contentAlignment = Alignment.Center
//            ) {
//                Image(
//                    painter = painterResource(id = R.drawable.menu_right_square_alt),
//                    contentDescription = "Menu",
//                    modifier = Modifier.size(24.dp),
//                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
//                )
//            }
//
//            // Center Location Display
//            Box(
//                modifier = Modifier
//                    .wrapContentWidth()
//                    .background(
//                        MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(4.dp)
//                    ), contentAlignment = Alignment.Center
//            ) {
//                Row(
//                    modifier = Modifier.padding(horizontal = 8.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Image(
//                        painter = painterResource(id = R.drawable.location_pin),
//                        contentDescription = "Location Pin",
//                        modifier = Modifier.size(14.dp)
//                    )
//                    Spacer(modifier = Modifier.width(4.dp))
//                    Text(
//                        text = locationName,
//                        color = MaterialTheme.colorScheme.onSurface,
//                        fontWeight = FontWeight.W600,
//                        fontSize = 12.sp
//                    )
//                }
//            }
//
//            // Right Spacer for balance
//            Spacer(Modifier.size(44.dp))
//        }
//
//        // Bottom Expandable Card
//        if (!hideDriverStatusCard) Box(
//            modifier = Modifier
//                .align(Alignment.BottomCenter)
//                .padding(horizontal = 16.dp, vertical = 24.dp)
//        ) {
////            TripRequestModalAnimated()
//            ExpandableDriverStatusCard(
//                onLogout = onLogout,
//                onOnlineStatusChanged = onIsOnlineChange,
//                isOnline = isOnline,
//                driverViewModel = driverViewModel
//            )
//        }
//    }
//}
//
//@Composable
//private fun ExpandableDriverStatusCard(
//    driverViewModel: DriverViewModel,
//    onLogout: () -> Unit,
//    onOnlineStatusChanged: (Boolean) -> Unit,
//    isOnline: Boolean,
//
//    ) {
//    var isExpanded by remember { mutableStateOf(false) }
//    val isUpdatingOnlineStatus by driverViewModel.isUpdatingOnlineStatus.collectAsState()
//
//    Column {
//        Box {
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(bottom = 18.dp),
//                shape = RoundedCornerShape(10.dp),
//                colors = CardDefaults.cardColors(containerColor = Color.White),
//                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
//            ) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .animateContentSize(),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Column(modifier = Modifier.padding(16.dp)) {
//                        OfflineStatus(isOnline = isOnline)
//                        Spacer(modifier = Modifier.height(16.dp))
//                        DriverStats(
//                            tripsCount = "0",
//                            tripsLabel = "Trip Today",
//                            earningsAmount = "0",
//                            earningsLabel = "Km covered today"
//
//                        )
//                    }
//
//                    if (isExpanded) {
//                        DriverStats(
//                            tripsCount = "₦0",
//                            tripsLabel = "Earned today",
//                            earningsAmount = "6.5",
//                            earningsLabel = "Driver Score"
//
//                        )
//                        Spacer(modifier = Modifier.height(16.dp))
//                        Divider(
//                            modifier = Modifier.padding(horizontal = 16.dp),
//                            thickness = 1.dp,
//                            color = Color(0xffF1F1F1)
//                        )
//                        Column(
//                            modifier = Modifier.padding(16.dp),
//                            horizontalAlignment = Alignment.CenterHorizontally
//                        ) {
//                            Text(
//                                "Preferred payment option (Select one or more)",
//                                textAlign = TextAlign.Center,
//                                fontWeight = FontWeight.W500,
//                                fontSize = 12.sp,
//                                color = Color.Black
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            PaymentOptions(driverViewModel = driverViewModel)
//                            Spacer(modifier = Modifier.height(8.dp))
//                        }
//                    }
//                }
//            }
//
//            ViewMoreButton(
//                modifier = Modifier.align(Alignment.BottomCenter),
//                isExpanded = isExpanded,
//                onClick = { isExpanded = !isExpanded })
//        }
//
//        Spacer(modifier = Modifier.height(32.dp))
//
//        SwipeButton(
//            isActive = isOnline,
//            isLoading = isUpdatingOnlineStatus,
//            activeText = "Swipe to go offline >",
//            inactiveText = "Swipe to go online >",
//            loadingText = if (isOnline) "Going offline..." else "Going online...",
//            onStateChange = { newStatus ->
//                onOnlineStatusChanged(newStatus)
//            }
//        )
//    }
//}
//
//@Composable
//fun DriverStats(
//    modifier: Modifier = Modifier,
//    tripsCount: String,
//    tripsLabel: String = "Trips today",
//    earningsAmount: String,
//    earningsLabel: String = "Earned today",
//    currencySymbol: String = "₦"
//) {
//    Row(
//        modifier = modifier.fillMaxWidth(),
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.SpaceEvenly
//    ) {
//        Column(
//            modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(
//                text = tripsCount,
//                fontWeight = FontWeight.W700,
//                fontSize = 18.sp,
//                color = Color.Black
//            )
//            Spacer(modifier = Modifier.height(4.dp))
//            Text(
//                text = tripsLabel,
//                fontWeight = FontWeight.W600,
//                fontSize = 12.sp,
//                color = Color(0xFF9A9A9A)
//            )
//        }
//
//        Divider(
//            modifier = Modifier
//                .height(20.dp)
//                .width(1.dp), color = BorderSubtle
//        )
//
//        Column(
//            modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(
//                text = earningsAmount,
//                fontWeight = FontWeight.W700,
//                fontSize = 18.sp,
//                color = MaterialTheme.colorScheme.onSurface
//            )
//            Spacer(modifier = Modifier.height(4.dp))
//            Text(
//                text = earningsLabel,
//                fontWeight = FontWeight.W600,
//                fontSize = 12.sp,
//                color = TextSecondary
//            )
//        }
//    }
//}
//
//
//@Composable
//private fun PaymentOptions(driverViewModel: DriverViewModel) {
//    // Observe the state from the ViewModel
//    val paymentMethods by driverViewModel.userPaymentMethods.collectAsState()
//
//    // A local copy to safely work with, handling the initial null case
//    val currentMethods = paymentMethods
//
//    // We only show the UI if we have successfully loaded the payment methods
//    if (currentMethods != null) {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceAround,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            PaymentOptionCheckbox(
//                text = "Cash", checked = currentMethods.cash!!, onCheckedChange = {
//                    // When this checkbox changes, call the ViewModel...
//                    driverViewModel.setPaymentMethodPreference(
//                        cash = it, // ...with the new value for "cash"
//                        wallet = currentMethods.wallet!!, //...and the existing values for others
//                        bankTransfer = currentMethods.card!!
//                    )
//                })
//            PaymentOptionCheckbox(
//                text = "Wallet", checked = currentMethods.wallet!!, onCheckedChange = {
//                    driverViewModel.setPaymentMethodPreference(
//                        cash = currentMethods.cash, wallet = it, // New value for "wallet"
//                        bankTransfer = currentMethods.card!!
//                    )
//                })
//            PaymentOptionCheckbox(
//                text = "Bank Transfer",
//                checked = currentMethods.card!!, // Note: This maps to 'card' in your data model
//                onCheckedChange = {
//                    driverViewModel.setPaymentMethodPreference(
//                        cash = currentMethods.cash,
//                        wallet = currentMethods.wallet,
//                        bankTransfer = it // New value for 'card'/'bankTransfer'
//                    )
//                })
//        }
//    } else {
//        // Optional: Show a loading indicator while fetching payment methods
//        CircularProgressIndicator(modifier = Modifier.size(24.dp))
//    }
//}
//
//@Composable
//private fun PaymentOptionCheckbox(
//    text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit
//) {
//    Row(
//        verticalAlignment = Alignment.CenterVertically,
//        modifier = Modifier.clickable { onCheckedChange(!checked) }) {
//        // Custom checkbox with filled background
//        Box(
//            modifier = Modifier
//                .size(18.dp)
//                .background(
//                    color = if (checked) Color(0xFFFFBF00) else Color(0xFF9E9E9E), // Orange when checked, grey when unchecked
//                    shape = RoundedCornerShape(4.dp)
//                )
//                .clickable { onCheckedChange(!checked) },
//            contentAlignment = Alignment.Center
//        ) {
//            // White checkmark icon using Material Icons
//            Icon(
//                imageVector = Icons.Default.Check,
//                contentDescription = if (checked) "Checked" else "Unchecked",
//                tint = Color.White,
//                modifier = Modifier.size(10.dp)
//            )
//        }
//        Spacer(modifier = Modifier.width(4.dp))
//        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.W500)
//    }
//}
//
//
//@Composable
//private fun ViewMoreButton(
//    modifier: Modifier = Modifier, isExpanded: Boolean, onClick: () -> Unit
//) {
//    Surface(
//        modifier = modifier.clickable(onClick = onClick),
//        shape = RoundedCornerShape(10.dp),
//        color = MaterialTheme.colorScheme.surface,
//        shadowElevation = 0.dp
//    ) {
//        Row(
//            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Image(
//                painter = painterResource(id = R.drawable.arrows_expand),
//                contentDescription = if (isExpanded) "View Less" else "View More",
//                modifier = Modifier.size(18.dp)
//            )
//            Spacer(modifier = Modifier.width(8.dp))
//            Text(
//                text = if (isExpanded) "View Less" else "View More",
//                fontWeight = FontWeight.W600,
//                fontSize = 14.sp,
//                color = MaterialTheme.colorScheme.onSurface
//            )
//        }
//    }
//}
//
//@Composable
//private fun OfflineStatus(isOnline: Boolean) {
//    val statusText = if (isOnline) "You are online" else "You are offline"
//    val dotColor = if (isOnline) Success else TextSecondary
//    val textColor = if (isOnline) MaterialTheme.colorScheme.onSurface else TextSecondary
//
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.Center,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Box(
//            modifier = Modifier
//                .size(8.dp)
//                .background(dotColor, CircleShape)
//        )
//        Spacer(modifier = Modifier.width(8.dp))
//        Text(
//            text = statusText, color = textColor, fontWeight = FontWeight.W500, fontSize = 14.sp
//        )
//    }
//}
//
//
//
