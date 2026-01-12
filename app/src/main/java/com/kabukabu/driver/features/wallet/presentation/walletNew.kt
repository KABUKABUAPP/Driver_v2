package com.kabukabu.driver.features.wallet.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import com.kabukabu.driver.core.utils.safeClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kabukabu.driver.R
import com.kabukabu.driver.core.theme.KabukabuYellow
import com.kabukabu.driver.core.utils.CustomToast
import com.kabukabu.driver.core.utils.composableSafeClickable

// --- 1. Color Palette ---
val KabuBlack = Color(0xFF1A1A1A)
val KabuYellow = KabukabuYellow
val KabuGreyLight = Color(0xFFF5F5F5)
val KabuGreyBg = Color(0xFFF2F2F2) // Input fields
val KabuRed = Color(0xFFE53935)
val KabuPurple = Color(0xFF6200EE)

// --- Reusable Stacked Bottom Sheet Component ---
@Composable
fun StackedBottomSheet(
    onDismiss: () -> Unit, heightFraction: Float = 0.88f, // Default 88% for large modals
    content: @Composable () -> Unit
) {
    // Root container with semi-transparent background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onDismiss() })
            }) {
        // --- STACKED BOTTOM SHEET IMPLEMENTATION ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .pointerInput(Unit) {
                    detectTapGestures { } // Consume taps to prevent propagation
                }, contentAlignment = Alignment.BottomCenter
        ) {
            // LAYER 1: The "Back" Sheet (Peeking out from behind)
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .fillMaxHeight(heightFraction + 0.005f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = Color(0xFFD8D8D8),
                shadowElevation = 4.dp
            ) {
                // Decorative empty layer
            }

            // LAYER 2: The "Front" Main Sheet
            Surface(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .fillMaxHeight(heightFraction)
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                // Content provided by caller
                content()
            }
        }
    }
}

