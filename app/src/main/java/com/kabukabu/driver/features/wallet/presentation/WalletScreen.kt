package com.kabukabu.driver.features.wallet.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kabukabu.driver.R
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import android.content.Intent
import android.net.Uri
import com.kabukabu.driver.features.wallet.presentation.WalletViewModel
import java.text.NumberFormat
import java.util.Locale
import com.kabukabu.driver.core.theme.KabukabuYellowLight
import com.kabukabu.driver.core.theme.SurfaceCard
import com.kabukabu.driver.core.theme.SurfaceCardAlt
import com.kabukabu.driver.core.theme.TextSecondary
import com.kabukabu.driver.core.theme.Error

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onBack: () -> Unit,
    onNavigatePaymentHistory: () -> Unit,
    onNavigateSharpPayment: () -> Unit,
    vm: WalletViewModel = viewModel()
) {
    val uiState by vm.uiState.collectAsState()
    var showTopup by remember { mutableStateOf(false) }
    var showWithdraw by remember { mutableStateOf(false) }
    var showMore by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "", color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceCardAlt),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.angle_right),
                                contentDescription = "Back",
                                tint = TextSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            // Header yellow container with balance and actions
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                WalletHeaderSection(
                    balanceText = "₦${formatCurrency(uiState.balance)}",
                    onAddMoney = { showTopup = true },
                    onWithdraw = { showWithdraw = true },
                    onMore = { showMore = true }
                )
            }

            // thin light yellow bar below header
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
                    .height(10.dp)
                    .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                    .background(KabukabuYellowLight)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Body replicating Flutter WalletBody structure (info cards + actions)
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        if (uiState.kabuSubscriptionAmount > 0) {
                            InfoTile(
                                leading = R.drawable.info_circle,
                                text = buildString {
                                    append("₦"); append(formatCurrency(uiState.kabuSubscriptionAmount)); append(" Kabu subscription")
                                    uiState.kabuDueLabel?.let { append(" "); append(it) }
                                }
                            )
                        }
                        InfoTile(
                            leading = R.drawable.info_circle,
                            text = "Trip charge cap ₦${formatCurrency(uiState.tripChargeCap)}"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom actions like Payment History and Sharps (if applicable)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ActionBlock(
                        title = "View payment history",
                        iconRes = R.drawable.receipt_percent,
                        onClick = { onNavigatePaymentHistory() }
                    )
                    if (uiState.hasSharpDetails) {
                        ActionBlock(
                            title = "View Sharps\npayment history",
                            iconRes = R.drawable.message_text,
                            onClick = { onNavigateSharpPayment() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        if (showTopup) {
            var amountText by remember { mutableStateOf("") }
            SimpleBottomSheet(title = "Add money", onDismiss = { showTopup = false }) {
                Column {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { new -> amountText = new.filter { it.isDigit() } },
                        label = { Text("Amount (₦)") }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (uiState.isTopupLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating payment link…")
                        }
                    } else {
                        Button(onClick = {
                            val amt = amountText.toIntOrNull() ?: 0
                            if (amt > 0) vm.initiateTopup(amt)
                        }) { Text("Continue") }
                    }
                    uiState.errorMessage?.let { err ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = err, color = Error)
                    }
                    // When URL is available, open it and clear state
                    uiState.topupUrl?.let { url ->
                        LaunchedEffect(url) {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } finally {
                                vm.clearTopupResult()
                                showTopup = false
                            }
                        }
                    }
                }
            }
        }
        if (showWithdraw) {
            var amountText by remember { mutableStateOf("") }
            var accountNumber by remember { mutableStateOf("") }
            var bankExpanded by remember { mutableStateOf(false) }
            var selectedBankName by remember { mutableStateOf("") }
            var selectedBankCode by remember { mutableStateOf("") }
            // Load banks when opening
            LaunchedEffect(Unit) { vm.fetchBanks() }
            SimpleBottomSheet(title = "Withdraw", onDismiss = {
                vm.clearWithdrawState()
                showWithdraw = false
            }) {
                Column {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { new -> amountText = new.filter { it.isDigit() } },
                        label = { Text("Amount (₦)") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Bank dropdown
                    Box {
                        OutlinedTextField(
                            value = if (selectedBankName.isNotBlank()) selectedBankName else "Select bank",
                            onValueChange = {},
                            label = { Text("Bank") },
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.angle_right),
                                    contentDescription = null,
                                    tint = Color(0xFF9A9A9A),
                                    modifier = Modifier.clickable { bankExpanded = !bankExpanded }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        androidx.compose.material3.DropdownMenu(
                            expanded = bankExpanded,
                            onDismissRequest = { bankExpanded = false }
                        ) {
                            if (uiState.isBanksLoading) {
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("Loading banks…") },
                                    onClick = {}
                                )
                            } else if (uiState.banksError != null) {
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("${uiState.banksError}") },
                                    onClick = {}
                                )
                            } else {
                                uiState.banks.forEach { bank ->
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text(bank.name) },
                                        onClick = {
                                            selectedBankName = bank.name
                                            selectedBankCode = bank.code
                                            bankExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Account number") }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (uiState.isWithdrawLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Processing…")
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = {
                                if (accountNumber.length >= 8 && selectedBankCode.isNotBlank()) {
                                    vm.confirmAccount(accountNumber, selectedBankCode)
                                }
                            }) { Text("Confirm account") }
                            Button(onClick = {
                                val amt = amountText.toIntOrNull() ?: 0
                                if (amt > 0 && accountNumber.length >= 8 && selectedBankCode.isNotBlank()) {
                                    vm.withdraw(amt, accountNumber, selectedBankCode)
                                }
                            }) { Text("Withdraw") }
                        }
                    }
                    uiState.accountName?.let { name ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Account name: $name")
                    }
                    uiState.withdrawError?.let { err ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = err, color = Error)
                    }
                    uiState.withdrawSuccess?.let { success ->
                        if (success) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Withdrawal requested successfully", color = Color(0xFF2E7D32))
                        }
                    }
                }
            }
        }
        if (showMore) {
            // Kick off hasPin check when opening
            LaunchedEffect(Unit) {
                vm.checkHasPin()
            }
            var oldPin by remember { mutableStateOf("") }
            var newPin by remember { mutableStateOf("") }
            var confirmPin by remember { mutableStateOf("") }
            var otp by remember { mutableStateOf("") }
            SimpleBottomSheet(title = "Wallet PIN", onDismiss = {
                vm.clearPinState()
                showMore = false
            }) {
                Column {
                    if (uiState.isPinLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Loading…")
                        }
                    } else {
                        if (uiState.hasPin) {
                            Text("Change PIN", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(value = oldPin, onValueChange = { oldPin = it.filter { ch -> ch.isDigit() }.take(4) }, label = { Text("Old PIN (4 digits)") })
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(value = newPin, onValueChange = { newPin = it.filter { ch -> ch.isDigit() }.take(4) }, label = { Text("New PIN (4 digits)") })
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(value = confirmPin, onValueChange = { confirmPin = it.filter { ch -> ch.isDigit() }.take(4) }, label = { Text("Confirm new PIN") })
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                if (newPin.length == 4 && newPin == confirmPin && oldPin.length == 4) {
                                    vm.changePin(oldPin, newPin)
                                }
                            }) { Text("Update PIN") }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Forgot PIN?", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { vm.requestPinReset() }) { Text("Request OTP") }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(value = otp, onValueChange = { otp = it.filter { ch -> ch.isDigit() }.take(6) }, label = { Text("OTP") })
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(value = newPin, onValueChange = { newPin = it.filter { ch -> ch.isDigit() }.take(4) }, label = { Text("New PIN (4 digits)") })
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(value = confirmPin, onValueChange = { confirmPin = it.filter { ch -> ch.isDigit() }.take(4) }, label = { Text("Confirm new PIN") })
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                if (otp.isNotBlank() && newPin.length == 4 && newPin == confirmPin) {
                                    vm.validatePinReset(otp, newPin)
                                }
                            }) { Text("Validate & Reset") }
                        } else {
                            Text("Create PIN", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(value = newPin, onValueChange = { newPin = it.filter { ch -> ch.isDigit() }.take(4) }, label = { Text("New PIN (4 digits)") })
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(value = confirmPin, onValueChange = { confirmPin = it.filter { ch -> ch.isDigit() }.take(4) }, label = { Text("Confirm new PIN") })
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                if (newPin.length == 4 && newPin == confirmPin) {
                                    vm.createPin(newPin)
                                }
                            }) { Text("Create PIN") }
                        }
                    }
                    uiState.pinError?.let { err ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = err, color = MaterialTheme.colorScheme.error)
                    }
                    uiState.pinSuccess?.let { msg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = msg, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

private fun formatCurrency(value: Int): String {
    return try {
        NumberFormat.getNumberInstance(Locale.getDefault()).format(value)
    } catch (e: Exception) {
        value.toString()
    }
}

@Composable
private fun WalletHeaderSection(
    balanceText: String,
    onAddMoney: () -> Unit,
    onWithdraw: () -> Unit,
    onMore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Balance text
            Text(
                text = balanceText,
                color = Color(0xFFD8D8D8),
                fontWeight = FontWeight.W700,
                fontSize = 31.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Wallet balance",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFFD8D8D8),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W500
                )
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallAction(
                text = "Top up",
                background = Color(0xFFFFBF00),
                iconRes = R.drawable.money_bill_receive,
                onClick = onAddMoney,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF161616),
                    fontWeight = FontWeight.Medium
                ),
                iconTint = Color(0xFF161616),
                modifier = Modifier.weight(1f)
            )
            SmallAction(
                text = "Withdraw",
                background = Color.Transparent,
                iconRes = R.drawable.money_bill_receive,
                onClick = onWithdraw,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFFFDFDFD),
                    fontWeight = FontWeight.Medium
                ),
                iconTint = Color(0xFFFDFDFD),
                borderColor = Color(0xFFFDFDFD),
                modifier = Modifier.weight(1f)
            )
            Card(
                onClick = onMore,
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, Color(0xFFFDFDFD))
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.more_horizontal),
                        contentDescription = "More",
                        tint = Color(0xFFFDFDFD),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SmallAction(
    text: String,
    background: Color,
    iconRes: Int,
    onClick: () -> Unit,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium.copy(
        color = Color.Black,
        fontWeight = FontWeight.W600,
        fontSize = 12.sp
    ),
    iconTint: Color? = null,
    borderColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = background),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = borderColor?.let { BorderStroke(1.dp, it) }
    ) {
        Row(
            modifier = Modifier
                .height(44.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = text,
                tint = iconTint ?: Color.Unspecified,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = textStyle
            )
        }
    }
}

@Composable
private fun InfoTile(leading: Int, text: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = leading),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.W400
            )
        }
    }
}

@Composable
private fun RowScope.ActionBlock(title: String, iconRes: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier.weight(1f),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = KabukabuYellowLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.W600
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleBottomSheet(title: String, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    androidx.compose.material3.ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            content()
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = "Close",
                    color = Color(0xFF0066CC),
                    modifier = Modifier.clickable { onDismiss() }
                )
            }
        }
    }
}
