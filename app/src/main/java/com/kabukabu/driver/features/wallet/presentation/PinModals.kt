package com.kabukabu.driver.features.wallet.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kabukabu.driver.core.utils.CustomToast


object PinModalConfig {
    // Modal Settings
    val heightFraction: Float = 0.75f

    // Padding
    val horizontalPadding: Dp = 20.dp
    val topPadding: Dp = 16.dp
    val bottomPadding: Dp = 12.dp

    // PIN Display Box
    val pinBoxSize: Dp = 56.dp
    val pinBoxSpacing: Dp = 12.dp
    val pinBoxCornerRadius: Dp = 8.dp
    val pinBoxBackgroundColor: Color = Color(0xFFF2F2F2)

    // Keypad
    val keypadRowSpacing: Dp = 12.dp
    val keypadButtonSize: Dp = 64.dp

    // Spacers
    val spacerAfterHeader: Dp = 12.dp
    val spacerBeforeKeypad: Dp = 8.dp
    val spacerBeforeButton: Dp = 16.dp
    val pinRowVerticalPadding: Dp = 16.dp

    // Button
    val buttonHeight: Dp = 58.dp
    val buttonCornerRadius: Dp = 12.dp

    // Typography
    val titleFontSize = 20.sp
    val subtitleFontSize = 14.sp
    val pinDigitFontSize = 20.sp

    // Colors
    val titleColor: Color = Color(0xFF1F1F1F)
    val subtitleColor: Color = Color.Black
    val buttonTextColor: Color = Color.Black

    // Close Button
    val closeButtonSize: Dp = 32.dp
    val closeIconSize: Dp = 16.dp
}

// ============================================================================
// REUSABLE PIN COMPONENTS
// ============================================================================

/**
 * Reusable PIN display boxes row
 */
@Composable
fun PinDisplayBoxes(
    currentPin: String,
    maxLength: Int = 4,
    cursorVisible: Boolean = true,
    boxSize: Dp = PinModalConfig.pinBoxSize,
    spacing: Dp = PinModalConfig.pinBoxSpacing,
    backgroundColor: Color = PinModalConfig.pinBoxBackgroundColor
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(spacing),
        modifier = Modifier.padding(vertical = PinModalConfig.pinRowVerticalPadding)
    ) {
        repeat(maxLength) { index ->
            val isActive = index == currentPin.length
            val hasPinDigit = index < currentPin.length

            Box(
                modifier = Modifier
                    .size(boxSize)
                    .background(backgroundColor, RoundedCornerShape(PinModalConfig.pinBoxCornerRadius)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    hasPinDigit -> {
                        Text(
                            text = currentPin[index].toString(),
                            color = Color.Black,
                            fontSize = PinModalConfig.pinDigitFontSize,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    isActive -> {
                        if (cursorVisible) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(24.dp)
                                    .background(Color.Black)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Reusable PIN keypad
 */
@Composable
fun PinKeypad(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    rowSpacing: Dp = PinModalConfig.keypadRowSpacing
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", ">")
    )

    Column(verticalArrangement = Arrangement.spacedBy(rowSpacing)) {
        keys.forEach { row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                row.forEach { key ->
                    KeypadButton(key) { clickedKey ->
                        when (clickedKey) {
                            ">" -> onDeleteClick()
                            "." -> { /* Do nothing */ }
                            else -> onNumberClick(clickedKey)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Reusable PIN modal header with close button
 */
@Composable
fun PinModalHeader(
    title: String,
    subtitle: String,
    onClose: () -> Unit,
    centerAligned: Boolean = true
) {
    if (centerAligned) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Close button aligned to the right
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .background(KabuRed.copy(alpha = 0.1f), CircleShape)
                    .size(PinModalConfig.closeButtonSize)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = KabuRed,
                    modifier = Modifier.size(PinModalConfig.closeIconSize)
                )
            }

            // Title and subtitle centered
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontSize = PinModalConfig.titleFontSize,
                    fontWeight = FontWeight.Bold,
                    color = PinModalConfig.titleColor
                )
                Text(
                    text = subtitle,
                    fontSize = PinModalConfig.subtitleFontSize,
                    color = PinModalConfig.subtitleColor
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = PinModalConfig.titleFontSize,
                    fontWeight = FontWeight.Bold,
                    color = PinModalConfig.titleColor
                )
                Text(
                    text = subtitle,
                    fontSize = PinModalConfig.subtitleFontSize,
                    color = PinModalConfig.subtitleColor
                )
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .background(KabuRed.copy(alpha = 0.1f), CircleShape)
                    .size(PinModalConfig.closeButtonSize)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = KabuRed,
                    modifier = Modifier.size(PinModalConfig.closeIconSize)
                )
            }
        }
    }
}

/**
 * Reusable PIN action button
 */
@Composable
fun PinActionButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(PinModalConfig.buttonHeight),
        colors = ButtonDefaults.buttonColors(containerColor = KabuYellow),
        shape = RoundedCornerShape(PinModalConfig.buttonCornerRadius),
        enabled = enabled && !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = PinModalConfig.buttonTextColor
            )
        } else {
            Text(
                text = text,
                color = PinModalConfig.buttonTextColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

/**
 * Reusable PIN input field (for Reset PIN modal with text fields)
 */
@Composable
fun PinInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isSecure: Boolean
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color.Gray) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = KabuGreyBg,
                unfocusedContainerColor = KabuGreyBg,
                disabledContainerColor = KabuGreyBg,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (isSecure) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isSecure) KeyboardType.NumberPassword else KeyboardType.Number
            )
        )
    }
}

