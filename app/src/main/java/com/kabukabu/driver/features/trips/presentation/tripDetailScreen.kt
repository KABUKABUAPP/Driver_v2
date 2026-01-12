package com.kabukabu.driver.features.trips.presentation

import com.kabukabu.driver.R

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import com.kabukabu.driver.core.utils.noRippleClickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.kabukabu.driver.features.trips.data.TripItem
import androidx.compose.ui.viewinterop.AndroidView
import com.kabukabu.driver.core.theme.KabuGray
import com.kabukabu.driver.core.theme.KabukabuDriverTheme
import com.kabukabu.driver.core.theme.KabukabuYellow
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.core.constants.Constants
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.scalebar.scalebar
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// --- 1. Utility Components (Placeholders) ---

// Placeholder for the profile image with a subtle border/ring
@Composable
fun ProfileImagePlaceholder(
    modifier: Modifier = Modifier,
    hasRing: Boolean = false,
    initial: String? = null
) {
    // Show first letter of provided initial (name) if available, fallback to 'P'
    val displayLetter = initial?.trim()?.firstOrNull()?.uppercaseChar()?.toString() ?: "P"

    Box(
        modifier = modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(Color.LightGray.copy(alpha = 0.5f))
            .then(
                if (hasRing) Modifier
                    .padding(2.dp)
                    .background(Color(0xFFE5B800), CircleShape) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        // Display the initial letter
        Text(displayLetter, fontSize = 24.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        if (hasRing) {
            // Small white badge on ring
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

// Small helpers to format ISO date/time strings
private fun formatDateTime(iso: String?): Pair<String, String> {
    if (iso.isNullOrBlank()) return Pair("", "")
    return try {
        val odt = OffsetDateTime.parse(iso)
        val date = odt.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()))
        val time = odt.format(DateTimeFormatter.ofPattern("h:mma", Locale.getDefault()))
        Pair(date, time)
    } catch (_: Exception) {
        Pair(iso, "")
    }
}

// Helper to format integer amounts with commas, return string like "12,345"
private fun formatAmountWithComma(amount: Int?): String {
    return amount?.let { String.format("%,d", it) } ?: "0"
}

// Capitalize first letter of each word in a name (safe for null)
private fun capitalizeWords(s: String?): String? {
    if (s.isNullOrBlank()) return s
    return s.split(" ").joinToString(" ") { part ->
        part.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}

// --- 2. Screen Sections ---

@Composable
fun MapSection(trip: TripItem?) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val isStyleLoaded = remember { mutableStateOf(false) }
    val polylineManagerState = remember { mutableStateOf<com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager?>(null) }
    val pointManagerState = remember { mutableStateOf<com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager?>(null) }

    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .height(160.dp)) {
        AndroidView(factory = { mapView }, modifier = Modifier.matchParentSize())

        // Top-right info badge: distance and time
        val distanceText = trip?.distanceInKm?.let { String.format("%.1f km", it) }
            ?: trip?.priceDetails?.totalCharge?.let { String.format("%.1f km", it) } ?: ""
        val timeText = trip?.durationInMinutes?.let { "$it min" }
            ?: trip?.priceDetails?.tripTime?.toInt()?.let { "$it min" } ?: ""
        val infoText = listOf(distanceText, timeText).filter { it.isNotBlank() }.joinToString(", ")

        if (infoText.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = infoText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                }
            }
        }
    }

    LaunchedEffect(mapView) {
        mapView.mapboxMap.loadStyle("mapbox://styles/kabukabuapp/cmd1e8u2s009k01s913jc4ywh") { _ ->
            isStyleLoaded.value = true
        }
        mapView.logo.enabled = false
        mapView.attribution.enabled = false
        mapView.compass.enabled = false
        mapView.scalebar.enabled = false
    }

    LaunchedEffect(isStyleLoaded.value, trip) {
        if (!isStyleLoaded.value) return@LaunchedEffect
        if (trip == null) return@LaunchedEffect

        val start = trip.startPoint
        val end = trip.endPoint
        if (start == null || end == null || start.size < 2 || end.size < 2) return@LaunchedEffect

        val origin = Point.fromLngLat(start[0], start[1])
        val destination = Point.fromLngLat(end[0], end[1])

        // Prepare managers
        val polyMgr = polylineManagerState.value ?: mapView.annotations.createPolylineAnnotationManager().also { polylineManagerState.value = it }
        val pointMgr = pointManagerState.value ?: mapView.annotations.createPointAnnotationManager().also { pointManagerState.value = it }

        // Clear existing
        polyMgr.deleteAll()
        pointMgr.deleteAll()

        // Draw pickup and destination markers
        bitmapFromDrawable(context, R.drawable.rider)?.let { bmp ->
            val pickupAnnotation = PointAnnotationOptions().withPoint(origin).withIconImage(bmp)
            pointMgr.create(pickupAnnotation)
        }
        bitmapFromDrawable(context, R.drawable.destination)?.let { bmp ->
            val destAnnotation = PointAnnotationOptions().withPoint(destination).withIconImage(bmp)
            pointMgr.create(destAnnotation)
        }

        // Fetch and draw route
        fetchAndDrawRoute(mapView, origin, destination, polyMgr)

        // Adjust camera to fit
        adjustCameraToShowRoute(mapView, origin, destination)
    }
}

