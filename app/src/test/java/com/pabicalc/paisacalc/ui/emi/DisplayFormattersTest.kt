package com.pabicalc.paisacalc.ui.emi

import com.pabicalc.paisacalc.engine.EmiEngine
import com.pabicalc.paisacalc.engine.LoanMonth
import com.pabicalc.paisacalc.engine.LoanTerms
import org.junit.Assert.assertEquals
import org.junit.Test

class DisplayFormattersTest {

    @Test
    fun tenureLabelsReadNaturally() {
        assertEquals("1 month", DisplayFormatters.tenureLabel(1))
        assertEquals("7 months", DisplayFormatters.tenureLabel(7))
        assertEquals("1 year", DisplayFormatters.tenureLabel(12))
        assertEquals("1 year 1 month", DisplayFormatters.tenureLabel(13))
        assertEquals("1 year 6 months", DisplayFormatters.tenureLabel(18))
        assertEquals("2 years", DisplayFormatters.tenureLabel(24))
        assertEquals("20 years", DisplayFormatters.tenureLabel(240))
        assertEquals("40 years", DisplayFormatters.tenureLabel(480))
    }

    @Test
    fun percentRoundsToOneDecimal() {
        assertEquals("89.6%", DisplayFormatters.percent(0.8963))
        assertEquals("0.0%", DisplayFormatters.percent(0.0))
        assertEquals("100.0%", DisplayFormatters.percent(1.0))
        assertEquals("52.0%", DisplayFormatters.percent(0.51997))
    }

    @Test
    fun rateLabelsTrimTrailingZeros() {
        assertEquals("8.5%", DisplayFormatters.rateLabel(8_500_000L))
        assertEquals("8.75%", DisplayFormatters.rateLabel(8_750_000L))
        assertEquals("8.75%", DisplayFormatters.rateLabel(8_755_000L))
        assertEquals("9.25%", DisplayFormatters.rateLabel(9_250_000L))
        assertEquals("12%", DisplayFormatters.rateLabel(12_000_000L))
        assertEquals("0%", DisplayFormatters.rateLabel(0L))
        assertEquals("36%", DisplayFormatters.rateLabel(36_000_000L))
    }

    @Test
    fun sliderRateTextMatchesTheFieldFormat() {
        assertEquals("8.75", DisplayFormatters.sliderRateText(8.75f))
        assertEquals("12", DisplayFormatters.sliderRateText(12f))
        assertEquals("1.05", DisplayFormatters.sliderRateText(1.05f))
        assertEquals("8.7", DisplayFormatters.sliderRateText(8.7f))
    }

    @Test
    fun installmentCountLabel() {
        assertEquals("1 installment", DisplayFormatters.installmentCount(1))
        assertEquals("240 installments", DisplayFormatters.installmentCount(240))
    }

    @Test
    fun heroLinesForARealLoan() {
        val summary = EmiEngine.calculate(LoanTerms(1_000_000L, 8_500_000L, 240), LoanMonth(2026, 10))
        assertEquals("20 years", DisplayFormatters.tenureLabel(summary.terms.tenureMonths))
        assertEquals("8.5%", DisplayFormatters.rateLabel(summary.terms.rateMicroPercent))
        assertEquals("Oct-26", summary.startMonth.shortLabel())
        assertEquals("Sep-46", summary.endMonth.shortLabel())
    }
}
