package com.kabukabu.driver.ui.screens

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.kabukabu.driver.ui.viewmodels.TripsViewModel
import androidx.compose.material3.IconButton
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.TextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTripsScreen(onBack: () -> Unit, vm: TripsViewModel = viewModel()) {
    val ui by vm.uiState.collectAsState()
    val listState = rememberLazyListState()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My trips", color = Color.Black) },
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
                ui.isLoading && ui.items.isEmpty() -> Text("Loading…")
                ui.error != null && ui.items.isEmpty() -> Text("Error: ${ui.error}", color = Color(0xFFB00020))
                !ui.isLoading && ui.items.isEmpty() -> Text("No trips yet")
                else -> {
                    if (ui.error != null) {
                        Text("Error: ${ui.error}", color = Color(0xFFB00020))
                        Spacer(Modifier.height(8.dp))
                    }
                    LazyColumn(state = listState) {
                        itemsIndexed(ui.items) { index, item ->
                            TripHistoryRow(
                                title = "${item.startAddress?.city ?: ""} ➜ ${item.endAddress?.city ?: ""}",
                                subtitle = (item.startTime ?: "").let { it },
                                price = item.price ?: 0,
                                paymentType = item.paymentType ?: "",
                                status = item.status ?: ""
                            )
                            Spacer(Modifier.height(8.dp))
                            if (index == ui.items.lastIndex && !ui.noMore && !ui.isLoadingMore) {
                                vm.loadMore()
                            }
                        }
                        item {
                            if (ui.isLoadingMore) {
                                Spacer(Modifier.height(12.dp))
                                Text("Loading more…")
                            }
                            Spacer(Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TripHistoryRow(
    title: String,
    subtitle: String,
    price: Int,
    paymentType: String,
    status: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = Color.Black, fontWeight = FontWeight.W600, fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(subtitle, color = Color(0xFF6A6A6A), fontSize = 12.sp)
                Text("₦${formatAmount(price)}", color = Color.Black, fontWeight = FontWeight.W700)
            }
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(paymentType, color = Color(0xFF9A9A9A), fontSize = 12.sp)
                Text(status, color = Color(0xFF9A9A9A), fontSize = 12.sp)
            }
        }
    }
}

private fun formatAmount(value: Int): String = try {
    java.text.NumberFormat.getNumberInstance(java.util.Locale.getDefault()).format(value)
} catch (e: Exception) { value.toString() }
