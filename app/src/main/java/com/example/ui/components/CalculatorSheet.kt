package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CurrencyItem
import com.example.ui.theme.AppTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorSheet(
    currency: CurrencyItem,
    initialAmount: Double,
    onAmountChanged: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var inputString by remember {
        mutableStateOf(
            if (initialAmount <= 0.0) "0"
            else if (currency.decimals == 0) initialAmount.toLong().toString()
            else String.format(Locale.US, "%.2f", initialAmount).trimEnd('0').trimEnd('.')
        )
    }

    var formulaDisplay by remember { mutableStateOf("") }
    var pendingOperator by remember { mutableStateOf<String?>(null) }
    var firstOperand by remember { mutableDoubleStateOf(0.0) }
    var shouldClearOnNextDigit by remember { mutableStateOf(false) }

    fun calculateCurrentValue(): Double {
        val cleaned = inputString.replace(",", "")
        return cleaned.toDoubleOrNull() ?: 0.0
    }

    fun notifyAmount(value: Double) {
        onAmountChanged(if (value < 0) 0.0 else value)
    }

    fun evaluatePending(secondOperand: Double): Double {
        val op = pendingOperator ?: return secondOperand
        return when (op) {
            "+" -> firstOperand + secondOperand
            "−" -> firstOperand - secondOperand
            "×" -> firstOperand * secondOperand
            "÷" -> if (secondOperand != 0.0) firstOperand / secondOperand else firstOperand
            else -> secondOperand
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag handle pill
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.cardBorder)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Header row with Flag, Name, and Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CountryFlag(currencyCode = currency.code)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = currency.name,
                            color = colors.textPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${currency.code} • Base Currency",
                            color = colors.accentGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("calculator_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Value Display Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                color = colors.cardBackground,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    if (formulaDisplay.isNotEmpty()) {
                        Text(
                            text = formulaDisplay,
                            color = colors.textSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = currency.symbol,
                            color = colors.accentGreen,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = inputString,
                            color = colors.textPrimary,
                            fontSize = if (inputString.length > 10) 28.sp else 36.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            // Quick High-Denomination Buttons (for VND, IDR, KRW)
            if (currency.isSmallUnit) {
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(
                        "+10K" to 10_000.0,
                        "+50K" to 50_000.0,
                        "+100K" to 100_000.0,
                        "+500K" to 500_000.0,
                        "+1M" to 1_000_000.0
                    )
                    items(presets) { (label, addAmount) ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = colors.keypadPresetBg,
                            modifier = Modifier
                                .testTag("preset_$label")
                                .clickable {
                                    val currentVal = calculateCurrentValue()
                                    val newVal = currentVal + addAmount
                                    inputString = if (newVal % 1.0 == 0.0) newVal.toLong().toString() else newVal.toString()
                                    notifyAmount(newVal)
                                    shouldClearOnNextDigit = false
                                }
                        ) {
                            Text(
                                text = label,
                                color = colors.normalPillText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Calculator Keypad Grid
            val numberBg = colors.keypadNumberBg
            val operatorBg = colors.keypadOperatorBg
            val accentBg = colors.accentGreen

            // Row 1: C, ±, ⌫, ÷
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KeypadButton(
                    text = "C",
                    bgColor = operatorBg,
                    textColor = Color(0xFFFF453A),
                    modifier = Modifier.weight(1f)
                ) {
                    inputString = "0"
                    formulaDisplay = ""
                    pendingOperator = null
                    firstOperand = 0.0
                    shouldClearOnNextDigit = false
                    notifyAmount(0.0)
                }

                KeypadButton(
                    text = "±",
                    bgColor = operatorBg,
                    textColor = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                ) {
                    val v = calculateCurrentValue()
                    val inverted = -v
                    inputString = if (inverted % 1.0 == 0.0) inverted.toLong().toString() else inverted.toString()
                    notifyAmount(inverted)
                }

                KeypadButton(
                    icon = Icons.Default.Backspace,
                    bgColor = operatorBg,
                    textColor = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                ) {
                    if (inputString.length > 1) {
                        inputString = inputString.dropLast(1)
                    } else {
                        inputString = "0"
                    }
                    notifyAmount(calculateCurrentValue())
                }

                KeypadButton(
                    text = "÷",
                    bgColor = if (pendingOperator == "÷") accentBg else operatorBg,
                    textColor = if (pendingOperator == "÷") colors.basePillText else colors.accentGreen,
                    modifier = Modifier.weight(1f)
                ) {
                    firstOperand = calculateCurrentValue()
                    pendingOperator = "÷"
                    formulaDisplay = "$firstOperand ÷"
                    shouldClearOnNextDigit = true
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: 7, 8, 9, ×
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("7", "8", "9").forEach { digit ->
                    KeypadButton(
                        text = digit,
                        bgColor = numberBg,
                        textColor = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    ) {
                        appendDigit(
                            digit = digit,
                            current = inputString,
                            shouldClear = shouldClearOnNextDigit,
                            onUpdate = { newStr ->
                                inputString = newStr
                                shouldClearOnNextDigit = false
                                notifyAmount(calculateCurrentValue())
                            }
                        )
                    }
                }
                KeypadButton(
                    text = "×",
                    bgColor = if (pendingOperator == "×") accentBg else operatorBg,
                    textColor = if (pendingOperator == "×") colors.basePillText else colors.accentGreen,
                    modifier = Modifier.weight(1f)
                ) {
                    firstOperand = calculateCurrentValue()
                    pendingOperator = "×"
                    formulaDisplay = "$firstOperand ×"
                    shouldClearOnNextDigit = true
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 3: 4, 5, 6, −
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("4", "5", "6").forEach { digit ->
                    KeypadButton(
                        text = digit,
                        bgColor = numberBg,
                        textColor = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    ) {
                        appendDigit(
                            digit = digit,
                            current = inputString,
                            shouldClear = shouldClearOnNextDigit,
                            onUpdate = { newStr ->
                                inputString = newStr
                                shouldClearOnNextDigit = false
                                notifyAmount(calculateCurrentValue())
                            }
                        )
                    }
                }
                KeypadButton(
                    text = "−",
                    bgColor = if (pendingOperator == "−") accentBg else operatorBg,
                    textColor = if (pendingOperator == "−") colors.basePillText else colors.accentGreen,
                    modifier = Modifier.weight(1f)
                ) {
                    firstOperand = calculateCurrentValue()
                    pendingOperator = "−"
                    formulaDisplay = "$firstOperand −"
                    shouldClearOnNextDigit = true
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 4: 1, 2, 3, +
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("1", "2", "3").forEach { digit ->
                    KeypadButton(
                        text = digit,
                        bgColor = numberBg,
                        textColor = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    ) {
                        appendDigit(
                            digit = digit,
                            current = inputString,
                            shouldClear = shouldClearOnNextDigit,
                            onUpdate = { newStr ->
                                inputString = newStr
                                shouldClearOnNextDigit = false
                                notifyAmount(calculateCurrentValue())
                            }
                        )
                    }
                }
                KeypadButton(
                    text = "+",
                    bgColor = if (pendingOperator == "+") accentBg else operatorBg,
                    textColor = if (pendingOperator == "+") colors.basePillText else colors.accentGreen,
                    modifier = Modifier.weight(1f)
                ) {
                    firstOperand = calculateCurrentValue()
                    pendingOperator = "+"
                    formulaDisplay = "$firstOperand +"
                    shouldClearOnNextDigit = true
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 5: Tailored for small units (K, 100K) or standard (00, .)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KeypadButton(
                    text = "0",
                    bgColor = numberBg,
                    textColor = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                ) {
                    appendDigit(
                        digit = "0",
                        current = inputString,
                        shouldClear = shouldClearOnNextDigit,
                        onUpdate = { newStr ->
                            inputString = newStr
                            shouldClearOnNextDigit = false
                            notifyAmount(calculateCurrentValue())
                        }
                    )
                }

                if (currency.isSmallUnit) {
                    KeypadButton(
                        text = "K",
                        bgColor = colors.keypadPresetBg,
                        textColor = colors.normalPillText,
                        modifier = Modifier.weight(1f)
                    ) {
                        val current = calculateCurrentValue()
                        val newVal = if (current == 0.0) 1000.0 else current * 1000.0
                        inputString = newVal.toLong().toString()
                        notifyAmount(newVal)
                        shouldClearOnNextDigit = false
                    }

                    KeypadButton(
                        text = "100K",
                        bgColor = colors.keypadPresetBg,
                        textColor = colors.normalPillText,
                        modifier = Modifier.weight(1f)
                    ) {
                        val current = calculateCurrentValue()
                        val newVal = if (current == 0.0) 100000.0 else current * 100000.0
                        inputString = newVal.toLong().toString()
                        notifyAmount(newVal)
                        shouldClearOnNextDigit = false
                    }
                } else {
                    KeypadButton(
                        text = "00",
                        bgColor = numberBg,
                        textColor = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    ) {
                        appendDigit(
                            digit = "00",
                            current = inputString,
                            shouldClear = shouldClearOnNextDigit,
                            onUpdate = { newStr ->
                                inputString = newStr
                                shouldClearOnNextDigit = false
                                notifyAmount(calculateCurrentValue())
                            }
                        )
                    }

                    KeypadButton(
                        text = ".",
                        bgColor = numberBg,
                        textColor = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (shouldClearOnNextDigit) {
                            inputString = "0."
                            shouldClearOnNextDigit = false
                        } else if (!inputString.contains(".")) {
                            inputString = "$inputString."
                        }
                    }
                }

                // "=" button evaluates arithmetic
                KeypadButton(
                    text = "=",
                    bgColor = Color(0xFF007AFF),
                    textColor = Color.White,
                    modifier = Modifier.weight(1f)
                ) {
                    val second = calculateCurrentValue()
                    val result = evaluatePending(second)
                    formulaDisplay = if (pendingOperator != null) "$firstOperand $pendingOperator $second =" else ""
                    inputString = if (result % 1.0 == 0.0) result.toLong().toString() else String.format(Locale.US, "%.2f", result).trimEnd('0').trimEnd('.')
                    pendingOperator = null
                    shouldClearOnNextDigit = true
                    notifyAmount(result)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Done Button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("calculator_done_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accentGreen,
                    contentColor = colors.basePillText
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Done",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun appendDigit(
    digit: String,
    current: String,
    shouldClear: Boolean,
    onUpdate: (String) -> Unit
) {
    if (shouldClear || current == "0") {
        onUpdate(digit)
    } else {
        if (current.length < 14) {
            onUpdate(current + digit)
        }
    }
}

@Composable
private fun KeypadButton(
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    bgColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(54.dp)
            .border(0.5.dp, colors.cardBorder, RoundedCornerShape(14.dp))
            .testTag("keypad_btn_${text ?: "icon"}"),
        shape = RoundedCornerShape(14.dp),
        color = bgColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(22.dp)
                )
            } else if (text != null) {
                Text(
                    text = text,
                    color = textColor,
                    fontSize = if (text.length > 2) 16.sp else 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
