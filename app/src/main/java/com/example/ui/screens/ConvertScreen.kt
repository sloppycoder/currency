package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.ui.theme.AppTheme
import com.example.viewmodel.MainViewModel

@Composable
fun ConvertScreen(
    viewModel: MainViewModel,
    onNavigateToAddCurrencies: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val selectedCodes by viewModel.selectedCurrencies.collectAsState()
    val baseCode by viewModel.baseCurrency.collectAsState()
    val baseAmount by viewModel.baseAmount.collectAsState()
    val ratesState by viewModel.ratesState.collectAsState()
    val calculatorCurrency by viewModel.calculatorCurrency.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()

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

                // Right Actions: Refresh and Edit
                Row(verticalAlignment = Alignment.CenterVertically) {
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

                    TextButton(
                        onClick = { viewModel.toggleEditMode() },
                        modifier = Modifier.testTag("edit_mode_button")
                    ) {
                        Text(
                            text = if (isEditMode) "Done" else "Edit",
                            color = colors.accentGreen,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium
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

            // Currency List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("currency_list"),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                val currencies = selectedCodes.mapNotNull { SupportedCurrencies.MAP[it] }

                itemsIndexed(currencies, key = { _, item -> item.code }) { index, item ->
                    val isBase = item.code == baseCode
                    val convertedAmount = convertedAmounts.getValue(item.code)
                    val formattedPillText = viewModel.formatFormattedWithSymbol(item, convertedAmount)

                    CurrencyRow(
                        item = item,
                        isBase = isBase,
                        formattedAmount = formattedPillText,
                        isEditMode = isEditMode,
                        canMoveUp = index > 0,
                        canMoveDown = index < currencies.size - 1,
                        canDelete = currencies.size > 1,
                        onRowClick = {
                            if (!isEditMode) {
                                viewModel.openCalculator(item)
                            }
                        },
                        onMoveUp = { viewModel.moveCurrencyUp(index) },
                        onMoveDown = { viewModel.moveCurrencyDown(index) },
                        onDelete = { viewModel.toggleCurrencySelection(item.code) }
                    )

                    HorizontalDivider(
                        color = colors.divider,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 76.dp)
                    )
                }
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
    formattedAmount: String,
    isEditMode: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    canDelete: Boolean,
    onRowClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = AppTheme.colors
    val rowBackground = if (isBase) colors.baseRowHighlight else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBackground)
            .clickable(onClick = onRowClick)
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

        if (isEditMode) {
            // Reorder & Remove Actions in Edit mode
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onMoveUp,
                    enabled = canMoveUp,
                    modifier = Modifier.size(36.dp).testTag("move_up_${item.code}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Move Up",
                        tint = if (canMoveUp) colors.textSecondary else colors.cardBorder,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onMoveDown,
                    enabled = canMoveDown,
                    modifier = Modifier.size(36.dp).testTag("move_down_${item.code}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Move Down",
                        tint = if (canMoveDown) colors.textSecondary else colors.cardBorder,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp).testTag("delete_${item.code}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFFF453A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        } else {
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
        }
    }
}
