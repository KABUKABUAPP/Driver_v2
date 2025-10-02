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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.kabukabu.driver.ui.viewmodels.PromotionsViewModel
import androidx.compose.material3.TextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionsScreen(onBack: () -> Unit, vm: PromotionsViewModel = viewModel()) {
    val ui by vm.uiState.collectAsState()
    val selectedTab = remember { mutableIntStateOf(0) }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Promotions", color = Color.Black) },
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
            TabRow(selectedTabIndex = selectedTab.intValue) {
                Tab(selected = selectedTab.intValue == 0, onClick = { selectedTab.intValue = 0 }) {
                    Text("Ongoing", modifier = Modifier.padding(12.dp), color = Color.Black)
                }
                Tab(selected = selectedTab.intValue == 1, onClick = { selectedTab.intValue = 1 }) {
                    Text("Completed", modifier = Modifier.padding(12.dp), color = Color.Black)
                }
            }
            Spacer(Modifier.height(12.dp))

            if (selectedTab.intValue == 0) {
                when {
                    ui.isLoadingOngoing && ui.ongoing.isEmpty() -> Text("Loading…")
                    ui.errorOngoing != null && ui.ongoing.isEmpty() -> Text("Error: ${ui.errorOngoing}", color = Color(0xFFB00020))
                    !ui.isLoadingOngoing && ui.ongoing.isEmpty() -> Text("No ongoing promotions")
                    else -> {
                        LazyColumn {
                            itemsIndexed(ui.ongoing) { index, item ->
                                PromoRow(title = item.name, status = item.status ?: "", expiry = item.expiryDate ?: "")
                                Spacer(Modifier.height(8.dp))
                                if (index == ui.ongoing.lastIndex && !ui.noMoreOngoing && !ui.isLoadingMoreOngoing) {
                                    vm.loadMore("ongoing")
                                }
                            }
                            item {
                                if (ui.isLoadingMoreOngoing) {
                                    Spacer(Modifier.height(12.dp))
                                    Text("Loading more…")
                                }
                                Spacer(Modifier.height(24.dp))
                            }
                        }
                    }
                }
            } else {
                when {
                    ui.isLoadingCompleted && ui.completed.isEmpty() -> Text("Loading…")
                    ui.errorCompleted != null && ui.completed.isEmpty() -> Text("Error: ${ui.errorCompleted}", color = Color(0xFFB00020))
                    !ui.isLoadingCompleted && ui.completed.isEmpty() -> Text("No completed promotions")
                    else -> {
                        LazyColumn {
                            itemsIndexed(ui.completed) { index, item ->
                                PromoRow(title = item.name, status = item.status ?: "", expiry = item.expiryDate ?: "")
                                Spacer(Modifier.height(8.dp))
                                if (index == ui.completed.lastIndex && !ui.noMoreCompleted && !ui.isLoadingMoreCompleted) {
                                    vm.loadMore("completed")
                                }
                            }
                            item {
                                if (ui.isLoadingMoreCompleted) {
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
}

@Composable
private fun PromoRow(title: String, status: String, expiry: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))) {
        Column(Modifier.padding(12.dp)) {
            Text(title, color = Color.Black, fontWeight = FontWeight.W600, fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))
            Text("Status: $status", color = Color(0xFF6A6A6A), fontSize = 12.sp)
            if (expiry.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text("Expiry: $expiry", color = Color(0xFF9A9A9A), fontSize = 12.sp)
            }
        }
    }
}