// ============================================================================
// PIN MODAL IMPLEMENTATIONS
// ============================================================================

/**
 * Create PIN Modal - For users creating a new PIN
 */
@Composable
fun CreatePinModalNew(
    onDismiss: () -> Unit,
    viewModel: WalletViewModel
) {
    var step by remember { mutableIntStateOf(1) }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var cursorVisible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            cursorVisible = !cursorVisible
        }
    }

    // Handle success
    LaunchedEffect(uiState.pinSuccess) {
        if (uiState.pinSuccess != null) {
            CustomToast.showSuccess(context, uiState.pinSuccess!!)
            kotlinx.coroutines.delay(1500)
            viewModel.clearPinState()
            onDismiss()
        }
    }

    // Handle error
    LaunchedEffect(uiState.pinError) {
        if (uiState.pinError != null) {
            CustomToast.showError(context, uiState.pinError!!)
        }
    }

    val currentPin = when (step) {
        1 -> newPin
        2 -> confirmPin
        else -> ""
    }

    StackedBottomSheet(
        onDismiss = {
            viewModel.clearPinState()
            onDismiss()
        },
        heightFraction = PinModalConfig.heightFraction
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = PinModalConfig.horizontalPadding)
                .padding(top = PinModalConfig.topPadding, bottom = PinModalConfig.bottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PinModalHeader(
                title = "Create PIN",
                subtitle = when (step) {
                    1 -> "Enter new pin"
                    2 -> "Confirm new pin"
                    else -> ""
                },
                onClose = {
                    viewModel.clearPinState()
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(PinModalConfig.spacerAfterHeader))

            PinDisplayBoxes(
                currentPin = currentPin,
                cursorVisible = cursorVisible
            )

            Spacer(modifier = Modifier.height(PinModalConfig.spacerBeforeKeypad))

            PinKeypad(
                onNumberClick = { num ->
                    if (currentPin.length < 4) {
                        when (step) {
                            1 -> newPin += num
                            2 -> confirmPin += num
                        }
                    }
                },
                onDeleteClick = {
                    when (step) {
                        1 -> if (newPin.isNotEmpty()) newPin = newPin.dropLast(1)
                        2 -> if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                    }
                }
            )

            Spacer(modifier = Modifier.height(PinModalConfig.spacerBeforeButton))

            PinActionButton(
                text = if (step == 2) "Create PIN" else "Continue",
                onClick = {
                    when (step) {
                        1 -> {
                            if (newPin.length != 4) {
                                CustomToast.showError(context, "PIN must be 4 digits")
                            } else {
                                step = 2
                            }
                        }
                        2 -> {
                            if (confirmPin != newPin) {
                                CustomToast.showError(context, "PINs don't match")
                            } else {
                                viewModel.createPin(newPin)
                            }
                        }
                    }
                },
                isLoading = uiState.isPinLoading,
                enabled = currentPin.length == 4
            )
        }
    }
}

