package com.kabukabu.driver.features.home.presentation.components

import android.location.Location
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kabukabu.driver.R
import com.kabukabu.driver.core.components.SwipeButton
import com.kabukabu.driver.core.utils.composableSafeClickable
import com.kabukabu.driver.features.home.presentation.viewmodel.DriverViewModel
import com.kabukabu.driver.features.home.presentation.viewmodel.TripStatus
import com.kabukabu.driver.features.home.presentation.views.components.WaitingTimeComponent
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnchoredMapControlsIntegrated(
    showArrivedButton: Boolean = false,
    isArrivedButtonLoading: Boolean = false,
    isStartTripLoading: Boolean = false,
    showEndTripButton: Boolean = false,
    isEndTripLoading: Boolean = false,
    currentTripStatus: TripStatus = TripStatus.STANDBY,
    sheetState: SheetState,
    offsetY: androidx.compose.ui.unit.Dp,
    isExpanded: Boolean,
    currentLocation: Location?,
    riderPoint: Point?,
    mapView: MapView?,
    driverViewModel: DriverViewModel,
    onOpenGoogleMaps: () -> Unit,
    onRecenterMap: () -> Unit
) {
    val density = LocalDensity.current
    val offsetYPx = with(density) { offsetY.toPx() }

    // Collect the showArrivedButton state from ViewModel
    val showArrivedButton by driverViewModel.showArrivedButton.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                val sheetTop = try {
                    sheetState.requireOffset()
                } catch (e: Exception) {
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
                // Google Maps Pill - Now functional!
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.composableSafeClickable(onClick = onOpenGoogleMaps)
                ) {
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

                // Compass/Location Button - Now functional!
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(60.dp).composableSafeClickable(onClick = onRecenterMap)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.resetlocation),
                            contentDescription = "Recenter Map",
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
            }

            // Show waiting time countdown if active (always visible regardless of expanded state)
            val waitingTimeSeconds by driverViewModel.waitingTimeSeconds.collectAsState()
            if (waitingTimeSeconds != null && waitingTimeSeconds!! > 0 && currentTripStatus == TripStatus.ARRIVED_PICKUP) {
                Spacer(modifier = Modifier.height(15.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                ) {
                    WaitingTimeComponent(
                        remainingSeconds = waitingTimeSeconds!!, totalSeconds = 300
                    )
                }
            }

            // Show buttons based on trip status when not expanded
            if (!isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(15.dp))

                    when (currentTripStatus) {
                        TripStatus.ACCEPTED -> {
                            // Show "I have arrived" button when driver is heading to pickup and within 500m
                            if (showArrivedButton) {
                                ArrivedButton(
                                    onClick = { driverViewModel.arrivedPickup() },
                                    isLoading = isArrivedButtonLoading
                                )
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
                            }
                        }

                        else -> {
                            // No button for other statuses
                        }
                    }
                }
            }
        }
    }
}

