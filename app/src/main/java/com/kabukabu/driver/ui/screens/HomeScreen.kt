package com.kabukabu.driver.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.kabukabu.driver.R
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import kotlinx.coroutines.launch
import androidx.compose.animation.core.Animatable

@Composable
fun HomeScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    var mapView: MapView? by remember { mutableStateOf(null) }

    val addMarkerToMap = { lat: Double, lng: Double ->
        mapView?.let {
            bitmapFromDrawable(context, R.drawable.ic_red_marker)?.let { bitmap ->
                val annotationApi = it.annotations
                val pointAnnotationManager = annotationApi.createPointAnnotationManager()
                pointAnnotationManager.deleteAll()
                val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                    .withPoint(Point.fromLngLat(lng, lat))
                    .withIconImage(bitmap)
                pointAnnotationManager.create(pointAnnotationOptions)
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                getCurrentLocation(context) { lat, lng ->
                    mapView?.getMapboxMap()?.setCamera(
                        CameraOptions.Builder()
                            .center(Point.fromLngLat(lng, lat))
                            .zoom(16.0)
                            .build()
                    )
                    addMarkerToMap(lat, lng)
                }
            }
        }
    )

    LaunchedEffect(mapView) {
        if (mapView != null) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                getCurrentLocation(context) { lat, lng ->
                    mapView?.getMapboxMap()?.setCamera(
                        CameraOptions.Builder()
                            .center(Point.fromLngLat(lng, lat))
                            .zoom(16.0)
                            .build()
                    )
                    addMarkerToMap(lat, lng)
                }
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                MapView(it).apply {
                    getMapboxMap().loadStyleUri("mapbox://styles/kabukabuapp/cmd1e8u2s009k01s913jc4ywh")
                    mapView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            ExpandableDriverStatusCard(onLogout = onLogout)
        }
    }
}

@Composable
private fun ExpandableDriverStatusCard(onLogout: () -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    var isOnline by remember { mutableStateOf(false) }

    Column {
        Box {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OfflineStatus(isOnline = isOnline)
                        Spacer(modifier = Modifier.height(16.dp))
                        DriverStats()
                    }

                    if (isExpanded) {
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("More content goes here...")
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onLogout) {
                                Text("Logout")
                            }
                        }
                    }
                }
            }

            ViewMoreButton(
                modifier = Modifier.align(Alignment.BottomCenter),
                isExpanded = isExpanded,
                onClick = { isExpanded = !isExpanded }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OnlineSlider(
            isOnline = isOnline,
            onStateChange = { isOnline = it }
        )
    }
}

@Composable
private fun ViewMoreButton(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.arrows_expand),
                contentDescription = if (isExpanded) "View Less" else "View More",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isExpanded) "View Less" else "View More",
                fontWeight = FontWeight.W600,
                fontSize = 14.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
private fun OfflineStatus(isOnline: Boolean) {
    val statusText = if (isOnline) "You are online" else "You are offline"
    val dotColor = if (isOnline) Color(0xFF26AE23) else Color.Gray
    val textColor = if (isOnline) Color.Black else Color.Gray

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(dotColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = statusText,
            color = textColor,
            fontWeight = FontWeight.W500,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun DriverStats() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "0",
                fontWeight = FontWeight.W700,
                fontSize = 18.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Trips today",
                fontWeight = FontWeight.W600,
                fontSize = 12.sp,
                color = Color(0xFF9A9A9A)
            )
        }

        Divider(
            modifier = Modifier
                .height(40.dp)
                .width(1.dp),
            color = Color.LightGray
        )

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "₦0",
                fontWeight = FontWeight.W700,
                fontSize = 18.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Earned today",
                fontWeight = FontWeight.W600,
                fontSize = 12.sp,
                color = Color(0xFF9A9A9A)
            )
        }
    }
}

@Composable
private fun OnlineSlider(
    isOnline: Boolean,
    onStateChange: (Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }

    // Online colors
    val onlineContainerColor = Color(0xFFFFBF00)
    val onlineTextColor = Color(0xFFFFF5D8)
    val onlineDraggerColor = Color(0xFFC39200)
    val onlineIconColor = Color(0xFFFDFDFD)

    // Offline colors
    val offlineContainerColor = Color(0xFF161616) // rgba(22, 22, 22, 1)
    val offlineTextColor = Color(0xFF9A9A9A)      // rgba(154, 154, 154, 1)
    val offlineDraggerColor = Color(0xFF3C3B3B)   // rgba(60, 59, 59, 1)
    val offlineIconColor = Color(0xFFFFBF00)      // rgba(255, 191, 0, 1)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(
                color = if (isOnline) onlineContainerColor else offlineContainerColor,
                shape = RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        val containerWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val handleSizePx = with(LocalDensity.current) { 44.dp.toPx() }
        val maxOffset = containerWidthPx - handleSizePx - with(LocalDensity.current) { (7.dp * 2).toPx() }

        Text(
            text = if (isOnline) "Swipe to go offline >" else "Swipe to go online >",
            color = if (isOnline) onlineTextColor else offlineTextColor,
            fontWeight = FontWeight.W600,
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = 16.dp)
        )

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .padding(horizontal = 7.dp)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        coroutineScope.launch {
                            offsetX.snapTo((offsetX.value + delta).coerceIn(0f, maxOffset))
                        }
                    },
                    onDragStopped = {
                        coroutineScope.launch {
                            if (offsetX.value > maxOffset / 2) {
                                onStateChange(!isOnline)
                                // Animate to the end, hold, then animate back to start
                                offsetX.animateTo(maxOffset, animationSpec = tween(100))
                                delay(200)
                                offsetX.animateTo(0f, animationSpec = tween(300))
                            } else {
                                // Animate back to start
                                offsetX.animateTo(0f, animationSpec = tween(200))
                            }
                        }
                    }
                )
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = if (isOnline) onlineDraggerColor else offlineDraggerColor,
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.caret_right),
                    contentDescription = "Slide to toggle online status",
                    modifier = Modifier.size(width = 30.dp, height = 20.dp),
                    colorFilter = ColorFilter.tint(if (isOnline) onlineIconColor else offlineIconColor)
                )
            }
        }
    }
}


private fun getCurrentLocation(context: Context, onLocation: (Double, Double) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    try {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    onLocation(location.latitude, location.longitude)
                } else {
                    onLocation(6.5244, 3.3792)
                }
            }
            .addOnFailureListener {
                onLocation(6.5244, 3.3792)
            }
    } catch (e: SecurityException) {
        onLocation(6.5244, 3.3792)
    }
}

private fun bitmapFromDrawable(context: Context, @DrawableRes resId: Int): Bitmap? {
    return AppCompatResources.getDrawable(context, resId)?.let { drawable ->
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    }
} 