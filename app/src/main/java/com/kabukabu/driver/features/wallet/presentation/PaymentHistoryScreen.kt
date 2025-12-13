package com.kabukabu.driver.features.wallet.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kabukabu.driver.R
import com.kabukabu.driver.features.wallet.data.TransactionRow
import java.text.SimpleDateFormat
import java.util.*

// Helper function to group transactions by month
fun groupTransactionsByMonth(transactions: List<TransactionRow>): Map<String, List<TransactionRow>> {
    return transactions.groupBy { transaction ->
        transaction.createdAt?.let {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(it)
                val outputFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                "Unknown"
            }
        } ?: "Unknown"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(
    onBack: () -> Unit,
    viewModel: PaymentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Group transactions by month
    val groupedTransactions = remember(uiState.transactions) {
        groupTransactionsByMonth(uiState.transactions)
    }


    // Detect when user scrolls to bottom for pagination
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem >= totalItems - 3 && !uiState.isLoadingMore && !uiState.noMore
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadMore()
        }
    }

    Scaffold(
        topBar = {
            // Custom top bar with back arrow, centered title, and calendar icon
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
//                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Payment History",
                    fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.weight(1f))
//                Icon(
//                    imageVector = Icons.Default.DateRange,
//                    contentDescription = "Calendar",
//                    modifier = Modifier.size(24.dp),
//                    tint = Color.Black
//                )
                Spacer(modifier = Modifier.width(16.dp))
            }
        },
        containerColor = Color.White,
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                // Loading State (first load)
                uiState.isLoading && uiState.transactions.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFF4C446))
                    }
                }

                // Error State
                uiState.error != null && uiState.transactions.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Error",
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Failed to load",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = uiState.error ?: "Unknown error",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF4C446)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Empty State
                uiState.transactions.isEmpty() && !uiState.isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Empty",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No transactions yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Your payment history will appear here",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                // Content State (with data)
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Iterate through grouped transactions by month
                        groupedTransactions.forEach { (month, transactions) ->
                            // Month Header
                            item {
                                Text(
                                    text = month,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }

                            // Transactions for this month
                            items(transactions) { transaction ->
                                TransactionItem(transaction = transaction)
                            }
                        }

                        // Loading more indicator
                        if (uiState.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color(0xFFF4C446)
                                    )
                                }
                            }
                        }

                        // No more items indicator
                        if (uiState.noMore && uiState.transactions.isNotEmpty()) {
                            item {
                                Text(
                                    text = "No more transactions",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .wrapContentWidth(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: TransactionRow) {
    val amount = when (val amt = transaction.amount) {
        is Number -> amt.toDouble()
        is String -> amt.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }

    val isDebit = transaction.type?.lowercase() == "dr"
    val amountColor = if (isDebit) Color(0xFFE53935) else Color(0xFF4CAF50)

    // Format date
    val formattedDate = transaction.createdAt?.let {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(it)
            val outputFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            it
        }
    } ?: "Unknown date"

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon (Debit/Credit indicator in Circle)
            val iconDrawable = if (isDebit) {
                R.drawable.debit
            } else {
                R.drawable.credit
            }

            Icon(
                painter = painterResource(id = iconDrawable),
                contentDescription = transaction.type,
                tint = amountColor,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Transaction Details (Type and Date)
            Column(modifier = Modifier.weight(1f)) {
                // Format narration: remove hyphens and convert to title case
                val formattedNarration = transaction.narration?.let {
                    it.replace("_", " ")
                      .split(" ")
                      .joinToString(" ") { word ->
                          word.lowercase().replaceFirstChar { char -> char.uppercase() }
                      }
                } ?: "Transaction"

                Text(
                    text = formattedNarration,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W700,
                    color = Color.Black
                )
                Text(
                    text = formattedDate,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W400,
                    color = Color(0xFF9A9A9A)
                )
            }

            // Amount
            Text(
                text = "₦${String.format("%,.0f", amount)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.W700,
                color = amountColor,
            )
        }
    }
}

