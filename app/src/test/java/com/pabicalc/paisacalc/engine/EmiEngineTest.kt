package com.pabicalc.paisacalc.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The acceptance suite for the EMI engine.
 *
 * Golden values were produced by an independent, exact-decimal implementation
 * of the same policy (`tools/emi_reference.py`) and cross-checked against
 * published bank examples: the two implementations agree on every row of every
 * case, and 4 of 5 published EMIs match to the rupee (the 5th, Ujjivan's
 * "10L @ 8.75% x 15y = 9,934", contradicts its own quoted rate — the formula
 * value is 9,994 — so it is deliberately not used as an expectation).
 */
class EmiEngineTest {

    private data class Golden(
        val name: String,
        val principal: Long,
        val rateMicroPercent: Long,
        val months: Int,
        val expectedEmi: Long,
        val expectedTotalInterest: Long,
        val expectedTotalPayable: Long,
        val expectedFirstInterest: Long,
        val expectedFirstPrincipal: Long,
        val expectedFirstPaid: Long,
        val expectedFirstClosing: Long,
        val expectedFinalPaid: Long,
        val expectedFinalPrincipal: Long,
        val expectedFinalInterest: Long,
        val expectedFinalClosing: Long,
    )

    private val startMonth = LoanMonth(2026, 10) // "Oct-26"

