package com.kabukabu.driver.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kabukabu.driver.R
import com.kabukabu.driver.ui.viewmodels.SupportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportNewTicketScreen(
    onBack: () -> Unit,
    onCreated: () -> Unit,
    vm: SupportViewModel = viewModel()
) {
    val ui by vm.uiState.collectAsState()
    val subject = remember { mutableStateOf("") }
    val message = remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("New Support Ticket", color = Color.Black) },
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
                    if (ui.isCreating) {
                        TextButton(onClick = { }) { Text("Creating…", color = Color(0xFF9A9A9A)) }
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
            OutlinedTextField(
                value = subject.value,
                onValueChange = { subject.value = it },
                label = { Text("Subject") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 0.dp)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = message.value,
                onValueChange = { message.value = it },
                label = { Text("Message") },
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    vm.openNewTicket(subject.value.trim(), message.value.trim())
                },
                enabled = subject.value.isNotBlank() && message.value.isNotBlank() && !ui.isCreating
            ) {
                Text("Create Ticket")
            }

            if (ui.createError != null) {
                Spacer(Modifier.height(8.dp))
                Text("Error: ${ui.createError}", color = Color(0xFFB00020))
            }
            if (ui.createSuccess == true) {
                onCreated()
            }
        }
    }
}