// --- 2. Main Entry Point ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KabukabuWalletApp(
    onBack: () -> Unit = {},
    onNavigateToPaymentHistory: () -> Unit = {},
    onNavigateToPaymentWebView: (String) -> Unit = {},
    onNavigateToWithdrawal: () -> Unit = {},
    refreshOnLaunch: Boolean = false,
    navigationKey: String = "", // Unique key that changes on each navigation
    viewModel: WalletViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    // State to control the visibility of the More Options Modal
    var showMoreOptionsModal by remember { mutableStateOf(false) }
    // State to control the visibility of the Change PIN Modal
    var showChangePinModal by remember { mutableStateOf(false) }
    // State to control the visibility of the Reset PIN Modal
    var showResetPinModal by remember { mutableStateOf(false) }
    // State to control the visibility of the TopUp Modal
    var showTopUpSheet by remember { mutableStateOf(false) }
    // State to control the visibility of the Pay Balance PIN Modal
    var showPayBalancePinModal by remember { mutableStateOf(false) }
    // State to control the visibility of the Create PIN Modal
    var showCreatePinModal by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Collect wallet UI state
    val uiState by viewModel.uiState.collectAsState()

    // Refresh wallet when navigating to the screen
    // navigationKey changes on each navigation, triggering this effect
    LaunchedEffect(navigationKey) {
        if (uiState.hasData && navigationKey.isNotEmpty()) {
            // We have previous data and this is a navigation (not initial composition)
            android.util.Log.d(
                "WalletUI",
                "Navigation detected (key=$navigationKey) - triggering refresh (hasData=true)"
            )
            viewModel.loadWallet()
        } else {
            android.util.Log.d(
                "WalletUI",
                "Navigation key=$navigationKey, hasData=${uiState.hasData} - skipping refresh"
            )
        }
    }

    // If called with refreshOnLaunch=true (e.g. after payment success), force a wallet reload once
    var hasRefreshed by remember { mutableStateOf(false) }
    LaunchedEffect(refreshOnLaunch) {
        if (refreshOnLaunch && !hasRefreshed) {
            hasRefreshed = true
            android.util.Log.d("WalletUI", "RefreshOnLaunch=true - triggering refresh")
            viewModel.loadWallet()
        }
    }

    // Handle payment success with toast
    LaunchedEffect(uiState.payBalanceSuccess) {
        if (uiState.payBalanceSuccess == true) {
            showPayBalancePinModal = false  // Close modal on success
            CustomToast.showSuccess(context, "Payment successful!")
            kotlinx.coroutines.delay(500)
            viewModel.clearPayBalanceState()
        }
    }

    // Handle payment error with toast
    LaunchedEffect(uiState.payBalanceError) {
        if (uiState.payBalanceError != null) {
            CustomToast.showError(context, uiState.payBalanceError ?: "Payment failed")
            kotlinx.coroutines.delay(500)
            viewModel.clearPayBalanceState()
        }
    }

    // Main Scaffold
    Scaffold(
        topBar = { TopAppBar(onBack = onBack) },
        containerColor = Color.White,
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
    ) { paddingValues ->
        // Pull-to-refresh state
        val pullToRefreshState =
            androidx.compose.material3.pulltorefresh.rememberPullToRefreshState()

        // Track if current refresh was initiated by pull-to-refresh
        var isPullRefreshing by remember { mutableStateOf(false) }

        // Trigger refresh when pull-to-refresh is activated
        if (pullToRefreshState.isRefreshing && !isPullRefreshing) {
            LaunchedEffect(true) {
                android.util.Log.d("WalletUI", "Pull-to-refresh triggered by user")
                isPullRefreshing = true
                viewModel.loadWallet()
            }
        }

        // Reset pull-to-refresh state when refresh completes
        if (!uiState.isRefreshing && isPullRefreshing) {
            LaunchedEffect(true) {
                android.util.Log.d("WalletUI", "Pull-to-refresh complete - resetting")
                isPullRefreshing = false
                pullToRefreshState.endRefresh()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            // Show loading, error, or content based on state
            when {
                uiState.isLoading && !uiState.hasData -> {
                    // First Load - Show center loading indicator
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = KabuYellow
                        )
                    }
                }

                uiState.loadError != null && !uiState.hasData -> {
                    // Error State (only if no previous data)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Error",
                                tint = KabuRed,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Failed to load wallet",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = uiState.loadError ?: "Unknown error",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                            Button(
                                onClick = { viewModel.loadWallet() },
                                colors = ButtonDefaults.buttonColors(containerColor = KabuYellow),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Retry", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                else -> {
                    // Content State - Show previous data while refreshing
                    WalletScreenContent(
                        modifier = Modifier.padding(paddingValues),
                        walletBalance = uiState.balance,
                        kabuSubscriptionAmount = uiState.kabuSubscriptionAmount,
                        tripChargeCap = uiState.tripChargeCap,
                        hasSharpDetails = uiState.hasSharpDetails,
                        kabuDueLabel = uiState.kabuDueLabel,
                        sharpDueLabel = uiState.sharpDueLabel,
                        hasPin = uiState.hasPin, // Pass hasPin to conditionally show Create PIN button
                        onTopUpClick = { showTopUpSheet = true },
                        onWithdrawClick = onNavigateToWithdrawal,
                        onMoreOptionsClick = { showMoreOptionsModal = true },
                        onNavigateToPaymentHistory = onNavigateToPaymentHistory,
                        onPayBalanceClick = { showPayBalancePinModal = true },
                        onCreatePinClick = { showCreatePinModal = true })
                }
            }

            // Linear progress indicator at top when refreshing with existing data (from navigation auto-refresh)
            // This is separate from pull-to-refresh indicator
            if (uiState.isRefreshing && !isPullRefreshing) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(paddingValues)
                        .align(Alignment.TopCenter),
                    color = KabuYellow,
                    trackColor = KabuGreyLight
                )
            }

            // Pull-to-refresh indicator - only show when user is actually pulling
            // The PullToRefreshContainer handles its own visibility based on pull state
            // It will show during pull gesture and hide when not pulling
            if (pullToRefreshState.verticalOffset > 0f || isPullRefreshing) {
                androidx.compose.material3.pulltorefresh.PullToRefreshContainer(
                    state = pullToRefreshState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(paddingValues),
                    containerColor = KabuYellow,
                    contentColor = Color.Black
                )
            }
        }


        // The More Options Modal
        if (showMoreOptionsModal) {
            MoreOptionsModal(
                onDismiss = { showMoreOptionsModal = false },
                onChangePinClick = { showChangePinModal = true },
                onResetPinClick = { showResetPinModal = true })
        }

        // The Change PIN Modal
        if (showChangePinModal) {
            ChangePinModalNew(
                onDismiss = { showChangePinModal = false }, viewModel = viewModel
            )
        }

        // The Reset PIN Modal
        if (showResetPinModal) {
            ResetPinModalNew(
                onDismiss = { showResetPinModal = false }, viewModel = viewModel
            )
        }

        // The TopUp Modal
        if (showTopUpSheet) {
            TopUpBottomSheet(
                onDismiss = { showTopUpSheet = false },
                onNavigateToPaymentWebView = onNavigateToPaymentWebView,
                viewModel = viewModel
            )
        }

        // The Pay Balance PIN Modal
        if (showPayBalancePinModal) {
            PayBalancePinModalNew(
                onDismiss = {
                showPayBalancePinModal = false
                viewModel.clearPayBalanceState()
            }, onConfirm = { pin ->
                viewModel.payDuePayment(pin)
                // Don't close modal immediately - let success handler close it
            }, isLoading = uiState.isPayBalanceLoading, error = uiState.payBalanceError
            )
        }

        // The Create PIN Modal
        if (showCreatePinModal) {
            CreatePinModalNew(
                onDismiss = {
                    showCreatePinModal = false
                    viewModel.clearPinState()
                }, viewModel = viewModel
            )
        }
    }
}