    private val goldenCases = listOf(
        Golden(
            name = "home 10L @ 8.5% x 240 (published: ~8,678)",
            principal = 1_000_000L,
            rateMicroPercent = 8_500_000L,
            months = 240,
            expectedEmi = 8_678L,
            expectedTotalInterest = 1_082_846L,
            expectedTotalPayable = 2_082_846L,
            expectedFirstInterest = 7_083L,
            expectedFirstPrincipal = 1_595L,
            expectedFirstPaid = 8_678L,
            expectedFirstClosing = 998_405L,
            expectedFinalPaid = 8_804L,
            expectedFinalPrincipal = 8_742L,
            expectedFinalInterest = 62L,
            expectedFinalClosing = 0L,
        ),
        Golden(
            name = "home 25L @ 9.25% x 180",
            principal = 2_500_000L,
            rateMicroPercent = 9_250_000L,
            months = 180,
            expectedEmi = 25_730L,
            expectedTotalInterest = 2_131_325L,
            expectedTotalPayable = 4_631_325L,
            expectedFirstInterest = 19_271L,
            expectedFirstPrincipal = 6_459L,
            expectedFirstPaid = 25_730L,
            expectedFirstClosing = 2_493_541L,
            expectedFinalPaid = 25_655L,
            expectedFinalPrincipal = 25_459L,
            expectedFinalInterest = 196L,
            expectedFinalClosing = 0L,
        ),
        Golden(
            name = "car 5L @ 12% x 60",
            principal = 500_000L,
            rateMicroPercent = 12_000_000L,
            months = 60,
            expectedEmi = 11_122L,
            expectedTotalInterest = 167_337L,
            expectedTotalPayable = 667_337L,
            expectedFirstInterest = 5_000L,
            expectedFirstPrincipal = 6_122L,
            expectedFirstPaid = 11_122L,
            expectedFirstClosing = 493_878L,
            expectedFinalPaid = 11_139L,
            expectedFinalPrincipal = 11_029L,
            expectedFinalInterest = 110L,
            expectedFinalClosing = 0L,
        ),
        Golden(
            name = "crore 1Cr @ 7.5% x 360",
            principal = 10_000_000L,
            rateMicroPercent = 7_500_000L,
            months = 360,
            expectedEmi = 69_921L,
            expectedTotalInterest = 15_172_199L,
            expectedTotalPayable = 25_172_199L,
            expectedFirstInterest = 62_500L,
            expectedFirstPrincipal = 7_421L,
            expectedFirstPaid = 69_921L,
            expectedFirstClosing = 9_992_579L,
            expectedFinalPaid = 70_560L,
            expectedFinalPrincipal = 70_122L,
            expectedFinalInterest = 438L,
            expectedFinalClosing = 0L,
        ),
        Golden(
            name = "zero-rate 2.5L x 24",
            principal = 250_000L,
            rateMicroPercent = 0L,
            months = 24,
            expectedEmi = 10_417L,
            expectedTotalInterest = 0L,
            expectedTotalPayable = 250_000L,
            expectedFirstInterest = 0L,
            expectedFirstPrincipal = 10_417L,
            expectedFirstPaid = 10_417L,
            expectedFirstClosing = 239_583L,
            expectedFinalPaid = 10_409L,
            expectedFinalPrincipal = 10_409L,
            expectedFinalInterest = 0L,
            expectedFinalClosing = 0L,
        ),
        Golden(
            name = "odd 9,99,999 @ 13.75% x 37",
            principal = 999_999L,
            rateMicroPercent = 13_750_000L,
            months = 37,
            expectedEmi = 33_312L,
            expectedTotalInterest = 232_545L,
            expectedTotalPayable = 1_232_544L,
            expectedFirstInterest = 11_458L,
            expectedFirstPrincipal = 21_854L,
            expectedFirstPaid = 33_312L,
            expectedFirstClosing = 978_145L,
            expectedFinalPaid = 33_312L,
            expectedFinalPrincipal = 32_935L,
            expectedFinalInterest = 377L,
            expectedFinalClosing = 0L,
        ),
        Golden(
            name = "lakh 1L @ 10% x 12 (published: ~8,792)",
            principal = 100_000L,
            rateMicroPercent = 10_000_000L,
            months = 12,
            expectedEmi = 8_792L,
            expectedTotalInterest = 5_499L,
            expectedTotalPayable = 105_499L,
            expectedFirstInterest = 833L,
            expectedFirstPrincipal = 7_959L,
            expectedFirstPaid = 8_792L,
            expectedFirstClosing = 92_041L,
            expectedFinalPaid = 8_787L,
            expectedFinalPrincipal = 8_714L,
            expectedFinalInterest = 73L,
            expectedFinalClosing = 0L,
        ),
        Golden(
            name = "10L @ 8.75% x 120 (published: 12,532 truncated)",
            principal = 1_000_000L,
            rateMicroPercent = 8_750_000L,
            months = 120,
            expectedEmi = 12_533L,
            expectedTotalInterest = 503_901L,
            expectedTotalPayable = 1_503_901L,
            expectedFirstInterest = 7_292L,
            expectedFirstPrincipal = 5_241L,
            expectedFirstPaid = 12_533L,
            expectedFirstClosing = 994_759L,
            expectedFinalPaid = 12_474L,
            expectedFinalPrincipal = 12_384L,
            expectedFinalInterest = 90L,
            expectedFinalClosing = 0L,
        ),
        Golden(
            name = "5L @ 8.35% x 120 (published: 6,159)",
            principal = 500_000L,
            rateMicroPercent = 8_350_000L,
            months = 120,
            expectedEmi = 6_159L,
            expectedTotalInterest = 239_129L,
            expectedTotalPayable = 739_129L,
            expectedFirstInterest = 3_479L,
            expectedFirstPrincipal = 2_680L,
            expectedFirstPaid = 6_159L,
            expectedFirstClosing = 497_320L,
            expectedFinalPaid = 6_208L,
            expectedFinalPrincipal = 6_165L,
            expectedFinalInterest = 43L,
            expectedFinalClosing = 0L,
        ),
        Golden(
            name = "15L @ 8.6% x 240 (published: 13,112)",
            principal = 1_500_000L,
            rateMicroPercent = 8_600_000L,
            months = 240,
            expectedEmi = 13_112L,
            expectedTotalInterest = 1_647_162L,
            expectedTotalPayable = 3_147_162L,
            expectedFirstInterest = 10_750L,
            expectedFirstPrincipal = 2_362L,
            expectedFirstPaid = 13_112L,
            expectedFirstClosing = 1_497_638L,
            expectedFinalPaid = 13_394L,
            expectedFinalPrincipal = 13_299L,
            expectedFinalInterest = 95L,
            expectedFinalClosing = 0L,
        ),
    )

    private fun summaryOf(golden: Golden): LoanSummary =
        EmiEngine.calculate(
            LoanTerms(golden.principal, golden.rateMicroPercent, golden.months),
            startMonth,
        )

