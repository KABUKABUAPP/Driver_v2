package com.kabukabu.driver.features.home.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kabukabu.driver.core.utils.composableSafeClickable

//private val ModalTextDark = Color(0xFF1A1A1A)
//private val ModalLightGrayBg = Color(0xFFF5F5F5)
private val PrimaryYellow = Color(0xFFF4C430)

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
fun ProfileIconButton(id: Int, onClick: () -> Unit = {}) {
    Surface(
        color = Color(0xFFEBEBEB),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(42.dp).composableSafeClickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = id),
                contentDescription = "Action Button",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun BadgedIconButton(id: Int, badgeCount: Int = 0, onClick: () -> Unit = {}) {
    Surface(
        color = Color(0xFFEBEBEB),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(42.dp).composableSafeClickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = id),
                contentDescription = "Action Button",
                modifier = Modifier.size(20.dp)
            )

            // Show badge if count > 0
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-2).dp, y = 2.dp)
                        .size(18.dp)
                        .background(Color.Red, CircleShape)
                        .wrapContentSize(Alignment.Center)
                ) {
                    Text(
                        text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        style = androidx.compose.ui.text.TextStyle(
                            lineHeight = 9.sp,
                            platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                                includeFontPadding = false
                            )
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ArrivedButton(
    onClick: () -> Unit = {},
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow),
        shape = RoundedCornerShape(40),
        border = BorderStroke(1.dp, Color.Black),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = ModalTextDark,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "I've Arrived",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = ModalTextDark
            )
        }
    }
}

@Composable
fun TripCompletionContainerBox(content: @Composable () -> Unit) {
    // Outer surface for clean white background
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White
    ) {
        Box(
            modifier = Modifier.border(
                    width = 1.dp,
                    color = Color(0xFFE0E0E0).copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            content()
        }
    }
}

@Composable
fun TripCompletionFeedbackChip(
    text: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Surface(
        color = if (isSelected) Color(0xFFF4C430) else Color(0xFFF5F5F5),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.composableSafeClickable(onClick = onClick)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color.Black else Color(0xFF1F1F1F)
        )
    }
}