// --- 3. Wallet Screen Components ---

@Composable
fun WalletScreenContent(
    modifier: Modifier = Modifier,
    walletBalance: Int = 0,
    kabuSubscriptionAmount: Int = 0,
    tripChargeCap: Int = 0,
    hasSharpDetails: Boolean = false,
    kabuDueLabel: String? = null,
    sharpDueLabel: String? = null,
    hasPin: Boolean = false, // Track if user has PIN
    onTopUpClick: () -> Unit = {},
    onWithdrawClick: () -> Unit,
    onMoreOptionsClick: () -> Unit = {},
    onNavigateToPaymentHistory: () -> Unit = {},
    onPayBalanceClick: () -> Unit = {},
    onCreatePinClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        // Balance Card
        WalletBalanceCard(
            balance = walletBalance,
            hasPin = hasPin,
            onTopUpClick = onTopUpClick,
            onWithdrawClick = onWithdrawClick,
            onMoreOptionsClick = onMoreOptionsClick,
            onCreatePinClick = onCreatePinClick
        )

        if (kabuSubscriptionAmount > 0 && hasPin) {

            // Info
            InfoSection(
                kabuAmount = kabuSubscriptionAmount, minWalletAmount = tripChargeCap
            )

            // Main Action
            Button(
                onClick = onPayBalanceClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KabuYellow),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.wal_black),
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Pay Kabukabu Balance",
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }

        }


        Divider(color = KabuGreyLight)

        if(hasPin){
            NavButton(text = "View payment history", onClick = onNavigateToPaymentHistory)

        }else{
            Surface(
                color = KabuGreyLight,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    InfoItem(
                        "You need to create a PIN to securely use the wallet"
                    )
                    InfoItem(
                        "Minimum of ₦${
                            String.format(
                                "%,d",
                                1000
                            )
                        } has to be in wallet for you to go online"
                    )

                }
            }

        }




        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable

fun TopAppBar(onBack: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
//            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
        Text("Wallet", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.weight(1f))
        Box(modifier = Modifier.size(24.dp))
    }
}

