package com.pabicalc.paisacalc.gst

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GstInputParserTest {

    @Test
    fun blankAmountIsIncomplete() {
        assertTrue(GstInputParser.parse(GstInput()) is GstParseResult.Incomplete)
    }

    @Test
    fun parsesRupeesAndPaiseWithoutFloatingPoint() {
        assertEquals(123_456L, GstInputParser.parseAmountPaise("1,234.56"))
        assertEquals(100L, GstInputParser.parseAmountPaise("1"))
        assertEquals(5L, GstInputParser.parseAmountPaise(".05"))
        assertEquals(50L, GstInputParser.parseAmountPaise("0.5"))
    }

    @Test
    fun parsesRatesExactlyAndRejectsValuesOverTheCap() {
        assertEquals(125_000L, GstCalculator.parseRateMicroPercent("0.125"))
        assertEquals(18_000_000L, GstCalculator.parseRateMicroPercent("18"))
        assertEquals(100_000_000L, GstCalculator.parseRateMicroPercent("100"))
        assertEquals(null, GstCalculator.parseRateMicroPercent("100.000001"))
        assertEquals(null, GstCalculator.parseRateMicroPercent("1.0000001"))
        assertEquals(null, GstCalculator.parseRateMicroPercent("-1"))
    }

    @Test
    fun formParserCreatesCorrectAddAndRemoveCalculations() {
        val added = GstInputParser.parse(
            GstInput(
                amountText = "1,000.00",
                rateText = "18",
                mode = GstCalculationMode.ADD,
                supplyType = GstSupplyType.INTRA_STATE,
            ),
        ) as GstParseResult.Valid
        val removed = GstInputParser.parse(
            GstInput(
                amountText = "1180.00",
                rateText = "18",
                mode = GstCalculationMode.REMOVE,
                supplyType = GstSupplyType.INTER_STATE,
            ),
        ) as GstParseResult.Valid

        assertEquals(118_000L, added.calculation.totalAmountPaise)
        assertEquals(100_000L, removed.calculation.taxableValuePaise)
        assertEquals(18_000L, removed.calculation.igstPaise)
    }

    @Test
    fun reportsBadAmountAndRateFields() {
        val tooLarge = GstInputParser.parse(
            GstInput(amountText = "1000000000.01", rateText = "18"),
        ) as GstParseResult.Invalid
        assertTrue(GstFieldIssue.AMOUNT_TOO_LARGE in tooLarge.issues)

        val badRate = GstInputParser.parse(
            GstInput(amountText = "100", rateText = "101"),
        ) as GstParseResult.Invalid
        assertTrue(GstFieldIssue.RATE_INVALID in badRate.issues)

        val zero = GstInputParser.parse(
            GstInput(amountText = "0.00", rateText = "18"),
        ) as GstParseResult.Invalid
        assertTrue(GstFieldIssue.AMOUNT_NOT_POSITIVE in zero.issues)
    }

    @Test
    fun inputSanitizerKeepsOnlyEditableAmountAndRateText() {
        assertEquals("1234.50", GstInputSanitizer.amount("₹1,23a4.501"))
        assertEquals("0.125", GstInputSanitizer.rate("0.12a5"))
    }
}
