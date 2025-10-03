package com.kabukabu.driver.features.support.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kabukabu.driver.R
import com.kabukabu.driver.features.support.presentation.SupportDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportDetailScreen(
    supportId: String,
    onBack: () -> Unit,
    vm: SupportDetailViewModel = viewModel()
) {
    val ui by vm.uiState.collectAsState()

    LaunchedEffect(supportId) {
        vm.load(supportId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(ui.subject, color = Color.Black) },
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
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = ui.draft,
                    onValueChange = vm::updateDraft,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message") },
                    singleLine = true
                )
                Button(onClick = { vm.sendReply(supportId, ui.draft) }, enabled = !ui.isSending && ui.draft.isNotBlank()) {
                    Text(if (ui.isSending) "Sending…" else "Send")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                ui.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Loading…") }
                ui.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Error: ${ui.error}", color = Color(0xFFB00020)) }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ui.messages) { msg ->
                            val isMine = (msg.sender?.lowercase()?.contains("driver") == true)
                            MessageBubble(
                                message = msg.message,
                                timestamp = msg.createdAt ?: "",
                                isMine = isMine
                            )
                        }
                        item { Spacer(Modifier.height(60.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: String, timestamp: String, isMine: Boolean) {
    val bg = if (isMine) Color(0xFFFFF5D8) else Color(0xFFF9F9F9)
    val align = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = align) {
            Card(colors = CardDefaults.cardColors(containerColor = bg)) {
                Column(Modifier.padding(12.dp)) {
                    Text(message, color = Color.Black, fontSize = 14.sp)
                    if (timestamp.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(timestamp, color = Color(0xFF9A9A9A), fontSize = 10.sp, fontWeight = FontWeight.W400)
                    }
                }
            }
        }
    }
}
