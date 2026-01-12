package com.kabukabu.driver.features.promotions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.core.data.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class PromotionsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PromotionsUiState())
    val uiState: StateFlow<PromotionsUiState> = _uiState

    private var ongoingPage = 1
    private var completedPage = 1
    private val limit = 10

    // Moshi instance and adapter for promotion DTO
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val promoAdapter = moshi.adapter(ApiPromotion::class.java)

    init {
        refresh()
    }

    fun refresh() {
        ongoingPage = 1
        completedPage = 1
        load(status = "ongoing", reset = true)
        load(status = "completed", reset = true)
    }

    fun loadMore(status: String) {
        if (status == "ongoing") {
            if (_uiState.value.isLoadingMoreOngoing || _uiState.value.noMoreOngoing) return
            ongoingPage += 1
            load(status = status, reset = false)
        } else {
            if (_uiState.value.isLoadingMoreCompleted || _uiState.value.noMoreCompleted) return
            completedPage += 1
            load(status = status, reset = false)
        }
    }

    private fun load(status: String, reset: Boolean) {
        viewModelScope.launch {
            try {
                if (status == "ongoing") {
                    _uiState.value = if (reset) _uiState.value.copy(isLoadingOngoing = true, errorOngoing = null)
                    else _uiState.value.copy(isLoadingMoreOngoing = true, errorOngoing = null)
                } else {
                    _uiState.value = if (reset) _uiState.value.copy(isLoadingCompleted = true, errorCompleted = null)
                    else _uiState.value.copy(isLoadingMoreCompleted = true, errorCompleted = null)
                }
                val prefs = KabukabuDriverApp.getInstance().userPreferences
                val token = prefs.authToken.first() ?: ""
                val userId = prefs.userId.first() ?: ""
                if (token.isBlank() || userId.isBlank()) {
                    if (status == "ongoing") _uiState.value = _uiState.value.copy(isLoadingOngoing = false, isLoadingMoreOngoing = false, errorOngoing = "Missing auth")
                    else _uiState.value = _uiState.value.copy(isLoadingCompleted = false, isLoadingMoreCompleted = false, errorCompleted = "Missing auth")
                    return@launch
                }
                val bearer = "Bearer $token"
                val page = if (status == "ongoing") ongoingPage else completedPage
                val res = ApiClient.rideService.getPromotions(
                    bearerToken = bearer,
                    userId = userId,
                    status = status,
                    page = page,
                    limit = limit
                )
                if (!res.isSuccessful) throw Exception(res.errorBody()?.string() ?: "Promo error")
                val body = res.body()?.string() ?: "{}"
                val root = JSONObject(body)
                val data = root.optJSONObject("data")
                val list = data?.optJSONArray("data")
                val pagination = data?.optJSONObject("pagination")
                val hasNext = pagination?.optBoolean("hasNext") ?: false
                val items = mutableListOf<PromotionItem>()
                if (list != null) {
                    for (i in 0 until list.length()) {
                        val obj = list.optJSONObject(i)
                        if (obj != null) {
                            // The API returns flat promotion objects (no nested promotion/coupon), parse directly
                            val statusStr = obj.optString("status")

                            val jsonToParse = obj.toString()

                            val parsed: ApiPromotion? = try {
                                promoAdapter.fromJson(jsonToParse)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                null
                            }

                            // Fallbacks for differing key names
                            val name = parsed?.name ?: obj.optString("name")
                            val expiry = parsed?.expiryDate ?: obj.optString("expiry_date") ?: obj.optString("expiryDate")

                            items.add(
                                PromotionItem(

                                    id = parsed?.id ?: parsed?._id ?: obj.optString("id") ?: obj.optString("_id"),
                                    name = name ?: "Promo",
                                    description = parsed?.description ?: obj.optString("description"),
                                    category = parsed?.category ?: obj.optString("category"),
                                    amount = parsed?.amount ?: if (obj.has("amount")) obj.optInt("amount") else null,
                                    rewardType = parsed?.rewardType ?: obj.optString("reward_type") ?: obj.optString("rewardType"),
                                    activationDate = parsed?.activationDate ?: obj.optString("activation_date") ?: obj.optString("activationDate"),
                                    expiryDate = expiry,
                                    targetValue = parsed?.targetValue ?: if (obj.has("target_value")) obj.optInt("target_value") else null,
                                    resetType = parsed?.resetType ?: obj.optString("reset_type"),
                                    user = parsed?.user ?: obj.optString("user"),
                                    v = parsed?.v ?: if (obj.has("__v")) obj.optInt("__v") else null,
                                    createdAt = parsed?.createdAt ?: obj.optString("createdAt"),
                                    updatedAt = parsed?.updatedAt ?: obj.optString("updatedAt"),
                                    comment = parsed?.comment ?: obj.optString("comment"),
                                    status = if (statusStr.isBlank()) null else statusStr,
                                    count = parsed?.count ?: if (obj.has("count")) obj.optInt("count") else null
                                )
                            )
                        }
                    }
                }

                if (status == "ongoing") {
                    _uiState.value = _uiState.value.copy(
                        isLoadingOngoing = false,
                        isLoadingMoreOngoing = false,
                        ongoing = if (reset) items else _uiState.value.ongoing + items,
                        noMoreOngoing = !hasNext
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoadingCompleted = false,
                        isLoadingMoreCompleted = false,
                        completed = if (reset) items else _uiState.value.completed + items,
                        noMoreCompleted = !hasNext
                    )
                }
            } catch (e: Exception) {
                if (status == "ongoing") {
                    _uiState.value = _uiState.value.copy(isLoadingOngoing = false, isLoadingMoreOngoing = false, errorOngoing = e.message)
                } else {
                    _uiState.value = _uiState.value.copy(isLoadingCompleted = false, isLoadingMoreCompleted = false, errorCompleted = e.message)
                }
            }
        }
    }
}

// DTO that mirrors the API promotion object. Use Moshi to parse snake_case keys into camelCase properties.
@JsonClass(generateAdapter = true)
data class ApiPromotion(
    @Json(name = "_id") val _id: String? = null,
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val category: String? = null,
    val amount: Int? = null,
    @Json(name = "reward_type") val rewardType: String? = null,
    @Json(name = "activation_date") val activationDate: String? = null,
    @Json(name = "expiry_date") val expiryDate: String? = null,
    @Json(name = "target_value") val targetValue: Int? = null,
    @Json(name = "reset_type") val resetType: String? = null,
    val user: String? = null,
    @Json(name = "__v") val v: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val comment: String? = null,
    val count: Int? = null
)

// UI model used by the viewmodel and UI layer
data class PromotionItem(
    val id: String?,
    val name: String?,
    val description: String?,
    val category: String?,
    val amount: Int?,
    val rewardType: String?,
    val activationDate: String?,
    val expiryDate: String?,
    val targetValue: Int?,
    val resetType: String?,
    val user: String?,
    val v: Int?,
    val createdAt: String?,
    val updatedAt: String?,
    val comment: String?,
    val status: String?,
    val count: Int? = 0
)

data class PromotionsUiState(
    val isLoadingOngoing: Boolean = false,
    val isLoadingMoreOngoing: Boolean = false,
    val errorOngoing: String? = null,
    val ongoing: List<PromotionItem> = emptyList(),
    val noMoreOngoing: Boolean = false,

    val isLoadingCompleted: Boolean = false,
    val isLoadingMoreCompleted: Boolean = false,
    val errorCompleted: String? = null,
    val completed: List<PromotionItem> = emptyList(),
    val noMoreCompleted: Boolean = false,
)
