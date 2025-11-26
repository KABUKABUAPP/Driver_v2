package com.kabukabu.driver.features.home.presentation.views.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kabukabu.driver.features.profile.data.ActiveTrip
import kotlinx.coroutines.launch

@Composable
fun DraggableActiveTripSheet(trip: ActiveTrip?) {
    val coroutineScope = rememberCoroutineScope()
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp

    // Drastically reduced heights
    val minHeight = screenHeightDp / 2.3f
    val maxHeight = screenHeightDp / 1.4f

    val minHeightPx = with(LocalDensity.current) { minHeight.toPx() }
    val maxHeightPx = with(LocalDensity.current) { maxHeight.toPx() }

    val currentHeightPx = remember { Animatable(minHeightPx) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter // Anchor to the bottom
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(LocalDensity.current) { currentHeightPx.value.toDp() })
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                // Snap to closest state
                                val midpoint = minHeightPx + (maxHeightPx - minHeightPx) / 2
                                if (currentHeightPx.value > midpoint) {
                                    currentHeightPx.animateTo(maxHeightPx, tween(150)) // Animate to expanded
                                } else {
                                    currentHeightPx.animateTo(minHeightPx, tween(150)) // Animate to collapsed
                                }
                            }
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            val newHeight = (currentHeightPx.value - dragAmount).coerceIn(minHeightPx, maxHeightPx)
                            currentHeightPx.snapTo(newHeight)
                        }
                    }
                },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color.LightGray, shape = CircleShape)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Active Trip", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Status: ${trip?.status ?: "N/A"}")
                Text(text = "From: ${trip?.startAddress?.fullAddress ?: "N/A"}")
                Text(text = "To: ${trip?.endAddress?.fullAddress ?: "N/A"}")
                Text(text = "Price: ₦${trip?.price ?: 0.0}")
            }
        }
    }
}