package com.kabukabu.driver.features.auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kabukabu.driver.features.auth.presentation.LoginViewModel
import com.kabukabu.driver.core.utils.LoginUiState
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import com.kabukabu.driver.R
import com.kabukabu.driver.components.ui.LoadingOverlay
import com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun LoginScreen(
    onNavigateToOtp: (String) -> Unit,
    viewModel: LoginViewModel = viewModel(),
    authViewModel: AuthViewModel = koinViewModel()
    ) {
    var email by remember { mutableStateOf(TextFieldValue()) }
    var isError by remember { mutableStateOf(false) }
    val uiState = viewModel.uiState
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused = interactionSource.collectIsFocusedAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Define colors
    val lightGray = Color(0xFFF1F1F1) // rgba(241, 241, 241, 1)
    val amber = Color(0xFFFFBF00) // rgba(255, 191, 0, 1)

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

    BackHandler { true }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(top = 60.dp) // Additional 30dp padding from top safe area
        ) {
            // Title and subtitle section aligned to the left
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 52.dp), // Increased from 32dp to 42dp (10dp more)
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Enter your email address",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.W600,
                        fontSize = 25.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Even if you're a new user",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.W700,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    textAlign = TextAlign.Start
                )
            }

            // Custom Email TextField
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    isError = false
                },
                placeholder = { 
                    Text(
                        "Email here", 
                        fontWeight = FontWeight.W500,
                        fontSize = 14.sp
                    ) 
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = "Email",
                        tint = if (isFocused.value) amber else Color.Gray,
                        modifier = Modifier.padding(start = 8.dp) // Add padding to align icon
                    )
                },
                isError = isError,
                supportingText = if (isError) {
                    { Text("Please enter a valid email") }
                } else null,
                modifier = Modifier
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = amber,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = lightGray,
                    unfocusedContainerColor = lightGray,
                    errorBorderColor = Color.Red,
                    cursorColor = amber
                ),
                interactionSource = interactionSource,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                shape = RoundedCornerShape(10.dp),
                textStyle = LocalTextStyle.current.copy(
                    fontWeight = FontWeight.W500,
                    fontSize = 14.sp
                )
            )

            // Increased spacing between text field and button
            Spacer(modifier = Modifier.height(86.dp)) // Changed from 16dp to 46dp (30dp more)

            // Continue button with amber background and black text
            Button(
                onClick = {
                    keyboardController?.hide()
                    if (viewModel.isValidEmail(email.text)) {
                        viewModel.login(email.text)
                    } else {
                        isError = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(53.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = amber // rgba(255, 191, 0, 1)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Continue",
                    fontWeight = FontWeight.W600,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }

        if (uiState is LoginUiState.Loading) {
            LoadingOverlay()
        }
    }
}


