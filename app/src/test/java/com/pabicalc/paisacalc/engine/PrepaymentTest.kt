package com.pabicalc.paisacalc.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Acceptance suite for part-prepayments (phase 2).
 *
 * Golden values were produced by an independent implementation of the same
 * policy (`tools/emi_reference.py --kotlin`); that tool's integer policy agrees
 * row-by-row with its 60-digit exact-decimal control path on every case here.
 * One case ([publishedReduceTenureVsReduceEmiExampleMatchesToTheRupee]) is a
 * fully worked published comparison, matched to the rupee.
 */
class PrepaymentTest {

    private data class PrepayGolden(
        val name: String,
        val principal: Long,
        val rateMicroPercent: Long,
        val months: Int,
        val prepayments: List<Prepayment>,
        val expectedMonthlyEmi: Long,
        val expectedFinalEmi: Long,
        val expectedTotalInterest: Long,
        val expectedTotalPayable: Long,
        val expectedTotalPrepaid: Long,
        val expectedApplications: Int,
        val expectedInstallments: Int,
        val expectedInterestSaved: Long,
        val expectedMonthsSaved: Int,
        val expectedBaselineInstallments: Int,
        val expectedFirstPaid: Long,
        val expectedFirstPrincipal: Long,
        val expectedFirstInterest: Long,
        val expectedFirstClosing: Long,
        val expectedFinalPaid: Long,
        val expectedFinalPrincipal: Long,
        val expectedFinalInterest: Long,
        val expectedFinalPrepaid: Long,
    )

    private val startMonth = LoanMonth(2026, 10) // "Oct-26"