@Composable
fun WalletBalanceCard(
    balance: Int = 0,
    hasPin: Boolean = false,
    onTopUpClick: () -> Unit = {},
    onWithdrawClick: () -> Unit,
    onMoreOptionsClick: () -> Unit = {},
    onCreatePinClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = KabuBlack),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "₦${String.format("%,d", balance)}",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Wallet Balance",
                color = Color.LightGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (!hasPin) {
                // User doesn't have PIN - Show Create PIN button (full width)
                Button(
                    onClick = onCreatePinClick,
                    colors = ButtonDefaults.buttonColors(containerColor = KabuYellow),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create PIN", color = Color.Black, fontWeight = FontWeight.SemiBold)
                }
            } else {
                // User has PIN - Show Top Up, Withdraw, and More Options buttons
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onTopUpClick,
                        colors = ButtonDefaults.buttonColors(containerColor = KabuYellow),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.wal_black),
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Text("  Top up", color = Color.Black)
                    }

                    OutlinedButton(
                        onClick = onWithdrawClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.Gray),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.wal_white),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text("  Withdraw")
                    }

                    OutlinedButton(
                        onClick = onMoreOptionsClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.Gray),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .width(48.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.more_horiz),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoSection(
    kabuAmount: Int = 0, minWalletAmount: Int = 0
) {
    Surface(
        color = KabuGreyLight,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            InfoItem(
                "₦${
                    String.format(
                        "%,d",
                        kabuAmount
                    )
                } to be paid as Kabukabu balance. Payment has to be made before you can go online"
            )
            InfoItem(
                "Minimum of ₦${
                    String.format(
                        "%,d",
                        minWalletAmount
                    )
                } has to be in wallet for you to go online"
            )

        }
    }
}

@Composable
fun InfoItem(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            Icons.Default.Info,
            null,
            tint = Color.Gray,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, color = Color.DarkGray, fontSize = 13.sp, lineHeight = 18.sp)
    }
}

@Composable
fun RepairRepaymentCard(
    kabuAmount: Int = 0,
    kabuDueLabel: String? = null,
    tripCapAmount: Int = 0,
    hasSharpDetails: Boolean = false,
    sharpDueLabel: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Kabukabu Subscription Payment
        if (kabuAmount > 0) {
            Surface(
                color = KabuGreyLight,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = KabuYellow,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Kabukabu Subscription",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "₦${
                            String.format(
                                "%,d",
                                kabuAmount
                            )
                        }${kabuDueLabel?.let { " $it" } ?: ""}",
                            fontSize = 13.sp,
                            color = Color.DarkGray)
                    }
                }
            }
        }

        // Trip Charge Cap
        if (tripCapAmount > 0) {
            Surface(
                color = KabuGreyLight,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = KabuYellow,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Trip Charge Cap",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "₦${String.format("%,d", tripCapAmount)}",
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }

        // Sharp Payment (if exists)
        if (hasSharpDetails) {
            Surface(
                color = KabuGreyLight,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = KabuPurple,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Sharp Payment",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 14.sp
                        )
                        Text(
                            text = sharpDueLabel ?: "Ongoing program",
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NavButton(text: String, onClick: () -> Unit = {}) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = KabuGreyLight),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text,
                color = Color.Black,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.Gray)
        }
    }
}


@Composable
fun InputLabel(text: String) {
    Text(text, fontSize = 14.sp, fontWeight = FontWeight.W500)
}


@Composable
fun KeypadButton(symbol: String, onClick: (String) -> Unit) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .safeClickable { onClick(symbol) },
        contentAlignment = Alignment.Center
    ) {
        if (symbol == ">") {
            Icon(Icons.Default.Clear, null, modifier = Modifier.size(32.dp))
        } else {
            Text(symbol, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreOptionsModal(
    onDismiss: () -> Unit, onChangePinClick: () -> Unit = {}, onResetPinClick: () -> Unit = {}
) {
    StackedBottomSheet(
        onDismiss = onDismiss, heightFraction = 0.48f // Smaller height for this modal
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "More options",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F1F1F)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(KabuRed.copy(alpha = 0.1f), CircleShape)
                        .size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = KabuRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Options List
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // Change PIN Option
                OptionItem(
                    id = R.drawable.lock,
                    icon = Icons.Default.Lock,
                    iconColor = KabuPurple,
                    title = "Change PIN",
                    subtitle = "Update your existing security PIN",
                    onClick = {
                        onDismiss()
                        onChangePinClick()
                    })

                // Reset PIN Option
                OptionItem(
                    id = R.drawable.shield,
                    icon = Icons.Default.Lock,
                    iconColor = KabuBlack,
                    title = "Reset PIN",
                    subtitle = "Create a new PIN if you've forgotten your current one.",
                    onClick = {
                        onDismiss()
                        onResetPinClick()
                    })
            }
        }
    }
}

