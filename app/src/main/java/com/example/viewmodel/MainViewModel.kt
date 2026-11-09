package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CurrencyRepository
import com.example.data.ExchangeRatesState
import com.example.model.CurrencyItem
import com.example.model.SupportedCurrencies
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CurrencyRepository(application.applicationContext)

    val ratesState: StateFlow<ExchangeRatesState> = repository.ratesState
    val selectedCurrencies: StateFlow<List<String>> = repository.selectedCurrencies
    val baseCurrency: StateFlow<String> = repository.baseCurrency
    val baseAmount: StateFlow<Double> = repository.baseAmount

    private val _calculatorCurrency = MutableStateFlow<CurrencyItem?>(null)
    val calculatorCurrency: StateFlow<CurrencyItem?> = _calculatorCurrency.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    init {
        // Initial check and fetch if needed
        viewModelScope.launch {
            repository.refreshRates(force = false)
        }
    }

    fun onResumeScreen() {
        viewModelScope.launch {
            repository.refreshRates(force = false)
        }
    }

    fun refreshRates() {
        viewModelScope.launch {
            repository.refreshRates(force = true)
        }
    }

    fun selectBaseCurrency(code: String) {
        val oldBase = baseCurrency.value
        val currentAmount = baseAmount.value
        if (oldBase != code) {
            // Convert current base amount into target currency so the user experiences seamless conversion
            val converted = repository.convert(currentAmount, oldBase, code)
            repository.setBaseCurrency(code)
            repository.setBaseAmount(converted)
        }
    }

    fun openCalculator(currency: CurrencyItem) {
        // If clicking a currency that isn't base yet, set it as base
        if (baseCurrency.value != currency.code) {
            val converted = repository.convert(baseAmount.value, baseCurrency.value, currency.code)
            repository.setBaseCurrency(currency.code)
            repository.setBaseAmount(converted)
        }
        _calculatorCurrency.value = currency
    }

    fun closeCalculator() {
        _calculatorCurrency.value = null
    }

    fun updateAmountFromCalculator(newAmount: Double) {
        repository.setBaseAmount(newAmount)
    }

    fun toggleEditMode() {
        _isEditMode.value = !_isEditMode.value
    }

    fun setEditMode(active: Boolean) {
        _isEditMode.value = active
    }

    fun toggleCurrencySelection(code: String) {
        val current = selectedCurrencies.value.toMutableList()
        if (current.contains(code)) {
            // Must keep at least 1 currency
            if (current.size > 1) {
                current.remove(code)
                // If removing current base, select first available
                if (baseCurrency.value == code) {
                    selectBaseCurrency(current.first())
                }
            }
        } else {
            current.add(code)
        }
        repository.saveSelectedCurrencies(current)
    }

    fun moveCurrencyUp(index: Int) {
        val current = selectedCurrencies.value.toMutableList()
        if (index > 0 && index < current.size) {
            val item = current.removeAt(index)
            current.add(index - 1, item)
            repository.saveSelectedCurrencies(current)
        }
    }

    fun moveCurrencyDown(index: Int) {
        val current = selectedCurrencies.value.toMutableList()
        if (index >= 0 && index < current.size - 1) {
            val item = current.removeAt(index)
            current.add(index + 1, item)
            repository.saveSelectedCurrencies(current)
        }
    }

    fun resetCurrenciesToDefault() {
        repository.saveSelectedCurrencies(SupportedCurrencies.DEFAULT_SELECTED_CODES)
    }

    fun getConvertedAmount(targetCode: String): Double {
        return repository.convert(baseAmount.value, baseCurrency.value, targetCode)
    }

    fun formatFormattedWithSymbol(item: CurrencyItem, amount: Double): String {
        val formatted = formatNumber(amount, item.decimals)
        // Space after symbol if symbol is letters like CA$, CN¥, RM, S$, NT$
        val needsSpace = item.symbol.length > 1 && !item.symbol.contains("¥") && !item.symbol.contains("CA")
        return if (needsSpace) {
            "${item.symbol} $formatted"
        } else {
            "${item.symbol}$formatted"
        }
    }

    fun formatNumber(amount: Double, decimals: Int): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        val pattern = when (decimals) {
            0 -> "#,##0"
            1 -> "#,##0.0"
            else -> "#,##0.00"
        }
        val df = DecimalFormat(pattern, symbols)
        return df.format(amount)
    }

    fun getLastUpdatedText(): String {
        val timestamp = ratesState.value.lastUpdated
        if (timestamp == 0L) return "Never updated"
        val diff = System.currentTimeMillis() - timestamp
        val minutes = diff / (60 * 1000)
        val hours = minutes / 60
        return when {
            minutes < 1 -> "Updated just now"
            minutes < 60 -> "Updated ${minutes}m ago"
            hours < 24 -> "Updated ${hours}h ago"
            else -> {
                val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                "Updated " + sdf.format(Date(timestamp))
            }
        }
    }
}