    private val prepayGoldenCases = listOf(
        PrepayGolden(
            name = "prepay_one_time_1L",
            principal = 1_000_000L, rateMicroPercent = 8_500_000L, months = 240,
            prepayments = listOf(Prepayment(PrepaymentFrequency.ONE_TIME, 25, 100_000L, PrepaymentStrategy.REDUCE_TENURE)),
            expectedMonthlyEmi = 8_678L, expectedFinalEmi = 8_678L,
            expectedTotalInterest = 793_721L, expectedTotalPayable = 1_793_721L,
            expectedTotalPrepaid = 100_000L, expectedApplications = 1,
            expectedInstallments = 196, expectedInterestSaved = 289_125L,
            expectedMonthsSaved = 44, expectedBaselineInstallments = 240,
            expectedFirstPaid = 8_678L, expectedFirstPrincipal = 1_595L,
            expectedFirstInterest = 7_083L, expectedFirstClosing = 998_405L,
            expectedFinalPaid = 1_511L, expectedFinalPrincipal = 1_500L,
            expectedFinalInterest = 11L, expectedFinalPrepaid = 0L,
        ),
        PrepayGolden(
            name = "prepay_monthly_5k",
            principal = 1_000_000L, rateMicroPercent = 8_500_000L, months = 240,
            prepayments = listOf(Prepayment(PrepaymentFrequency.MONTHLY, 13, 5_000L, PrepaymentStrategy.REDUCE_TENURE)),
            expectedMonthlyEmi = 8_678L, expectedFinalEmi = 8_678L,
            expectedTotalInterest = 476_882L, expectedTotalPayable = 1_476_882L,
            expectedTotalPrepaid = 500_000L, expectedApplications = 100,
            expectedInstallments = 113, expectedInterestSaved = 605_964L,
            expectedMonthsSaved = 127, expectedBaselineInstallments = 240,
            expectedFirstPaid = 8_678L, expectedFirstPrincipal = 1_595L,
            expectedFirstInterest = 7_083L, expectedFirstClosing = 998_405L,
            expectedFinalPaid = 4_946L, expectedFinalPrincipal = 4_911L,
            expectedFinalInterest = 35L, expectedFinalPrepaid = 0L,
        ),
        PrepayGolden(
            name = "prepay_quarterly_25k",
            principal = 1_000_000L, rateMicroPercent = 8_500_000L, months = 240,
            prepayments = listOf(Prepayment(PrepaymentFrequency.QUARTERLY, 13, 25_000L, PrepaymentStrategy.REDUCE_TENURE)),
            expectedMonthlyEmi = 8_678L, expectedFinalEmi = 8_678L,
            expectedTotalInterest = 362_322L, expectedTotalPayable = 1_362_322L,
            expectedTotalPrepaid = 624_692L, expectedApplications = 25,
            expectedInstallments = 85, expectedInterestSaved = 720_524L,
            expectedMonthsSaved = 155, expectedBaselineInstallments = 240,
            expectedFirstPaid = 8_678L, expectedFirstPrincipal = 1_595L,
            expectedFirstInterest = 7_083L, expectedFirstClosing = 998_405L,
            expectedFinalPaid = 8_678L, expectedFinalPrincipal = 8_443L,
            expectedFinalInterest = 235L, expectedFinalPrepaid = 24_692L,
        ),
        PrepayGolden(
            name = "prepay_half_yearly_50k",
            principal = 1_000_000L, rateMicroPercent = 8_500_000L, months = 240,
            prepayments = listOf(Prepayment(PrepaymentFrequency.HALF_YEARLY, 13, 50_000L, PrepaymentStrategy.REDUCE_TENURE)),
            expectedMonthlyEmi = 8_678L, expectedFinalEmi = 8_678L,
            expectedTotalInterest = 353_953L, expectedTotalPayable = 1_353_953L,
            expectedTotalPrepaid = 616_323L, expectedApplications = 13,
            expectedInstallments = 85, expectedInterestSaved = 728_893L,
            expectedMonthsSaved = 155, expectedBaselineInstallments = 240,
            expectedFirstPaid = 8_678L, expectedFirstPrincipal = 1_595L,
            expectedFirstInterest = 7_083L, expectedFirstClosing = 998_405L,
            expectedFinalPaid = 8_678L, expectedFinalPrincipal = 8_502L,
            expectedFinalInterest = 176L, expectedFinalPrepaid = 16_323L,
        ),
        PrepayGolden(
            name = "prepay_yearly_1L",
            principal = 1_000_000L, rateMicroPercent = 8_500_000L, months = 240,
            prepayments = listOf(Prepayment(PrepaymentFrequency.YEARLY, 13, 100_000L, PrepaymentStrategy.REDUCE_TENURE)),
            expectedMonthlyEmi = 8_678L, expectedFinalEmi = 8_678L,
            expectedTotalInterest = 337_049L, expectedTotalPayable = 1_337_049L,
            expectedTotalPrepaid = 600_000L, expectedApplications = 6,
            expectedInstallments = 85, expectedInterestSaved = 745_797L,
            expectedMonthsSaved = 155, expectedBaselineInstallments = 240,
            expectedFirstPaid = 8_678L, expectedFirstPrincipal = 1_595L,
            expectedFirstInterest = 7_083L, expectedFirstClosing = 998_405L,
            expectedFinalPaid = 8_097L, expectedFinalPrincipal = 8_040L,
            expectedFinalInterest = 57L, expectedFinalPrepaid = 0L,
        ),
        PrepayGolden(
            name = "prepay_quarterly_25k_reduce_emi",
            principal = 1_000_000L, rateMicroPercent = 8_500_000L, months = 240,
            prepayments = listOf(Prepayment(PrepaymentFrequency.QUARTERLY, 13, 25_000L, PrepaymentStrategy.REDUCE_EMI)),
            expectedMonthlyEmi = 8_678L, expectedFinalEmi = 112L,
            expectedTotalInterest = 429_317L, expectedTotalPayable = 1_429_317L,
            expectedTotalPrepaid = 859_228L, expectedApplications = 35,
            expectedInstallments = 115, expectedInterestSaved = 653_529L,
            expectedMonthsSaved = 125, expectedBaselineInstallments = 240,
            expectedFirstPaid = 8_678L, expectedFirstPrincipal = 1_595L,
            expectedFirstInterest = 7_083L, expectedFirstClosing = 998_405L,
            expectedFinalPaid = 112L, expectedFinalPrincipal = 46L,
            expectedFinalInterest = 66L, expectedFinalPrepaid = 9_228L,
        ),
        PrepayGolden(
            name = "prepay_one_time_closes_loan",
            principal = 1_000_000L, rateMicroPercent = 8_500_000L, months = 240,
            prepayments = listOf(Prepayment(PrepaymentFrequency.ONE_TIME, 5, 950_000L, PrepaymentStrategy.REDUCE_TENURE)),
            expectedMonthlyEmi = 8_678L, expectedFinalEmi = 8_678L,
            expectedTotalInterest = 36_191L, expectedTotalPayable = 1_036_191L,
            expectedTotalPrepaid = 950_000L, expectedApplications = 1,
            expectedInstallments = 10, expectedInterestSaved = 1_046_655L,
            expectedMonthsSaved = 230, expectedBaselineInstallments = 240,
            expectedFirstPaid = 8_678L, expectedFirstPrincipal = 1_595L,
            expectedFirstInterest = 7_083L, expectedFirstClosing = 998_405L,
            expectedFinalPaid = 8_089L, expectedFinalPrincipal = 8_032L,
            expectedFinalInterest = 57L, expectedFinalPrepaid = 0L,
        ),
        PrepayGolden(
            name = "prepay_capped_at_balance",
            principal = 100_000L, rateMicroPercent = 10_000_000L, months = 12,
            prepayments = listOf(Prepayment(PrepaymentFrequency.ONE_TIME, 6, 500_000L, PrepaymentStrategy.REDUCE_TENURE)),
            expectedMonthlyEmi = 8_792L, expectedFinalEmi = 8_792L,
            expectedTotalInterest = 3_994L, expectedTotalPayable = 103_994L,
            expectedTotalPrepaid = 51_242L, expectedApplications = 1,
            expectedInstallments = 6, expectedInterestSaved = 1_505L,
            expectedMonthsSaved = 6, expectedBaselineInstallments = 12,
            expectedFirstPaid = 8_792L, expectedFirstPrincipal = 7_959L,
            expectedFirstInterest = 833L, expectedFirstClosing = 92_041L,
            expectedFinalPaid = 8_792L, expectedFinalPrincipal = 8_296L,
            expectedFinalInterest = 496L, expectedFinalPrepaid = 51_242L,
        ),
        PrepayGolden(
            name = "prepay_zero_rate",
            principal = 250_000L, rateMicroPercent = 0L, months = 24,
            prepayments = listOf(Prepayment(PrepaymentFrequency.QUARTERLY, 5, 20_000L, PrepaymentStrategy.REDUCE_TENURE)),
            expectedMonthlyEmi = 10_417L, expectedFinalEmi = 10_417L,
            expectedTotalInterest = 0L, expectedTotalPayable = 250_000L,
            expectedTotalPrepaid = 80_000L, expectedApplications = 4,
            expectedInstallments = 17, expectedInterestSaved = 0L,
            expectedMonthsSaved = 7, expectedBaselineInstallments = 24,
            expectedFirstPaid = 10_417L, expectedFirstPrincipal = 10_417L,
            expectedFirstInterest = 0L, expectedFirstClosing = 239_583L,
            expectedFinalPaid = 3_328L, expectedFinalPrincipal = 3_328L,
            expectedFinalInterest = 0L, expectedFinalPrepaid = 0L,
        ),
        PrepayGolden(
            name = "prepay_car_quarterly",
            principal = 500_000L, rateMicroPercent = 12_000_000L, months = 60,
            prepayments = listOf(Prepayment(PrepaymentFrequency.QUARTERLY, 7, 15_000L, PrepaymentStrategy.REDUCE_TENURE)),
            expectedMonthlyEmi = 11_122L, expectedFinalEmi = 11_122L,
            expectedTotalInterest = 112_353L, expectedTotalPayable = 612_353L,
            expectedTotalPrepaid = 167_473L, expectedApplications = 12,
            expectedInstallments = 40, expectedInterestSaved = 54_984L,
            expectedMonthsSaved = 20, expectedBaselineInstallments = 60,
            expectedFirstPaid = 11_122L, expectedFirstPrincipal = 6_122L,
            expectedFirstInterest = 5_000L, expectedFirstClosing = 493_878L,
            expectedFinalPaid = 11_122L, expectedFinalPrincipal = 10_987L,
            expectedFinalInterest = 135L, expectedFinalPrepaid = 2_473L,
        ),
        PrepayGolden(
            name = "prepay_mixed_strategies",
            principal = 1_000_000L, rateMicroPercent = 8_500_000L, months = 240,
            prepayments = listOf(
                Prepayment(PrepaymentFrequency.QUARTERLY, 13, 25_000L, PrepaymentStrategy.REDUCE_EMI),
                Prepayment(PrepaymentFrequency.YEARLY, 37, 50_000L, PrepaymentStrategy.REDUCE_TENURE),
            ),
            expectedMonthlyEmi = 8_678L, expectedFinalEmi = 174L,
            expectedTotalInterest = 362_087L, expectedTotalPayable = 1_362_087L,
            expectedTotalPrepaid = 891_163L, expectedApplications = 26,
            expectedInstallments = 88, expectedInterestSaved = 720_759L,
            expectedMonthsSaved = 152, expectedBaselineInstallments = 240,
            expectedFirstPaid = 8_678L, expectedFirstPrincipal = 1_595L,
            expectedFirstInterest = 7_083L, expectedFirstClosing = 998_405L,
            expectedFinalPaid = 174L, expectedFinalPrincipal = 59L,
            expectedFinalInterest = 115L, expectedFinalPrepaid = 16_163L,
        ),
    )

