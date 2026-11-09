package com.example.model

data class CurrencyItem(
    val code: String,
    val name: String,
    val symbol: String,
    val countryName: String,
    val decimals: Int = 2,
    val defaultRateToUsd: Double,
    val isSmallUnit: Boolean = false // e.g. VND, IDR, KRW
)

object SupportedCurrencies {
    val ALL: List<CurrencyItem> = listOf(
        CurrencyItem(
            code = "USD",
            name = "United States Dollar",
            symbol = "$",
            countryName = "United States",
            decimals = 2,
            defaultRateToUsd = 1.0
        ),
        CurrencyItem(
            code = "SGD",
            name = "Singapore Dollar",
            symbol = "S$",
            countryName = "Singapore",
            decimals = 2,
            defaultRateToUsd = 1.2817
        ),
        CurrencyItem(
            code = "VND",
            name = "Vietnamese Dong",
            symbol = "₫",
            countryName = "Vietnam",
            decimals = 0,
            defaultRateToUsd = 25493.9,
            isSmallUnit = true
        ),
        CurrencyItem(
            code = "IDR",
            name = "Indonesian Rupiah",
            symbol = "Rp",
            countryName = "Indonesia",
            decimals = 0,
            defaultRateToUsd = 15680.0,
            isSmallUnit = true
        ),
        CurrencyItem(
            code = "CNY",
            name = "Chinese Yuan",
            symbol = "CN¥",
            countryName = "China",
            decimals = 2,
            defaultRateToUsd = 6.6749
        ),
        CurrencyItem(
            code = "MYR",
            name = "Malaysian Ringgit",
            symbol = "RM",
            countryName = "Malaysia",
            decimals = 2,
            defaultRateToUsd = 4.0613
        ),
        CurrencyItem(
            code = "EUR",
            name = "Euro",
            symbol = "€",
            countryName = "European Union",
            decimals = 2,
            defaultRateToUsd = 0.89656
        ),
        CurrencyItem(
            code = "JPY",
            name = "Japanese Yen",
            symbol = "¥",
            countryName = "Japan",
            decimals = 1,
            defaultRateToUsd = 158.304
        ),
        CurrencyItem(
            code = "THB",
            name = "Thai Baht",
            symbol = "฿",
            countryName = "Thailand",
            decimals = 2,
            defaultRateToUsd = 33.531
        ),
        CurrencyItem(
            code = "CAD",
            name = "Canadian Dollar",
            symbol = "CA$",
            countryName = "Canada",
            decimals = 2,
            defaultRateToUsd = 1.41144
        ),
        CurrencyItem(
            code = "HKD",
            name = "Hong Kong Dollar",
            symbol = "HK$",
            countryName = "Hong Kong",
            decimals = 2,
            defaultRateToUsd = 7.84086
        ),
        CurrencyItem(
            code = "KRW",
            name = "South Korean Won",
            symbol = "₩",
            countryName = "South Korea",
            decimals = 0,
            defaultRateToUsd = 1380.5,
            isSmallUnit = true
        ),
        CurrencyItem(
            code = "TWD",
            name = "Taiwan Dollar",
            symbol = "NT$",
            countryName = "Taiwan",
            decimals = 2,
            defaultRateToUsd = 32.15
        )
    )

    val MAP: Map<String, CurrencyItem> = ALL.associateBy { it.code }

    val DEFAULT_SELECTED_CODES: List<String> = listOf(
        "USD", "SGD", "VND", "IDR", "CNY", "MYR", "EUR", "JPY", "THB", "CAD", "HKD"
    )
}