/**
 * Change PIN Modal - For users changing their existing PIN
 */
@Composable
fun ChangePinModalNew(
    onDismiss: () -> Unit,
    viewModel: WalletViewModel
) {
    var step by remember { mutableIntStateOf(1) }
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var cursorVisible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            cursorVisible = !cursorVisible
        }
    }

    // Handle success
    LaunchedEffect(uiState.pinSuccess) {
        if (uiState.pinSuccess != null) {
            CustomToast.showSuccess(context, uiState.pinSuccess!!)
            kotlinx.coroutines.delay(1500)
            viewModel.clearPinState()
            onDismiss()
        }
    }

    // Handle error
    LaunchedEffect(uiState.pinError) {
        if (uiState.pinError != null) {
            CustomToast.showError(context, uiState.pinError!!)
            viewModel.clearPinState()
        }
    }

    val currentPin = when (step) {
        1 -> oldPin
        2 -> newPin
        3 -> confirmPin
        else -> ""
    }

    StackedBottomSheet(
        onDismiss = {
            viewModel.clearPinState()
            onDismiss()
        },
        heightFraction = PinModalConfig.heightFraction
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = PinModalConfig.horizontalPadding)
                .padding(top = PinModalConfig.topPadding, bottom = PinModalConfig.bottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PinModalHeader(
                title = "Change PIN",
                subtitle = when (step) {
                    1 -> "Enter old pin"
                    2 -> "Enter new pin"
                    3 -> "Confirm new pin"
                    else -> ""
                },
                onClose = {
                    viewModel.clearPinState()
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(PinModalConfig.spacerAfterHeader))

            PinDisplayBoxes(
                currentPin = currentPin,
                cursorVisible = cursorVisible
            )

            Spacer(modifier = Modifier.height(PinModalConfig.spacerBeforeKeypad))

            PinKeypad(
                onNumberClick = { num ->
                    if (currentPin.length < 4) {
                        when (step) {
                            1 -> oldPin += num
                            2 -> newPin += num
                            3 -> confirmPin += num
                        }
                    }
                },
                onDeleteClick = {
                    when (step) {
                        1 -> if (oldPin.isNotEmpty()) oldPin = oldPin.dropLast(1)
                        2 -> if (newPin.isNotEmpty()) newPin = newPin.dropLast(1)
                        3 -> if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                    }
                }
            )

            Spacer(modifier = Modifier.height(PinModalConfig.spacerBeforeButton))

            PinActionButton(
                text = if (step == 3) "Change PIN" else "Continue",
                onClick = {
                    when (step) {
                        1 -> step = 2
                        2 -> {
                            when {
                                newPin.length != 4 -> CustomToast.showError(context, "PIN must be 4 digits")
                                newPin == oldPin -> CustomToast.showError(context, "New PIN must be different from old PIN")
                                else -> step = 3
                            }
                        }
                        3 -> {
                            if (confirmPin != newPin) {
                                CustomToast.showError(context, "PINs don't match")
                            } else {
                                viewModel.changePin(oldPin, newPin)
                            }
                        }
                    }
                },
                isLoading = uiState.isPinLoading,
                enabled = currentPin.length == 4
            )
        }
    }
}

/**
 * Reset PIN Modal - For users who forgot their PIN
 */