@Composable
fun OptionItem(
    id: Int,
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = KabuGreyLight),
        modifier = Modifier
            .fillMaxWidth()
            .composableSafeClickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Icon(
                painter = painterResource(id = id),
//                    imageVector = icon,
                contentDescription = null,
//                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.W700,
                    color = Color.Black
                )
                Text(
                    text = subtitle,
                    fontWeight = FontWeight.W400,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// --- Change PIN Modal with 3-Step Flow ---
@Composable
fun ChangePinModal(
    onDismiss: () -> Unit, viewModel: WalletViewModel
) {
    // State management for 3-step flow
    var step by remember { mutableIntStateOf(1) } // 1 = Old PIN, 2 = New PIN, 3 = Confirm PIN
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Collect UI state
    val uiState by viewModel.uiState.collectAsState()

    // Handle success
    LaunchedEffect(uiState.pinSuccess) {
        if (uiState.pinSuccess != null) {
            // Show success and close after delay
            kotlinx.coroutines.delay(1500)
            viewModel.clearPinState()
            onDismiss()
        }
    }

    // Handle error from ViewModel
    LaunchedEffect(uiState.pinError) {
        if (uiState.pinError != null) {
            errorMessage = uiState.pinError
        }
    }

    StackedBottomSheet(
        onDismiss = {
            viewModel.clearPinState()
            onDismiss()
        }, heightFraction = 0.75f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 12.dp)
        ) {
            // Header
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Close button aligned to the right
                IconButton(
                    onClick = {
                        viewModel.clearPinState()
                        onDismiss()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .background(KabuRed.copy(alpha = 0.1f), CircleShape)
                        .size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = KabuRed,
                        modifier = Modifier.size(16.dp)
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
                        text = "Change PIN",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F1F1F)
                    )
                    Text(
                        text = when (step) {
                            1 -> "Enter old pin"
                            2 -> "Enter new pin"
                            3 -> "Confirm new pin"
                            else -> ""
                        }, fontSize = 14.sp, color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Success message
            if (uiState.pinSuccess != null) {
                Surface(
                    color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.pinSuccess ?: "",
                            color = Color(0xFF4CAF50),
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Error message
            if (errorMessage != null) {
                Surface(
                    color = KabuRed.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = KabuRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "", color = KabuRed, fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // PIN Entry based on current step
            ChangePinStepContent(
                step = step, currentPin = when (step) {
                1 -> oldPin
                2 -> newPin
                3 -> confirmPin
                else -> ""
            }, isLoading = uiState.isPinLoading, onPinChange = { pin ->
                errorMessage = null // Clear error when user types
                when (step) {
                    1 -> oldPin = pin
                    2 -> newPin = pin
                    3 -> confirmPin = pin
                }
            }, onContinue = {
                when (step) {
                    1 -> {
                        // Move to next step - enter new PIN
                        errorMessage = null
                        step = 2
                    }

                    2 -> {
                        // Validate new PIN requirements
                        if (newPin.length != 4) {
                            errorMessage = "PIN must be 4 digits"
                        } else if (newPin == oldPin) {
                            errorMessage = "New PIN must be different from old PIN"
                        } else {
                            errorMessage = null
                            step = 3
                        }
                    }

                    3 -> {
                        // Validate confirm PIN matches new PIN
                        if (confirmPin != newPin) {
                            errorMessage = "PINs don't match"
                        } else {
                            // Submit to server
                            errorMessage = null
                            viewModel.changePin(oldPin, newPin)
                        }
                    }
                }
            })
        }
    }
}

@Composable
fun ChangePinStepContent(
    step: Int,
    currentPin: String,
    isLoading: Boolean = false,
    onPinChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    val maxPinLength = 4

    // Blinking cursor animation
    var cursorVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            cursorVisible = !cursorVisible
        }
    }

    fun onNumClick(num: String) {
        if (currentPin.length < maxPinLength) {
            onPinChange(currentPin + num)
        }
    }

    fun onDeleteClick() {
        if (currentPin.isNotEmpty()) {
            onPinChange(currentPin.dropLast(1))
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Display Boxes
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            repeat(4) { index ->
                val isActive = index == currentPin.length
                val hasPinDigit = index < currentPin.length

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(KabuGreyBg, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        hasPinDigit -> {
                            Text(
                                text = currentPin[index].toString(),
                                color = Color.Black,
                                fontSize = 20.sp,
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

        Spacer(Modifier.height(8.dp))

        // Keypad
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf(".", "0", ">")
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            keys.forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    row.forEach { key ->
                        KeypadButton(key) { clickedKey ->
                            when (clickedKey) {
                                ">" -> onDeleteClick()
                                "." -> { /* Do nothing */
                                }

                                else -> onNumClick(clickedKey)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Continue/Change PIN Button
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = KabuYellow),
            shape = RoundedCornerShape(12.dp),
            enabled = currentPin.length == maxPinLength && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp), color = Color.Black
                )
            } else {
                Text(
                    text = if (step == 3) "Change PIN" else "Continue",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun CreatePinModal(
    onDismiss: () -> Unit, viewModel: WalletViewModel
) {
    // State management for 2-step flow
    var step by remember { mutableIntStateOf(1) } // 1 = New PIN, 2 = Confirm PIN
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Collect UI state
    val uiState by viewModel.uiState.collectAsState()

    // Show toast for client-side validation errors
    fun showError(message: String) {
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
    }

    // Handle success
    LaunchedEffect(uiState.pinSuccess) {
        if (uiState.pinSuccess != null) {
            // Show success toast
            android.widget.Toast.makeText(
                context,
                uiState.pinSuccess,
                android.widget.Toast.LENGTH_SHORT
            ).show()
            kotlinx.coroutines.delay(1500)
            viewModel.clearPinState()
            onDismiss()
        }
    }

    // Handle error from ViewModel (server errors)
    LaunchedEffect(uiState.pinError) {
        if (uiState.pinError != null) {
            android.widget.Toast.makeText(
                context,
                uiState.pinError,
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    StackedBottomSheet(
        onDismiss = {
            viewModel.clearPinState()
            onDismiss()
        }, heightFraction = 0.75f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 12.dp)
        ) {
            // Header
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Close button aligned to the right
                IconButton(
                    onClick = {
                        viewModel.clearPinState()
                        onDismiss()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .background(KabuRed.copy(alpha = 0.1f), CircleShape)
                        .size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = KabuRed,
                        modifier = Modifier.size(16.dp)
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
                        text = "Create PIN",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F1F1F)
                    )
                    Text(
                        text = when (step) {
                            1 -> "Enter new pin"
                            2 -> "Confirm new pin"
                            else -> ""
                        }, fontSize = 14.sp, color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))


            // PIN Entry based on current step
            CreatePinStepContent(
                step = step, currentPin = when (step) {
                1 -> newPin
                2 -> confirmPin
                else -> ""
            }, isLoading = uiState.isPinLoading, onPinChange = { pin ->
                when (step) {
                    1 -> newPin = pin
                    2 -> confirmPin = pin
                }
            }, onContinue = {
                when (step) {
                    1 -> {
                        // Validate new PIN requirements
                        if (newPin.length != 4) {
                            showError("PIN must be 4 digits")
                        } else {
                            step = 2
                        }
                    }

                    2 -> {
                        // Validate confirm PIN matches new PIN
                        if (confirmPin != newPin) {
                            showError("PINs don't match")
                        } else {
                            // Submit to server
                            viewModel.createPin(newPin)
                        }
                    }
                }
            })
        }
    }
}

@Composable
fun CreatePinStepContent(
    step: Int,
    currentPin: String,
    isLoading: Boolean = false,
    onPinChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    val maxPinLength = 4
    var cursorVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            cursorVisible = !cursorVisible
        }
    }

    fun onNumClick(num: String) {
        if (currentPin.length < maxPinLength) {
            onPinChange(currentPin + num)
        }
    }

    fun onDeleteClick() {
        if (currentPin.isNotEmpty()) {
            onPinChange(currentPin.dropLast(1))
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // PIN Display Boxes
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            repeat(4) { index ->
                val isActive = index == currentPin.length
                val hasPinDigit = index < currentPin.length

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0xFFF2F2F2), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        hasPinDigit -> {
                            Text(
                                text = currentPin[index].toString(),
                                color = Color.Black,
                                fontSize = 20.sp,
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

        Spacer(Modifier.height(8.dp))

        // Keypad
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf(".", "0", ">")
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            keys.forEach { row ->
                Row(
                    Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround
                ) {
                    row.forEach { key ->
                        KeypadButton(key) { clickedKey ->
                            when (clickedKey) {
                                ">" -> onDeleteClick()
                                "." -> { /* Do nothing */
                                }

                                else -> onNumClick(clickedKey)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Continue/Create Button
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = KabuYellow),
            shape = RoundedCornerShape(12.dp),
            enabled = currentPin.length == maxPinLength && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp), color = Color.Black
                )
            } else {
                Text(
                    text = if (step == 2) "Create PIN" else "Continue",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPinModal(
    onDismiss: () -> Unit, viewModel: WalletViewModel
) {
    val context = LocalContext.current

    // State for the input fields
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var otpRequested by remember { mutableStateOf(false) }

    // Collect UI state
    val uiState by viewModel.uiState.collectAsState()

    // Request OTP when modal opens
    LaunchedEffect(Unit) {
        if (!otpRequested) {
            viewModel.requestPinReset()
            otpRequested = true
        }
    }

    // Handle success with toast
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

    // Handle error with toast
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
        }, heightFraction = 0.75f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 12.dp)
        ) {
            // Header (Title and Close Button)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Reset PIN",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F1F1F)
                    )
                    Text(
                        text = "Create a new PIN for your wallet",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                IconButton(
                    onClick = {
                        viewModel.clearPinState()
                        onDismiss()
                    },
                    modifier = Modifier
                        .background(KabuRed.copy(alpha = 0.1f), CircleShape)
                        .size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = KabuRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Input Fields ---
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // OTP Field (first since it's sent automatically)
                CustomPinInputField(
                    label = "Input OTP (sent to your phone)",
                    value = otp,
                    onValueChange = { otp = it },
                    placeholder = "xxxx",
                    isSecure = false
                )

                // New PIN Field
                CustomPinInputField(
                    label = "New 4-Digit PIN",
                    value = newPin,
                    onValueChange = { newPin = it },
                    placeholder = "xxxx",
                    isSecure = true
                )

                // Confirm PIN Field
                CustomPinInputField(
                    label = "Confirm New 4-Digit PIN",
                    value = confirmPin,
                    onValueChange = { confirmPin = it },
                    placeholder = "xxxx",
                    isSecure = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button
            Button(
                onClick = {
                    // Validate inputs
                    when {
                        otp.isEmpty() -> {
                            CustomToast.showError(context, "Please enter OTP")
                        }

                        otp.length < 4 -> {
                            CustomToast.showError(context, "Please enter valid OTP")
                        }

                        newPin.isEmpty() -> {
                            CustomToast.showError(context, "Please enter new PIN")
                        }

                        newPin.length != 4 -> {
                            CustomToast.showError(context, "PIN must be 4 digits")
                        }

                        confirmPin.isEmpty() -> {
                            CustomToast.showError(context, "Please confirm new PIN")
                        }

                        confirmPin != newPin -> {
                            CustomToast.showError(context, "PINs don't match")
                        }

                        else -> {
                            // All validations passed, call API
                            viewModel.validatePinReset(otp, newPin)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KabuYellow),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isPinLoading
            ) {
                if (uiState.isPinLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp), color = Color.Black
                    )
                } else {
                    Text(
                        text = "Reset PIN",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

// --- Reusable Input Component ---

@Composable
fun CustomPinInputField(
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

        // Custom Styled TextField
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp), // Ensure a decent height
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
            keyboardOptions = KeyboardOptions(keyboardType = if (isSecure) KeyboardType.NumberPassword else KeyboardType.Number)
        )
    }
}

// --- TopUp Bottom Sheet ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpBottomSheet(
    onDismiss: () -> Unit, onNavigateToPaymentWebView: (String) -> Unit, viewModel: WalletViewModel
) {
    val context = LocalContext.current
    var amount by remember { mutableStateOf("") }

    // Collect UI state
    val uiState by viewModel.uiState.collectAsState()

    // Handle success - when topup URL is generated
    LaunchedEffect(uiState.topupUrl) {
        if (uiState.topupUrl != null) {
            // Navigate to WebView with payment URL
            onNavigateToPaymentWebView(uiState.topupUrl!!)
            viewModel.clearTopupResult()
            onDismiss()
        }
    }

    // Handle error from ViewModel - show toast
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            CustomToast.showError(context, uiState.errorMessage ?: "Top-up failed")
        }
    }

    StackedBottomSheet(
        onDismiss = {
            viewModel.clearTopupResult()
            onDismiss()
        }, heightFraction = 0.5f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Top Up",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.W700,
                        color = Color(0xFF1F1F1F)
                    )
                    Text(
                        text = "Add money to your wallet",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W500,
                        color = Color.Black
                    )
                }
                IconButton(
                    onClick = {
                        viewModel.clearTopupResult()
                        onDismiss()
                    },
                    modifier = Modifier
                        .background(KabuRed.copy(alpha = 0.1f), CircleShape)
                        .size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = KabuRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Amount Input Field
            TopUpForm(amount = amount, onAmountChange = { newValue ->
                // Remove commas and validate it's a number
                val cleanValue = newValue.replace(",", "")
                if (cleanValue.isEmpty() || cleanValue.all { it.isDigit() }) {
                    // Format with commas
                    amount = formatAmountWithComma(cleanValue)
                }
            }, isLoading = uiState.isTopupLoading, onContinue = {
                // Remove commas from amount for validation
                val cleanAmount = removeCommasFromAmount(amount)

                // Validate input
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

                    cleanAmount.toInt() < 100 -> {
                        CustomToast.showError(context, "Minimum top-up amount is ₦100")
                    }

                    else -> {
                        // All validations passed, call API with clean amount
                        viewModel.initiateTopup(cleanAmount.toInt())
                    }
                }
            })
        }
    }
}

@Composable
fun TopUpForm(
    amount: String, onAmountChange: (String) -> Unit, isLoading: Boolean, onContinue: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InputLabel("Amount")

        // Amount Input Field
        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter amount", color = Color.Gray) },
            singleLine = true,
            leadingIcon = {
                Text(
                    text = "₦", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = KabuGreyBg,
                unfocusedContainerColor = KabuGreyBg,
                disabledContainerColor = KabuGreyBg,
                focusedBorderColor = KabuYellow,
                unfocusedBorderColor = Color.Transparent,
            ),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Continue Button
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = KabuYellow),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp), color = Color.Black
                )
            } else {
                Text(
                    "Continue",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun PayBalancePinModal(
    onDismiss: () -> Unit, onConfirm: (String) -> Unit, isLoading: Boolean, error: String? = null
) {
    var pin by remember { mutableStateOf("") }
    val maxPinLength = 4
    var cursorVisible by remember { mutableStateOf(true) }
    val context = LocalContext.current

    // Show toast when error occurs
    LaunchedEffect(error) {
        if (error != null) {
            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            cursorVisible = !cursorVisible
        }
    }

    fun onNumClick(num: String) {
        if (pin.length < maxPinLength) pin += num
    }

    fun onDeleteClick() {
        if (pin.isNotEmpty()) {
            pin = pin.dropLast(1)
        }
    }

    StackedBottomSheet(
        onDismiss = onDismiss, heightFraction = 0.75f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Enter PIN",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F1F1F)
                    )
                    Text(
                        text = "Input pin to confirm payment", fontSize = 14.sp, color = Color.Gray
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(KabuRed.copy(alpha = 0.1f), CircleShape)
                        .size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = KabuRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Display Boxes
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                repeat(4) { index ->
                    val isActive = index == pin.length
                    val hasPinDigit = index < pin.length

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFFF2F2F2), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            hasPinDigit -> {
                                Text(
                                    text = pin[index].toString(),
                                    color = Color.Black,
                                    fontSize = 20.sp,
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

            Spacer(Modifier.height(8.dp))

            // Keypad
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf(".", "0", ">")
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                keys.forEach { row ->
                    Row(
                        Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        row.forEach { key ->
                            KeypadButton(key) { clickedKey ->
                                when (clickedKey) {
                                    ">" -> onDeleteClick()
                                    "." -> { /* Do nothing */
                                    }

                                    else -> onNumClick(clickedKey)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Pay Button
            Button(
                onClick = { onConfirm(pin) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KabuYellow),
                shape = RoundedCornerShape(12.dp),
                enabled = pin.length == maxPinLength && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp), color = Color.Black
                    )
                } else {
                    Text(
                        "Pay Balance",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

