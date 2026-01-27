package com.kabukabu.driver.features.support.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kabukabu.driver.R
import com.kabukabu.driver.core.theme.KabuGray
import com.kabukabu.driver.core.theme.KabukabuYellow
import com.kabukabu.driver.core.theme.KabukabuDriverTheme
import com.kabukabu.driver.core.utils.noRippleClickable
import com.kabukabu.driver.features.support.viewmodel.SupportSharedViewModel
import com.kabukabu.driver.features.support.viewmodel.SupportViewModel
import com.kabukabu.driver.features.wallet.presentation.StackedBottomSheet
import com.kabukabu.driver.features.trips.data.TripItem
import com.kabukabu.driver.features.trips.presentation.TripsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// Helper functions to format dates and amounts
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

private fun formatAmountWithComma(amount: Int?): String {
    return amount?.let { String.format("%,d", it) } ?: "0"
}

private fun groupToSections(items: List<TripItem>): List<String> {
    if (items.isEmpty()) return emptyList()
    val months = items.map { formatMonthYear(it.createdAt ?: it.startTime) }.distinct()
    return months.sortedDescending()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectSupportTripScreen(
    onBack: () -> Unit,
    onNavigateToDetail: (ticketId: String) -> Unit,
    viewModel: TripsViewModel = viewModel(),
    supportViewModel: SupportViewModel = viewModel(),
    supportSharedViewModel: SupportSharedViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val supportUiState by supportViewModel.uiState.collectAsState()

    // Modal states
    var showSelectSubjectModal by remember { mutableStateOf(false) }
    var selectedTrip by remember { mutableStateOf<TripItem?>(null) }

    // Handle successful ticket creation and navigation
    LaunchedEffect(supportUiState.newlyCreatedTicket) {
        val newTicket = supportUiState.newlyCreatedTicket
        if (newTicket != null && supportUiState.createSuccess == true) {
            // Close modal and navigate to detail screen
            showSelectSubjectModal = false
            selectedTrip = null
            supportViewModel.clearNewlyCreatedTicket()
            // set the newly created ticket in the shared viewmodel so Detail screen can access it
            supportSharedViewModel.selectTicket(newTicket)
            newTicket.id?.let { ticketId ->
                onNavigateToDetail(ticketId)
            }
        }
    }

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
            .filter { (_, total) -> total > 0 }
            .collectLatest { (visibleEndIndex, total) ->
                // If user scrolled within 3 items of the end, try load more
                if (visibleEndIndex >= total - 3 && !uiState.isLoadingMore && !uiState.noMore && !uiState.isLoading) {
                    viewModel.loadMore()
                }
            }
    }

    Scaffold(
        topBar = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text("New Support", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.size(24.dp)) // Placeholder for alignment
                }
                Text(
                    text = "Select a trip you need support for",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        },
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp),
                placeholder = {
                    Text("Search for a ticket", color = KabuGray)
                },
//                trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF8F8F8),
                    focusedContainerColor = Color(0xFFF8F8F8),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Content area with loading and error states
            val isInitialLoading = uiState.isLoading && uiState.items.isEmpty()
            val isErrorEmpty = uiState.error != null && uiState.items.isEmpty()

            // Filter trips based on search query
            val filteredTrips = if (searchQuery.isBlank()) {
                uiState.items
            } else {
                uiState.items.filter { trip ->
                    val dateStr = formatDate(trip.createdAt ?: trip.startTime)
                    val destination = trip.endAddress?.street ?: trip.endAddress?.fullAddress ?: ""
                    val origin = trip.startAddress?.street ?: trip.startAddress?.fullAddress ?: ""

                    dateStr.contains(searchQuery, ignoreCase = true) ||
                    destination.contains(searchQuery, ignoreCase = true) ||
                    origin.contains(searchQuery, ignoreCase = true)
                }
            }

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
                    if (filteredTrips.isEmpty() && searchQuery.isNotBlank()) {
                        // Empty search results
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "No trips found", fontSize = 16.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Try a different search query", fontSize = 14.sp, color = Color.LightGray)
                            }
                        }
                    } else if (uiState.items.isEmpty()) {
                        // No trips at all
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "No trips yet", fontSize = 16.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Your trips will appear here", fontSize = 14.sp, color = Color.LightGray)
                            }
                        }
                    } else {
                        val sections = groupToSections(filteredTrips)

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState
                    ) {
                        for (monthYear in sections) {
                            item {
                                Text(
                                    text = monthYear,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
                                )
                            }
                            // Use original TripItem objects grouped into sections
                            val itemsInSection = filteredTrips.filter {
                                formatMonthYear(it.createdAt ?: it.startTime) == monthYear
                            }
                            items(itemsInSection) { tripItem ->
                                SupportTripItemRow(
                                    trip = tripItem,
                                    onTripSelected = {
                                        selectedTrip = tripItem
                                        showSelectSubjectModal = true
                                    }
                                )
                            }
                        }

                        // Loading more indicator at the end
                        item {
                            if (uiState.isLoadingMore) {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                                }
                            }
                        }
                    }
                    }
                }
            }
        }
    }

    // Show SelectSubjectModal when a trip is selected
    if (showSelectSubjectModal && selectedTrip != null) {
        SelectSubjectModal(
            onDismiss = {
                showSelectSubjectModal = false
                selectedTrip = null
            },
            onSubmit = { subject  ->
                selectedTrip?.id?.let { tripId ->
                    // store selected trip in shared viewmodel so detail screen and others can access it
                    supportSharedViewModel.selectTrip(selectedTrip)
                    supportViewModel.openNewTicketByTrip(
                        subject = subject,
                        message = "abc",
                        tripId = tripId
                    )
                }
            },
            isLoading = supportUiState.isCreating,
            error = supportUiState.createError
        )
    }
}


