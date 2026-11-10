package com.example

import com.example.ui.components.AmountEntryState
import com.example.ui.components.amountShortcuts
import org.junit.Assert.*
import org.junit.Test

class AmountEntryStateTest {
    private fun entered(digits: String): AmountEntryState =
        digits.fold(AmountEntryState()) { state, digit -> state.digit(digit.toString()) }

    @Test fun shortcutsMatchCurrencyScale() {
        assertEquals(listOf("1K", "100", "10", "1"), amountShortcuts("SGD").map { it.label })
        assertEquals(listOf("10K", "1K", "100", "1"), amountShortcuts("JPY").map { it.label })
        for (code in listOf("VND", "IDR")) {
            assertEquals(listOf("1M", "100K", "10K", "1"), amountShortcuts(code).map { it.label })
        }
        assertEquals(listOf("100K", "10K", "1K", "1"), amountShortcuts("KRW").map { it.label })
        assertEquals(1_000_000.0, AmountEntryState().shortcut(amountShortcuts("VND")[0].multiplier).value, 0.0)
    }

    @Test fun shortcutAndTwoDigitsSwitchModes() {
        assertFalse(AmountEntryState().canConvert)
        assertFalse(entered("1").operatorsVisible)
        assertTrue(entered("12").operatorsVisible)
        assertEquals(1000.0, entered("1").shortcut(1000.0).value, 0.0)
        assertTrue(entered("1").shortcut(1000.0).operatorsVisible)
        assertFalse(entered("12").backspace().operatorsVisible)
        assertEquals("", entered("1").backspace().input)
    }

    @Test fun percentAppliesToLastOperandAndEvaluates() {
        assertEquals(0.1, entered("10").percent().value, 0.0)
        val product = entered("200").operation("×").digit("1").digit("0").percent()
        assertEquals(20.0, product.value, 0.0)
        assertNull(product.operator)
        assertEquals("200 × 10% =", product.formula)
        assertEquals(200.1, entered("200").operation("+").digit("1").digit("0").percent().value, 0.0)
    }

    @Test fun arithmeticChainsAndOperatorReplacementWork() {
        val sum = entered("12").operation("+").digit("3").operation("×").digit("2").equals()
        assertEquals(30.0, sum.value, 0.0)
        assertEquals(24.0, entered("12").operation("+").operation("×").digit("2").equals().value, 0.0)
        assertEquals("0.3", entered("0.1").operation("+").digit("0").digit(".").digit("2").equals().input)
    }

    @Test fun invalidDivisionCannotBeSubmittedAndCanBeCorrected() {
        val invalid = entered("12").operation("÷").digit("0").equals()
        assertNotNull(invalid.error)
        assertFalse(invalid.canConvert)
        assertEquals(6.0, invalid.digit("2").equals().value, 0.0)
    }
}
