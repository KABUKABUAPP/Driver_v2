@file:Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UNUSED_VALUE", "UNUSED_VARIABLE")
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.kabukabu.driver.features.analytics.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import com.kabukabu.driver.core.utils.noRippleClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.kabukabu.driver.core.theme.KabukabuYellow
import com.kabukabu.driver.features.analytics.data.PaymentMethods
import com.kabukabu.driver.features.analytics.data.TotalTripsData
import com.kabukabu.driver.features.analytics.data.TripsChart
import com.kabukabu.driver.features.analytics.data.YourEarnings
import com.kabukabu.driver.features.wallet.presentation.StackedBottomSheet
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.res.painterResource
import com.kabukabu.driver.R
import com.kabukabu.driver.features.wallet.presentation.KabuRed
import com.kabukabu.driver.features.wallet.presentation.KabuYellow
import com.kabukabu.driver.features.wallet.presentation.PinModalConfig
import androidx.compose.foundation.horizontalScroll

// Color Palette for AnalyticsScreen (renamed to avoid collisions)
private val analyticsBgGray = Color(0xFFF8F8F8)
private val analyticsChartYellow = KabukabuYellow
private val analyticsChartBlue = Color(0xFF3341ED)
private val analyticsTextGray = Color(0xFF8E8E8E)

// Helper: format numbers with grouping separators (e.g. 1,234,567)
private fun formatNumberWithCommas(value: Number?): String {
    val num = value?.toLong() ?: 0L
    return java.text.NumberFormat.getInstance(java.util.Locale.getDefault()).format(num)
}

