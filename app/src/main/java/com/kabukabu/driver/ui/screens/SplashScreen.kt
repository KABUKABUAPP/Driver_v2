package com.kabukabu.driver.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.kabukabu.driver.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import android.os.Handler
import android.os.Looper
import androidx.compose.ui.res.colorResource

@Composable
fun SplashScreen(
    onGetStartedClick: () -> Unit
) {
    val context = LocalContext.current
    var visible by remember { mutableStateOf(true) }
    
    // Get the dark gray color from resources
    val darkGrayColor = colorResource(id = R.color.dark_gray)
    
    // Create custom ImageLoader that can handle GIFs
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 500)),
        exit = fadeOut(animationSpec = tween(durationMillis = 500))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(darkGrayColor)  // Set the background color to dark gray
        ) {
            // GIF container taking up 2/3 of screen height
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.67f) // Takes up approximately 2/3 of the screen
                    .align(Alignment.TopCenter)
                    .background(darkGrayColor)  // Set the GIF background to dark gray
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(context)
                            .data(R.drawable.splash)
                            .build(),
                        imageLoader = imageLoader
                    ),
                    contentDescription = "Splash Animation",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Bottom Sheet
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.33f) // Exactly 1/3 of the screen
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(darkGrayColor)  // Set the bottom sheet color to dark gray
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo at the top of the bottom sheet
                    Image(
                        painter = painterResource(id = R.drawable.splashlogo),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .width(200.dp)
                            .height(60.dp)
                            .padding(bottom = 24.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Get Started Button at the bottom
                    Button(
                        onClick = {
                            // Trigger fade-out animation, then navigate
                            visible = false
                            // Delay navigation slightly to allow animation to play
                            Handler(Looper.getMainLooper()).postDelayed({
                                onGetStartedClick()
                            }, 300)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Get Started",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF161616) // rgba(22, 22, 22, 1)
                        )
                    }
                }
            }
        }
    }
} 