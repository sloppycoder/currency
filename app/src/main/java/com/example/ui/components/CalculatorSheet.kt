package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorSheet(
    currency: CurrencyItem,
    onConvert: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var draft by remember(currency.code) { mutableStateOf(AmountEntryState()) }
    val shortcuts = remember(currency.code) { amountShortcuts(currency.code) }

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
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
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
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    CountryFlag(currencyCode = currency.code)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currency.name,
                            color = colors.textPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currency.code,
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
                    if (draft.formula.isNotEmpty()) {
                        Text(
                            text = draft.formula,
                            color = colors.textSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (draft.input.isNotEmpty()) {
                            Text(
                                text = currency.symbol,
                                color = colors.accentGreen,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                        }
                        Text(
                            text = draft.error ?: draft.input.ifEmpty { "Enter amount" },
                            color = if (draft.input.isEmpty()) colors.textSecondary else colors.textPrimary,
                            fontSize = if (draft.error != null || draft.input.isEmpty()) 24.sp
                                else if (draft.input.length > 10) 28.sp else 36.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f, fill = false).testTag("calculator_amount")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KeypadButton(
                    text = "C", bgColor = colors.keypadOperatorBg,
                    textColor = Color(0xFFFF453A), modifier = Modifier.weight(1f)
                ) { draft = AmountEntryState() }
                KeypadButton(
                    text = "%", bgColor = colors.keypadOperatorBg,
                    textColor = colors.accentGreen, modifier = Modifier.weight(1f)
                ) { draft = draft.percent() }
                KeypadButton(
                    icon = Icons.Default.Backspace, bgColor = colors.keypadOperatorBg,
                    textColor = colors.textPrimary, modifier = Modifier.weight(1f)
                ) { draft = draft.backspace() }
                KeypadButton(
                    text = "=", bgColor = colors.accentGreen,
                    textColor = colors.basePillText, modifier = Modifier.weight(1f)
                ) { draft = draft.equals() }
            }

            val numberRows = listOf(listOf("7", "8", "9"), listOf("4", "5", "6"),
                listOf("1", "2", "3"), listOf("0", ".", "00"))
            val operators = listOf("÷", "×", "−", "+")
            numberRows.forEachIndexed { index, digits ->
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    digits.forEach { digit ->
                        KeypadButton(
                            text = digit, bgColor = colors.keypadNumberBg,
                            textColor = colors.textPrimary, modifier = Modifier.weight(1f)
                        ) { draft = draft.digit(digit) }
                    }
                    val selected = draft.operatorsVisible && draft.operator == operators[index]
                    KeypadButton(
                        text = if (draft.operatorsVisible) operators[index] else shortcuts[index].label,
                        tag = if (!draft.operatorsVisible && index == 3) "shortcut_btn_1" else null,
                        bgColor = if (selected) colors.accentGreen else colors.keypadOperatorBg,
                        textColor = if (selected) colors.basePillText else colors.accentGreen,
                        modifier = Modifier.weight(1f)
                    ) {
                        draft = if (draft.operatorsVisible) draft.operation(operators[index])
                            else draft.shortcut(shortcuts[index].multiplier)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Commit the displayed amount (or pending calculation) before closing.
            Button(
                onClick = {
                    val evaluated = draft.equals()
                    draft = evaluated
                    if (evaluated.canConvert) onConvert(evaluated.value.coerceAtLeast(0.0))
                },
                enabled = draft.canConvert,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("calculator_convert_button"),
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
                    text = "Convert",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun KeypadButton(
    modifier: Modifier = Modifier,
    text: String? = null,
    tag: String? = null,
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
            .testTag(tag ?: "keypad_btn_${text ?: "icon"}"),
        shape = RoundedCornerShape(14.dp),
        color = bgColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Backspace",
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
