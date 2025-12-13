package com.kabukabu.driver.features.wallet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.core.data.remote.ApiClient
import com.kabukabu.driver.core.data.remote.TransactionApiClient
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class WalletViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState

    /**
     * Parse ISO date string and return "due Mon", "due Tue", etc.
     * Compatible with API 24+
     */
    private fun parseDueDateLabel(iso: String): String? {
        return try {
            // Try parsing ISO 8601 format (e.g., "2025-12-15T10:30:00.000Z")
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("EEE", Locale.getDefault())

            // Handle both with and without milliseconds/timezone
            val dateStr = iso.substringBefore(".")  // Remove milliseconds if present
            val date = inputFormat.parse(dateStr)

            if (date != null) {
                "due ${outputFormat.format(date)}"
            } else {
                null
            }
        } catch (e: Exception) {
            // Fallback: try parsing just the date part
            try {
                val simpleFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("EEE", Locale.getDefault())
                val date = simpleFormat.parse(iso.substring(0, 10))
                if (date != null) {
                    "due ${outputFormat.format(date)}"
                } else {
                    null
                }
            } catch (e2: Exception) {
                null
            }
        }
    }
    init {
        val viewModelId = System.identityHashCode(this).toString(16)
        android.util.Log.d("WalletViewModel", "ViewModel CREATED with ID: $viewModelId")
        loadWallet()
        checkHasPin() // Check if user has PIN to show/hide Create PIN button
    }

    override fun onCleared() {
        val viewModelId = System.identityHashCode(this).toString(16)
        android.util.Log.d("WalletViewModel", "ViewModel CLEARED with ID: $viewModelId")
        super.onCleared()
    }

    /**
     * Extract error message from backend JSON response
     * Expected format: {"status":"error","code":401,"message":"Wrong Pin Code Inputted"}
     * @param errorBody The error response body
     * @param fallback Fallback message if parsing fails
     * @return Extracted error message or fallback
     */
    private fun parseErrorMessage(errorBody: okhttp3.ResponseBody?, fallback: String): String {
        return try {
            val errorString = errorBody?.string() ?: return fallback
            val json = JSONObject(errorString)
            json.optString("message", fallback)
        } catch (e: Exception) {
            fallback
        }
    }

    fun loadWallet() {
        viewModelScope.launch {
            val hasData = _uiState.value.hasData
            android.util.Log.d("WalletViewModel", "loadWallet called - hasData: $hasData")

            // Set loading state based on whether we have data
            // isLoading = true only for first load
            // isRefreshing = true for subsequent refreshes
            if (_uiState.value.hasData) {
                android.util.Log.d("WalletViewModel", "Setting isRefreshing=true (refresh)")
                _uiState.value = _uiState.value.copy(isRefreshing = true, loadError = null)
            } else {
                android.util.Log.d("WalletViewModel", "Setting isLoading=true (first load)")
                _uiState.value = _uiState.value.copy(isLoading = true, loadError = null)
            }

            try {
                val prefs = KabukabuDriverApp.getInstance().userPreferences
                val token = prefs.authToken.first() ?: ""
                val userId = prefs.userId.first() ?: ""
                if (token.isBlank() || userId.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loadError = "Authentication required"
                    )
                    return@launch
                }

                val bearer = "Bearer $token"
                val res = ApiClient.rideService.getDuePayment(bearerToken = bearer, userId = userId)
                val data = res.data

                val balance = data?.walletBalance ?: 0
                val kabuSub = data?.subscriptionPayment.toIntSafe()
                val tripCap = data?.tripChargeCap.toIntSafe()
                val hasSharp = data?.ongoingSharpProgram != null

                val subDueLabel = data?.subDueDate?.let { iso ->
                    parseDueDateLabel(iso)
                }
                val sharpDueLabel = data?.sharpDueDate?.let { iso ->
                    parseDueDateLabel(iso)
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    loadError = null,
                    hasData = true, // Mark that we have loaded data
                    balance = balance,
                    kabuSubscriptionAmount = kabuSub,
                    tripChargeCap = tripCap,
                    hasSharpDetails = hasSharp,
                    kabuDueLabel = subDueLabel,
                    sharpDueLabel = sharpDueLabel
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    loadError = e.message ?: "Failed to load wallet data"
                )
            }
        }
    }

    fun initiateTopup(amount: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isTopupLoading = true, errorMessage = null, topupUrl = null)
                val prefs = KabukabuDriverApp.getInstance().userPreferences
                val token = prefs.authToken.first() ?: ""
                val userId = prefs.userId.first() ?: ""
                val userDetails = prefs.userDetails.first()

                // Get email, fullname, and phone from userDetails or fallback to stored values
                val email = userDetails?.user?.email ?: prefs.userEmail.first() ?: "emmaprechi@gmail.com"
                val fullName = userDetails?.user?.fullName ?: prefs.fullName.first() ?: ""
                val phoneNumber = userDetails?.user?.phoneNumber ?: ""

                if (token.isBlank() || userId.isBlank()) {
                    _uiState.value = _uiState.value.copy(isTopupLoading = false, errorMessage = "Missing auth")
                    return@launch
                }

                val bearer = "Bearer $token"
                // Generate UUID for clientRef
                val clientRef = java.util.UUID.randomUUID().toString()

                val payload = mapOf(
                    "amount" to amount,
                    "clientRef" to clientRef,
                    "userId" to userId,
                    "email" to email,
                    "fullname" to fullName,
                    "phone" to phoneNumber
                )

                val res = TransactionApiClient.service.generateTopupLink(bearer, userId, payload)
                if (res.isSuccessful) {
                    val body = res.body()?.string() ?: ""
                    // Try to extract a URL from response body; backend may return JSON
                    val url = Regex("https?://[\\w./?=&%-]+", RegexOption.IGNORE_CASE).find(body)?.value
                    _uiState.value = _uiState.value.copy(isTopupLoading = false, topupUrl = url)
                } else {
                    val errorMessage = parseErrorMessage(res.errorBody(), "Topup failed")
                    _uiState.value = _uiState.value.copy(isTopupLoading = false, errorMessage = errorMessage)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isTopupLoading = false, errorMessage = e.message ?: "Topup error")
            }
        }
    }

    private fun Any?.toIntSafe(): Int {
        return when (this) {
            null -> 0
            is Int -> this
            is Long -> this.toInt()
            is Double -> this.toInt()
            is Float -> this.toInt()
            is String -> this.toIntOrNull() ?: 0
            else -> 0
        }
    }

    fun clearTopupResult() {
        _uiState.value = _uiState.value.copy(topupUrl = null, errorMessage = null, isTopupLoading = false)
    }

    fun confirmAccount(accountNumber: String, bankCode: String, bankName: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isWithdrawLoading = true, withdrawError = null, accountName = null)
                val prefs = KabukabuDriverApp.getInstance().userPreferences
                val token = prefs.authToken.first() ?: ""
                val userId = prefs.userId.first() ?: ""
                if (token.isBlank() || userId.isBlank()) {
                    _uiState.value = _uiState.value.copy(isWithdrawLoading = false, withdrawError = "Missing auth")
                    return@launch
                }
                val bearer = "Bearer $token"
                val payload = mapOf(
                    "account_number" to accountNumber,
                    "bank_code" to bankCode,
                    "account_bank" to bankName
                )
                val res = TransactionApiClient.service.confirmBankAccount(bearer, userId, payload)
                if (res.isSuccessful) {
                    val body = res.body()?.string() ?: "{}"
                    // Response structure: { data: { account_name: "...", account_number: "..." } }
                    val root = JSONObject(body)
                    val data = root.optJSONObject("data")
                    val accountName = data?.optString("account_name") ?: ""
                    android.util.Log.d("WalletViewModel", "Account confirmed: $accountName")
                    _uiState.value = _uiState.value.copy(isWithdrawLoading = false, accountName = accountName)
                } else {
                    val errorMessage = parseErrorMessage(res.errorBody(), "Unable to confirm account")
                    _uiState.value = _uiState.value.copy(isWithdrawLoading = false, withdrawError = errorMessage)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isWithdrawLoading = false, withdrawError = e.message ?: "Confirm error")
            }
        }
    }

    fun withdraw(amount: Int, accountNumber: String, bankCode: String, pin: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isWithdrawLoading = true, withdrawError = null, withdrawSuccess = null)
                val prefs = KabukabuDriverApp.getInstance().userPreferences
                val token = prefs.authToken.first() ?: ""
                val userId = prefs.userId.first() ?: ""
                if (token.isBlank() || userId.isBlank()) {
                    _uiState.value = _uiState.value.copy(isWithdrawLoading = false, withdrawError = "Missing auth")
                    return@launch
                }
                val bearer = "Bearer $token"
                val payload = mapOf(
                    "amount" to amount,
                    "account_number" to accountNumber,
                    "account_bank" to bankCode,
                    "pin" to pin
                )
                android.util.Log.d("WalletViewModel", "Withdrawing ₦$amount to account $accountNumber")
                val res = TransactionApiClient.service.withdrawFromWallet(bearer, userId, payload)
                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isWithdrawLoading = false, withdrawSuccess = true)
                } else {
                    val errorMessage = parseErrorMessage(res.errorBody(), "Withdraw failed")
                    _uiState.value = _uiState.value.copy(isWithdrawLoading = false, withdrawError = errorMessage)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isWithdrawLoading = false, withdrawError = e.message ?: "Withdraw error")
            }
        }
    }

    fun clearWithdrawState() {
        _uiState.value = _uiState.value.copy(isWithdrawLoading = false, withdrawError = null, withdrawSuccess = null, accountName = null)
    }