// --- Helpers (adapted from MapComponent) ---
private fun fetchAndDrawRoute(
    mapView: MapView,
    origin: Point,
    destination: Point,
    polylineAnnotationManager: com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
) {
    // validate
    val distance = origin.distanceTo(destination)
    if (distance < 10.0) {
        drawStraightLine(polylineAnnotationManager, origin, destination)
        return
    }

    val accessToken = mapView.context.getString(R.string.mapbox_access_token)

    val client = MapboxDirections.builder()
        .origin(origin)
        .destination(destination)
        .overview(DirectionsCriteria.OVERVIEW_FULL)
        .profile(DirectionsCriteria.PROFILE_DRIVING)
        .accessToken(accessToken)
        .build()

    client.enqueueCall(object : Callback<DirectionsResponse> {
        override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
            if (!response.isSuccessful) {
                drawStraightLine(polylineAnnotationManager, origin, destination)
                return
            }
            val routes = response.body()?.routes()
            if (routes.isNullOrEmpty()) {
                drawStraightLine(polylineAnnotationManager, origin, destination)
                return
            }
            val currentRoute = routes[0]
            val routeGeometry = currentRoute.geometry()
            if (routeGeometry == null) {
                drawStraightLine(polylineAnnotationManager, origin, destination)
                return
            }
            try {
                val points = LineString.fromPolyline(routeGeometry, Constants.PRECISION_6).coordinates()
                if (points.isEmpty()) {
                    drawStraightLine(polylineAnnotationManager, origin, destination)
                    return
                }
                val polylineOptions = PolylineAnnotationOptions()
                    .withPoints(points)
                    .withLineColor("#000000")
                    .withLineWidth(5.0)
                polylineAnnotationManager.create(polylineOptions)
            } catch (_: Exception) {
                drawStraightLine(polylineAnnotationManager, origin, destination)
            }
        }

        override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
            drawStraightLine(polylineAnnotationManager, origin, destination)
        }
    })
}

private fun drawStraightLine(
    polylineAnnotationManager: com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager,
    origin: Point,
    destination: Point
) {
    val polylineOptions = PolylineAnnotationOptions()
        .withPoints(listOf(origin, destination))
        .withLineColor("#000000")
        .withLineWidth(5.0)
    polylineAnnotationManager.create(polylineOptions)
}

