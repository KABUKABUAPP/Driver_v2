package com.kabukabu.driver.features.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kabukabu.driver.features.auth.presentation.OtpViewModel
import com.kabukabu.driver.core.utils.OtpUiState
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import com.kabukabu.driver.R
import com.kabukabu.driver.features.auth.presentation.LoginViewModel
import com.kabukabu.driver.core.utils.LoginUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.util.Log

@Composable
fun OtpVerificationScreen(
    email: String,
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit, // Added navigation back to login
    viewModel: OtpViewModel = viewModel(),
    loginViewModel: LoginViewModel = viewModel() // Add login view model for resending OTP
) {
    var otpValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue("", TextRange(0)))
    }
    var isError by remember { mutableStateOf(false) }
    val uiState = viewModel.uiState
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    
    // Countdown timer state
    var secondsLeft by remember { mutableStateOf(15) }
    var isTimerRunning by remember { mutableStateOf(true) }
    
    // Start countdown timer
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (secondsLeft > 0) {
                delay(1000L)
                secondsLeft--
            }
            isTimerRunning = false
        }
    }

    // Track resend OTP state
    val loginUiState = loginViewModel.uiState

    // Define colors
    val lightGray = Color(0xFFF1F1F1) // rgba(241, 241, 241, 1)
    val amber = Color(0xFFFFBF00) // rgba(255, 191, 0, 1)
    val lightGrayBg = Color(0xFFF8F8F8) // rgba(248, 248, 248, 1)
    val disabledTextColor = Color(0xFF9A9A9A) // rgba(154, 154, 154, 1)

    // Function to handle OTP verification
    fun verifyOtp() {
        if (viewModel.isValidOtp(otpValue.text)) {
            viewModel.verifyOtp(otpValue.text, email)
        } else {
            isError = true
        }
    }
    
    // Function to resend OTP
    fun resendOtp() {
        loginViewModel.login(email)
        secondsLeft = 15
        isTimerRunning = true
    }

    // Handle OTP verification state
    LaunchedEffect(uiState) {
        Log.d("OtpVerificationScreen", "UI State changed: $uiState")
        when (uiState) {
            is OtpUiState.Success -> {
                Log.d("OtpVerificationScreen", "Success state detected, navigating to home")
                Toast.makeText(context, "Login successful!", Toast.LENGTH_LONG).show()
                
                // Add a delay before navigation to ensure token is saved
                delay(500)
                
                // Navigate to home
                onNavigateToHome()
                
                // Add a longer delay before resetting state
                delay(1000)
                viewModel.resetState()
            }
            is OtpUiState.Error -> {
                Log.e("OtpVerificationScreen", "Error state: ${uiState.message}")
                Toast.makeText(context, uiState.message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {
                Log.d("OtpVerificationScreen", "Other state: $uiState")
            }
        }
    }
    
    // Handle login/resend OTP state
    LaunchedEffect(loginUiState) {
        when (loginUiState) {
            is LoginUiState.Success -> {
                Toast.makeText(context, "OTP resent successfully", Toast.LENGTH_SHORT).show()
                loginViewModel.resetState()
            }
            is LoginUiState.Error -> {
                Toast.makeText(context, "Failed to resend OTP: ${loginUiState.message}", Toast.LENGTH_SHORT).show()
                loginViewModel.resetState()
            }
            is LoginUiState.Loading -> {
                // Show loading state for resend if needed
            }
            else -> {}
        }
    }

    // Request focus when the screen is first displayed
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(top = 60.dp) // Same padding as login screen
        ) {
            // Title and subtitle section aligned to the left
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 52.dp), // Same padding as login screen
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Enter OTP",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.W600,
                        fontSize = 25.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "An OTP has been sent to $email\nEnter the code to validate your number",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.W700,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    textAlign = TextAlign.Start
                )
            }

            // OTP Pin Fields
            BasicTextField(
                value = otpValue,
                onValueChange = { newValue ->
                    // Only allow up to 4 digits
                    if (newValue.text.length <= 4 && newValue.text.all { it.isDigit() }) {
                        otpValue = newValue.copy(selection = TextRange(newValue.text.length))
                        isError = false
                        
                        // Auto-submit when 4 digits are entered
                        if (newValue.text.length == 4) {
                            coroutineScope.launch {
                                delay(300) // Small delay before submission
                                verifyOtp() // Use the extracted function
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                textStyle = TextStyle(
                    fontSize = 0.sp, // Set to zero to make it invisible
                    color = Color.Transparent // Make the text transparent
                ),
                decorationBox = { innerTextField ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        repeat(4) { index ->
                            val char = otpValue.text.getOrNull(index)?.toString() ?: ""
                            val isFocused = otpValue.text.length == index
                            
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(lightGray)
                                    .then(
                                        if (isError) Modifier.border(
                                            width = 2.dp,
                                            color = Color.Red,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        else if (char.isNotEmpty() || isFocused) Modifier.border(
                                            width = 2.dp,
                                            color = amber,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        else Modifier
                                    )
                            ) {
                                Text(
                                    text = char,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                
                                // Show cursor indicator
                                if (isFocused) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(24.dp)
                                            .background(amber)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Hide the actual text field by placing it in a zero-sized box
                    Box(modifier = Modifier.size(0.dp)) {
                        innerTextField()
                    }
                }
            )

            if (isError) {
                Text(
                    text = "Please enter a valid 4-digit code",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                )
            }
            
            // Resend Code Button with countdown
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(top = 32.dp) // Increased from 16dp to 32dp
                    .align(Alignment.CenterHorizontally)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isTimerRunning) lightGrayBg else amber)
                        .clickable(enabled = !isTimerRunning) {
                            if (!isTimerRunning) {
                                // Use login API to resend OTP
                                resendOtp()
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                ) {
                    if (isTimerRunning) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Resend Code in ",
                                style = TextStyle(
                                    fontWeight = FontWeight.W500,
                                    fontSize = 14.sp,
                                    color = disabledTextColor // Changed to rgba(154, 154, 154, 1)
                                )
                            )
                            Text(
                                text = "${secondsLeft}s",
                                style = TextStyle(
                                    fontWeight = FontWeight.W500,
                                    fontSize = 14.sp,
                                    color = Color.Black // Countdown in black
                                )
                            )
                        }
                    } else {
                        Text(
                            text = "Resend Code",
                            style = TextStyle(
                                fontWeight = FontWeight.W500,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        )
                    }
                }
            }

            // Fixed spacing between resend button and verify button
            Spacer(modifier = Modifier.height(86.dp)) // Back to original spacing

            // Change Email Button with envelope icon
            Button(
                onClick = onNavigateToLogin, // Navigate back to login screen
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = lightGray // rgba(241, 241, 241, 1)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Use custom envelope icon from drawable resources
                    Image(
                        painter = painterResource(id = R.drawable.envelope),
                        contentDescription = "Envelope Icon",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Change email",
                        fontWeight = FontWeight.W500,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
            }
        }

        // Show loading indicator for either OTP verification or resend OTP
        if (uiState is OtpUiState.Loading || loginUiState is LoginUiState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = amber
            )
        }
    }
} 