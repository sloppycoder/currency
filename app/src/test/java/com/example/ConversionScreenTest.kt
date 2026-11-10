package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    private lateinit var viewModel: MainViewModel

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
        viewModel = MainViewModel(app)
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

        // Each opening starts a fresh draft; cancel preserves the submitted amount.
        compose.onNodeWithTag("currency_row_USD").performClick()
        compose.onNodeWithTag("calculator_amount").assertTextContains("Enter amount")
        compose.onNodeWithTag("calculator_convert_button").assertIsNotEnabled()
        compose.onNodeWithTag("calculator_close_button").performClick()
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

    @Test
    fun draftAndCancelPreserveMainAmountsAndBase() {
        enterAmount("EUR", "25")
        compose.onNodeWithTag("keypad_btn_+").performClick()
        compose.onNodeWithTag("keypad_btn_5").performClick()
        compose.onNodeWithTag("keypad_btn_=").performClick()
        compose.runOnIdle {
            assertEquals("USD", viewModel.baseCurrency.value)
            assertEquals(1000.0, viewModel.baseAmount.value, 0.0)
        }
        compose.onNodeWithTag("calculator_close_button").performClick()
        assertAmount("USD", "$1,000.00")
        assertAmount("EUR", "€800.00")
    }

    @Test
    fun shortcutsSwitchToOperatorsAndPercentCalculatesWithoutCommitting() {
        compose.onNodeWithTag("currency_row_USD").performClick()
        compose.onNodeWithTag("calculator_amount").assertTextContains("Enter amount")
        compose.onNodeWithTag("keypad_btn_1K").assertExists()
        compose.onNodeWithTag("keypad_btn_2").performClick()
        compose.onNodeWithTag("keypad_btn_1K").performClick()
        compose.onNodeWithTag("keypad_btn_1K").assertDoesNotExist()
        compose.onNodeWithTag("calculator_amount").assertTextContains("2000")
        compose.onNodeWithTag("keypad_btn_×").performClick()
        compose.onNodeWithTag("keypad_btn_1").performClick()
        compose.onNodeWithTag("keypad_btn_0").performClick()
        compose.onNodeWithTag("keypad_btn_%").performClick()
        compose.onNodeWithTag("calculator_amount").assertTextContains("200")
        compose.runOnIdle { assertEquals(1000.0, viewModel.baseAmount.value, 0.0) }
        convert()
        assertAmount("USD", "$200.00")
        assertAmount("EUR", "€160.00")
    }

    @Test
    fun partialSwipeRevealsDeleteAndButtonRemovesBaseCurrency() {
        toggleEditMode()
        compose.onNodeWithTag("currency_row_USD").performTouchInput {
            swipe(Offset(width * 0.8f, centerY), Offset(width * 0.5f, centerY), 400)
        }
        compose.onNodeWithTag("delete_USD").assertTextContains("Delete").performClick()
        compose.onNodeWithTag("currency_row_USD").assertDoesNotExist()
        assertAmount("EUR", "€800.00")
        compose.runOnIdle {
            assertEquals(listOf("EUR"), viewModel.selectedCurrencies.value)
            assertEquals("EUR", viewModel.baseCurrency.value)
        }
    }

    @Test
    fun fullSwipeDeletesAndLastCurrencyCannotBeDeleted() {
        toggleEditMode()
        compose.onNodeWithTag("currency_row_EUR").performTouchInput { swipeLeft() }
        compose.onNodeWithTag("currency_row_EUR").assertDoesNotExist()
        compose.onNodeWithTag("currency_row_USD").performTouchInput { swipeLeft() }
        assertAmount("USD", "$1,000.00")
        compose.onNodeWithTag("delete_USD").assertDoesNotExist()
    }

    @Test
    fun swipeRightClosesRevealedDelete() {
        toggleEditMode()
        compose.onNodeWithTag("currency_row_EUR").performTouchInput {
            swipe(Offset(width * 0.8f, centerY), Offset(width * 0.5f, centerY), 400)
        }
        compose.onNodeWithTag("delete_EUR").assertExists()
        compose.onNodeWithTag("currency_row_EUR").performTouchInput {
            swipe(Offset(width * 0.2f, centerY), Offset(width * 0.7f, centerY), 400)
        }
        compose.onNodeWithTag("delete_EUR").assertDoesNotExist()
        assertAmount("EUR", "€800.00")
    }

    @Test
    fun dragHandleReordersAndPersistsWithoutOpeningCalculator() {
        toggleEditMode()
        val rowHeight = compose.onNodeWithTag("currency_row_USD").fetchSemanticsNode().boundsInRoot.height
        compose.onNodeWithTag("drag_handle_USD", useUnmergedTree = true).performTouchInput {
            swipe(center, center + Offset(0f, rowHeight * 1.2f), 600)
        }
        compose.runOnIdle { assertEquals(listOf("EUR", "USD"), viewModel.selectedCurrencies.value) }
        compose.onNodeWithTag("calculator_convert_button").assertDoesNotExist()
        val usdY = compose.onNodeWithTag("currency_row_USD").fetchSemanticsNode().boundsInRoot.top
        val eurY = compose.onNodeWithTag("currency_row_EUR").fetchSemanticsNode().boundsInRoot.top
        assertTrue(usdY > eurY)
        compose.runOnIdle {
            val app = ApplicationProvider.getApplicationContext<Application>()
            assertEquals("EUR,USD", app.getSharedPreferences("currency_prefs", Context.MODE_PRIVATE)
                .getString("selected_currency_codes", null))
        }
        // Drag the same row back upward.
        compose.onNodeWithTag("drag_handle_USD", useUnmergedTree = true).performTouchInput {
            swipe(center, center - Offset(0f, rowHeight * 1.2f), 600)
        }
        compose.runOnIdle { assertEquals(listOf("USD", "EUR"), viewModel.selectedCurrencies.value) }
    }

    @Test
    fun holdingRowCanReorderAcrossSeveralCurrencies() {
        toggleEditMode()
        compose.runOnIdle {
            viewModel.toggleCurrencySelection("JPY")
            viewModel.toggleCurrencySelection("SGD")
        }
        val rowHeight = compose.onNodeWithTag("currency_row_USD").fetchSemanticsNode().boundsInRoot.height
        compose.onNodeWithTag("currency_row_USD").performTouchInput {
            val start = Offset(width * 0.3f, centerY)
            down(start)
            advanceEventTime(650)
            moveTo(start + Offset(0f, rowHeight * 3.2f), 600)
            up()
        }
        compose.runOnIdle {
            assertEquals(listOf("EUR", "JPY", "SGD", "USD"), viewModel.selectedCurrencies.value)
        }
        compose.onNodeWithTag("calculator_convert_button").assertDoesNotExist()
    }

    @Test
    fun normalModeHidesHandlesAndDisablesDeletionAndReordering() {
        compose.onNodeWithTag("drag_handle_USD", useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithTag("currency_row_EUR").performTouchInput { swipeLeft() }
        compose.onNodeWithTag("delete_EUR").assertDoesNotExist()
        val rowHeight = compose.onNodeWithTag("currency_row_USD").fetchSemanticsNode().boundsInRoot.height
        compose.onNodeWithTag("currency_row_USD").performTouchInput {
            val start = Offset(width * 0.3f, centerY)
            down(start)
            advanceEventTime(650)
            moveTo(start + Offset(0f, rowHeight * 1.2f), 600)
            up()
        }
        compose.runOnIdle { assertEquals(listOf("USD", "EUR"), viewModel.selectedCurrencies.value) }
        assertAmount("USD", "$1,000.00")
        assertAmount("EUR", "€800.00")
    }

    @Test
    fun leavingEditModeHidesHandlesClosesDeleteAndRestoresCalculator() {
        toggleEditMode()
        compose.onNodeWithTag("drag_handle_USD", useUnmergedTree = true).assertExists()
        compose.onNodeWithTag("currency_row_USD").performClick()
        compose.onNodeWithTag("calculator_convert_button").assertDoesNotExist()
        compose.onNodeWithTag("currency_row_EUR").performTouchInput {
            swipe(Offset(width * 0.8f, centerY), Offset(width * 0.5f, centerY), 400)
        }
        compose.onNodeWithTag("delete_EUR").assertExists()
        toggleEditMode()
        compose.onNodeWithTag("drag_handle_USD", useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithTag("delete_EUR").assertDoesNotExist()
        compose.onNodeWithTag("currency_row_EUR").performTouchInput { swipeLeft() }
        compose.runOnIdle { assertEquals(listOf("USD", "EUR"), viewModel.selectedCurrencies.value) }
        enterAmount("USD", "25")
        convert()
        assertAmount("USD", "$25.00")
        assertAmount("EUR", "€20.00")
        toggleEditMode()
        compose.onNodeWithTag("drag_handle_USD", useUnmergedTree = true).assertExists()
        compose.onNodeWithTag("delete_EUR").assertDoesNotExist()
    }

    private fun toggleEditMode() {
        compose.onNodeWithTag("edit_mode_button").performClick()
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
