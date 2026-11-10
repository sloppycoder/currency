package com.example.ui.components

import java.math.BigDecimal
import java.math.RoundingMode

internal data class AmountShortcut(val label: String, val multiplier: Double)

internal fun amountShortcuts(code: String): List<AmountShortcut> {
    val amounts = when (code) {
        "JPY" -> listOf("10K" to 10_000.0, "1K" to 1_000.0, "100" to 100.0)
        "VND", "IDR" -> listOf("1M" to 1_000_000.0, "100K" to 100_000.0, "10K" to 10_000.0)
        "KRW" -> listOf("100K" to 100_000.0, "10K" to 10_000.0, "1K" to 1_000.0)
        else -> listOf("1K" to 1_000.0, "100" to 100.0, "10" to 10.0)
    }
    return (amounts + ("1" to 1.0)).map { AmountShortcut(it.first, it.second) }
}

/** A local draft. Only Convert submits its value to the currency list. */
internal data class AmountEntryState(
    val input: String = "",
    val formula: String = "",
    val firstOperand: Double? = null,
    val operator: String? = null,
    val replaceInput: Boolean = false,
    val operatorsVisible: Boolean = false,
    val error: String? = null
) {
    val value: Double get() = input.toDoubleOrNull() ?: 0.0
    val canConvert: Boolean get() = input.isNotEmpty() && error == null

    fun digit(digit: String): AmountEntryState {
        val current = if (replaceInput || error != null) "" else input
        val next = when {
            digit == "." && current.contains('.') -> return this
            digit == "." && current.isEmpty() -> "0."
            current == "0" && digit != "." -> digit
            current.count { it.isDigit() } >= 14 -> return this
            else -> current + digit
        }
        if (next.count { it.isDigit() } > 14) return this
        return copy(
            input = next,
            formula = if (operator == null) "" else formula,
            replaceInput = false,
            operatorsVisible = operatorsVisible || next.count { it.isDigit() } >= 2,
            error = null
        )
    }

    fun shortcut(multiplier: Double): AmountEntryState {
        val result = if (input.isEmpty()) multiplier else value * multiplier
        return result(result).copy(operatorsVisible = true, replaceInput = false)
    }

    fun backspace(): AmountEntryState {
        if (replaceInput && operator != null) {
            return copy(operator = null, firstOperand = null, formula = "", replaceInput = false)
        }
        val next = if (error != null) "" else input.dropLast(1)
        return copy(
            input = next, formula = if (operator == null) "" else formula,
            replaceInput = false, error = null,
            operatorsVisible = operator != null || next.count { it.isDigit() } >= 2
        )
    }

    fun operation(nextOperator: String): AmountEntryState {
        if (!canConvert) return this
        if (operator != null && replaceInput) {
            return copy(operator = nextOperator, formula = "${format(firstOperand!!)} $nextOperator")
        }
        val evaluated = if (operator != null) equals() else this
        if (evaluated.error != null) return evaluated
        return evaluated.copy(
            firstOperand = evaluated.value, operator = nextOperator,
            formula = "${format(evaluated.value)} $nextOperator",
            replaceInput = true, operatorsVisible = true
        )
    }

    // Percent applies to the last entered operand, then completes the pending calculation.
    fun percent(): AmountEntryState {
        if (!canConvert || (operator != null && replaceInput)) return this
        return copy(input = format(value / 100.0)).equals(operandLabel = "$input%")
    }

    fun equals(operandLabel: String = input): AmountEntryState {
        if (!canConvert || (operator != null && replaceInput)) return this
        val second = value
        val first = firstOperand ?: second
        if (operator == "÷" && second == 0.0) return copy(error = "Cannot divide by zero")
        val answer = when (operator) {
            "+" -> first + second
            "−" -> first - second
            "×" -> first * second
            "÷" -> first / second
            else -> second
        }
        return result(answer).copy(
            formula = if (operator == null) {
                if (operandLabel.endsWith('%')) "$operandLabel =" else formula
            } else "${format(first)} $operator $operandLabel =",
            firstOperand = null, operator = null, replaceInput = true, operatorsVisible = true
        )
    }

    private fun result(number: Double): AmountEntryState =
        if (!number.isFinite()) copy(error = "Amount is too large")
        else copy(input = format(number), error = null)

    private fun format(number: Double): String = BigDecimal.valueOf(number)
        .setScale(12, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
}