    private fun termsOf(golden: PrepayGolden) =
        LoanTerms(golden.principal, golden.rateMicroPercent, golden.months)

    private fun baselineOf(golden: PrepayGolden): LoanSummary =
        EmiEngine.calculate(termsOf(golden), startMonth)

    private fun summaryOf(golden: PrepayGolden): LoanSummary =
        EmiEngine.calculate(termsOf(golden), startMonth, PrepaymentPlan(golden.prepayments))

    @Test
    fun goldenPrepaymentCasesMatchTheIndependentReference() {
        prepayGoldenCases.forEach { golden ->
            val summary = summaryOf(golden)
            assertEquals("$golden EMI", golden.expectedMonthlyEmi, summary.monthlyEmi)
            assertEquals("$golden final EMI", golden.expectedFinalEmi, summary.finalMonthlyEmi)
            assertEquals("$golden total interest", golden.expectedTotalInterest, summary.totalInterest)
            assertEquals("$golden total payable", golden.expectedTotalPayable, summary.totalPayable)
            assertEquals("$golden total prepaid", golden.expectedTotalPrepaid, summary.totalPrepaid)
            assertEquals("$golden applications", golden.expectedApplications, summary.prepaymentApplications)
            assertEquals("$golden rows", golden.expectedInstallments, summary.installments.size)
            assertEquals("$golden interest saved", golden.expectedInterestSaved, summary.interestSaved)
            assertEquals("$golden months saved", golden.expectedMonthsSaved, summary.monthsSaved)
        }
    }

