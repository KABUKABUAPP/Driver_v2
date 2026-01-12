package com.kabukabu.driver.components.ui


import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import com.kabukabu.driver.core.theme.KabukabuYellow
import com.kabukabu.driver.core.utils.noRippleClickable


@Composable
fun AnimatedLogo(
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    assetResId: Int // SVG resource (like R.drawable.logo)
) {
    // Animation setup
    val infiniteTransition = rememberInfiniteTransition(label = "logo_animation")

    // Animate scale between 0.9f and 1.1f
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f, targetValue = 1.1f, animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing), repeatMode = RepeatMode.Reverse
        ), label = "scale_anim"
    )

    // Animate rotation (0 → 4π)
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 4 * Math.PI.toFloat(), animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing), repeatMode = RepeatMode.Restart
        ), label = "rotation_anim"
    )

    // Apply transformations
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = rotation * (180f / Math.PI.toFloat()) // radians → degrees
            }, contentAlignment = Alignment.Center
    ) {
        // Display SVG
        Image(
            painter = painterResource(id = assetResId),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentScale = ContentScale.Crop,

            )
    }
}

@Composable
fun LogoLoader() {
    Box(
        modifier = Modifier
            .height(72.dp)
            .width(72.dp)
            .clip(RoundedCornerShape(10.dp)).background(KabukabuYellow),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
    }}


@Composable
fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .noRippleClickable(enabled = false) { }, // Prevent clicks
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.5f)
        ) {}

        LogoLoader()
    }
}
