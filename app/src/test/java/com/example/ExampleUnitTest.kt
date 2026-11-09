package com.example

import com.example.model.SupportedCurrencies
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testSupportedCurrenciesList() {
        val codes = SupportedCurrencies.ALL.map { it.code }
        assertTrue(codes.contains("USD"))
        assertTrue(codes.contains("SGD"))
        assertTrue(codes.contains("VND"))
        assertTrue(codes.contains("IDR"))
        assertTrue(codes.contains("KRW"))
        assertTrue(codes.contains("CAD"))
        assertTrue(codes.contains("TWD"))
        assertTrue(codes.contains("EUR"))
        assertTrue(codes.contains("CNY"))
        assertTrue(codes.contains("MYR"))
        assertTrue(codes.contains("JPY"))
        assertTrue(codes.contains("HKD"))
        assertTrue(codes.contains("THB"))
    }

    @Test
    fun testConversionCalculation() {
        val fromRate = 1.0
        val toRate = 1.2817
        val converted = (1000.0 / fromRate) * toRate
        assertEquals(1281.7, converted, 0.01)
    }

    @Test
    fun testSmallUnitCurrencies() {
        val vnd = SupportedCurrencies.MAP["VND"]
        val idr = SupportedCurrencies.MAP["IDR"]
        val krw = SupportedCurrencies.MAP["KRW"]
        assertTrue(vnd?.isSmallUnit == true)
        assertTrue(idr?.isSmallUnit == true)
        assertTrue(krw?.isSmallUnit == true)
    }
}
