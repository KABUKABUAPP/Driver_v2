package com.kabukabu.driver.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kabukabu.driver.ui.viewmodels.LoginViewModel
import com.kabukabu.driver.utils.LoginUiState
import android.widget.Toast

@Composable
fun LoginScreen(
    onNavigateToOtp: (String) -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    var email by remember { mutableStateOf(TextFieldValue()) }
    var isError by remember { mutableStateOf(false) }
    val uiState = viewModel.uiState
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        when (uiState) {
            is LoginUiState.Success -> {
                Toast.makeText(context, uiState.response.message, Toast.LENGTH_SHORT).show()
                onNavigateToOtp(email.text)
                viewModel.resetState()
            }
            is LoginUiState.Error -> {
                Toast.makeText(context, uiState.message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to Kabukabu",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    isError = false
                },
                label = { Text("Email") },
                isError = isError,
                supportingText = if (isError) {
                    { Text("Please enter a valid email") }
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    if (viewModel.isValidEmail(email.text)) {
                        viewModel.login(email.text)
                    } else {
                        isError = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Continue")
            }
        }

        if (uiState is LoginUiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
} 