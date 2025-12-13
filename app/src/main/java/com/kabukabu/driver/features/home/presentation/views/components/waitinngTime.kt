package com.kabukabu.driver.features.home.presentation.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kabukabu.driver.R

@Composable
fun WaitingTimeComponent(
    remainingSeconds: Int,
    totalSeconds: Int = 300 // 5 minutes default
) {
    // Calculate progress (1.0 = full, 0.0 = empty)
    val progress = remainingSeconds.toFloat() / totalSeconds.toFloat()

    // Format time as MM:SS
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeText = String.format("%02d:%02d", minutes, seconds)

    // 1. The Container: A pill-shaped surface
    Box( contentAlignment = Alignment.Center) {
        Surface(
            color = Color(0xFF161616), // Dark background similar to the screenshot
            shape = RoundedCornerShape(percent = 30), // Fully rounded corners (Pill)
            modifier = Modifier
                .width(223.dp)
                .height(37.dp) // Standard touch target height
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 34.dp, vertical = 4.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Timer Icon
                    Icon(
                        painter = painterResource(id = R.drawable.clock_alt),
                        contentDescription = "Timer",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Text
                    Text(
                        text = "Waiting time: $timeText",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W600,
                        // Use standard letter spacing
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFCCCCCC)
@Composable
fun WaitingTimePreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        WaitingTimeComponent(remainingSeconds = 180) // 3 minutes for preview
    }
}