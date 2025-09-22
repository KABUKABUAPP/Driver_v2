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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.kabukabu.driver.ui.viewmodels.AnalyticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(onBack: () -> Unit, vm: AnalyticsViewModel = viewModel()) {
    val ui by vm.uiState.collectAsState()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Analytics", color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.angle_right),
                            contentDescription = "Back",
                            tint = Color(0xFF9A9A9A)
                        )
                    }
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
                ui.isLoading -> Text("Loading…")
                ui.error != null -> Text("Error: ${ui.error}", color = Color(0xFFB00020))
                else -> {
                    val data = ui.data
                    if (data == null) {
                        Text("No data")
                    } else {
                        // Earnings
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Your earnings", fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(8.dp))
                                Text("Current: ${data.yourEarnings?.current ?: 0}")
                                Text("Previous: ${data.yourEarnings?.previous ?: 0}")
                                data.yourEarnings?.comment?.let { Text(it, color = Color(0xFF6A6A6A), fontSize = 12.sp) }
                            }
                        }
                        Spacer(Modifier.height(12.dp))

                        // Quick stats
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            StatTile(title = "Wallet", value = data.walletBalance?.toString() ?: "0")
                            StatTile(title = "Dues", value = data.duePayments?.toString() ?: "0")
                            StatTile(title = "Rating", value = String.format("%.1f", data.averageRating ?: 0.0))
                        }
                        Spacer(Modifier.height(12.dp))

                        // Trips totals
                        data.totalTripsData?.let { totals ->
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Trip totals", fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(8.dp))
                                    Text("Trips: ${totals.totalTrips ?: 0}")
                                    Text("Distance (km): ${totals.totalDistanceInKm ?: 0}")
                                    Text("Time (mins): ${totals.totalTimeInMinutes ?: 0}")
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))

                        // Payment methods
                        data.paymentMethods?.let { pm ->
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Payment methods", fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(8.dp))
                                    Text("Cash: ${pm.cashPayment?.value ?: 0} (${pm.cashPayment?.percentage ?: 0}%)")
                                    Text("Other: ${pm.otherPayment?.value ?: 0} (${pm.otherPayment?.percentage ?: 0}%)")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(title: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5D8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.weight(1f)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color(0xFF6A6A6A), fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            Text(value, color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.W700)
        }
    }
}