    @Test
    fun goldenFirstAndFinalInstallmentRowsMatch() {
        prepayGoldenCases.forEach { golden ->
            val first = summaryOf(golden).installments.first()
            assertEquals("$golden first paid", golden.expectedFirstPaid, first.amountPaid)
            assertEquals("$golden first principal", golden.expectedFirstPrincipal, first.principalComponent)
            assertEquals("$golden first interest", golden.expectedFirstInterest, first.interestComponent)
            assertEquals("$golden first closing", golden.expectedFirstClosing, first.closingBalance)

            val last = summaryOf(golden).installments.last()
            assertEquals("$golden final paid", golden.expectedFinalPaid, last.amountPaid)
            assertEquals("$golden final principal", golden.expectedFinalPrincipal, last.principalComponent)
            assertEquals("$golden final interest", golden.expectedFinalInterest, last.interestComponent)
            assertEquals("$golden final prepaid", golden.expectedFinalPrepaid, last.prepaymentComponent)
            assertEquals("$golden final closing", 0L, last.closingBalance)
        }
    }

    @Test
    fun prepaymentInvariantsHoldForEveryCase() {
        prepayGoldenCases.forEach { golden ->
            val summary = summaryOf(golden)
            assertEquals(
                "$golden principal repaid = EMI principal + prepayments",
                golden.principal,
                summary.installments.sumOf { it.principalComponent } + summary.totalPrepaid,
            )
            assertEquals(
                "$golden prepaid column sums to the total",
                summary.totalPrepaid,
                summary.installments.sumOf { it.prepaymentComponent },
            )
            assertEquals(
                "$golden interest column sums to the total",
                summary.totalInterest,
                summary.installments.sumOf { it.interestComponent },
            )
            assertEquals(
                "$golden prepayment month count",
                summary.prepaymentApplications,
                summary.installments.count { it.prepaymentComponent > 0L },
            )
            assertEquals(
                "$golden payable = principal + interest",
                golden.principal + summary.totalInterest,
                summary.totalPayable,
            )
            assertEquals("$golden baseline runs the full tenure", golden.expectedBaselineInstallments, baselineOf(golden).installments.size)
            assertTrue("$golden schedule is never longer than the tenure", summary.installments.size <= golden.months)
            assertTrue("$golden loan closes at zero", summary.installments.last().closingBalance == 0L)

            summary.installments.forEach { row ->
                assertEquals("$golden row ${row.number} paid", row.amountPaid, row.principalComponent + row.interestComponent)
                assertEquals("$golden row ${row.number} total paid", row.totalPaid, row.amountPaid + row.prepaymentComponent)
                assertTrue("$golden row ${row.number} principal >= 0", row.principalComponent >= 0L)
                assertTrue("$golden row ${row.number} interest >= 0", row.interestComponent >= 0L)
                assertTrue("$golden row ${row.number} prepaid >= 0", row.prepaymentComponent >= 0L)
                assertTrue("$golden row ${row.number} closing >= 0", row.closingBalance >= 0L)
            }
        }
    }

