package com.kabukabu.driver.features.trips.presentation

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.kabukabu.driver.R
import com.kabukabu.driver.core.theme.KabuGray
import com.kabukabu.driver.core.theme.KabukabuYellow
import com.kabukabu.driver.features.trips.data.TripItem
import dev.shreyaspatil.capturable.Capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

// Enum for capture actions
private enum class CaptureAction {
    SHARE, DOWNLOAD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripReceiptScreen(
    trip: TripItem?,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val captureController = rememberCaptureController()
    val coroutineScope = rememberCoroutineScope()
    var isCapturing by remember { mutableStateOf(false) }
    var captureAction by remember { mutableStateOf(CaptureAction.SHARE) }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        topBar = {
            TopAppBar(
                title = { Text("Receipt", fontSize = 18.sp, fontWeight = FontWeight.W600) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 15.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Scrollable receipt content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                    ) {
                        Capturable(
                            controller = captureController,
                            onCaptured = { imageBitmap, _ ->
                                if (imageBitmap != null && !isCapturing) {
                                    isCapturing = true
                                    coroutineScope.launch {
                                        when (captureAction) {
                                            CaptureAction.SHARE -> shareReceipt(context, imageBitmap.asAndroidBitmap())
                                            CaptureAction.DOWNLOAD -> downloadReceipt(context, imageBitmap.asAndroidBitmap())
                                        }
                                        isCapturing = false
                                    }
                                }
                            }
                        ) {
                            ReceiptContent(trip = trip)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Download button
                    Button(
                        onClick = {
                            if (!isCapturing) {
                                captureAction = CaptureAction.DOWNLOAD
                                captureController.capture()
                            }
                        },
                        enabled = !isCapturing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "Download",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Download",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W600
                        )
                    }

                    // Share button
                    Button(
                        onClick = {
                            if (!isCapturing) {
                                captureAction = CaptureAction.SHARE
                                captureController.capture()
                            }
                        },
                        enabled = !isCapturing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KabukabuYellow,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Share",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W600
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun ReceiptContent(trip: TripItem?) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        color = Color.White,
        shape = RoundedCornerShape(15.dp),
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, Color(0xFFE6E6E6))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // App name
            Text(
                text = "Kabukabu",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle
            Text(
                text = "Your trip details",
                fontSize = 13.sp,
                fontWeight = FontWeight.W500,
                color = KabuGray
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Route section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Icon column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    // Pickup icon
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE6E6E6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.Black)
                        )
                    }

                    // Dotted line
                    DottedVerticalLine(height = 70.dp)

                    // Destination icon
                    Icon(
                        painter = painterResource(id = R.drawable.destination),
                        contentDescription = "Destination",
                        modifier = Modifier.size(12.dp),
                        tint = Color.Blue
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Address column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 5.dp)
                ) {
                    // Pickup address
                    Text(
                        text = trip?.startAddress?.street ?: "Pickup Location",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.W600,
                        color = Color.Black,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    HorizontalDivider(color = Color(0xFFE6E6E6), thickness = 1.dp)

                    Spacer(modifier = Modifier.height(15.dp))

                    // Dropoff address
                    Column {
                        Text(
                            text = trip?.endAddress?.street ?: "Dropoff Location",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.W600,
                            color = Color.Black,
                            lineHeight = 22.sp
                        )
                        Text(
                            text = trip?.endAddress?.state ?: "",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.W400,
                            color = KabuGray
                        )
                    }
                }
            }

            HorizontalDivider(
                color = Color(0xFFE6E6E6),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Date and payment type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTripDate(trip?.createdAt ?: trip?.startTime),
                    fontSize = 10.sp,
                    color = KabuGray
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.wallet),
                        contentDescription = "Payment",
                        modifier = Modifier.size(15.dp),
                        tint = KabuGray
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = trip?.paymentType ?: "Cash",
                        fontSize = 13.sp,
                        color = KabuGray
                    )
                }
            }

            HorizontalDivider(
                color = Color(0xFFE6E6E6),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // You Earn
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "You Earn",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.W600,
                    color = KabuGray
                )
                Text(
                    text = "₦${formatAmount(trip?.priceDetails?.driverEarned?.toInt() ?: 0)}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.W600,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Actual Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Actual Price",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.W600,
                    color = KabuGray
                )
                Text(
                    text = "₦${formatAmount(trip?.priceDetails?.totalCharge?.toInt() ?: 0)}",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Booking Fee
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Booking Fee",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.W600,
                    color = KabuGray
                )
                Text(
                    text = "₦${formatAmount(trip?.priceDetails?.bookingFee?.toInt() ?: 0)}",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Rider VAT
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Rider VAT",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.W600,
                    color = KabuGray
                )
                Text(
                    text = "₦${formatAmount(trip?.priceDetails?.stateLevy?.toInt() ?: 0)}",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }

            HorizontalDivider(
                color = Color(0xFFE6E6E6),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Rider info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile image placeholder
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (trip?.user?.fullName?.trim()?.firstOrNull()?.uppercaseChar()?.toString() ?: "R"),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.width(15.dp))

                // Rider details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = trip?.user?.fullName?.split(" ")?.joinToString(" ") {
                            it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase() else char.toString() }
                        } ?: "Rider",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W600,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Rating stars
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val rating = trip?.user?.averageRating ?: 0.0
                        repeat(5) { index ->
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (index < rating.toInt()) Color(0xFFFFD700) else Color.LightGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${trip?.user?.totalTrips ?: 0} Total Trips",
                        fontSize = 13.sp,
                        color = KabuGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun DottedVerticalLine(height: Dp) {
    Canvas(
        modifier = Modifier
            .width(2.dp)
            .height(height)
    ) {
        val dashHeight = 4.dp.toPx()
        val gapHeight = 4.dp.toPx()
        var currentY = 0f

        while (currentY < size.height) {
            drawLine(
                color = Color(0xFFE6E6E6),
                start = androidx.compose.ui.geometry.Offset(size.width / 2, currentY),
                end = androidx.compose.ui.geometry.Offset(size.width / 2, (currentY + dashHeight).coerceAtMost(size.height)),
                strokeWidth = 2.dp.toPx()
            )
            currentY += dashHeight + gapHeight
        }
    }
}

private fun formatTripDate(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        if (date != null) outputFormat.format(date) else dateString
    } catch (_: Exception) {
        try {
            // Try alternative format
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            if (date != null) outputFormat.format(date) else dateString
        } catch (_: Exception) {
            dateString
        }
    }
}

