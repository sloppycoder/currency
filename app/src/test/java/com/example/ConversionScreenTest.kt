package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.ui.screens.ConvertScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w412dp-h915dp")
class ConversionScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("currency_prefs", Context.MODE_PRIVATE).edit()
            .clear()
            .putString("selected_currency_codes", "USD,EUR")
            .putString("base_currency", "USD")
            .putFloat("base_amount", 1000f)
            .putString("cached_rates_json", "{\"USD\":1.0,\"EUR\":0.8}")
            .putLong("rates_last_updated", System.currentTimeMillis())
            .commit()
        val viewModel = MainViewModel(app)
        compose.setContent {
            MyApplicationTheme {
                ConvertScreen(viewModel, onNavigateToAddCurrencies = {})
            }
        }
        assertAmount("USD", "$1,000.00")
        assertAmount("EUR", "€800.00")
    }

    @Test
    fun editingCurrentBaseRefreshesAllAmounts() {
        enterAmount("USD", "25")
        convert()
        assertAmount("USD", "$25.00")
        assertAmount("EUR", "€20.00")

        // Reopening the same currency must keep the submitted amount.
        compose.onNodeWithTag("currency_row_USD").performClick()
        convert()
        assertAmount("USD", "$25.00")
        assertAmount("EUR", "€20.00")
    }

    @Test
    fun editingAnotherCurrencyUsesItsAmountAsTheNewBase() {
        enterAmount("EUR", "40")
        convert()
        assertAmount("EUR", "€40.00")
        assertAmount("USD", "$50.00")

        enterAmount("EUR", "80")
        convert()
        assertAmount("EUR", "€80.00")
        assertAmount("USD", "$100.00")
    }

    @Test
    fun convertEvaluatesPendingArithmetic() {
        enterAmount("USD", "25")
        compose.onNodeWithTag("keypad_btn_+").performClick()
        compose.onNodeWithTag("keypad_btn_5").performClick()
        convert()
        assertAmount("USD", "$30.00")
        assertAmount("EUR", "€24.00")
    }

    private fun enterAmount(code: String, digits: String) {
        compose.onNodeWithTag("currency_row_$code").performClick()
        compose.onNodeWithTag("keypad_btn_C").performClick()
        digits.forEach { compose.onNodeWithTag("keypad_btn_$it").performClick() }
    }

    private fun convert() {
        compose.onNodeWithTag("calculator_convert_button")
            .assertTextContains("Convert")
            .performClick()
        compose.onNodeWithTag("calculator_convert_button").assertDoesNotExist()
    }

    private fun assertAmount(code: String, amount: String) {
        compose.onNodeWithTag("currency_row_$code").assertTextContains(amount)
    }
}