    @Test
    fun goldenCasesMatchTheIndependentReference() {
        goldenCases.forEach { golden ->
            val summary = summaryOf(golden)
            assertEquals("$golden EMI", golden.expectedEmi, summary.monthlyEmi)
            assertEquals("$golden total interest", golden.expectedTotalInterest, summary.totalInterest)
            assertEquals("$golden total payable", golden.expectedTotalPayable, summary.totalPayable)
        }
    }

    @Test
    fun goldenFirstAndFinalInstallmentsMatch() {
        goldenCases.forEach { golden ->
            val first = summaryOf(golden).installments.first()
            assertEquals("$golden first interest", golden.expectedFirstInterest, first.interestComponent)
            assertEquals("$golden first principal", golden.expectedFirstPrincipal, first.principalComponent)
            assertEquals("$golden first paid", golden.expectedFirstPaid, first.amountPaid)
            assertEquals("$golden first closing", golden.expectedFirstClosing, first.closingBalance)

            val last = summaryOf(golden).installments.last()
            assertEquals("$golden final paid", golden.expectedFinalPaid, last.amountPaid)
            assertEquals("$golden final principal", golden.expectedFinalPrincipal, last.principalComponent)
            assertEquals("$golden final interest", golden.expectedFinalInterest, last.interestComponent)
            assertEquals("$golden final closing", golden.expectedFinalClosing, last.closingBalance)
        }
    }

    @Test
    fun scheduleInvariantsHoldForEveryCase() {
        goldenCases.forEach { golden ->
            val summary = summaryOf(golden)
            assertEquals("$golden row count", golden.months, summary.installments.size)
            assertEquals(
                "$golden principal must be repaid exactly",
                golden.principal,
                summary.installments.sumOf { it.principalComponent },
            )
            assertEquals(
                "$golden interest column must sum to the total",
                summary.totalInterest,
                summary.installments.sumOf { it.interestComponent },
            )
            assertEquals("$golden total payable", golden.principal + summary.totalInterest, summary.totalPayable)
            assertEquals("$golden loan must close at zero", 0L, summary.installments.last().closingBalance)

            summary.installments.forEach { row ->
                assertEquals("$golden row ${row.number} paid = principal + interest", row.amountPaid, row.principalComponent + row.interestComponent)
                assertTrue("$golden row ${row.number} principal >= 0", row.principalComponent >= 0L)
                assertTrue("$golden row ${row.number} interest >= 0", row.interestComponent >= 0L)
            }
        }
    }

    @Test
    fun scheduleMonthsAdvanceOneMonthPerRowAndStartAtTheGivenMonth() {
        val golden = goldenCases.first { it.months == 240 }
        val summary = summaryOf(golden)
        summary.installments.forEachIndexed { index, row ->
            assertEquals("row ${row.number} month", startMonth.plusMonths(index), row.month)
        }
        assertEquals("Oct-26", summary.installments.first().month.shortLabel())
        assertEquals("Sep-46", summary.installments.last().month.shortLabel())
        assertEquals("Oct-26", summary.startMonth.shortLabel())
        assertEquals("Sep-46", summary.endMonth.shortLabel())
    }

    @Test
    fun emiExactMatchesThePublishedFormulaValues() {
        assertEquals(8678.232334, EmiEngine.emiExact(LoanTerms(1_000_000L, 8_500_000L, 240)), 0.001)
        assertEquals(8791.588723, EmiEngine.emiExact(LoanTerms(100_000L, 10_000_000L, 12)), 0.001)
        assertEquals(69921.450855, EmiEngine.emiExact(LoanTerms(10_000_000L, 7_500_000L, 360)), 0.001)
        assertEquals(10416.666667, EmiEngine.emiExact(LoanTerms(250_000L, 0L, 24)), 0.001)
    }

    @Test
    fun emiRoundsHalfUpToWholeRupees() {
        // 10,416.666... -> 10,417 and 8,678.232... -> 8,678 (see golden cases);
        // half-up on a tie is proven directly on the rounding helper.
        assertEquals(1L, EmiEngine.halfUpDivide(1L, 2L))
        assertEquals(1L, EmiEngine.halfUpDivide(2L, 4L))
        assertEquals(0L, EmiEngine.halfUpDivide(1L, 3L))
        assertEquals(1L, EmiEngine.halfUpDivide(2L, 3L))
        assertEquals(0L, EmiEngine.halfUpDivide(0L, 5L))
    }