    /**
     * The cadence of each frequency, counted from the chosen start installment.
     *
     * The amount is one rupee so the loan still runs its full course: the
     * cadence is what is under test. On the closing row (installment 240 here)
     * the regular EMI already clears the loan, so a monthly prepayment has no
     * balance left to pay — which is why the monthly cadence ends at 239.
     */
    @Test
    fun eachFrequencyAppliesOnItsOwnCadence() {
        val terms = LoanTerms(1_000_000L, 8_500_000L, 240)
        val start = 13
        val expectedRows: Map<PrepaymentFrequency, List<Int>> = mapOf(
            PrepaymentFrequency.MONTHLY to (13..239).toList(),
            PrepaymentFrequency.QUARTERLY to (13..240 step 3).toList(),
            PrepaymentFrequency.HALF_YEARLY to (13..240 step 6).toList(),
            PrepaymentFrequency.YEARLY to (13..240 step 12).toList(),
            PrepaymentFrequency.ONE_TIME to listOf(13),
        )

        expectedRows.forEach { (frequency, rows) ->
            val summary = EmiEngine.calculate(
                terms,
                startMonth,
                PrepaymentPlan(listOf(Prepayment(frequency, start, 1L, PrepaymentStrategy.REDUCE_TENURE))),
            )
            assertEquals("$frequency rows", rows, summary.installments.filter { it.prepaymentComponent > 0L }.map { it.number })
            assertEquals("$frequency applications", rows.size, summary.prepaymentApplications)
            assertEquals("$frequency tenure untouched", 240, summary.installments.size)
            rows.forEach { number ->
                assertEquals("$frequency row $number amount", 1L, summary.installments[number - 1].prepaymentComponent)
            }
            assertEquals("$frequency nothing charged on the closing row", 0L, summary.installments.last().prepaymentComponent)
            assertTrue("$frequency must save interest", summary.interestSaved > 0L)
        }
    }

