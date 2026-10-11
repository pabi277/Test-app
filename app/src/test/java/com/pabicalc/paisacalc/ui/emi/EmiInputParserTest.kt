package com.pabicalc.paisacalc.ui.emi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EmiInputParserTest {

    private fun parse(amount: String = "", rate: String = "", tenure: String = "", unit: TenureUnit = TenureUnit.YEARS) =
        EmiInputParser.parse(EmiInput(amount, rate, tenure, unit))

    @Test
    fun blankFieldsMeanNotFinishedTyping() {
        assertTrue(parse() is EmiParseResult.Incomplete)
        assertTrue(parse(amount = "1000000") is EmiParseResult.Incomplete)
        assertTrue(parse(amount = "1000000", rate = "8.5") is EmiParseResult.Incomplete)
        assertTrue(parse(amount = "1000000", tenure = "20") is EmiParseResult.Incomplete)
    }

    @Test
    fun validInputProducesLoanTerms() {
        val result = parse(amount = "1000000", rate = "8.5", tenure = "20")
        assertTrue(result is EmiParseResult.Valid)
        val terms = (result as EmiParseResult.Valid).terms
        assertEquals(1_000_000L, terms.principal)
        assertEquals(8_500_000L, terms.rateMicroPercent)
        assertEquals(240, terms.tenureMonths)
    }

    @Test
    fun tenureMonthsUnitIsNotMultiplied() {
        val result = parse(amount = "500000", rate = "12", tenure = "60", unit = TenureUnit.MONTHS)
        assertEquals(60, (result as EmiParseResult.Valid).terms.tenureMonths)
    }

    @Test
    fun groupedAmountTextStillParses() {
        val result = parse(amount = "25,00,000", rate = "9.25", tenure = "15")
        assertEquals(2_500_000L, (result as EmiParseResult.Valid).terms.principal)
    }

    @Test
    fun zeroRateIsAllowedAndPartiallyTypedRatesAreTolerated() {
        assertEquals(0L, (parse(amount = "100000", rate = "0", tenure = "1") as EmiParseResult.Valid).terms.rateMicroPercent)
        assertEquals(8_000_000L, (parse(amount = "100000", rate = "8.", tenure = "1") as EmiParseResult.Valid).terms.rateMicroPercent)
        assertEquals(500_000L, (parse(amount = "100000", rate = ".5", tenure = "1") as EmiParseResult.Valid).terms.rateMicroPercent)
    }

    @Test
    fun rateTextParsesExactlyWithoutFloatingPoint() {
        assertEquals(8_750_000L, EmiInputParser.rateMicroPercent("8.75"))
        assertEquals(100_000L, EmiInputParser.rateMicroPercent("0.1"))
        assertEquals(500_000L, EmiInputParser.rateMicroPercent("0.5"))
        assertEquals(12_000_000L, EmiInputParser.rateMicroPercent("12"))
        assertEquals(36_000_000L, EmiInputParser.rateMicroPercent("36"))
        assertEquals(null, EmiInputParser.rateMicroPercent(""))
        assertEquals(null, EmiInputParser.rateMicroPercent("."))
    }

    @Test
    fun outOfRangeValuesReportTheRightField() {
        assertEquals(listOf(FieldIssue.AMOUNT_NOT_POSITIVE), (parse(amount = "0", rate = "8", tenure = "1") as EmiParseResult.Invalid).issues)
        assertEquals(listOf(FieldIssue.AMOUNT_TOO_LARGE), (parse(amount = "1000000001", rate = "8", tenure = "1") as EmiParseResult.Invalid).issues)
        assertEquals(listOf(FieldIssue.RATE_INVALID), (parse(amount = "100000", rate = "36.5", tenure = "1") as EmiParseResult.Invalid).issues)
        assertEquals(listOf(FieldIssue.TENURE_NOT_POSITIVE), (parse(amount = "100000", rate = "8", tenure = "0") as EmiParseResult.Invalid).issues)
        assertEquals(listOf(FieldIssue.TENURE_TOO_LONG), (parse(amount = "100000", rate = "8", tenure = "41") as EmiParseResult.Invalid).issues)
        assertEquals(listOf(FieldIssue.TENURE_TOO_LONG), (parse(amount = "100000", rate = "8", tenure = "481", unit = TenureUnit.MONTHS) as EmiParseResult.Invalid).issues)
    }

    @Test
    fun boundaryValuesAreAccepted() {
        assertTrue(parse(amount = "1", rate = "36", tenure = "480", unit = TenureUnit.MONTHS) is EmiParseResult.Valid)
        assertTrue(parse(amount = "1000000000", rate = "0", tenure = "1") is EmiParseResult.Valid)
    }

    @Test
    fun severalIssuesAreReportedTogether() {
        val issues = (parse(amount = "0", rate = "50", tenure = "0") as EmiParseResult.Invalid).issues
        assertEquals(3, issues.size)
        assertEquals(setOf(EmiField.AMOUNT, EmiField.RATE, EmiField.TENURE), issues.map { it.field }.toSet())
    }

    @Test
    fun parseResultsMapToTheFourUiStates() {
        assertEquals(EmiUiState.Empty, EmiParseResult.Incomplete.toUiState())
        assertEquals(
            EmiUiState.Invalid(listOf(FieldIssue.RATE_INVALID)),
            EmiParseResult.Invalid(listOf(FieldIssue.RATE_INVALID)).toUiState(),
        )
        assertEquals(EmiUiState.Loading, (parse(amount = "100000", rate = "8", tenure = "1") as EmiParseResult).toUiState())
        assertEquals(emptyMap<EmiField, FieldIssue>(), EmiUiState.Empty.issuesByField())
        assertEquals(
            mapOf(EmiField.RATE to FieldIssue.RATE_INVALID),
            EmiUiState.Invalid(listOf(FieldIssue.RATE_INVALID)).issuesByField(),
        )
    }

    @Test
    fun sanitizersKeepTextTypeableAndLegal() {
        assertEquals("1234", EmiInputSanitizer.amount("12a34"))
        assertEquals("123456789012", EmiInputSanitizer.amount("1234567890123456"))
        assertEquals("8.75", EmiInputSanitizer.rate("8.756"))
        assertEquals("8.56", EmiInputSanitizer.rate("8.5.6"))
        assertEquals("123.56", EmiInputSanitizer.rate("1234.56"))
        assertEquals("8.", EmiInputSanitizer.rate("8."))
        assertEquals("0.05", EmiInputSanitizer.rate("0.05"))
        assertEquals("240", EmiInputSanitizer.tenure("24a0"))
    }
}