@Composable
fun SupportTripItemRow(trip: TripItem, onTripSelected: (TripItem) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .noRippleClickable { onTripSelected(trip) }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.trip_car),
            contentDescription = "Activity Icon",
            modifier = Modifier.size(50.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            val destination = trip.endAddress?.street ?: trip.endAddress?.fullAddress ?: "Trip"
            Text(text = destination, fontWeight = FontWeight.W600, fontSize = 14.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            val date = formatDate(trip.createdAt ?: trip.startTime)
            val time = formatTime(trip.createdAt ?: trip.startTime)
            Text(text = "$date, $time", fontSize = 12.sp, color = KabuGray)
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val isCancelled = trip.status.equals("cancelled", true)
                val amountColor = if (isCancelled) Color.Gray else Color.Black
                val amountText = if (isCancelled) {
                    "₦0"
                } else {
                    trip.price?.let { "₦${formatAmountWithComma(it)}" }
                        ?: trip.priceRange?.let { range ->
                            if (range.isNotEmpty()) {
                                val a = formatAmountWithComma(range[0])
                                val b = formatAmountWithComma(range.getOrNull(1) ?: range[0])
                                "₦$a - ₦$b"
                            } else {
                                "₦0"
                            }
                        } ?: "₦0"
                }
                Text(text = amountText, fontWeight = FontWeight.W700, fontSize = 12.sp, color = amountColor)
                Text(text = " • ", fontSize = 14.sp, color = Color.LightGray)
                val paymentText = if (isCancelled) "cancelled" else (trip.paymentType ?: "")
                Text(text = paymentText, fontSize = 12.sp, color = KabuGray)
            }
        }
        Button(
            onClick = { onTripSelected(trip) },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF2F2F2), 
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Select", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SelectSubjectModal(
    onDismiss: () -> Unit,
    onSubmit: (subject: String) -> Unit,
    isLoading: Boolean = false,
    error: String? = null
) {
    val subjects = listOf(
        "I lost my phone",
        "I forgot my bag",
        "I took the rider's charger",
        "I transferred to the driver twice",
        "Rider stole from me",
        "Rider called to harass me",
        "Rider keeps calling me",
        "Other"
    )
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    var otherSubjectText by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }

    StackedBottomSheet(
        onDismiss = onDismiss,
        heightFraction = 0.8f
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Select Subject",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "This will give clarity on the support type",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Subject selection
            subjects.forEach { subject ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .previewSafeClickable { selectedSubject = subject }
                        .padding(vertical = 10.dp)
                ) {
                    CustomRadioButton(
                        selected = selectedSubject == subject,
                        onClick = { selectedSubject = subject }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(subject, fontSize = 16.sp)
                }
            }

            // Show text field if "Other" is selected
            if (selectedSubject == "Other") {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Subject",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = otherSubjectText,
                    onValueChange = { otherSubjectText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("Tell us what happened", color = Color.Gray) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KabukabuYellow,
                        unfocusedBorderColor = KabukabuYellow
                    )
                )
            }

            // Message field
            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                "Message",
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.padding(bottom = 8.dp)
//            )
//            OutlinedTextField(
//                value = messageText,
//                onValueChange = { messageText = it },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(120.dp),
//                placeholder = { Text("Describe the issue", color = Color.Gray) },
//                shape = RoundedCornerShape(12.dp),
//                colors = OutlinedTextFieldDefaults.colors(
//                    focusedBorderColor = KabukabuYellow,
//                    unfocusedBorderColor = KabukabuYellow
//                )
//            )

            // Error message
            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }

            // Submit button
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val finalSubject = if (selectedSubject == "Other") otherSubjectText else selectedSubject ?: ""
                    if (finalSubject.isNotBlank()) {
                        onSubmit(finalSubject)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KabukabuYellow),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading && ((selectedSubject == "Other" && otherSubjectText.isNotBlank()) ||
                         (selectedSubject != null && selectedSubject != "Other"))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black
                    )
                } else {
                    Text(
                        "Submit",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SelectSupportTripScreenPreview() {
    KabukabuDriverTheme {
        SelectSupportTripScreen(onBack = {}, onNavigateToDetail = {})
    }
}
