package com.kabukabu.driver.features.wallet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.core.data.remote.ApiClient
import com.kabukabu.driver.core.data.remote.TransactionApiClient
import org.json.JSONObject
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class WalletViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState

    init {
        loadWallet()
    }

    fun loadWallet() {
        viewModelScope.launch {
            try {
                val prefs = KabukabuDriverApp.getInstance().userPreferences
                val token = prefs.authToken.first() ?: ""
                val userId = prefs.userId.first() ?: ""
                if (token.isBlank() || userId.isBlank()) {
                    // Not logged in yet or missing data; keep defaults
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
                    runCatching {
                        // Parse ISO string into date and show weekday label
                        val odt = try { OffsetDateTime.parse(iso) } catch (e: Exception) { null }
                        val date = odt?.toLocalDate() ?: LocalDate.parse(iso.substring(0, 10))
                        "due " + date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    }.getOrNull()
                }
                val sharpDueLabel = data?.sharpDueDate?.let { iso ->
                    runCatching {
                        val odt = try { OffsetDateTime.parse(iso) } catch (e: Exception) { null }
                        val date = odt?.toLocalDate() ?: LocalDate.parse(iso.substring(0, 10))
                        "due " + date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    }.getOrNull()
                }

                _uiState.value = _uiState.value.copy(
                    balance = balance,
                    kabuSubscriptionAmount = kabuSub,
                    tripChargeCap = tripCap,
                    hasSharpDetails = hasSharp,
                    kabuDueLabel = subDueLabel,
                    sharpDueLabel = sharpDueLabel
                )
            } catch (e: Exception) {
                // Keep existing state on error; could add error handling later
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
                if (token.isBlank() || userId.isBlank()) {
                    _uiState.value = _uiState.value.copy(isTopupLoading = false, errorMessage = "Missing auth")
                    return@launch
                }
                val bearer = "Bearer $token"
                val payload = mapOf("amount" to amount)
                val res = TransactionApiClient.service.generateTopupLink(bearer, userId, payload)
                if (res.isSuccessful) {
                    val body = res.body()?.string() ?: ""
                    // Try to extract a URL from response body; backend may return JSON
                    val url = Regex("https?://[\\w./?=&%-]+", RegexOption.IGNORE_CASE).find(body)?.value
                    _uiState.value = _uiState.value.copy(isTopupLoading = false, topupUrl = url)
                } else {
                    _uiState.value = _uiState.value.copy(isTopupLoading = false, errorMessage = res.errorBody()?.string() ?: "Topup failed")
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

    fun confirmAccount(accountNumber: String, bankCode: String) {
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
                    "bank_code" to bankCode
                )
                val res = TransactionApiClient.service.confirmBankAccount(bearer, userId, payload)
                if (res.isSuccessful) {
                    val name = res.body()?.string() ?: ""
                    _uiState.value = _uiState.value.copy(isWithdrawLoading = false, accountName = name)
                } else {
                    _uiState.value = _uiState.value.copy(isWithdrawLoading = false, withdrawError = res.errorBody()?.string() ?: "Unable to confirm account")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isWithdrawLoading = false, withdrawError = e.message ?: "Confirm error")
            }
        }
    }

    fun withdraw(amount: Int, accountNumber: String, bankCode: String) {
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
                    "bank_code" to bankCode
                )
                val res = TransactionApiClient.service.withdrawFromWallet(bearer, userId, payload)
                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isWithdrawLoading = false, withdrawSuccess = true)
                } else {
                    _uiState.value = _uiState.value.copy(isWithdrawLoading = false, withdrawError = res.errorBody()?.string() ?: "Withdraw failed")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isWithdrawLoading = false, withdrawError = e.message ?: "Withdraw error")
            }
        }
    }

    fun clearWithdrawState() {
        _uiState.value = _uiState.value.copy(isWithdrawLoading = false, withdrawError = null, withdrawSuccess = null, accountName = null)
    }

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
                    val data = root.optJSONObject("data")
                    val bankData = data?.optJSONObject("bankData")
                    val list = bankData?.optJSONArray("bankList")
                    val banks = mutableListOf<BankItem>()
                    if (list != null) {
                        for (i in 0 until list.length()) {
                            val obj = list.optJSONObject(i)
                            val name = obj?.optString("name") ?: ""
                            val code = obj?.optString("code") ?: obj?.optString("bank_code") ?: ""
                            if (name.isNotBlank() && code.isNotBlank()) {
                                banks.add(BankItem(name, code))
                            }
                        }
                    }
                    _uiState.value = _uiState.value.copy(isBanksLoading = false, banks = banks)
                } else {
                    _uiState.value = _uiState.value.copy(isBanksLoading = false, banksError = res.errorBody()?.string() ?: "Failed to load banks")
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
                    _uiState.value = _uiState.value.copy(isPinLoading = false, pinError = res.errorBody()?.string() ?: "PIN create failed")
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
                    _uiState.value = _uiState.value.copy(isPinLoading = false, pinError = res.errorBody()?.string() ?: "PIN change failed")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isPinLoading = false, pinError = e.message ?: "PIN change error")
            }
        }
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
                    _uiState.value = _uiState.value.copy(isPinLoading = false, pinError = res.errorBody()?.string() ?: "PIN reset request failed")
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
                    _uiState.value = _uiState.value.copy(isPinLoading = false, pinError = res.errorBody()?.string() ?: "PIN validate failed")
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
    val hasPin: Boolean = false
    ,
    // Banks list
    val isBanksLoading: Boolean = false,
    val banksError: String? = null,
    val banks: List<WalletViewModel.BankItem> = emptyList()
)