    /**
     * The Techinfornity review scenario (`docs/RESEARCH.md`): "I add payment
     * amount & starting month, save button doesn't work". Amount ₹25,000,
     * quarterly, starting at installment 13 must recompute the schedule.
     */
    @Test
    fun quarterlyPrepaymentFromAStartMonthRecomputesTheSchedule() {
        val terms = LoanTerms(1_000_000L, 8_500_000L, 240)
        val plan = PrepaymentPlan(
            listOf(Prepayment(PrepaymentFrequency.QUARTERLY, 13, 25_000L, PrepaymentStrategy.REDUCE_TENURE)),
        )
        val baseline = EmiEngine.calculate(terms, startMonth)
        val prepaid = EmiEngine.calculate(terms, startMonth, plan)

        // The EMI never changes under "shorten tenure".
        assertEquals(baseline.monthlyEmi, prepaid.monthlyEmi)
        assertEquals(8_678L, prepaid.monthlyEmi)

        // The schedule really is recomputed: fewer installments, less interest.
        assertEquals(240, baseline.installments.size)
        assertEquals(85, prepaid.installments.size)
        assertEquals(1_082_846L, baseline.totalInterest)
        assertEquals(362_322L, prepaid.totalInterest)
        assertEquals(720_524L, prepaid.interestSaved)
        assertEquals(155, prepaid.monthsSaved)

        // The prepayment lands on the chosen month and every third month after.
        assertEquals(25_000L, prepaid.installments[12].prepaymentComponent) // installment 13
        assertEquals(startMonth.plusMonths(12).shortLabel(), prepaid.installments[12].month.shortLabel())
        prepaid.installments
            .filter { it.prepaymentComponent > 0L }
            .forEach { row -> assertEquals("quarterly cadence", 0, (row.number - 13) % 3) }

        // Interest saved is exactly the baseline difference, and the balance
        // closes on the last row of the shortened schedule.
        assertEquals(baseline.totalInterest - prepaid.totalInterest, prepaid.interestSaved)
        assertEquals(0L, prepaid.installments.last().closingBalance)
        assertEquals(terms.tenureMonths - prepaid.installments.size, prepaid.monthsSaved)
    }

    /**
     * Published worked comparison (optimizeapp.in, HDFC example, in
     * `docs/phases/02-prepayments.md`): ₹50,00,000 at 8.5% for 240 months with
     * a ₹5,00,000 part-payment after 36 EMIs, modelled both ways.
     */
    @Test
    fun publishedReduceTenureVsReduceEmiExampleMatchesToTheRupee() {
        val terms = LoanTerms(5_000_000L, 8_500_000L, 240)
        val baseline = EmiEngine.calculate(terms, startMonth)
        assertEquals(43_391L, baseline.monthlyEmi)
        assertEquals(5_413_923L, baseline.totalInterest) // published: ~₹54.14 L

        val shorten = EmiEngine.calculate(
            terms,
            startMonth,
            PrepaymentPlan(listOf(Prepayment(PrepaymentFrequency.ONE_TIME, 36, 500_000L, PrepaymentStrategy.REDUCE_TENURE))),
        )
        assertEquals("published: EMI unchanged", 43_391L, shorten.monthlyEmi)
        assertEquals("published: about 199 months", 199, shorten.installments.size)
        assertEquals("published: ~₹40.94 L interest", 4_094_274L, shorten.totalInterest)
        assertEquals("published: ~₹13.20 L saved", 1_319_649L, shorten.interestSaved)

        val reduceEmi = EmiEngine.calculate(
            terms,
            startMonth,
            PrepaymentPlan(listOf(Prepayment(PrepaymentFrequency.ONE_TIME, 36, 500_000L, PrepaymentStrategy.REDUCE_EMI))),
        )
        assertEquals("published: EMI drops to ~₹38,750", 38_750L, reduceEmi.finalMonthlyEmi)
        assertEquals("published: tenure unchanged", 240, reduceEmi.installments.size)
        assertEquals("published: ~₹49.67 L interest", 4_966_956L, reduceEmi.totalInterest)
        assertEquals("published: ~₹4.47 L saved", 446_967L, reduceEmi.interestSaved)
    }