@Composable
fun AnalyticsScreen(onBack: () -> Unit, onNavigateToWallet: () -> Unit = {}, vm: AnalyticsViewModel = viewModel()) {
    val ui by vm.uiState.collectAsState()

    // capture context once in composable scope to use inside callbacks
    val context = LocalContext.current

    // Modal / selection state
    var showTimeFilterModal by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(0) } // default: This week
    var customDate by remember { mutableStateOf("") }
    // date range picker state (epoch millis)
    var customRangeStart by remember { mutableStateOf<Long?>(null) }
    var customRangeEnd by remember { mutableStateOf<Long?>(null) }
    var customDateForBackend by remember { mutableStateOf<String?>(null) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    val filterLabels = listOf(
        "This week",
        "Last week",
        "This month",
        "Past 6 Months",
        "This year",
        "Past 2 years",
    )

    // helper to map index to time frame value expected by backend
    fun mapIndexToTimeFrame(index: Int): String {
        return when (index) {
            0 -> "this_week"
            1 -> "7_days"
            2 -> "this_month"
            3 -> "6_months"
            4 -> "12_months"
            5 -> "2_years"
            else -> "this_week"
        }
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        topBar = { AnalyticsHeader(onBack = onBack, selectedLabel = filterLabels[selectedIndex], onTimeFilterClick = { showTimeFilterModal = true }) },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (ui.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (ui.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${ui.error}", color = Color.Red)
                }
            } else if (ui.data == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No analytics data available.")
                }
            } else {
                val data = ui.data!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AnalyticsEarningsCard(data.yourEarnings)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AnalyticsStatCard(
                            "Wallet Balance",
                            "₦${formatNumberWithCommas(data.walletBalance)}",
                            "Go to wallet",
                            Modifier.weight(1f),
                            onClick = { onNavigateToWallet() }
                        )
                        AnalyticsStatCard(
                            "Due Payment",
                            "₦${formatNumberWithCommas(data.duePayments)}",
                            "Go to due payment",
                            Modifier.weight(1f),
                            onClick = { onNavigateToWallet() }
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AnalyticsStatCard("Average ratings", "${data.averageRating}", null, Modifier.weight(1f))
                        AnalyticsStatCard("Driver Score", "${data.averageRating}", null, Modifier.weight(1f)) // Using rating as placeholder for score
                    }

                    // only show trips card if we have trips data
                    data.totalTripsData?.let { totals ->
                        AnalyticsTripsBarChartCard(totals, data.tripsChart ?: emptyList(), selectedIndex)
                    }

                    data.paymentMethods?.let { AnalyticsPaymentMethodsCard(it) }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    // Time filter modal
    if (showTimeFilterModal) {
        StackedBottomSheet(
            heightFraction = 0.65f,
            onDismiss = { showTimeFilterModal = false }, ) {
            Column( modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    // Modal header
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = { showTimeFilterModal = false },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .background(KabuRed.copy(alpha = 0.1f), CircleShape)
                                .size(PinModalConfig.closeButtonSize)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = KabuRed,
                                modifier = Modifier.size(PinModalConfig.closeIconSize)
                            )
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Select filter", fontWeight = FontWeight.W700, fontSize = 18.sp)
                        }

                    }

                    // Options
                    filterLabels.forEachIndexed { idx, label ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .noRippleClickable { selectedIndex = idx },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (idx == selectedIndex),
                                onClick = { selectedIndex = idx })
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(label)
                        }
                    }

                    // Custom date input when selected
                    //                if (selectedIndex == 6) {
                    //                    // Use a read-only text field that opens a Compose-native DateRangePicker when clicked
                    //                    val displayValue = customDate.ifEmpty { "Select date range" }
                    //
                    //                    Box(modifier = Modifier.fillMaxWidth().clickable {
                    //                        showDatePickerDialog = true
                    //                    }) {
                    //                        OutlinedTextField(
                    //                            value = displayValue,
                    //                            onValueChange = {},
                    //                            label = { Text("Select date range") },
                    //                            modifier = Modifier.fillMaxWidth(),
                    //                            readOnly = true
                    //                        )
                    //                    }
                    //
                    //                    // Dialog with Compose DateRangePicker
                    //                    if (showDatePickerDialog) {
                    //                        // initialize state with existing selection if present
                    //                        val datePickerState = rememberDateRangePickerState(
                    //                            initialSelectedStartDateMillis = customRangeStart,
                    //                            initialSelectedEndDateMillis = customRangeEnd
                    //                        )
                    //
                    //                        AlertDialog(
                    //                            onDismissRequest = { showDatePickerDialog = false },
                    //                            confirmButton = {
                    //                                TextButton(onClick = {
                    //                                    val start = datePickerState.selectedStartDateMillis
                    //                                    val end = datePickerState.selectedEndDateMillis
                    //                                    if (start == null || end == null) {
                    //                                        Toast.makeText(context, "Please select a start and end date", Toast.LENGTH_SHORT).show()
                    //                                        return@TextButton
                    //                                    }
                    //                                    if (start > end) {
                    //                                        Toast.makeText(context, "Start date cannot be after end date", Toast.LENGTH_SHORT).show()
                    //                                        return@TextButton
                    //                                    }
                    //
                    //                                    // format for display and backend
                    //                                    val fmt = DateTimeFormatter.ISO_LOCAL_DATE
                    //                                    val startDate = Instant.ofEpochMilli(start).atZone(ZoneId.systemDefault()).toLocalDate().format(fmt)
                    //                                    val endDate = Instant.ofEpochMilli(end).atZone(ZoneId.systemDefault()).toLocalDate().format(fmt)
                    //                                    customRangeStart = start
                    //                                    customRangeEnd = end
                    //                                    customDate = "$startDate to $endDate"
                    //                                    customDateForBackend = "$startDate,$endDate"
                    //                                    showDatePickerDialog = false
                    //                                }) {
                    //                                    Text("OK")
                    //                                }
                    //                            },
                    //                            dismissButton = {
                    //                                TextButton(onClick = { showDatePickerDialog = false }) {
                    //                                    Text("Cancel")
                    //                                }
                    //                            },
                    //                            title = { Text("Select date range") },
                    //                            text = {
                    //                                // DateRangePicker composable
                    //                                Column {
                    //                                    DateRangePicker(state = datePickerState)
                    //                                }
                    //                            }
                    //                        )
                    //                    }
                    //                }

                    Spacer(modifier = Modifier.height(8.dp))


                }
                Button(

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(PinModalConfig.buttonHeight),
                    colors = ButtonDefaults.buttonColors(containerColor = KabuYellow),
                    shape = RoundedCornerShape(PinModalConfig.buttonCornerRadius),
                    onClick = {
                        val tf = mapIndexToTimeFrame(selectedIndex)
                        if (tf == "custom") {
                            // validate we have a picked range
                            if (customRangeStart == null || customRangeEnd == null) {
                                Toast.makeText(
                                    context,
                                    "Please select a valid date range",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                            if (customRangeStart!! > customRangeEnd!!) {
                                Toast.makeText(
                                    context,
                                    "Start date cannot be after end date",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                            vm.fetchAnalysis(timeFrame = tf, customDate = customDateForBackend)
                        } else {
                            vm.fetchAnalysis(timeFrame = tf, customDate = null)
                        }
                        showTimeFilterModal = false
                    }) {
                    Text("Continue")
                }
            }
        }
    }
}

