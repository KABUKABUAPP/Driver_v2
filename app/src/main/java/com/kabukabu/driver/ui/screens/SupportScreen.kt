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
import com.kabukabu.driver.ui.viewmodels.SupportViewModel
import androidx.compose.foundation.clickable
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(onBack: () -> Unit, onOpenTicket: (String) -> Unit, onCreateNew: () -> Unit = {}, vm: SupportViewModel = viewModel()) {
    val ui by vm.uiState.collectAsState()
    val selectedTab = remember { mutableIntStateOf(0) }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Support", color = Color.Black) },
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
                    TextButton(onClick = onCreateNew) { Text("New", color = Color(0xFF9A9A9A)) }
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
                    Text("Open", modifier = Modifier.padding(12.dp), color = Color.Black)
                }
                Tab(selected = selectedTab.intValue == 1, onClick = { selectedTab.intValue = 1 }) {
                    Text("Closed", modifier = Modifier.padding(12.dp), color = Color.Black)
                }
            }
            Spacer(Modifier.height(12.dp))

            if (selectedTab.intValue == 0) {
                when {
                    ui.isLoadingOpen && ui.open.isEmpty() -> Text("Loading…")
                    ui.errorOpen != null && ui.open.isEmpty() -> Text("Error: ${ui.errorOpen}", color = Color(0xFFB00020))
                    else -> {
                        LazyColumn {
                            itemsIndexed(ui.open) { index, item ->
                                SupportRow(
                                    subject = item.subject,
                                    lastMessage = item.lastMessage ?: "",
                                    status = item.status ?: "",
                                    createdAt = item.createdAt ?: "",
                                    modifier = Modifier.clickable(enabled = !item.id.isNullOrBlank()) {
                                        onOpenTicket(item.id!!)
                                    }
                                )
                                Spacer(Modifier.height(8.dp))
                                if (index == ui.open.lastIndex && !ui.noMoreOpen && !ui.isLoadingMoreOpen) {
                                    vm.loadMore("open")
                                }
                            }
                            item {
                                if (ui.isLoadingMoreOpen) {
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
                    ui.isLoadingClosed && ui.closed.isEmpty() -> Text("Loading…")
                    ui.errorClosed != null && ui.closed.isEmpty() -> Text("Error: ${ui.errorClosed}", color = Color(0xFFB00020))
                    else -> {
                        LazyColumn {
                            itemsIndexed(ui.closed) { index, item ->
                                SupportRow(
                                    subject = item.subject,
                                    lastMessage = item.lastMessage ?: "",
                                    status = item.status ?: "",
                                    createdAt = item.createdAt ?: "",
                                    modifier = Modifier.clickable(enabled = !item.id.isNullOrBlank()) {
                                        onOpenTicket(item.id!!)
                                    }
                                )
                                Spacer(Modifier.height(8.dp))
                                if (index == ui.closed.lastIndex && !ui.noMoreClosed && !ui.isLoadingMoreClosed) {
                                    vm.loadMore("closed")
                                }
                            }
                            item {
                                if (ui.isLoadingMoreClosed) {
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
private fun SupportRow(subject: String, lastMessage: String, status: String, createdAt: String, modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)), modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(subject, color = Color.Black, fontWeight = FontWeight.W600, fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))
            if (lastMessage.isNotBlank()) {
                Text(lastMessage, color = Color(0xFF6A6A6A), fontSize = 12.sp)
                Spacer(Modifier.height(2.dp))
            }
            Text("Status: $status", color = Color(0xFF9A9A9A), fontSize = 12.sp)
            if (createdAt.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(createdAt, color = Color(0xFF9A9A9A), fontSize = 12.sp)
            }
        }
    }
}
