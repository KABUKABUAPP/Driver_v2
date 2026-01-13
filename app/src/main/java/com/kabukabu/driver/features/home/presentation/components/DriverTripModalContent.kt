package com.kabukabu.driver.features.home.presentation.components

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kabukabu.driver.R
import com.kabukabu.driver.core.components.SwipeButton
import com.kabukabu.driver.core.data.socket.TripFoundEvent
import com.kabukabu.driver.features.chat.presentation.viewmodel.ChatViewModel
import com.kabukabu.driver.features.home.presentation.viewmodel.DriverViewModel
import com.kabukabu.driver.features.home.presentation.viewmodel.TripStatus
import com.kabukabu.driver.features.home.presentation.viewmodel.TripViewModel
import com.kabukabu.driver.features.home.presentation.views.components.RouteState
import com.kabukabu.driver.features.profile.data.ActiveTrip

val ModalTextDark = Color(0xFF1A1A1A)
val ModalTextGray = Color(0xFF9A9A9A)
val ModalLightGrayBg = Color(0xF8F8F8)
val CancelRed = Color(0xFFEF2C5B)

@Composable
fun DriverTripModalContent(
    driverViewModel: DriverViewModel,
    tripViewModel: TripViewModel,
    trip: ActiveTrip?,
    pendingTripEvent: TripFoundEvent? = null,
    showArrivedButton: Boolean = false,
    isArrivedButtonLoading: Boolean = false,
    isStartTripLoading: Boolean = false,
    showEndTripButton: Boolean = false,
    isEndTripLoading: Boolean = false,
    currentTripStatus: TripStatus = TripStatus.STANDBY,
    routeState: RouteState? = null,
    isExpanded: Boolean = false,
    onNavigateToChat: (orderId: String, riderName: String, riderPhone: String?) -> Unit = { _, _, _ -> },
    chatViewModel: ChatViewModel? = null
) {
    val context = LocalContext.current
    val isCanceling by tripViewModel.isDeclining.collectAsState()

    // Helper functions to get data from either source
    val getTripDuration = { trip?.durationInMinutes ?: pendingTripEvent?.duration }
    val getPickupAddress = { trip?.startAddress?.fullAddress ?: pendingTripEvent?.pickupLocation?.name }
    val getRiderName = { trip?.user?.fullName ?: pendingTripEvent?.user?.fullname }
    val getRiderTrips = { trip?.user?.totalTrips ?: pendingTripEvent?.user?.totalTrips }
    val getRiderPhone = { trip?.user?.phoneNumber ?: pendingTripEvent?.user?.phoneNumber }
    val getDistance = { trip?.distanceInKm ?: pendingTripEvent?.distance }
    val getPriceRange = { trip?.priceRange ?: pendingTripEvent?.priceRange }
    val getDropoffAddress = { trip?.endAddress?.fullAddress ?: pendingTripEvent?.destinationLocation?.name }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        // 1. HEADER: Title + Time Badge (dynamic title based on trip status and distance)
        val headerText = when {
            showEndTripButton -> "You have arrived at destination"
            currentTripStatus == TripStatus.TRIP_STARTED -> "You are driving to destination"
            showArrivedButton -> "You have arrived at pickup location"
            currentTripStatus == TripStatus.ACCEPTED -> "You are driving to rider"
            else -> "You are driving to rider"
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = headerText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.W700,
                    color = ModalTextDark,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = routeState?.formattedDuration
                        ?: getTripDuration()?.let { "Est. $it minutes" } ?: "Calculating...",
                    fontSize = 12.sp,
                    color = ModalTextGray,
                    fontWeight = FontWeight.W500)
            }

            // Real-time "X Mins away" Badge - fixed width, won't be pushed
            Surface(
                color = ModalLightGrayBg,
                border = BorderStroke(1.dp, Color(0xFFF1F1F1)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.widthIn(min = 72.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${routeState?.durationInMinutes ?: getTripDuration() ?: "~"}",
                        fontWeight = FontWeight.W700,
                        fontSize = 16.sp,
                        color = ModalTextDark
                    )
                    Text(
                        text = "Mins away",
                        fontSize = 12.sp,
                        color = ModalTextDark,
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
                    contentDescription = "Pickup Pin",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Pickup Point",
                        fontSize = 12.sp,
                        color = ModalTextGray,
                        fontWeight = FontWeight.W400
                    )
                    Text(
                        text = getPickupAddress() ?: "Pickup location",
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
                        text = getRiderName() ?: "Rider",
                        fontWeight = FontWeight.W700,
                        fontSize = 18.sp,
                        color = ModalTextDark
                    )
                    Text(
                        text = "${getRiderTrips() ?: 0} total trips",
                        fontSize = 12.sp,
                        color = ModalTextGray,
                        fontWeight = FontWeight.W600
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Chat & Call Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Get unread message count from chatViewModel
                        val unreadCount = chatViewModel?.unreadMessageCount?.collectAsState()?.value ?: 0

                        BadgedIconButton(
                            id = R.drawable.message_1,
                            badgeCount = unreadCount,
                            onClick = {
                                // Navigate to chat screen with shared ViewModel
                                val orderId = trip?.order ?: ""
                                val riderName = getRiderName() ?: "Rider"
                                val riderPhone = getRiderPhone()
                                if (orderId.isNotBlank()) {
                                    // Navigate immediately - ChatScreen will mark as read
                                    onNavigateToChat(orderId, riderName, riderPhone)
                                    Log.d("TripModal", "Navigating to chat - orderId: $orderId, riderName: $riderName, riderPhone: $riderPhone")
                                } else {
                                    Log.w("TripModal", "Cannot navigate to chat: orderId is blank")
                                }
                            })
                        ProfileIconButton(
                            id = R.drawable.phone_1, onClick = {
                                // Dial the user's phone number
                                val phoneNumber = getRiderPhone()
                                if (!phoneNumber.isNullOrBlank()) {
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:$phoneNumber")
                                        }
                                        context.startActivity(intent)
                                        Log.d("TripModal", "Dialing: $phoneNumber")
                                    } catch (e: Exception) {
                                        Log.e("TripModal", "Failed to open dialer: ${e.message}")
                                    }
                                } else {
                                    Log.w("TripModal", "Phone number is not available")
                                }
                            })
                    }
                }

                // Right Side: Avatar Placeholder
                Surface(
                    shape = CircleShape, color = Color(0xFFE0E0E0), modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Rider Avatar",
                            tint = Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
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
                    Text(
                        text = "Estimated Price",
                        fontSize = 12.sp,
                        color = ModalTextGray,
                        fontWeight = FontWeight.W400
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.wallet_1),
                            contentDescription = "Wallet",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Wallet Payment",
                            fontSize = 12.sp,
                            color = ModalTextDark,
                            fontWeight = FontWeight.W400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))


                // Price Text
                Text(
                    buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.W700,
                                fontSize = 16.sp,
                                color = ModalTextDark
                            )
                        ) {
                            val priceRange = getPriceRange()
                            append(if (priceRange != null && priceRange.isNotEmpty()) "₦${priceRange.last()}" else "₦0")
                        }
                    })
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // 5. BUTTONS ("I have arrived", "Start Trip", "End Trip" & "Cancel")
        // Show buttons based on trip status - only when sheet is expanded
        if (isExpanded) {
            when (currentTripStatus) {
                TripStatus.ACCEPTED -> {
                    // Show "I have arrived" button when driver is heading to pickup and within 500m
                    if (showArrivedButton) {
                        ArrivedButton(
                            onClick = { driverViewModel.arrivedPickup() },
                            isLoading = isArrivedButtonLoading
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                TripStatus.ARRIVED_PICKUP -> {
                    // Show "Start Trip" swipe button when driver has arrived at pickup
                    SwipeButton(
                        isActive = true,
                        isLoading = isStartTripLoading,
                        activeText = "Swipe to start trip >",
                        inactiveText = "Swipe to start trip >",
                        loadingText = "Starting trip...",
                        onStateChange = { driverViewModel.startTrip() })
                    Spacer(modifier = Modifier.height(16.dp))
                }

                TripStatus.TRIP_STARTED -> {
                    // Show End Trip button when driver is near destination
                    if (showEndTripButton) {
                        SwipeButton(
                            isActive = true,
                            isLoading = isEndTripLoading,
                            activeText = "Swipe to end trip >",
                            inactiveText = "Swipe to end trip >",
                            loadingText = "Ending trip...",
                            onStateChange = { driverViewModel.endTrip() })
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                else -> {
                    // No button for other statuses
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if(currentTripStatus == TripStatus.ACCEPTED || currentTripStatus == TripStatus.ARRIVED_PICKUP){
            Button(
                onClick = {
                    // Get trip ID from either activeTrip or pendingTripEvent
                    val orderId = trip?.order ?: pendingTripEvent?.eventId
                    if (!orderId.isNullOrBlank()) {
                        tripViewModel.cancelActiveTrip(
                            orderId = orderId,
                            reason = "Driver cancelled",
                            driverViewModel = driverViewModel
                        )
                        chatViewModel?.clearChatData()
                    }
                },
                enabled = !isCanceling,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0XFFF1F1F1)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                if (isCanceling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = CancelRed,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Cancel Trip",
                        color = CancelRed,
                        fontWeight = FontWeight.W500,
                        fontSize = 14.sp
                    )
                }
            }

        }


        // Extra space at the bottom for better scrolling experience
        Spacer(modifier = Modifier.height(24.dp))
    }
}