@Composable
private fun AnalyticsHeader(onBack: () -> Unit, selectedLabel: String = "This week", onTimeFilterClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Analytics", fontWeight = FontWeight.W500, fontSize = 18.sp, modifier = Modifier.align(Alignment.Center))
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.noRippleClickable { onTimeFilterClick() }) {
            Text(selectedLabel, color = Color.Black, fontSize = 14.sp)
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = analyticsTextGray, modifier = Modifier.size(18.dp))
        }
    }
}

val bgGrey = Color(0xffF8F8F8)

@Composable
private fun AnalyticsEarningsCard(earnings: YourEarnings? ) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgGrey),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("You earned", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.W700)

            // Safely coerce the "current" amount to an integer for display
            val currentAmount = when (val c = earnings?.current) {
                is Number -> c.toLong()
                is String -> c.toDoubleOrNull()?.toLong() ?: 0L
                else -> 0L
            }

            Text("₦${formatNumberWithCommas(currentAmount)}", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
            Text("This week", color = analyticsTextGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = Color.Green, modifier = Modifier.size(16.dp))
                Text(earnings?.comment ?: "", color = analyticsTextGray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun AnalyticsStatCard(title: String, value: String, linkText: String?, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val cardModifier = if (onClick != null) modifier
        .height(110.dp)
        .noRippleClickable { onClick() } else modifier.height(110.dp)
    Card(
        modifier = cardModifier,
        colors = CardDefaults.cardColors(containerColor = bgGrey),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, color = analyticsTextGray, fontSize = 12.sp)
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            linkText?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(it, color = analyticsTextGray, fontSize = 11.sp)
                    Image(
                        painter = painterResource(id = R.drawable.arrow_up_right),
                        contentDescription = title,
                        modifier = Modifier
                            .size(15.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalyticsTripsBarChartCard(stats: TotalTripsData, chartData: List<TripsChart>, selectedIndex: Int) {
    // decide label mode: day shortform for week-like filters, month shortform for month/longer
    val useMonthLabels = selectedIndex >= 2 // index 2 is "This month" and above

    // shared horizontal scroll state so bars and labels align when scrolling
    val scrollState = rememberScrollState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgGrey),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Trips completed", fontWeight = FontWeight.Bold)
                Text("${stats.totalTrips ?: 0}", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Precompute labels and values with robust date parsing
            val labelsAndValues = chartData.map { entry ->
                val raw = entry.date ?: ""
                val label = run {
                    // Try several parse patterns; backend example: "Wed Jan 29 2025"
                    val patterns = listOf("EEE MMM d yyyy", "EEE MMM dd yyyy", "yyyy-MM-dd")
                    var parsedDate: java.time.LocalDate? = null
                    for (p in patterns) {
                        try {
                            val fmt = java.time.format.DateTimeFormatter.ofPattern(p, java.util.Locale.ENGLISH)
                            parsedDate = java.time.LocalDate.parse(raw, fmt)
                            break
                        } catch (e: java.time.format.DateTimeParseException) {
                            // try next
                        }
                    }

                    if (parsedDate != null) {
                        if (useMonthLabels) {
                            parsedDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM", java.util.Locale.ENGLISH)).uppercase(java.util.Locale.ENGLISH)
                        } else {
                            parsedDate.format(java.time.format.DateTimeFormatter.ofPattern("EEE", java.util.Locale.ENGLISH))
                        }
                    } else {
                        // fallback: pick a token from the raw string
                        val tokens = raw.split(' ').filter { it.isNotBlank() }
                        if (tokens.isEmpty()) "" else {
                            if (useMonthLabels) tokens.getOrNull(1)?.takeIf { it.length >= 3 }?.take(3)?.uppercase(java.util.Locale.ENGLISH) ?: tokens.first().take(3).uppercase(java.util.Locale.ENGLISH)
                            else tokens.first().take(3)
                        }
                    }
                }

                Pair(label ?: "", entry.noOfTrips ?: 0)
            }

            // compute max for scaling
            val maxTrips = labelsAndValues.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1

            // Horizontal scrollable bars
            val barWidth = 25.dp
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
//                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
//                Spacer(modifier = Modifier.width(8.dp))
                labelsAndValues.forEach { (label, trips) ->
                    val fraction = (trips.toFloat() / maxTrips).coerceAtLeast(0.05f)
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(barWidth)
                                .height((120.dp * fraction))
                                .background(
                                    analyticsChartYellow,
                                    RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                                )
                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Text(trips.toString(), fontSize = 12.sp)
                    }
                }
//                Spacer(modifier = Modifier.width(8.dp))
            }

            // Labels aligned to the same scroll state
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
//                Spacer(modifier = Modifier.width(8.dp))
                labelsAndValues.forEach { (label, _) ->
                    Column ( modifier = Modifier.weight(1f),  horizontalAlignment = Alignment.CenterHorizontally){
                        Text(label, fontSize = 12.sp, color = analyticsTextGray)
                    }
                }
//                Spacer(modifier = Modifier.width(8.dp))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = analyticsBgGray)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.route),
                        contentDescription = "",
                        modifier = Modifier
                            .size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    val distanceStr = stats.totalDistanceInKm?.toString() ?: "0"
                    Text("${distanceStr}km", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.clock_alt1),
                        contentDescription = "",
                        modifier = Modifier
                            .size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    val totalTime = stats.totalTimeInMinutes ?: 0
                    val hours = totalTime / 60
                    val mins = totalTime % 60
                    Text("${hours}hr, ${mins}min", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun AnalyticsPaymentMethodsCard(payments: PaymentMethods) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgGrey),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Payment methods", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Donut Chart
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                    Canvas(modifier = Modifier.size(100.dp)) {
                        val stroke = 30f
                        val cashPercent = payments.cashPayment?.percentage ?: 0
                        val otherPercent = payments.otherPayment?.percentage ?: 0
                        val totalPercent = (cashPercent + otherPercent).takeIf { it > 0 } ?: 100

                        val cashAngle = (cashPercent.toFloat() / totalPercent.toFloat()) * 360f
                        val otherAngle = (otherPercent.toFloat() / totalPercent.toFloat()) * 360f

                        drawArc(analyticsChartYellow, -90f, cashAngle, false, style = Stroke(stroke, cap = StrokeCap.Round))
                        drawArc(analyticsChartBlue, -90f + cashAngle, otherAngle, false, style = Stroke(stroke, cap = StrokeCap.Round))
                    }
                }
                Spacer(modifier = Modifier.width(24.dp))
                // Legend
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnalyticsLegendItem(analyticsChartYellow, "Cash payment (${formatNumberWithCommas(payments.cashPayment?.value)})")
                    AnalyticsLegendItem(analyticsChartBlue, "Card / Wallet payment (${formatNumberWithCommas(payments.otherPayment?.value)})")
                }
            }
        }
    }
}

@Composable
private fun AnalyticsLegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier
            .size(12.dp)
            .background(color)
            .clip(RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