@Composable
fun ResetPinModalNew(
    onDismiss: () -> Unit,
    viewModel: WalletViewModel
) {
    val context = LocalContext.current
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var otpRequested by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    // Request OTP when modal opens
    LaunchedEffect(Unit) {
        if (!otpRequested) {
            viewModel.requestPinReset()
            otpRequested = true
        }
    }

    // Handle success
    LaunchedEffect(uiState.pinSuccess) {
        if (uiState.pinSuccess == "PIN reset successful") {
            CustomToast.showSuccess(context, "PIN reset successful!")
            kotlinx.coroutines.delay(500)
            viewModel.clearPinState()
            onDismiss()
        } else if (uiState.pinSuccess == "OTP sent") {
            CustomToast.showSuccess(context, "OTP sent to your phone")
            viewModel.clearPinState()
        }
    }

    // Handle error
    LaunchedEffect(uiState.pinError) {
        if (uiState.pinError != null) {
            CustomToast.showError(context, uiState.pinError ?: "Reset failed")
            viewModel.clearPinState()
        }
    }

    StackedBottomSheet(
        onDismiss = {
            viewModel.clearPinState()
            onDismiss()
        },
        heightFraction = PinModalConfig.heightFraction
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = PinModalConfig.horizontalPadding)
                .padding(top = PinModalConfig.topPadding, bottom = PinModalConfig.bottomPadding)
        ) {
            PinModalHeader(
                title = "Reset PIN",
                subtitle = "Create a new PIN for your wallet",
                onClose = {
                    viewModel.clearPinState()
                    onDismiss()
                },
                centerAligned = false
            )

            Spacer(modifier = Modifier.height(PinModalConfig.spacerAfterHeader))
            Spacer(modifier = Modifier.height(PinModalConfig.spacerAfterHeader))
            Spacer(modifier = Modifier.height(PinModalConfig.spacerAfterHeader))

            // Input Fields
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PinInputField(
                    label = "Input OTP (sent to your phone)",
                    value = otp,
                    onValueChange = { otp = it },
                    placeholder = "xxxx",
                    isSecure = false
                )

                PinInputField(
                    label = "New 4-Digit PIN",
                    value = newPin,
                    onValueChange = { newPin = it },
                    placeholder = "xxxx",
                    isSecure = true
                )

                PinInputField(
                    label = "Confirm New 4-Digit PIN",
                    value = confirmPin,
                    onValueChange = { confirmPin = it },
                    placeholder = "xxxx",
                    isSecure = true
                )
            }

            Spacer(modifier = Modifier.height(PinModalConfig.spacerBeforeButton))
            Spacer(modifier = Modifier.height(PinModalConfig.spacerBeforeButton))
            Spacer(modifier = Modifier.height(PinModalConfig.spacerBeforeButton))

            PinActionButton(
                text = "Reset PIN",
                onClick = {
                    when {
                        otp.isEmpty() -> CustomToast.showError(context, "Please enter OTP")
                        otp.length < 4 -> CustomToast.showError(context, "Please enter valid OTP")
                        newPin.isEmpty() -> CustomToast.showError(context, "Please enter new PIN")
                        newPin.length != 4 -> CustomToast.showError(context, "PIN must be 4 digits")
                        confirmPin.isEmpty() -> CustomToast.showError(context, "Please confirm new PIN")
                        confirmPin != newPin -> CustomToast.showError(context, "PINs don't match")
                        else -> viewModel.validatePinReset(otp, newPin)
                    }
                },
                isLoading = uiState.isPinLoading
            )
        }
    }
}

/**
 * Pay Balance PIN Modal - For confirming payment with PIN
 */
