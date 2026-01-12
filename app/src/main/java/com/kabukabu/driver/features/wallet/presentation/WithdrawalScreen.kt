package com.kabukabu.driver.features.wallet.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kabukabu.driver.core.utils.CustomToast
import com.kabukabu.driver.core.utils.noRippleClickable

/**
 * Format amount with commas for display
 * e.g., 1000 -> 1,000
 */
fun formatAmountWithComma(amount: String): String {
    if (amount.isEmpty()) return ""
    val cleanAmount = amount.replace(",", "")
    return try {
        val number = cleanAmount.toLongOrNull() ?: return amount
        String.format("%,d", number)
    } catch (e: Exception) {
        amount
    }
}

/**
 * Remove commas from amount before sending to backend
 * e.g., 1,000 -> 1000
 */
fun removeCommasFromAmount(amount: String): String {
    return amount.replace(",", "")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawalScreen(
    onBack: () -> Unit,
    viewModel: WalletViewModel = viewModel()
) {
    val context = LocalContext.current
    var amount by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var selectedBank by remember { mutableStateOf<WalletViewModel.BankItem?>(null) }
    var showBankModal by remember { mutableStateOf(false) }
    var showPinModal by remember { mutableStateOf(false) }

    // Collect UI state
    val uiState by viewModel.uiState.collectAsState()

    // Load banks on first composition
    LaunchedEffect(Unit) {
        viewModel.fetchBanks()
    }

    // Auto-confirm account when 10 digits entered
    LaunchedEffect(accountNumber, selectedBank) {
        if (accountNumber.length == 10 && selectedBank != null) {
            viewModel.confirmAccount(accountNumber, selectedBank!!.code, selectedBank!!.name)
        } else if (accountNumber.length != 10) {
            // Clear account name if digits change
            viewModel.clearWithdrawState()
        }
    }

    // Handle withdrawal success
    LaunchedEffect(uiState.withdrawSuccess) {
        if (uiState.withdrawSuccess == true) {
            CustomToast.showSuccess(context, "Withdrawal successful!")
            kotlinx.coroutines.delay(3000)
            viewModel.clearWithdrawState()
            onBack()
        }
    }

    // Handle error from ViewModel
    LaunchedEffect(uiState.withdrawError) {
        if (uiState.withdrawError != null) {
            CustomToast.showError(context, uiState.withdrawError ?: "Withdrawal failed")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {  },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", modifier = Modifier.size(24.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 80.dp) // Space for button
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Title
                Text("Withdraw", color = Color.Black,fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "Send money out of your wallet",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Field
            Text(
                text = "Amount",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { newValue ->
                    // Remove commas and validate it's a number
                    val cleanValue = newValue.replace(",", "")
                    if (cleanValue.isEmpty() || cleanValue.all { it.isDigit() }) {
                        // Format with commas
                        amount = formatAmountWithComma(cleanValue)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter amount", color = Color.Gray) },
                singleLine = true,
                leadingIcon = {
                    Text(
                        text = "₦",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF2F2F2),
                    unfocusedContainerColor = Color(0xFFF2F2F2),
                    disabledContainerColor = Color(0xFFF2F2F2),
                    focusedBorderColor = Color(0xFFF4C446),
                    unfocusedBorderColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Select Bank Field
            Text(
                text = "Select Bank",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFFF2F2F2), RoundedCornerShape(12.dp))
                    .noRippleClickable { showBankModal = true }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedBank?.name ?: "Select a bank",
                        color = if (selectedBank == null) Color.Gray else Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.ArrowDropDown, null, tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Account Number Field
            Text(
                text = "Account Number",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = accountNumber,
                onValueChange = {
                    if (it.length <= 10) {
                        accountNumber = it
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter account number", color = Color.Gray) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF2F2F2),
                    unfocusedContainerColor = Color(0xFFF2F2F2),
                    disabledContainerColor = Color(0xFFF2F2F2),
                    focusedBorderColor = Color(0xFFF4C446),
                    unfocusedBorderColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    if (accountNumber.length == 10 && uiState.isWithdrawLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFFF4C446)
                        )
                    }
                }
            )

            // Account Name Field (auto-filled after confirmation)
            if (uiState.accountName != null && uiState.accountName!!.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Account Name",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Color(0xFFF2F2F2), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Person, null, tint = Color.Black)
                        Spacer(Modifier.width(12.dp))
                        Text(uiState.accountName ?: "", color = Color.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Continue Button - Fixed at bottom
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Button(
                onClick = {
                    // Remove commas from amount for validation
                    val cleanAmount = removeCommasFromAmount(amount)

                    // Validate inputs
                    when {
                        amount.isEmpty() -> {
                            CustomToast.showError(context, "Please enter amount")
                        }
                        cleanAmount.toIntOrNull() == null -> {
                            CustomToast.showError(context, "Please enter valid amount")
                        }
                        cleanAmount.toInt() <= 0 -> {
                            CustomToast.showError(context, "Amount must be greater than zero")
                        }
                        cleanAmount.toInt() > uiState.balance -> {
                            CustomToast.showError(context, "Insufficient balance. Your balance is ₦${String.format("%,d", uiState.balance)}")
                        }
                        selectedBank == null -> {
                            CustomToast.showError(context, "Please select a bank")
                        }
                        accountNumber.isEmpty() -> {
                            CustomToast.showError(context, "Please enter account number")
                        }
                        accountNumber.length != 10 -> {
                            CustomToast.showError(context, "Account number must be 10 digits")
                        }
                        uiState.accountName.isNullOrBlank() -> {
                            CustomToast.showError(context, "Unable to confirm account. Please check details")
                        }
                        else -> {
                            // All validations passed, show PIN modal
                            showPinModal = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(vertical = 16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF4C446)),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isWithdrawLoading
            ) {
                if (uiState.isWithdrawLoading && !showPinModal) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black
                    )
                } else {
                    Text("Continue", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            }
        }
    }

    // Bank Selection Modal
    if (showBankModal) {
        BankSelectionModal(
            banks = uiState.banks,
            isLoading = uiState.isBanksLoading,
            onDismiss = { showBankModal = false },
            onBankSelected = { bank ->
                selectedBank = bank
                showBankModal = false
                // Clear account number and name when bank changes
                accountNumber = ""
                viewModel.clearWithdrawState()
            }
        )
    }

    // PIN Entry Modal
    if (showPinModal) {
        WithdrawalPinModalNew(
            onDismiss = { showPinModal = false },
            onConfirm = { pin ->
                // Remove commas from amount before sending to backend
                val cleanAmount = removeCommasFromAmount(amount)

                // Call withdrawal API with PIN
                viewModel.withdraw(
                    amount = cleanAmount.toInt(),
                    accountNumber = accountNumber,
                    bankCode = selectedBank!!.code,
                    pin = pin
                )
                showPinModal = false
            },
            isLoading = uiState.isWithdrawLoading
        )
    }
}

@Composable
fun BankSelectionModal(
    banks: List<WalletViewModel.BankItem>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onBankSelected: (WalletViewModel.BankItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredBanks = remember(banks, searchQuery) {
        if (searchQuery.isEmpty()) {
            banks
        } else {
            banks.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    StackedBottomSheet(
        onDismiss = onDismiss,
        heightFraction = 0.85f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Bank",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F1F1F)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(Color(0xFFE53935).copy(alpha = 0.1f), CircleShape)
                        .size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Field - Now properly clickable
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search banks...", color = Color.Gray) },
                leadingIcon = {
                    Icon(Icons.Default.Search, null, tint = Color.Gray)
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF2F2F2),
                    unfocusedContainerColor = Color(0xFFF2F2F2),
                    disabledContainerColor = Color(0xFFF2F2F2),
                    focusedBorderColor = Color(0xFFF4C446),
                    unfocusedBorderColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Banks List
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFF4C446))
                    }
                }
                filteredBanks.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isEmpty()) "No banks available" else "No banks match your search",
                            color = Color.Gray
                        )
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredBanks.forEach { bank ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .noRippleClickable { onBankSelected(bank) },
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF5F5F5)
                            ) {
                                Text(
                                    text = bank.name,
                                    modifier = Modifier.padding(16.dp),
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


