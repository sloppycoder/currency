package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.model.SupportedCurrencies
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ExchangeRatesState(
    val rates: Map<String, Double> = emptyMap(),
    val base: String = "USD",
    val lastUpdated: Long = 0L,
    val isLive: Boolean = false,
    val isOffline: Boolean = false,
    val isLoading: Boolean = false,
    val statusMessage: String = ""
)

class CurrencyRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("currency_prefs", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .build()

    private val _ratesState = MutableStateFlow(loadInitialRates())
    val ratesState: StateFlow<ExchangeRatesState> = _ratesState.asStateFlow()

    private val _selectedCurrencies = MutableStateFlow(loadSelectedCurrencies())
    val selectedCurrencies: StateFlow<List<String>> = _selectedCurrencies.asStateFlow()

    private val _baseCurrency = MutableStateFlow(prefs.getString(KEY_BASE_CURRENCY, "USD") ?: "USD")
    val baseCurrency: StateFlow<String> = _baseCurrency.asStateFlow()

    private val _baseAmount = MutableStateFlow(
        prefs.getFloat(KEY_BASE_AMOUNT, 1000f).toDouble()
    )
    val baseAmount: StateFlow<Double> = _baseAmount.asStateFlow()

    companion object {
        private const val PREFS_NAME = "currency_prefs"
        private const val KEY_CACHED_RATES = "cached_rates_json"
        private const val KEY_LAST_UPDATED = "rates_last_updated"
        private const val KEY_SELECTED_CODES = "selected_currency_codes"
        private const val KEY_BASE_CURRENCY = "base_currency"
        private const val KEY_BASE_AMOUNT = "base_amount"
        private const val CACHE_EXPIRY_MS = 24 * 60 * 60 * 1000L // 24 hours

        // Fallback reference rates in case of first launch completely offline
        val DEFAULT_RATES: Map<String, Double> = mapOf(
            "USD" to 1.0,
            "SGD" to 1.2817,
            "VND" to 25493.9,
            "IDR" to 15680.0,
            "CNY" to 6.6749,
            "MYR" to 4.0613,
            "EUR" to 0.89656,
            "JPY" to 158.304,
            "THB" to 33.531,
            "CAD" to 1.41144,
            "HKD" to 7.84086,
            "KRW" to 1380.5,
            "TWD" to 32.15
        )
    }

    private fun loadInitialRates(): ExchangeRatesState {
        val lastUpdated = prefs.getLong(KEY_LAST_UPDATED, 0L)
        val cachedJson = prefs.getString(KEY_CACHED_RATES, null)
        val now = System.currentTimeMillis()

        if (!cachedJson.isNullOrEmpty()) {
            try {
                val json = JSONObject(cachedJson)
                val map = mutableMapOf<String, Double>()
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    map[k] = json.getDouble(k)
                }
                val isFresh = (now - lastUpdated) < CACHE_EXPIRY_MS
                return ExchangeRatesState(
                    rates = map,
                    lastUpdated = lastUpdated,
                    isLive = isFresh && isNetworkAvailable(),
                    isOffline = !isNetworkAvailable() || !isFresh,
                    statusMessage = if (!isNetworkAvailable()) "Offline • Using 24h cached rates" else if (isFresh) "Live rates (24h cached)" else "Rates older than 24h"
                )
            } catch (_: Exception) {
                // fall through to default
            }
        }

        // Default offline rates
        return ExchangeRatesState(
            rates = DEFAULT_RATES,
            lastUpdated = if (lastUpdated > 0L) lastUpdated else now,
            isLive = false,
            isOffline = true,
            statusMessage = "Offline • Using bundled reference rates"
        )
    }

    private fun loadSelectedCurrencies(): List<String> {
        val raw = prefs.getString(KEY_SELECTED_CODES, null)
        if (raw.isNullOrEmpty()) {
            return SupportedCurrencies.DEFAULT_SELECTED_CODES
        }
        val list = raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        return if (list.isNotEmpty()) list else SupportedCurrencies.DEFAULT_SELECTED_CODES
    }

    fun saveSelectedCurrencies(codes: List<String>) {
        _selectedCurrencies.value = codes
        prefs.edit().putString(KEY_SELECTED_CODES, codes.joinToString(",")).apply()
    }

    fun setBaseCurrency(code: String) {
        _baseCurrency.value = code
        prefs.edit().putString(KEY_BASE_CURRENCY, code).apply()
    }

    fun setBaseAmount(amount: Double) {
        val validAmount = if (amount.isNaN() || amount < 0) 0.0 else amount
        _baseAmount.value = validAmount
        prefs.edit().putFloat(KEY_BASE_AMOUNT, validAmount.toFloat()).apply()
    }

    fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun refreshRates(force: Boolean = false) {
        val currentState = _ratesState.value
        val now = System.currentTimeMillis()
        val isCacheValid = (now - currentState.lastUpdated) < CACHE_EXPIRY_MS

        if (!force && isCacheValid && currentState.rates.isNotEmpty()) {
            // Already fresh cache
            _ratesState.value = currentState.copy(
                isOffline = !isNetworkAvailable(),
                isLive = isNetworkAvailable(),
                statusMessage = if (!isNetworkAvailable()) "Offline • Using 24h cached rates" else "Live rates (24h cached)"
            )
            return
        }

        if (!isNetworkAvailable()) {
            // Offline: keep current rates, update indicator
            _ratesState.value = currentState.copy(
                isLoading = false,
                isLive = false,
                isOffline = true,
                statusMessage = "Offline • Using 24h cached rates"
            )
            return
        }

        _ratesState.value = currentState.copy(isLoading = true)

        withContext(Dispatchers.IO) {
            val urls = listOf(
                "https://open.er-api.com/v6/latest/USD",
                "https://api.exchangerate-api.com/v4/latest/USD"
            )

            var success = false
            for (url in urls) {
                try {
                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrEmpty()) {
                            val json = JSONObject(body)
                            val ratesJson = json.getJSONObject("rates")
                            val newRates = mutableMapOf<String, Double>()
                            SupportedCurrencies.ALL.forEach { curr ->
                                if (ratesJson.has(curr.code)) {
                                    newRates[curr.code] = ratesJson.getDouble(curr.code)
                                } else {
                                    newRates[curr.code] = DEFAULT_RATES[curr.code] ?: curr.defaultRateToUsd
                                }
                            }
                            newRates["USD"] = 1.0

                            // Save to SharedPreferences
                            val cacheObj = JSONObject(newRates as Map<*, *>)
                            val updateTime = System.currentTimeMillis()
                            prefs.edit()
                                .putString(KEY_CACHED_RATES, cacheObj.toString())
                                .putLong(KEY_LAST_UPDATED, updateTime)
                                .apply()

                            _ratesState.value = ExchangeRatesState(
                                rates = newRates,
                                lastUpdated = updateTime,
                                isLive = true,
                                isOffline = false,
                                isLoading = false,
                                statusMessage = "Rates updated just now"
                            )
                            success = true
                            break
                        }
                    }
                } catch (_: Exception) {
                    // Try fallback url
                }
            }

            if (!success) {
                // Network call failed, retain cached
                _ratesState.value = currentState.copy(
                    isLoading = false,
                    isLive = false,
                    isOffline = true,
                    statusMessage = "Network unavailable • Using 24h cached rates"
                )
            }
        }
    }

    /**
     * Convert an amount from fromCurrency to toCurrency
     */
    fun convert(amount: Double, fromCode: String, toCode: String): Double {
        if (fromCode.equals(toCode, ignoreCase = true)) return amount
        val rates = _ratesState.value.rates.ifEmpty { DEFAULT_RATES }

        val fromRate = rates[fromCode] ?: DEFAULT_RATES[fromCode] ?: 1.0
        val toRate = rates[toCode] ?: DEFAULT_RATES[toCode] ?: 1.0

        if (fromRate == 0.0) return 0.0

        // Amount in USD = amount / fromRate
        // Amount in Target = (amount / fromRate) * toRate
        return (amount / fromRate) * toRate
    }
}