private fun adjustCameraToShowRoute(mapView: MapView, p1: Point, p2: Point) {
    val minLng = minOf(p1.longitude(), p2.longitude())
    val maxLng = maxOf(p1.longitude(), p2.longitude())
    val minLat = minOf(p1.latitude(), p2.latitude())
    val maxLat = maxOf(p1.latitude(), p2.latitude())

    val centerLng = (minLng + maxLng) / 2
    val centerLat = (minLat + maxLat) / 2
    val centerPoint = Point.fromLngLat(centerLng, centerLat)

    val lngDiff = maxLng - minLng
    val latDiff = maxLat - minLat
    val maxDiff = maxOf(lngDiff, latDiff)
    val zoom = when {
        maxDiff > 0.1 -> 11.0
        maxDiff > 0.05 -> 12.0
        maxDiff > 0.02 -> 13.0
        maxDiff > 0.01 -> 14.0
        maxDiff > 0.005 -> 15.0
        else -> 16.0
    }

    val cameraOptions = CameraOptions.Builder().center(centerPoint).zoom(zoom).build()
    mapView.mapboxMap.flyTo(cameraOptions = cameraOptions, animationOptions = MapAnimationOptions.Builder().duration(800L).build())
}

private fun bitmapFromDrawable(context: Context, @DrawableRes resId: Int): Bitmap? {
    return AppCompatResources.getDrawable(context, resId)?.let { drawable ->
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    }
}

private fun Point.distanceTo(other: Point): Double {
    val results = FloatArray(1)
    android.location.Location.distanceBetween(this.latitude(), this.longitude(), other.latitude(), other.longitude(), results)
    return results[0].toDouble()
}

@Composable
fun RideDetailsCard() {
    Column {
        // This composable now used inside TripDetailScreen; its content will be replaced below when we pass trip
        // Left as generic fallback
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Ride details", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TripLocationItem(text: String, isPickup: Boolean, connectorHeight: Dp = 0.dp, topOffset: Dp = 0.dp) {
    val circleSize = 10.dp
    // total height for the canvas: circle + connector (if any)
    val totalHeight = if (connectorHeight > 0.dp) circleSize + connectorHeight else circleSize

    // compute padding for text so its vertical center aligns with circle center
    val radiusDp = circleSize / 2
    val circleCenterDp = if (connectorHeight > 0.dp) radiusDp else totalHeight / 2
    val fontSize = 14.sp
    val density = LocalDensity.current
    val textTopPad = with(density) {
        val fontPx = fontSize.toPx()
        // align text center to circle center: text top pad = circleCenter - (fontHeight / 2)
        val padPx = circleCenterDp.toPx() - (fontPx / 2f)
        padPx.coerceAtLeast(0f).toDp()
    }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = topOffset)) {
        // single Canvas: circle centered vertically, connector (if any) drawn from below the circle to the bottom
        Canvas(modifier = Modifier
            .width(circleSize)
            .height(totalHeight)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val radius = (circleSize.toPx() / 2f).coerceAtMost(size.minDimension / 2f)

            // draw circle centered vertically
            drawCircle(color = if (isPickup) Color.Red else Color.Blue, radius = radius, center = Offset(cx, cy))
            if (isPickup) drawCircle(color = Color.White, radius = radius * 0.4f, center = Offset(cx, cy))

            if (connectorHeight > 0.dp) {
                val gapBetweenCircleAndConnector = 4.dp.toPx()
                val startY = cy + radius + gapBetweenCircleAndConnector
                val endY = size.height
                if (endY > startY) {
                    drawLine(
                        color = Color(0xFFCCCCCC),
                        start = Offset(cx, startY),
                        end = Offset(cx, endY),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, fontSize = fontSize, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}

@Composable
fun ActionButton(
    text: String,
    iconPainter: Painter, // Only Painter
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    contentColor: Color,
    border: BorderStroke? = null
) {
    Button(
        border = border,
        onClick = {},
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor, contentColor = contentColor),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 1.dp),
        modifier = modifier
            .height(34.dp)
            .width(120.dp)
    ) {
        Icon(iconPainter, contentDescription = null, modifier = Modifier
            .size(13.dp)
            .padding(end = 5.dp))
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.W700)
    }
}

@Composable
fun DriverCard(trip: TripItem?) {
    val user = trip?.user
    val name = capitalizeWords(user?.fullName ) ?: "Rider"
    val totalTrips = user?.totalTrips ?: 0
    val phone = user?.phoneNumber ?: ""
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, top = 0.dp, end = 16.dp),
        color = Color(0xffF8F8F8),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFE6E6E6)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "${totalTrips} total trips",
                    fontSize = 14.sp,
                    color = Color.Gray,
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Contact Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ContactIcon(id = R.drawable.message_1, contentDescription = "Chat")
                    Spacer(modifier = Modifier.width(0.dp))
                    Surface(
                        color = Color(0xFFEBEBEB),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .size(40.dp)
                            .noRippleClickable {
                                if (phone.isNotBlank()) {
                                    val intent =
                                        android.content.Intent(android.content.Intent.ACTION_DIAL)
                                            .apply {
                                                data = android.net.Uri.parse("tel:$phone")
                                            }
                                    context.startActivity(intent)
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.phone_1),
                                contentDescription = "Call",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Profile image placeholder or actual image if available
            if (!user?.profileImage.isNullOrBlank()) {
                // For now we still show placeholder; network image loading can be added (Coil)
                ProfileImagePlaceholder(modifier = Modifier.size(60.dp), hasRing = false, initial = name)
            } else {
                ProfileImagePlaceholder(modifier = Modifier.size(60.dp), hasRing = false, initial = name)
            }
        }
    }
}

