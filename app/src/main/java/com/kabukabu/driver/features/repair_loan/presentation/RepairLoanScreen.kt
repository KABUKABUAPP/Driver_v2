package com.kabukabu.driver.features.repair_loan.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kabukabu.driver.R
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.kabukabu.driver.features.repair_loan.presentation.RepairLoanViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepairLoanScreen(onBack: () -> Unit, vm: RepairLoanViewModel = viewModel()) {
    val ui by vm.uiState.collectAsState()
    val amount = remember { mutableStateOf("") }
    val reason = remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Repair Loan", color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.angle_right),
                            contentDescription = "Back",
                            tint = Color(0xFF9A9A9A)
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { vm.refresh() }) { Text("Refresh", color = Color(0xFF9A9A9A)) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                ui.isLoading && ui.loans.isEmpty() -> Text("Loading…")
                ui.error != null && ui.loans.isEmpty() -> Text("Error: ${ui.error}", color = Color(0xFFB00020))
                else -> {
                    LazyColumn {
                        itemsIndexed(ui.loans) { index, item ->
                            LoanRow(
                                amount = item.amount,
                                status = item.status ?: "",
                                reason = item.reason ?: "",
                                createdAt = item.createdAt ?: ""
                            )
                            Spacer(Modifier.height(8.dp))
                            if (index == ui.loans.lastIndex && !ui.noMore && !ui.isLoadingMore) {
                                vm.loadMore()
                            }
                        }
                        item {
                            if (ui.isLoadingMore) {
                                Spacer(Modifier.height(12.dp))
                                Text("Loading more…")
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))) {
                Column(Modifier.padding(12.dp)) {
                    Text("Request new loan", color = Color.Black, fontWeight = FontWeight.W600, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amount.value,
                        onValueChange = { amount.value = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Amount") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reason.value,
                        onValueChange = { reason.value = it },
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                val amt = amount.value.toIntOrNull() ?: 0
                                vm.requestLoan(amt, reason.value.trim())
                            },
                            enabled = !ui.isRequesting && amount.value.isNotBlank() && reason.value.isNotBlank()
                        ) { Text(if (ui.isRequesting) "Submitting…" else "Submit") }
                    }
                    if (ui.requestError != null) {
                        Spacer(Modifier.height(6.dp))
                        Text("Error: ${ui.requestError}", color = Color(0xFFB00020))
                    }
                }
            }
        }
    }
}

@Composable
private fun LoanRow(amount: Int, status: String, reason: String, createdAt: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))) {
        Column(Modifier.padding(12.dp)) {
            Text("₦${formatAmount(amount)}", color = Color.Black, fontWeight = FontWeight.W700)
            Spacer(Modifier.height(4.dp))
            Text("Status: $status", color = Color(0xFF6A6A6A), fontSize = 12.sp)
            if (reason.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(reason, color = Color(0xFF9A9A9A), fontSize = 12.sp)
            }
            if (createdAt.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(createdAt, color = Color(0xFF9A9A9A), fontSize = 12.sp)
            }
        }
    }
}

private fun formatAmount(value: Int): String = try {
    java.text.NumberFormat.getNumberInstance(java.util.Locale.getDefault()).format(value)
} catch (e: Exception) { value.toString() }
