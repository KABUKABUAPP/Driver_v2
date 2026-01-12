package com.kabukabu.driver.features.trips.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.kabukabu.driver.core.theme.KabuGray
import com.kabukabu.driver.core.theme.KabukabuDriverTheme
import com.kabukabu.driver.R
import com.kabukabu.driver.core.utils.noRippleClickable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*

private fun formatMonthYear(iso: String?): String {
    if (iso.isNullOrBlank()) return "Unknown"
    return try {
        val odt = OffsetDateTime.parse(iso)
        odt.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault()))
    } catch (_: Exception) {
        if (iso.length >= 7) iso.substring(0, 7) else iso
    }
}

private fun formatDate(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return try {
        val odt = OffsetDateTime.parse(iso)
        odt.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))
    } catch (_: Exception) {
        ""
    }
}

private fun formatTime(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return try {
        val odt = OffsetDateTime.parse(iso)
        odt.format(DateTimeFormatter.ofPattern("h:mma", Locale.getDefault()))
    } catch (_: Exception) {
        ""
    }
}

// Helper to format integer amounts with commas, e.g. 12345 -> 12,345
private fun formatAmountWithComma(amount: Int?): String {
    return amount?.let { String.format("%,d", it) } ?: "0"
}

// Return list of month-year headers present in items (sorted desc)
private fun groupToSections(items: List<com.kabukabu.driver.features.trips.data.TripItem>): List<String> {
    if (items.isEmpty()) return emptyList()
    val months = items.map { formatMonthYear(it.createdAt ?: it.startTime) }.distinct()
    return months.sortedDescending()
}

@Composable
fun TripActivityIconPlaceholder() {
    Icon(
        painter = painterResource(id = R.drawable.trip_car),
        contentDescription = "Activity Icon",
        modifier = Modifier.size(50.dp),
        tint = Color.Unspecified
    )
}

@Composable
fun TripItemRow(trip: com.kabukabu.driver.features.trips.data.TripItem, onClick: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .noRippleClickable { onClick() }
        .padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TripActivityIconPlaceholder()

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                val title = trip.startAddress?.street ?: "Trip"
                Text(text = title, fontWeight = FontWeight.W600, fontSize = 14.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                val date = formatDate(trip.createdAt ?: trip.startTime)
                val time = formatTime(trip.createdAt ?: trip.startTime)
                Text(text = "${date}, ${time}", fontSize = 12.sp, color = KabuGray)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusColor = if (trip.status.equals("cancelled", true)) Color.DarkGray else Color.Black
                    val amountText =  trip.price?.let { "₦${formatAmountWithComma(it)}" }
                        ?: trip.priceRange?.let { range ->
                            if (range.isNotEmpty()) {
                                val a = formatAmountWithComma(range[0])
                                val b = formatAmountWithComma(range.getOrNull(1) ?: range[0])
                                "₦$a - ₦$b"
                            } else {
                                "₦0"
                            }
                        } ?: "₦0"
                    Text(text = if(trip.status == "cancelled") "cancelled" else amountText, fontWeight = FontWeight.W700, fontSize = 12.sp, color = statusColor)
                    Text(text = " • ", fontSize = 14.sp, color = Color.LightGray)
                    Text(text = trip.paymentType ?: "", fontSize = 12.sp, color = KabuGray)
                }
            }
        }
        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp, modifier = Modifier.padding(start = 66.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsTopAppBar(onBack: () -> Unit = {}, title: String = "Trip activities") {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.weight(1f))
        Box(modifier = Modifier.size(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsScreen(
    onBack: () -> Unit,
    onTripClick: (com.kabukabu.driver.features.trips.data.TripItem) -> Unit,
    viewModel: TripsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Pull-to-refresh state (using material3 pull-to-refresh API as in walletNew)
    val pullToRefreshState = androidx.compose.material3.pulltorefresh.rememberPullToRefreshState()
    var isPullRefreshing by remember { mutableStateOf(false) }

    // Lazy list state for infinite scroll
    val listState = rememberLazyListState()

    // Watch scroll and trigger loadMore when near the end
    LaunchedEffect(listState, uiState.noMore, uiState.isLoadingMore, uiState.isLoading) {
        snapshotFlow {
            val visible = listState.layoutInfo.visibleItemsInfo
            val first = listState.firstVisibleItemIndex
            val visibleCount = visible.size
            val total = listState.layoutInfo.totalItemsCount
            Triple(first, visibleCount, total)
        }
            .map { triple -> Pair(triple.second + triple.first, triple.third) }
            .distinctUntilChanged()
            .filter { (visibleEndIndex, total) -> total > 0 }
            .collectLatest { (visibleEndIndex, total) ->
                // If user scrolled within 3 items of the end, try load more
                if (visibleEndIndex >= total - 3 && !uiState.isLoadingMore && !uiState.noMore && !uiState.isLoading) {
                    viewModel.loadMore()
                }
            }
    }

    // Trigger refresh when user pulls
    if (pullToRefreshState.isRefreshing && !isPullRefreshing) {
        LaunchedEffect(Unit) {
            isPullRefreshing = true
            viewModel.refresh()
        }
    }

    // End refresh when loading finishes
    if (!uiState.isLoading && isPullRefreshing) {
        LaunchedEffect(Unit) {
            isPullRefreshing = false
            pullToRefreshState.endRefresh()
        }
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        topBar = { TripsTopAppBar(onBack = onBack, title = "Trip activities") }
    ) { paddingValues ->
        Box(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            val isInitialLoading = uiState.isLoading && uiState.items.isEmpty()
            val isErrorEmpty = uiState.error != null && uiState.items.isEmpty()

            when {
                isInitialLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                isErrorEmpty -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = uiState.error ?: "Unknown error", color = Color.Red)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.refresh() }) { Text("Retry") }
                        }
                    }
                }
                else -> {
                    val sections = groupToSections(uiState.items)

                    LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                        for (monthYear in sections) {
                            item {
                                Text(
                                    text = monthYear,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                                )
                            }
                            // Use original TripItem objects grouped into sections
                            val itemsInSection = uiState.items.filter { formatMonthYear(it.createdAt ?: it.startTime) == monthYear }
                            items(itemsInSection) { tripItem ->
                                TripItemRow(trip = tripItem, onClick = { onTripClick(tripItem) })
                            }
                        }

                        // Loading more indicator at the end (no button)
                        item {
                            if (uiState.isLoadingMore) {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Preview using mock data
@Preview(showBackground = true)
@Composable
fun PreviewTripsScreen() {
    KabukabuDriverTheme {
        // Simple preview - use mock
        Column(modifier = Modifier.fillMaxSize()) {
            Text(text = "Preview not wired to ViewModel", modifier = Modifier.padding(16.dp))
        }
    }
}