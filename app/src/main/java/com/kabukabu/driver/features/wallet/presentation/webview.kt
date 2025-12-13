package com.kabukabu.driver.features.wallet.presentation

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun PaymentWebViewScreen(
    paymentUrl: String,
    onPaymentSuccess: () -> Unit,
    onPaymentCancelled: () -> Unit,
    onBack: () -> Unit,
) {
    var isLoading by remember { mutableStateOf(true) }
    var loadProgress by remember { mutableIntStateOf(0) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            // Custom TopAppBar matching wallet screen design
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .windowInsetsPadding(WindowInsets.statusBars),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        "Back",
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "Complete Payment",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(modifier = Modifier.size(24.dp))
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // WebView
            PaymentWebView(
                url = paymentUrl,
                onLoadingChanged = { loading ->
                    isLoading = loading
                },
                onProgressChanged = { progress ->
                    loadProgress = progress
                },
                onPaymentSuccess = {
                    // Do not call viewModel.loadWallet() here; let the Wallet screen refresh
                    onPaymentSuccess()
                },
                onPaymentCancelled = onPaymentCancelled,
                onError = { error ->
                    hasError = true
                    errorMessage = error
                    isLoading = false
                }
            )

            // Loading Progress Bar
            if (isLoading && loadProgress < 100) {
                LinearProgressIndicator(
                    progress = { loadProgress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = Color(0xFFF4C446)
                )
            }

            // Loading Indicator
            if (isLoading && loadProgress == 0) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFF4C446)
                )
            }

            // Error State
            if (hasError) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Error",
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Payment Error",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF4C446)
                            )
                        ) {
                            Text("Go Back", color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PaymentWebView(
    url: String,
    onLoadingChanged: (Boolean) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onPaymentSuccess: () -> Unit,
    onPaymentCancelled: () -> Unit,
    onError: (String) -> Unit
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                // Enable JavaScript
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.builtInZoomControls = false
                settings.displayZoomControls = false
                settings.setSupportZoom(false)

                // Set WebView Client
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        onLoadingChanged(true)
                        android.util.Log.d("PaymentWebView", "Page started loading: $url")
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        onLoadingChanged(false)
                        android.util.Log.d("PaymentWebView", "Page finished loading: $url")
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val url = request?.url?.toString() ?: ""
                        android.util.Log.d("PaymentWebView", "Navigation request: $url")

                        // Check for success indicators
                        if (url.contains("success", ignoreCase = true) ||
                            url.contains("flw-callback", ignoreCase = true) ||
                            url.contains("callback", ignoreCase = true) ||
                            url.contains("completed", ignoreCase = true)
                        ) {
                            android.util.Log.d("PaymentWebView", "Payment success detected")
                            onPaymentSuccess()
                            return true // Prevent navigation
                        }

                        // Check for cancellation/failure
                        if (url.contains("cancel", ignoreCase = true) ||
                            url.contains("failed", ignoreCase = true) ||
                            url.contains("error", ignoreCase = true)
                        ) {
                            android.util.Log.d("PaymentWebView", "Payment cancelled/failed")
                            onPaymentCancelled()
                            return true // Prevent navigation
                        }

                        // Allow navigation for other URLs
                        return false
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        val errorMsg = error?.description?.toString() ?: "Unknown error occurred"
                        android.util.Log.e("PaymentWebView", "Error: $errorMsg")
                        onError(errorMsg)
                    }
                }

                // Set WebChrome Client for progress updates
                webChromeClient = object : android.webkit.WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        onProgressChanged(newProgress)
                        android.util.Log.d("PaymentWebView", "Progress: $newProgress%")
                    }
                }

                // Load the payment URL
                loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

// Extension function to add to navigation
// Add this to your navigation graph or composable navigation setup
/*
Usage Example:

// In your TopUp modal or wherever you handle the payment URL
if (uiState.topupUrl != null) {
    PaymentWebViewScreen(
        paymentUrl = uiState.topupUrl!!,
        onPaymentSuccess = {
            // Payment completed successfully
            Toast.makeText(context, "Payment Successful!", Toast.LENGTH_LONG).show()
            // Navigate back to wallet
            navController.popBackStack()
        },
        onPaymentCancelled = {
            // Payment was cancelled
            Toast.makeText(context, "Payment Cancelled", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        },
        onBack = {
            // User pressed back button
            navController.popBackStack()
        }
    )
}
*/