@Composable
fun ContactIcon(id: Int, contentDescription: String) {
    Surface(
        color = Color(0xFFEBEBEB),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(40.dp),
//        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = id),
                contentDescription = contentDescription,
                modifier = Modifier.size(20.dp)
            )
        }
    }
//    IconButton(
//        onClick = { /* Handle click */ },
//        modifier = Modifier
//            .size(36.dp)
//            .clip(RoundedCornerShape(8.dp))
//            .background(Color(0xFFEEEEEE))
//    ) {
//        Icon(icon, contentDescription = contentDescription, tint = Color.Black, modifier = Modifier.size(20.dp))
//    }
}

@Composable
fun TripRatingCard(trip: TripItem?) {
    val ratingValue = trip?.rating?.value
    if (ratingValue == null || ratingValue == 0.0) return

    val ratingCount = trip?.rating?.count ?: 0
    val comment = trip?.ratingComment

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, top = 0.dp, end = 16.dp),
        color = Color(0xffF8F8F8),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFE6E6E6)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Trip rating",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W700,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                // Numeric rating and count
                Text(
                    text = String.format("%.1f", ratingValue),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W700,
                    color = Color.Black,
                    modifier = Modifier.padding(end = 8.dp)
                )

//                if (ratingCount > 0) {
//                    val countText = if (ratingCount == 1) "$ratingCount rating" else "$ratingCount ratings"
//                    Text(
//                        text = countText,
//                        fontSize = 12.sp,
//                        color = Color.Gray
//                    )
//                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stars - filled for whole part of rating
            Row(modifier = Modifier.fillMaxWidth()) {
                val fullStars = ratingValue.toInt().coerceIn(0, 5)
                val emptyStars = 5 - fullStars
                repeat(fullStars) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "Rated Star",
                        tint = Color(0xFFFFC107), // amber
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 4.dp)
                    )
                }
                repeat(emptyStars) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "Unrated Star",
                        tint = Color.LightGray,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rating comment if present
            if (!comment.isNullOrBlank()) {
                Text(
                    text = comment,
                    fontSize = 14.sp,
                    color = Color.Black,
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFE6E6E6), thickness = 1.dp)
            }



            Spacer(modifier = Modifier.height(16.dp))

            // Get help button (restored)
            OutlinedButton(
                border = BorderStroke(1.dp, Color(0xFFE6E6E6)),
                onClick = { /* Handle help click */ },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black, containerColor = Color(0xFFEEEEEE)),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_help),
                    contentDescription = "Get help",
                    modifier = Modifier
                        .size(18.dp)
                        .padding(end = 4.dp)
                )
                Text("Get help", fontWeight = FontWeight.W700, color = Color.Black)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(trip: TripItem?, onBack: () -> Unit = {}) {
    val riderName = capitalizeWords(trip?.user?.fullName) ?: "Rider"
    val (date, time) = formatDateTime(trip?.createdAt ?: trip?.startTime)
    val pickup = trip?.startAddress?.street ?: trip?.startAddress?.street ?: "Pickup"
    val dropoff = trip?.endAddress?.street ?: trip?.endAddress?.street ?: "Dropoff"
    val priceText = trip?.price?.let { "₦${formatAmountWithComma(it)}" }
        ?: trip?.priceRange?.let { range ->
            if (range.isNotEmpty()) {
                val a = formatAmountWithComma(range[0])
                val b = formatAmountWithComma(range.getOrNull(1) ?: range[0])
                "₦$a - ₦$b"
            } else {
                "₦0"
            }
        } ?: "₦0"

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        topBar = { TripsTopAppBar(onBack = onBack, title = "") }
    ) { paddingValues ->
        Box(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                item { MapSection(trip) }
                item { Spacer(Modifier.height(20.dp)) }

                item {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Column(modifier = Modifier.padding(0.dp).weight(1f)) {
                            Text(
                                text = "Kabu Ride with",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.W600,
                                color = Color.Black
                            )
                            Text(
                                text = riderName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.W600,
                                color = Color.Black
                            )
                            Text(text = "$date, $time", fontSize = 12.sp, color = KabuGray,fontWeight = FontWeight.W500)
                        }
                        ProfileImagePlaceholder(modifier = Modifier.size(60.dp), hasRing = false, initial = riderName)
                    }
                }

                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp, top = 0.dp, end = 16.dp),
                        color = Color(0xffF8F8F8),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFE6E6E6)),
                    ) {
                        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
//                            Spacer(modifier = Modifier.height(8.dp))

                            // Pickup / Dropoff block
                            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                                val circleSize = 10.dp
                                val half = circleSize / 2
                                val gap = 70.dp
                                // connector should span from just below pickup center to just above dropoff center
                                val connectorHeight = gap + half

                                // Pickup marker with connector
                                TripLocationItem(text = pickup, isPickup = true, connectorHeight = connectorHeight, topOffset = 0.dp)

                                TripLocationItem(text = dropoff, isPickup = false, connectorHeight = 0.dp, topOffset = 0.dp)
                            }

                            Row(modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 0.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(text = priceText, fontWeight = FontWeight.W700, fontSize = 14.sp, color = Color.Black)
                                Text(text = " • ${trip?.paymentType ?: ""}", fontSize = 14.sp, color = Color(0xff9A9A9A), modifier = Modifier.padding(start = 4.dp))
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ActionButton(text = "Get receipt", iconPainter = painterResource(id = R.drawable.bk), modifier = Modifier.width(120.dp), backgroundColor = KabukabuYellow, contentColor = Color.Black)
                                ActionButton(text = "Share receipt", iconPainter = painterResource(id = android.R.drawable.ic_menu_share), modifier = Modifier.width(120.dp), backgroundColor = Color(0xFFEEEEEE), contentColor = Color.Black, border = BorderStroke(1.dp, Color(0xFFE6E6E6)))
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(20.dp)) }
                item { DriverCard(trip) }
                item { Spacer(Modifier.height(20.dp)) }
                item { TripRatingCard(trip) }
            }
        }
    }
}

// --- 3. Preview ---

@Preview(showBackground = true)
@Composable
fun PreviewTripDetailScreen() {
    KabukabuDriverTheme {
        TripDetailScreen(trip = null)
    }
}