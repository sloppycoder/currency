package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CurrencyItem
import com.example.model.SupportedCurrencies
import com.example.ui.components.CalculatorSheet
import com.example.ui.components.CountryFlag
import com.example.ui.components.CurrencyGestureList
import com.example.ui.theme.AppTheme
import com.example.viewmodel.MainViewModel

@Composable
fun ConvertScreen(
    viewModel: MainViewModel,
    onNavigateToAddCurrencies: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    var isEditMode by rememberSaveable { mutableStateOf(false) }
    val selectedCodes by viewModel.selectedCurrencies.collectAsState()
    val baseCode by viewModel.baseCurrency.collectAsState()
    val baseAmount by viewModel.baseAmount.collectAsState()
    val ratesState by viewModel.ratesState.collectAsState()
    val calculatorCurrency by viewModel.calculatorCurrency.collectAsState()

    // Read conversion inputs as Compose state so cached list rows refresh with every amount change.
    val convertedAmounts = remember(selectedCodes, baseCode, baseAmount, ratesState) {
        selectedCodes.associateWith { viewModel.getConvertedAmount(it) }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "refresh_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing)
        ),
        label = "spin_angle"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Navigation & Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Add Button
                IconButton(
                    onClick = onNavigateToAddCurrencies,
                    modifier = Modifier.testTag("add_currency_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Currency",
                        tint = colors.accentGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Refresh exchange rates
                    IconButton(
                        onClick = { viewModel.refreshRates() },
                        enabled = !ratesState.isLoading,
                        modifier = Modifier.testTag("refresh_rates_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Rates",
                            tint = if (ratesState.isLoading) colors.accentGreen else colors.textSecondary,
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(if (ratesState.isLoading) spinAngle else 0f)
                        )
                    }
                    IconToggleButton(
                        checked = isEditMode,
                        onCheckedChange = { isEditMode = it },
                        modifier = Modifier.testTag("edit_mode_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = if (isEditMode) "Finish editing currencies" else "Edit currencies",
                            tint = if (isEditMode) colors.accentGreen else colors.textSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

            }

            // Big Bold Screen Title "Currency"
            Text(
                text = "Currency",
                color = colors.textPrimary,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .testTag("screen_title")
            )

            // Subtle Status / Offline Indicator Pill
            val pillBg = if (ratesState.isOffline) colors.offlinePillBg else colors.normalPillBg
            val pillTextColor = if (ratesState.isOffline) colors.offlinePillText else colors.normalPillText
            val indicatorDotColor = if (ratesState.isOffline) colors.offlinePillText else colors.accentGreen

            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(pillBg)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(indicatorDotColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (ratesState.isOffline) {
                        "Offline • 24h cached rates (${viewModel.getLastUpdatedText()})"
                    } else {
                        "Live rates • ${viewModel.getLastUpdatedText()}"
                    },
                    color = pillTextColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            CurrencyGestureList(
                currencies = selectedCodes.mapNotNull { SupportedCurrencies.MAP[it] },
                isEditMode = isEditMode,
                onMove = viewModel::moveCurrency,
                onDelete = viewModel::removeCurrency,
                modifier = Modifier.weight(1f)
            ) { item, dragHandle ->
                CurrencyRow(
                    item = item,
                    isBase = item.code == baseCode,
                    isEditMode = isEditMode,
                    formattedAmount = viewModel.formatFormattedWithSymbol(
                        item, convertedAmounts.getValue(item.code)
                    ),
                    onRowClick = { viewModel.openCalculator(item) },
                    dragHandle = dragHandle
                )
            }
        }

        // Calculator Sheet when user taps a line
        calculatorCurrency?.let { curr ->
            CalculatorSheet(
                currency = curr,
                initialAmount = convertedAmounts.getValue(curr.code),
                onAmountChanged = { newAmount ->
                    viewModel.updateAmountFromCalculator(newAmount)
                },
                onDismiss = {
                    viewModel.closeCalculator()
                }
            )
        }
    }
}

@Composable
private fun CurrencyRow(
    item: CurrencyItem,
    isBase: Boolean,
    isEditMode: Boolean,
    formattedAmount: String,
    onRowClick: () -> Unit,
    dragHandle: @Composable () -> Unit
) {
    val colors = AppTheme.colors
    val rowBackground = if (isBase) colors.baseRowHighlight else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBackground)
            .clickable(enabled = !isEditMode, onClick = onRowClick)
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("currency_row_${item.code}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Flag
        CountryFlag(
            currencyCode = item.code,
            width = 46.dp,
            height = 32.dp
        )

        Spacer(modifier = Modifier.width(14.dp))

        // Currency Name & Code
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.name,
                color = colors.textPrimary,
                fontSize = 17.sp,
                fontWeight = if (isBase) FontWeight.Bold else FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.code,
                color = colors.textSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        val pillBg = if (isBase) colors.basePillBg else colors.normalPillBg
        val pillTextColor = if (isBase) colors.basePillText else colors.normalPillText

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = pillBg,
            modifier = Modifier
                .testTag("amount_pill_${item.code}")
        ) {
            Text(
                text = formattedAmount,
                color = pillTextColor,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
            )
        }
        dragHandle()
    }
}
