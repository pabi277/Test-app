package com.pabicalc.paisacalc.ui.emi

import com.pabicalc.paisacalc.engine.EmiEngine
import com.pabicalc.paisacalc.engine.LoanMonth
import com.pabicalc.paisacalc.engine.LoanTerms
import com.pabicalc.paisacalc.engine.PrepaymentFrequency
import com.pabicalc.paisacalc.engine.PrepaymentStrategy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The editor's text -> plan path: a blank amount is "no prepayment", and the
 * amount/start-month values the Techinfornity review typed must end up in a
 * plan that really changes the schedule.
 */
class PrepaymentInputParserTest {

    private val startMonth = LoanMonth(2026, 10)

    private fun parse(
        amount: String = "",
        from: String = "13",
        frequency: PrepaymentFrequency = PrepaymentFrequency.QUARTERLY,
        strategy: PrepaymentStrategy = PrepaymentStrategy.REDUCE_TENURE,
    ) = PrepaymentInputParser.parse(
        PrepaymentInput(
            amountText = amount,
            fromInstallmentText = from,
            frequency = frequency,
            strategy = strategy,
        ),
    )

    @Test
    fun blankAmountMeansNoPrepayment() {
        assertTrue(parse() is PrepaymentParseResult.Inactive)
        assertTrue(parse(amount = "") is PrepaymentParseResult.Inactive)
    }

    @Test
    fun typedAmountAndStartMonthBecomeAPlan() {
        val result = parse(amount = "25000", from = "13")
        assertTrue(result is PrepaymentParseResult.Active)
        val prepayment = (result as PrepaymentParseResult.Active).plan.prepayments.single()
        assertEquals(PrepaymentFrequency.QUARTERLY, prepayment.frequency)
        assertEquals(13, prepayment.fromInstallment)
        assertEquals(25_000L, prepayment.amount)
        assertEquals(PrepaymentStrategy.REDUCE_TENURE, prepayment.strategy)
    }

    @Test
    fun groupedAmountTextStillParses() {
        val result = parse(amount = "25,000")
        assertEquals(
            25_000L,
            (result as PrepaymentParseResult.Active).plan.prepayments.single().amount,
        )
    }

    @Test
    fun theFullEditorFlowRecomputesTheSchedule() {
        // The store review's flow end to end: type amount + start month, hit
        // save (here: recompute) and the schedule must actually change.
        val terms = LoanTerms(1_000_000L, 8_500_000L, 240)
        val plan = (parse(amount = "25000", from = "13") as PrepaymentParseResult.Active).plan
        val baseline = EmiEngine.calculate(terms, startMonth)
        val prepaid = EmiEngine.calculate(terms, startMonth, plan)

        assertEquals(240, baseline.installments.size)
        assertEquals(85, prepaid.installments.size)
        assertEquals(720_524L, prepaid.interestSaved)
        assertTrue(prepaid.totalPrepaid > 0L)
    }

    @Test
    fun outOfRangeValuesAreRejectedWithTheRightField() {
        val tooSmall = parse(amount = "0")
        assertTrue(tooSmall is PrepaymentParseResult.Invalid)
        assertEquals(PrepaymentField.AMOUNT, (tooSmall as PrepaymentParseResult.Invalid).issues.single().field)

        val tooLarge = parse(amount = "1000000001")
        assertEquals(
            PrepaymentField.AMOUNT,
            (tooLarge as PrepaymentParseResult.Invalid).issues.single().field,
        )

        val badFrom = parse(amount = "25000", from = "0")
        assertEquals(
            PrepaymentField.FROM_INSTALLMENT,
            (badFrom as PrepaymentParseResult.Invalid).issues.single().field,
        )

        val wayTooLate = parse(amount = "25000", from = "481")
        assertEquals(
            PrepaymentField.FROM_INSTALLMENT,
            (wayTooLate as PrepaymentParseResult.Invalid).issues.single().field,
        )
    }

    @Test
    fun bothFieldsCanBeWrongAtOnce() {
        val result = parse(amount = "0", from = "0")
        assertEquals(2, (result as PrepaymentParseResult.Invalid).issues.size)
    }

    @Test
    fun sanitizerKeepsFieldsTypeable() {
        assertEquals("25000", PrepaymentInputSanitizer.amount("2,5a0-0 0"))
        assertEquals("123", PrepaymentInputSanitizer.installment("12x3"))
        assertEquals("", PrepaymentInputSanitizer.amount("abc"))
    }

    @Test
    fun defaultsAreTheReviewedScenario() {
        val input = PrepaymentInput()
        assertEquals("", input.amountText)
        assertEquals(PrepaymentFrequency.QUARTERLY, input.frequency)
        assertEquals(PrepaymentStrategy.REDUCE_TENURE, input.strategy)
        assertFalse(parse(amount = "1").let { it is PrepaymentParseResult.Inactive })
    }
}
