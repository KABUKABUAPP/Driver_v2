package com.kabukabu.driver.core.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kabukabu.driver.R
import kotlinx.coroutines.delay

// --- Colors ---
val KabuSuccessGreen = Color(0xFF4CAF50)
val KabuErrorRed = Color(0xFFE53935)

/**
 * Reusable Success Toast
 * @param message The success message to display
 * @param isVisible Controls the visibility of the toast
 * @param modifier Optional modifier
 */
@Composable
fun SuccessToast(
    message: String,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Card(
            shape = RoundedCornerShape(99.dp), // Pill shape
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Success Icon from drawable
                Icon(
                    painter = painterResource(id = R.drawable.success),
                    contentDescription = null,
                    tint = KabuSuccessGreen,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Message Text
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.W500
                )
            }
        }
    }
}

/**
 * Reusable Error Toast
 * @param message The error message to display
 * @param isVisible Controls the visibility of the toast
 * @param modifier Optional modifier
 */
@Composable
fun ErrorToast(
    message: String,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Card(
            shape = RoundedCornerShape(99.dp), // Pill shape
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Error Icon from drawable
                Icon(
                    painter = painterResource(id = R.drawable.error),
                    contentDescription = null,
                    tint = KabuErrorRed,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Message Text
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.W500
                )
            }
        }
    }
}

/**
 * Legacy function - kept for backward compatibility
 * @deprecated Use SuccessToast instead
 */
@Deprecated("Use SuccessToast with custom message", ReplaceWith("SuccessToast(message = \"Card removed successfully\", isVisible = isVisible)"))
@Composable
fun CardRemovedSuccessToast(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    SuccessToast(
        message = "Card removed successfully",
        isVisible = isVisible,
        modifier = modifier
    )
}

/**
 * Demo Composable to show how the toasts appear and disappear.
 */
@Composable
fun ToastDemoScreen() {
    var showSuccessToast by remember { mutableStateOf(false) }
    var showErrorToast by remember { mutableStateOf(false) }

    // Auto-hide success toast after 3 seconds
    LaunchedEffect(showSuccessToast) {
        if (showSuccessToast) {
            delay(3000)
            showSuccessToast = false
        }
    }

    // Auto-hide error toast after 3 seconds
    LaunchedEffect(showErrorToast) {
        if (showErrorToast) {
            delay(3000)
            showErrorToast = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        // Trigger buttons
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = { showSuccessToast = true }) {
                    Text("Show Success Toast")
                }

                Button(onClick = { showErrorToast = true }) {
                    Text("Show Error Toast")
                }
            }
        }

        // The actual Toasts, anchored to the top/center of the screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 50.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            SuccessToast(
                message = "Operation completed successfully",
                isVisible = showSuccessToast
            )

            ErrorToast(
                message = "Something went wrong. Please try again",
                isVisible = showErrorToast
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SuccessToastPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray)
                .padding(top = 50.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            SuccessToast(
                message = "Card removed successfully",
                isVisible = true
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorToastPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray)
                .padding(top = 50.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            ErrorToast(
                message = "Failed to remove card",
                isVisible = true
            )
        }
    }
}