@Composable
fun PayBalancePinModalNew(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isLoading: Boolean,
    error: String? = null
) {
    var pin by remember { mutableStateOf("") }
    val context = LocalContext.current

    var cursorVisible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            cursorVisible = !cursorVisible
        }
    }

    // Show toast when error occurs
    LaunchedEffect(error) {
        if (error != null) {
            CustomToast.showError(context, error)
        }
    }

    StackedBottomSheet(
        onDismiss = onDismiss,
        heightFraction = PinModalConfig.heightFraction
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = PinModalConfig.horizontalPadding)
                .padding(top = PinModalConfig.topPadding, bottom = PinModalConfig.bottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PinModalHeader(
                title = "Enter PIN",
                subtitle = "Input pin to confirm payment",
                onClose = onDismiss,
                centerAligned = false
            )

            Spacer(modifier = Modifier.height(PinModalConfig.spacerAfterHeader))

            PinDisplayBoxes(
                currentPin = pin,
                cursorVisible = cursorVisible
            )

            Spacer(modifier = Modifier.height(PinModalConfig.spacerBeforeKeypad))

            PinKeypad(
                onNumberClick = { num ->
                    if (pin.length < 4) pin += num
                },
                onDeleteClick = {
                    if (pin.isNotEmpty()) pin = pin.dropLast(1)
                }
            )

            Spacer(modifier = Modifier.height(PinModalConfig.spacerBeforeButton))

            PinActionButton(
                text = "Pay Balance",
                onClick = { onConfirm(pin) },
                isLoading = isLoading,
                enabled = pin.length == 4
            )
        }
    }
}

/**
 * Generic PIN Entry Modal - Can be used for any PIN verification
 */
@Composable
fun GenericPinEntryModal(
    title: String,
    subtitle: String,
    buttonText: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isLoading: Boolean = false,
    error: String? = null,
    centerAligned: Boolean = true
) {
    var pin by remember { mutableStateOf("") }
    val context = LocalContext.current

    var cursorVisible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            cursorVisible = !cursorVisible
        }
    }

    LaunchedEffect(error) {
        if (error != null) {
            CustomToast.showError(context, error)
        }
    }

    StackedBottomSheet(
        onDismiss = onDismiss,
        heightFraction = PinModalConfig.heightFraction
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = PinModalConfig.horizontalPadding)
                .padding(top = PinModalConfig.topPadding, bottom = PinModalConfig.bottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PinModalHeader(
                title = title,
                subtitle = subtitle,
                onClose = onDismiss,
                centerAligned = centerAligned
            )

            Spacer(modifier = Modifier.height(PinModalConfig.spacerAfterHeader))

            PinDisplayBoxes(
                currentPin = pin,
                cursorVisible = cursorVisible
            )

            Spacer(modifier = Modifier.height(PinModalConfig.spacerBeforeKeypad))

            PinKeypad(
                onNumberClick = { num ->
                    if (pin.length < 4) pin += num
                },
                onDeleteClick = {
                    if (pin.isNotEmpty()) pin = pin.dropLast(1)
                }
            )

            Spacer(modifier = Modifier.height(PinModalConfig.spacerBeforeButton))

            PinActionButton(
                text = buttonText,
                onClick = { onConfirm(pin) },
                isLoading = isLoading,
                enabled = pin.length == 4
            )
        }
    }
}

/**
 * Withdrawal PIN Modal - For confirming withdrawal with PIN
 */
@Composable
fun WithdrawalPinModalNew(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isLoading: Boolean
) {
    var pin by remember { mutableStateOf("") }

    var cursorVisible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            cursorVisible = !cursorVisible
        }
    }

    StackedBottomSheet(
        onDismiss = onDismiss,
        heightFraction = PinModalConfig.heightFraction
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = PinModalConfig.horizontalPadding)
                .padding(top = PinModalConfig.topPadding, bottom = PinModalConfig.bottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PinModalHeader(
                title = "Enter PIN",
                subtitle = "Input pin to confirm transaction",
                onClose = onDismiss,
                centerAligned = false
            )

            Spacer(modifier = Modifier.height(PinModalConfig.spacerAfterHeader))

            PinDisplayBoxes(
                currentPin = pin,
                cursorVisible = cursorVisible
            )

            Spacer(modifier = Modifier.height(PinModalConfig.spacerBeforeKeypad))

            PinKeypad(
                onNumberClick = { num ->
                    if (pin.length < 4) pin += num
                },
                onDeleteClick = {
                    if (pin.isNotEmpty()) pin = pin.dropLast(1)
                }
            )

            Spacer(modifier = Modifier.height(PinModalConfig.spacerBeforeButton))

            PinActionButton(
                text = "Withdraw",
                onClick = { onConfirm(pin) },
                isLoading = isLoading,
                enabled = pin.length == 4
            )
        }
    }
}

