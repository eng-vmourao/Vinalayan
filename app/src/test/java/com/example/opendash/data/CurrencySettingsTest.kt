package com.example.opendash.data

import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencySettingsTest {
    @Test
    fun brazilianRealIsTheDefaultCurrency() {
        assertEquals(OpenDashCurrency.BRL, CurrencySettings.defaultCurrency)
        assertEquals("BRL", CurrencySettings.defaultCurrency.code)
        assertEquals("R$", CurrencySettings.defaultCurrency.symbol)
    }

    @Test
    fun brazilianRealUsesBrazilianNumberFormatting() {
        assertEquals("R$ 1.234,56", formatCurrencyAmount(1234.56, OpenDashCurrency.BRL, 2))
    }

    @Test
    fun existingCurrencyFormattingRemainsUnchanged() {
        assertEquals("$1,234.56", formatCurrencyAmount(1234.56, OpenDashCurrency.USD, 2))
    }
}