private fun shareReceipt(context: Context, bitmap: Bitmap) {
    try {
        // Save bitmap to cache directory
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "kabukabu_receipt_${System.currentTimeMillis()}.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        // Get URI for the file
        val contentUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        // Create share intent
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, contentUri)
            type = "image/png"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share Receipt"))
    } catch (_: Exception) {
        // Handle error silently
    }
}

private fun downloadReceipt(context: Context, bitmap: Bitmap) {
    try {
        val filename = "Kabukabu_Receipt_${System.currentTimeMillis()}.png"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 and above - use MediaStore
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Kabukabu")
            }

            val resolver = context.contentResolver
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            imageUri?.let { uri ->
                resolver.openOutputStream(uri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
                Toast.makeText(context, "Receipt saved to gallery", Toast.LENGTH_SHORT).show()
            }
        } else {
            // For Android 9 and below
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val kabukabuDir = File(imagesDir, "Kabukabu")
            if (!kabukabuDir.exists()) {
                kabukabuDir.mkdirs()
            }

            val file = File(kabukabuDir, filename)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            // Notify the media scanner
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            intent.data = Uri.fromFile(file)
            context.sendBroadcast(intent)

            Toast.makeText(context, "Receipt saved to gallery", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to save receipt", Toast.LENGTH_SHORT).show()
    }
}

private fun formatAmount(amount: Int): String {
    return String.format(Locale.getDefault(), "%,d", amount)
}

