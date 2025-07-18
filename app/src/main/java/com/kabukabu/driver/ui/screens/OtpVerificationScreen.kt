package com.kabukabu.driver.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kabukabu.driver.ui.viewmodels.OtpViewModel
import com.kabukabu.driver.utils.OtpUiState
import android.widget.Toast
import kotlinx.coroutines.delay

@Composable
fun OtpVerificationScreen(
    email: String,
    onNavigateToHome: () -> Unit,
    viewModel: OtpViewModel = viewModel()
) {
    var otp by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val uiState = viewModel.uiState
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        when (uiState) {
            is OtpUiState.Success -> {
                Toast.makeText(context, uiState.response.message, Toast.LENGTH_SHORT).show()
                onNavigateToHome()
                // Add a small delay to allow navigation to complete before resetting state
                delay(100)
                viewModel.resetState()
            }
            is OtpUiState.Error -> {
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
                text = "Enter Verification Code",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "We've sent a verification code to\n$email",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = otp,
                onValueChange = {
                    if (it.length <= 4) {
                        otp = it.filter { char -> char.isDigit() }
                        isError = false
                    }
                },
                label = { Text("Enter 4-digit code") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                isError = isError,
                supportingText = if (isError) {
                    { Text("Please enter a valid 4-digit code") }
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    if (viewModel.isValidOtp(otp)) {
                        viewModel.verifyOtp(otp)
                    } else {
                        isError = true
                    }
                },
                enabled = otp.length == 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Verify")
            }
        }

        if (uiState is OtpUiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
} 