    @Test
    fun reduceEmiLowersTheInstalmentWhileShortenTenureKeepsIt() {
        val terms = LoanTerms(1_000_000L, 8_500_000L, 240)
        val shorten = EmiEngine.calculate(
            terms,
            startMonth,
            PrepaymentPlan(listOf(Prepayment(PrepaymentFrequency.ONE_TIME, 13, 100_000L, PrepaymentStrategy.REDUCE_TENURE))),
        )
        val reduceEmi = EmiEngine.calculate(
            terms,
            startMonth,
            PrepaymentPlan(listOf(Prepayment(PrepaymentFrequency.ONE_TIME, 13, 100_000L, PrepaymentStrategy.REDUCE_EMI))),
        )

        assertEquals(8_678L, shorten.monthlyEmi)
        assertEquals(8_678L, shorten.finalMonthlyEmi)
        assertTrue("shorten tenure runs shorter", shorten.installments.size < 240)

        assertEquals(8_678L, reduceEmi.monthlyEmi)
        assertTrue("reduce EMI lowers the later installments", reduceEmi.finalMonthlyEmi < reduceEmi.monthlyEmi)
        assertEquals("reduce EMI runs the full tenure", 240, reduceEmi.installments.size)
        // Over the same 240 months the loan closes, and the shortened way
        // always saves at least as much interest.
        assertEquals(0L, reduceEmi.installments.last().closingBalance)
        assertTrue(shorten.interestSaved >= reduceEmi.interestSaved)
    }

    @Test
    fun aPrepaymentLargerThanTheBalanceIsCappedAndClosesTheLoan() {
        val summary = EmiEngine.calculate(
            LoanTerms(100_000L, 10_000_000L, 12),
            startMonth,
            PrepaymentPlan(listOf(Prepayment(PrepaymentFrequency.ONE_TIME, 6, 500_000L, PrepaymentStrategy.REDUCE_TENURE))),
        )
        assertEquals("capped at the balance", 51_242L, summary.totalPrepaid)
        assertEquals("one application only", 1, summary.prepaymentApplications)
        assertEquals("loan closes at the prepayment month", 6, summary.installments.size)
        assertEquals(0L, summary.installments.last().closingBalance)
        assertEquals(51_242L, summary.installments.last().prepaymentComponent)
        assertEquals(6, summary.monthsSaved)
    }

    @Test
    fun prepaymentsScheduledBeyondTheTenureNeverApply() {
        val terms = LoanTerms(100_000L, 10_000_000L, 12)
        val baseline = EmiEngine.calculate(terms, startMonth)
        val summary = EmiEngine.calculate(
            terms,
            startMonth,
            PrepaymentPlan(listOf(Prepayment(PrepaymentFrequency.YEARLY, 24, 5_000L, PrepaymentStrategy.REDUCE_TENURE))),
        )
        assertEquals(0, summary.prepaymentApplications)
        assertEquals(0L, summary.totalPrepaid)
        assertEquals(0L, summary.interestSaved)
        assertEquals(baseline.installments, summary.installments)
        assertEquals(baseline.totalInterest, summary.totalInterest)
    }