//    payBalance

    data class BankItem(val name: String, val code: String)

    fun fetchBanks() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isBanksLoading = true, banksError = null)
                val prefs = KabukabuDriverApp.getInstance().userPreferences
                val token = prefs.authToken.first() ?: ""
                val userId = prefs.userId.first() ?: ""
                if (token.isBlank() || userId.isBlank()) {
                    _uiState.value = _uiState.value.copy(isBanksLoading = false, banksError = "Missing auth")
                    return@launch
                }
                val bearer = "Bearer $token"
                val res = TransactionApiClient.service.fetchBanks(bearer, userId)
                if (res.isSuccessful) {
                    val body = res.body()?.string() ?: "{}"
                    val root = JSONObject(body)
                    // Response structure: { data: { data: { rows: [...] } } }
                    val outerData = root.optJSONObject("data")
                    val innerData = outerData?.optJSONObject("data")
                    val rows = innerData?.optJSONArray("rows")
                    val banks = mutableListOf<BankItem>()
                    if (rows != null) {
                        for (i in 0 until rows.length()) {
                            val obj = rows.optJSONObject(i)
                            val name = obj?.optString("name") ?: ""
                            // Use cbnCode as the bank code
                            val code = obj?.optString("cbnCode") ?: ""
                            if (name.isNotBlank() && code.isNotBlank()) {
                                banks.add(BankItem(name, code))
                            }
                        }
                    }
                    android.util.Log.d("WalletViewModel", "Parsed ${banks.size} banks from API")
                    _uiState.value = _uiState.value.copy(isBanksLoading = false, banks = banks)
                } else {
                    val errorMessage = parseErrorMessage(res.errorBody(), "Failed to load banks")
                    _uiState.value = _uiState.value.copy(isBanksLoading = false, banksError = errorMessage)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isBanksLoading = false, banksError = e.message ?: "Banks error")
            }
        }
    }

    fun checkHasPin() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isPinLoading = true, pinError = null)
                val prefs = KabukabuDriverApp.getInstance().userPreferences
                val token = prefs.authToken.first() ?: ""
                val userId = prefs.userId.first() ?: ""
                if (token.isBlank() || userId.isBlank()) {
                    _uiState.value = _uiState.value.copy(isPinLoading = false, pinError = "Missing auth")
                    return@launch
                }
                val bearer = "Bearer $token"
                val res = TransactionApiClient.service.hasPin(bearer, userId)
                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isPinLoading = false, hasPin = true)
                } else {
                    // treat non-200 as no pin
                    _uiState.value = _uiState.value.copy(isPinLoading = false, hasPin = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isPinLoading = false, pinError = e.message ?: "PIN check error")
            }
        }
    }

    fun createPin(newPin: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isPinLoading = true, pinError = null, pinSuccess = null)
                val prefs = KabukabuDriverApp.getInstance().userPreferences
                val token = prefs.authToken.first() ?: ""
                val userId = prefs.userId.first() ?: ""
                val bearer = "Bearer $token"
                val res = TransactionApiClient.service.createPin(bearer, userId, mapOf("new_pin" to newPin))
                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isPinLoading = false, pinSuccess = "PIN created", hasPin = true)
                } else {
                    val errorMessage = parseErrorMessage(res.errorBody(), "PIN create failed")
                    _uiState.value = _uiState.value.copy(isPinLoading = false, pinError = errorMessage)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isPinLoading = false, pinError = e.message ?: "PIN create error")
            }
        }
    }

    fun changePin(oldPin: String, newPin: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isPinLoading = true, pinError = null, pinSuccess = null)
                val prefs = KabukabuDriverApp.getInstance().userPreferences
                val token = prefs.authToken.first() ?: ""
                val userId = prefs.userId.first() ?: ""
                val bearer = "Bearer $token"
                val res = TransactionApiClient.service.changePin(bearer, userId, mapOf("old_pin" to oldPin, "new_pin" to newPin))
                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isPinLoading = false, pinSuccess = "PIN updated")
                } else {
                    val errorMessage = parseErrorMessage(res.errorBody(), "PIN change failed")
                    _uiState.value = _uiState.value.copy(isPinLoading = false, pinError = errorMessage)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isPinLoading = false, pinError = e.message ?: "PIN change error")
            }
        }
    }


    fun payDuePayment(pin: String,) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isPayBalanceLoading = true, payBalanceError = null, payBalanceSuccess = null)
                val prefs = KabukabuDriverApp.getInstance().userPreferences
                val token = prefs.authToken.first() ?: ""
                val userId = prefs.userId.first() ?: ""
                if (token.isBlank() || userId.isBlank()) {
                    _uiState.value = _uiState.value.copy(isPayBalanceLoading = false, payBalanceError = "Missing auth")
                    return@launch
                }
                val bearer = "Bearer $token"
                val res = ApiClient.rideService.payDuePayment(bearer, userId, pin)
                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isPayBalanceLoading = false, payBalanceSuccess = true)
                    // Refresh wallet after successful payment
                    loadWallet()
                } else {
                    val errorMessage = parseErrorMessage(res.errorBody(), "Payment failed")
                    _uiState.value = _uiState.value.copy(isPayBalanceLoading = false, payBalanceError = errorMessage)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isPayBalanceLoading = false, payBalanceError = e.message ?: "Payment error")
            }
        }
    }

    fun clearPayBalanceState() {
        _uiState.value = _uiState.value.copy(isPayBalanceLoading = false, payBalanceError = null, payBalanceSuccess = null)
    }

    fun requestPinReset() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isPinLoading = true, pinError = null, pinSuccess = null)
                val prefs = KabukabuDriverApp.getInstance().userPreferences
                val token = prefs.authToken.first() ?: ""
                val userId = prefs.userId.first() ?: ""
                val bearer = "Bearer $token"
                val res = TransactionApiClient.service.requestPinReset(bearer, userId)
                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isPinLoading = false, pinSuccess = "OTP sent")
                } else {
                    val errorMessage = parseErrorMessage(res.errorBody(), "PIN reset request failed")
                    _uiState.value = _uiState.value.copy(isPinLoading = false, pinError = errorMessage)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isPinLoading = false, pinError = e.message ?: "PIN reset request error")
            }
        }
    }

    fun validatePinReset(otp: String, newPin: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isPinLoading = true, pinError = null, pinSuccess = null)
                val prefs = KabukabuDriverApp.getInstance().userPreferences
                val token = prefs.authToken.first() ?: ""
                val userId = prefs.userId.first() ?: ""
                val bearer = "Bearer $token"
                val res = TransactionApiClient.service.validatePinReset(bearer, userId, mapOf("new_pin" to newPin, "otp" to otp))
                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isPinLoading = false, pinSuccess = "PIN reset successful", hasPin = true)
                } else {
                    val errorMessage = parseErrorMessage(res.errorBody(), "PIN validate failed")
                    _uiState.value = _uiState.value.copy(isPinLoading = false, pinError = errorMessage)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isPinLoading = false, pinError = e.message ?: "PIN validate error")
            }
        }
    }

    fun clearPinState() {
        _uiState.value = _uiState.value.copy(isPinLoading = false, pinError = null, pinSuccess = null)
    }
}

