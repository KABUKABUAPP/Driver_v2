package com.kabukabu.driver.features.wallet.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
//import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Colors (Reusing the theme) ---
val KabuGreen = Color(0xFF4CAF50) // Standard green for positive transactions
val KabuTextGrey = Color(0xFF888888)
val KabuCardBg = Color(0xFFFAFAFA) // Light grey background for transaction cards

// --- Data Model ---
data class Transaction(
    val type: String,
    val date: String,
    val time: String,
    val amount: String,
    val isDebit: Boolean // true for red/up arrow (money out), false for green/down arrow (money in)
)

// Dummy Data replicating the screenshot structure
val dummyHistoryData = mapOf(
    "November 2024" to listOf(
        Transaction("Top Up", "Nov 1, 2023", "2:30pm", "1,300", isDebit = false),
        Transaction("Trip Payment", "Nov 1, 2023", "2:30pm", "1,300", isDebit = true),
        Transaction("Top Up", "Nov 1, 2023", "2:30pm", "1,300", isDebit = false),
        Transaction("Top Up", "Nov 1, 2023", "2:30pm", "1,300", isDebit = false),
        Transaction("Trip Payment", "Nov 1, 2023", "2:30pm", "1,300", isDebit = true),
        Transaction("Trip Payment", "Nov 1, 2023", "2:30pm", "1,300", isDebit = true),
    ),
    "October 2024" to listOf(
        Transaction("Top Up", "Oct 1, 2023", "2:30pm", "1,300", isDebit = false),
    ),
    "September 2024" to listOf(
        // Placeholder to show the header
    )
)

@Composable
fun PaymentHistoryScreen2(onBack: () -> Unit) {
    Scaffold(
        topBar = { PaymentHistoryTopBar() },
        containerColor = Color.White,
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
    ) { paddingValues ->
        // LazyColumn is efficient for long scrolling lists
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Iterate through the grouped transactions (e.g., November, October)
            dummyHistoryData.forEach { (month, transactions) ->
                item {
                    // Month Header
                    Text(
                        text = month,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (transactions.isNotEmpty()) {
                    // Iterate through transactions within the month
                    items(transactions.size) { index ->
                        TransactionItem(transaction = transactions[index])
                    }
                }
            }
        }
    }
}

// --- Top Bar Component ---

@Composable
fun PaymentHistoryTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Payment History",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = "Calendar",
            modifier = Modifier.size(24.dp)
        )
    }
}

// --- Transaction Item Component ---

@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = KabuCardBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon (Debit/Credit indicator in Circle)
            val iconColor = if (transaction.isDebit) KabuRed else KabuGreen
            // Use ArrowDropUp for debit (money out/up), ArrowDropDown for credit (money in/down)
            val iconArrow = if (transaction.isDebit) Icons.Default.ArrowDropDown else Icons.Default.ArrowDropDown

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconArrow,
                    contentDescription = transaction.type,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Transaction Details (Type and Date)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.type,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = "${transaction.date} at ${transaction.time}",
                    fontSize = 12.sp,
                    color = KabuTextGrey
                )
            }

            // Amount
            Text(
                text = "₦${transaction.amount}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (transaction.isDebit) KabuRed else KabuGreen
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentHistoryScreen2Preview() {
    MaterialTheme {
        PaymentHistoryScreen2(onBack = {})

    }
}