    @Test
    fun interestSavedIsMeasuredAgainstTheSameLoanWithoutPrepayments() {
        val terms = LoanTerms(1_000_000L, 8_500_000L, 240)
        val baseline = EmiEngine.calculate(terms, startMonth)
        listOf(
            Prepayment(PrepaymentFrequency.MONTHLY, 13, 5_000L, PrepaymentStrategy.REDUCE_TENURE),
            Prepayment(PrepaymentFrequency.QUARTERLY, 13, 25_000L, PrepaymentStrategy.REDUCE_TENURE),
            Prepayment(PrepaymentFrequency.YEARLY, 13, 100_000L, PrepaymentStrategy.REDUCE_EMI),
        ).forEach { prepayment ->
            val summary = EmiEngine.calculate(terms, startMonth, PrepaymentPlan(listOf(prepayment)))
            assertEquals(
                "$prepayment saved interest",
                baseline.totalInterest - summary.totalInterest,
                summary.interestSaved,
            )
            assertTrue("$prepayment must not cost more interest", summary.interestSaved >= 0L)
        }
    }

    @Test
    fun zeroRatePrepaymentsShortenTheLoanWithoutInterestSavings() {
        val summary = EmiEngine.calculate(
            LoanTerms(250_000L, 0L, 24),
            startMonth,
            PrepaymentPlan(listOf(Prepayment(PrepaymentFrequency.QUARTERLY, 5, 20_000L, PrepaymentStrategy.REDUCE_TENURE))),
        )
        assertEquals(0L, summary.totalInterest)
        assertEquals(0L, summary.interestSaved)
        assertEquals(80_000L, summary.totalPrepaid)
        assertTrue("still closes early", summary.monthsSaved > 0)
        assertEquals(0L, summary.installments.last().closingBalance)
    }

    @Test
    fun noPlanIsExactlyThePhaseOneSchedule() {
        prepayGoldenCases.forEach { golden ->
            val terms = termsOf(golden)
            val withoutPlan = EmiEngine.calculate(terms, startMonth, PrepaymentPlan.NONE)
            val defaultCall = EmiEngine.calculate(terms, startMonth)
            assertEquals("$golden default == NONE", defaultCall, withoutPlan)
            assertEquals("$golden untouched rows", golden.months, withoutPlan.installments.size)
            assertEquals("$golden no prepaid money", 0L, withoutPlan.totalPrepaid)
            assertEquals("$golden no applications", 0, withoutPlan.prepaymentApplications)
            assertEquals("$golden no savings", 0L, withoutPlan.interestSaved)
            assertEquals("$golden no months saved", 0, withoutPlan.monthsSaved)
            assertFalse("$golden hasPrepayments", withoutPlan.hasPrepayments)
            withoutPlan.installments.forEach { row ->
                assertEquals("$golden row ${row.number} prepaid", 0L, row.prepaymentComponent)
            }
        }
    }

    @Test
    fun prepaymentAndPlanValidation() {
        assertThrows(IllegalArgumentException::class.java) {
            Prepayment(PrepaymentFrequency.QUARTERLY, 0, 1_000L, PrepaymentStrategy.REDUCE_TENURE)
        }
        assertThrows(IllegalArgumentException::class.java) {
            Prepayment(PrepaymentFrequency.QUARTERLY, LoanTerms.MAX_TENURE_MONTHS + 1, 1_000L, PrepaymentStrategy.REDUCE_TENURE)
        }
        assertThrows(IllegalArgumentException::class.java) {
            Prepayment(PrepaymentFrequency.QUARTERLY, 13, 0L, PrepaymentStrategy.REDUCE_TENURE)
        }
        assertThrows(IllegalArgumentException::class.java) {
            Prepayment(PrepaymentFrequency.QUARTERLY, 13, LoanTerms.MAX_PRINCIPAL + 1, PrepaymentStrategy.REDUCE_TENURE)
        }
        assertTrue(PrepaymentPlan.NONE.isEmpty)
        assertFalse(PrepaymentPlan(listOf(Prepayment(PrepaymentFrequency.ONE_TIME, 1, 1L, PrepaymentStrategy.REDUCE_EMI))).isEmpty)
    }
}