    @Test
    fun monthlyInterestUsesExactIntegerArithmeticAndRoundsTiesUp() {
        // 10,00,000 x 8.5% / 12 = 7,083.33... -> 7,083
        assertEquals(7_083L, EmiEngine.monthlyInterest(1_000_000L, 8_500_000L))
        // 5,00,000 x 12% / 12 = 5,000 exactly
        assertEquals(5_000L, EmiEngine.monthlyInterest(500_000L, 12_000_000L))
        // 12,34,550 x 12% / 12 = 12,345.5 exactly -> ties round up
        assertEquals(12_346L, EmiEngine.monthlyInterest(1_234_550L, 12_000_000L))
        // 12,34,549 x 12% / 12 = 12,345.49 -> rounds down
        assertEquals(12_345L, EmiEngine.monthlyInterest(1_234_549L, 12_000_000L))
    }

    @Test
    fun zeroRateLoanSplitsEvenlyAndTheLastInstallmentIsShort() {
        val summary = EmiEngine.calculate(LoanTerms(250_000L, 0L, 24), startMonth)
        assertEquals(10_417L, summary.monthlyEmi)
        assertEquals(0L, summary.totalInterest)
        assertEquals(250_000L, summary.totalPayable)
        assertEquals(10_417L, summary.installments.first().principalComponent)
        assertEquals(10_409L, summary.installments.last().amountPaid)
        assertEquals(0L, summary.installments.last().closingBalance)
        assertTrue(summary.hasAdjustedFinalInstallment)
    }

    @Test
    fun adjustedFinalInstallmentFlagTracksReality() {
        goldenCases.forEach { golden ->
            val summary = summaryOf(golden)
            val last = summary.installments.last()
            assertEquals(
                "$golden adjusted flag",
                last.amountPaid != summary.monthlyEmi,
                summary.hasAdjustedFinalInstallment,
            )
        }
        assertFalse(EmiEngine.calculate(LoanTerms(1_200_000L, 0L, 12), startMonth).hasAdjustedFinalInstallment)
    }

    @Test
    fun microLoansNeverProduceNegativeAmounts() {
        val summary = EmiEngine.calculate(LoanTerms(1L, 36_000_000L, 2), startMonth)
        assertEquals(2, summary.installments.size)
        assertEquals(0L, summary.installments.last().closingBalance)
        summary.installments.forEach { row ->
            assertTrue(row.principalComponent >= 0L)
            assertTrue(row.interestComponent >= 0L)
            assertTrue(row.amountPaid >= 0L)
        }
    }

    @Test
    fun sharesSumToOne() {
        val summary = summaryOf(goldenCases.first())
        assertEquals(1.0, summary.principalShare + summary.interestShare, 1e-9)
        assertEquals(10_000_000.0 / 25_172_199.0, EmiEngine.calculate(LoanTerms(10_000_000L, 7_500_000L, 360), startMonth).principalShare, 1e-9)
    }

    @Test
    fun loanTermsRejectsIllegalInputs() {
        assertThrows(IllegalArgumentException::class.java) { LoanTerms(0L, 8_500_000L, 12) }
        assertThrows(IllegalArgumentException::class.java) { LoanTerms(LoanTerms.MAX_PRINCIPAL + 1, 8_500_000L, 12) }
        assertThrows(IllegalArgumentException::class.java) { LoanTerms(100_000L, LoanTerms.MAX_RATE_MICRO_PERCENT + 1, 12) }
        assertThrows(IllegalArgumentException::class.java) { LoanTerms(100_000L, 8_500_000L, 0) }
        assertThrows(IllegalArgumentException::class.java) { LoanTerms(100_000L, 8_500_000L, LoanTerms.MAX_TENURE_MONTHS + 1) }
    }

    @Test
    fun loanTermsOfConvertsPercentExactly() {
        assertEquals(8_750_000L, LoanTerms.of(1_000_000L, 8.75, 240).rateMicroPercent)
        assertEquals(100_000L, LoanTerms.of(1_000_000L, 0.1, 240).rateMicroPercent)
        assertEquals(0L, LoanTerms.of(1_000_000L, 0.0, 240).rateMicroPercent)
        assertEquals(8.75, LoanTerms.of(1_000_000L, 8.75, 240).annualRatePercent, 1e-12)
    }
}
