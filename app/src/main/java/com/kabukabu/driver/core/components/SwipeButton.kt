package com.kabukabu.driver.core.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kabukabu.driver.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * A reusable swipe button component with loading state support
 *
 * @param isActive Whether the button is in active state (e.g., "online")
 * @param isLoading Whether the button is in loading state (shows spinner, disables interaction)
 * @param activeText Text to show when active (e.g., "Swipe to go offline >")
 * @param inactiveText Text to show when inactive (e.g., "Swipe to go online >")
 * @param loadingText Text to show when loading (e.g., "Going online...")
 * @param activeColors Colors to use when active
 * @param inactiveColors Colors to use when inactive
 * @param onStateChange Callback when the swipe is completed
 * @param modifier Optional modifier for customization
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SwipeButton(
    isActive: Boolean,
    isLoading: Boolean = false,
    activeText: String = "Swipe to go offline >",
    inactiveText: String = "Swipe to go online >",
    loadingText: String = "Please wait...",
    activeColors: SwipeButtonColors = SwipeButtonDefaults.activeColors(),
    inactiveColors: SwipeButtonColors = SwipeButtonDefaults.inactiveColors(),
    onStateChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }

    val colors = if (isActive) activeColors else inactiveColors
    val displayText = when {
        isLoading -> loadingText
        isActive -> activeText
        else -> inactiveText
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(
                color = colors.containerColor,
                shape = RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        val containerWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val handleSizePx = with(LocalDensity.current) { 44.dp.toPx() }
        val maxOffset =
            containerWidthPx - handleSizePx - with(LocalDensity.current) { (7.dp * 2).toPx() }

        // Text in center
        Text(
            text = displayText,
            color = colors.textColor,
            fontWeight = FontWeight.W600,
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = 16.dp)
        )

        // Draggable handle
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .padding(horizontal = 7.dp)
                .then(
                    if (!isLoading) {
                        Modifier.draggable(
                            orientation = Orientation.Horizontal,
                            state = rememberDraggableState { delta ->
                                coroutineScope.launch {
                                    offsetX.snapTo((offsetX.value + delta).coerceIn(0f, maxOffset))
                                }
                            },
                            onDragStopped = {
                                coroutineScope.launch {
                                    if (offsetX.value > maxOffset / 2) {
                                        // Animate to the end, trigger callback, then reset
                                        offsetX.animateTo(maxOffset, animationSpec = tween(100))
                                        onStateChange(!isActive)
                                        delay(200)
                                        offsetX.animateTo(0f, animationSpec = tween(300))
                                    } else {
                                        // Animate back to start
                                        offsetX.animateTo(0f, animationSpec = tween(200))
                                    }
                                }
                            }
                        )
                    } else {
                        Modifier // No dragging when loading
                    }
                )
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = colors.draggerColor,
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    // Show loading spinner
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = colors.iconColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    // Show arrow icon
                    Image(
                        painter = painterResource(id = R.drawable.caret_right),
                        contentDescription = "Slide to toggle status",
                        modifier = Modifier.size(width = 30.dp, height = 20.dp),
                        colorFilter = ColorFilter.tint(colors.iconColor)
                    )
                }
            }
        }
    }
}

/**
 * Data class to hold colors for the SwipeButton
 */
data class SwipeButtonColors(
    val containerColor: Color,
    val textColor: Color,
    val draggerColor: Color,
    val iconColor: Color
)

/**
 * Default colors and configurations for SwipeButton
 */
object SwipeButtonDefaults {
    /**
     * Default colors for active state (online/enabled)
     */
    @Composable
    fun activeColors(
        containerColor: Color = Color(0xFFFFBF00),
        textColor: Color = Color(0xFFFFF5D8),
        draggerColor: Color = Color(0xFFC39200),
        iconColor: Color = Color(0xFFFDFDFD)
    ) = SwipeButtonColors(
        containerColor = containerColor,
        textColor = textColor,
        draggerColor = draggerColor,
        iconColor = iconColor
    )

    /**
     * Default colors for inactive state (offline/disabled)
     */
    @Composable
    fun inactiveColors(
        containerColor: Color = Color(0xFF161616),
        textColor: Color = Color(0xFF9A9A9A),
        draggerColor: Color = Color(0xFF3C3B3B),
        iconColor: Color = Color(0xFFFFBF00)
    ) = SwipeButtonColors(
        containerColor = containerColor,
        textColor = textColor,
        draggerColor = draggerColor,
        iconColor = iconColor
    )
}

