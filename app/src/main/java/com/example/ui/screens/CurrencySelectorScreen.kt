package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CurrencyItem
import com.example.model.SupportedCurrencies
import com.example.ui.components.CountryFlag
import com.example.ui.theme.AppTheme
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelectorScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val colors = AppTheme.colors

    val selectedCodes by viewModel.selectedCurrencies.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) {
            SupportedCurrencies.ALL
        } else {
            SupportedCurrencies.ALL.filter { item ->
                item.code.lowercase().contains(q) ||
                        item.name.lowercase().contains(q) ||
                        item.countryName.lowercase().contains(q)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("currency_selector_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.accentGreen
                    )
                }

                Text(
                    text = "Add / Edit Currencies",
                    color = colors.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                TextButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("currency_selector_done_btn")
                ) {
                    Text(
                        text = "Done",
                        color = colors.accentGreen,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_currency_input"),
                placeholder = {
                    Text("Search currency code, country...", color = colors.textSecondary)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = colors.textSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = colors.textSecondary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.accentGreen,
                    unfocusedBorderColor = colors.searchBarBorder,
                    focusedContainerColor = colors.searchBarBg,
                    unfocusedContainerColor = colors.searchBarBg,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                )
            )

            // Subtitle & Reset
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedCodes.size} of ${SupportedCurrencies.ALL.size} active",
                    color = colors.textSecondary,
                    fontSize = 13.sp
                )
                TextButton(
                    onClick = { viewModel.resetCurrenciesToDefault() },
                    modifier = Modifier.testTag("reset_currencies_btn")
                ) {
                    Text(
                        text = "Reset Default",
                        color = colors.accentGreen,
                        fontSize = 13.sp
                    )
                }
            }

            // Currency list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("selector_currency_list"),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredList, key = { it.code }) { item ->
                    val isSelected = selectedCodes.contains(item.code)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.toggleCurrencySelection(item.code)
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("currency_select_item_${item.code}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CountryFlag(currencyCode = item.code)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                color = colors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${item.code} (${item.symbol}) • ${item.countryName}",
                                color = colors.textSecondary,
                                fontSize = 13.sp
                            )
                        }

                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = {
                                viewModel.toggleCurrencySelection(item.code)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = colors.accentGreen,
                                uncheckedColor = colors.textSecondary,
                                checkmarkColor = colors.basePillText
                            ),
                            modifier = Modifier.testTag("checkbox_${item.code}")
                        )
                    }

                    HorizontalDivider(
                        color = colors.divider,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 72.dp)
                    )
                }
            }
        }
    }
}