data class WalletUiState(
    // Loading state for initial wallet load
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false, // Track refresh when we already have data
    val loadError: String? = null,
    val hasData: Boolean = false, // Track if data has been loaded at least once
    // Wallet data
    val balance: Int = 0,
    val kabuSubscriptionAmount: Int = 0,
    val tripChargeCap: Int = 0,
    val hasSharpDetails: Boolean = false,
    val kabuDueLabel: String? = null,
    val sharpDueLabel: String? = null,
    val isTopupLoading: Boolean = false,
    val topupUrl: String? = null,
    val errorMessage: String? = null,
    // Withdraw
    val isWithdrawLoading: Boolean = false,
    val withdrawError: String? = null,
    val withdrawSuccess: Boolean? = null,
    val accountName: String? = null,
    // PIN
    val isPinLoading: Boolean = false,
    val pinError: String? = null,
    val pinSuccess: String? = null,
    val hasPin: Boolean = true
    ,
    // Banks list
    val isBanksLoading: Boolean = false,
    val banksError: String? = null,
    val banks: List<WalletViewModel.BankItem> = emptyList(),
    // Pay Balance
    val isPayBalanceLoading: Boolean = false,
    val payBalanceError: String? = null,
    val payBalanceSuccess: Boolean? = null
)
//
//{"status":"success","code":200,"data":{"data":{"count":28,"rows":[{"id":27,"name":"CARBON MICROFINANCE BANK","isActive":true,"cbnCode":"565","code":"CARBON MICROFINANCE BANK","createdAt":"2023-08-24T05:37:08.000Z","updatedAt":"2023-08-24T05:37:08.000Z"},{"id":26,"name":"MONIEPOINT","isActive":true,"cbnCode":"50515","code":"MONIEPOINT","createdAt":"2023-08-24T05:37:08.000Z","updatedAt":"2023-08-24T05:37:08.000Z"},{"id":24,"name":"KUDIMONEY MFB 2","isActive":true,"cbnCode":"66666","code":"KUDI","createdAt":"2022-02-10T05:37:08.000Z","updatedAt":"2022-02-10T05:37:08.000Z"},{"id":23,"name":"KUDA MICROFINANCE BANK","isActive":true,"cbnCode":"50211","code":"KUDA","createdAt":"2022-02-10T05:37:08.000Z","updatedAt":"2022-02-10T05:37:08.000Z"},{"id":22,"name":"VFD MICROFINANCE BANK LIMITED","isActive":true,"cbnCode":"51161","code":"VFD MICROFINANCE BANK LIMITED","createdAt":"2022-02-10T05:37:08.000Z","updatedAt":"2022-02-10T05:37:08.000Z"},{"id":21,"name":"PAGA","isActive":true,"cbnCode":"327","code":"PAGA","createdAt":"2022-02-10T05:37:08.000Z","updatedAt":"2022-02-10T05:37:08.000Z"},{"id":25,"name":"OPAY/ PAYCOM","isActive":true,"cbnCode":"999992","code":"OPAY/ PAYCOM","createdAt":"2022-02-10T05:37:08.000Z","updatedAt":"2022-02-10T05:37:08.000Z"},{"id":20,"name":"STERLING BANK PLC","isActive":true,"cbnCode":"232","code":"STERLING","createdAt":"2022-02-10T05:36:39.000Z","updatedAt":"2022-02-10T05:36:39.000Z"},{"id":19,"name":"STANBIC IBTC BANK PLC","isActive":true,"cbnCode":"221","code":"STANBIC","createdAt":"2022-02-10T05:36:25.000Z","updatedAt":"2022-02-10T05:36:25.000Z"},{"id":18,"name":"UNITY BANK PLC","isActive":true,"cbnCode":"215","code":"UNITY","createdAt":"2022-02-10T05:36:10.000Z","updatedAt":"2022-02-10T05:36:10.000Z"},{"id":17,"name":"FIRST CITY MONUMENT BANK","isActive":true,"cbnCode":"214","code":"FCMB","createdAt":"2022-02-10T05:35:57.000Z","updatedAt":"2022-02-10T05:35:57.000Z"},{"id":16,"name":"PROVIDUS BANK","isActive":true,"cbnCode":"101","code":"PROVIDUS","createdAt":"2022-02-10T05:35:44.000Z","updatedAt":"2022-02-10T05:35:44.000Z"},{"id":15,"name":"SUNTRUST BANK","isActive":true,"cbnCode":"100","code":"SUNTRUST","createdAt":"2022-02-10T05:35:33.000Z","updatedAt":"2022-02-10T05:35:33.000Z"},{"id":14,"name":"KEYSTONE BANK LTD","isActive":true,"cbnCode":"082","code":"KEYSTONE","createdAt":"2022-02-10T05:35:19.000Z","updatedAt":"2022-02-10T05:35:19.000Z"},{"id":13,"name":"POLARIS BANK (SKYE BANK)","isActive":true,"cbnCode":"076","code":"POLARIS","createdAt":"2022-02-10T05:35:03.000Z","updatedAt":"2022-02-10T05:35:03.000Z"},{"id":12,"name":"FIDELITY BANK PLC","isActive":true,"cbnCode":"070","code":"FIDELITY","createdAt":"2022-02-10T05:34:44.000Z","updatedAt":"2022-02-10T05:34:44.000Z"},{"id":11,"name":"STANDARD CHARTERED BANK NIGERIA LTD","isActive":true,"cbnCode":"068","code":"STANCHARTD","createdAt":"2022-02-10T05:34:28.000Z","updatedAt":"2022-02-10T05:34:28.000Z"},{"id":10,"name":"ACESS(DIAMOND) BANK LTD","isActive":true,"cbnCode":"063","code":"ACCESS(DIAMOND)","createdAt":"2022-02-10T05:34:08.000Z","updatedAt":"2022-02-10T05:34:08.000Z"},{"id":9,"name":"ZENITH INTERNATIONAL BANK LTD","isActive":true,"cbnCode":"057","code":"ZENITH","createdAt":"2022-02-10T05:33:40.000Z","updatedAt":"2022-02-10T05:33:40.000Z"},{"id":8,"name":"ECOBANK NIGERIA PLC","isActive":true,"cbnCode":"050","code":"ECO","createdAt":"2022-02-10T05:33:25.000Z","updatedAt":"2022-02-10T05:33:25.000Z"},{"id":7,"name":"ACCESS BANK NIGERIA LTD","isActive":true,"cbnCode":"044","code":"ACCESS","createdAt":"2022-02-10T05:33:10.000Z","updatedAt":"2022-02-10T05:33:10.000Z"},{"id":6,"name":"WEMA BANK PLC","isActive":true,"cbnCode":"035","code":"WEMA","createdAt":"2022-02-10T05:32:54.000Z","updatedAt":"2022-02-10T05:32:54.000Z"},{"id":5,"name":"UNITED BANK FOR AFRICA PLC","isActive":true,"cbnCode":"033","code":"UBA","createdAt":"2022-02-10T05:32:40.000Z","updatedAt":"2022-02-10T05:32:40.000Z"},{"id":28,"name":"PALMPAY","isActive":true,"cbnCode":"999991","code"
//    14:30:18.773  I  :"PALMPAY","createdAt":"2022-02-10T05:32:26.000Z","updatedAt":"2022-02-10T05:32:26.000Z"},{"id":4,"name":"UNION BANK OF NIGERIA PLC","isActive":true,"cbnCode":"032","code":"UNION","createdAt":"2022-02-10T05:32:26.000Z","updatedAt":"2022-02-10T05:32:26.000Z"},{"id":3,"name":"HERITAGE BANK","isActive":true,"cbnCode":"030","code":"HERITAGE","createdAt":"2022-02-10T05:32:12.000Z","updatedAt":"2022-02-10T05:32:12.000Z"},{"id":2,"name":"FIRST BANK OF NIGERIA PLC","isActive":true,"cbnCode":"011","code":"FBN","createdAt":"2022-02-10T05:31:54.000Z","updatedAt":"2022-02-10T05:31:54.000Z"},{"id":1,"name":"Guaranty Trust Bank","isActive":true,"cbnCode":"058","code":"GTB","createdAt":"2022-02-09T17:26:04.000Z","updatedAt":"2022-02-09T17:26:04.000Z"}]},"total":28,"currentPage":1,"hasNext":false,"hasPrevious":false,"perPage":150,"totalPages":1},"message":"Banks Fetched SUccessfully"}
