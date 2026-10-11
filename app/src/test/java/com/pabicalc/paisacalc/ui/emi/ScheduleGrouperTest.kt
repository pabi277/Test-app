package com.pabicalc.paisacalc.ui.emi

import com.pabicalc.paisacalc.engine.EmiEngine
import com.pabicalc.paisacalc.engine.LoanMonth
import com.pabicalc.paisacalc.engine.LoanSummary
import com.pabicalc.paisacalc.engine.LoanTerms
import com.pabicalc.paisacalc.engine.Prepayment
import com.pabicalc.paisacalc.engine.PrepaymentFrequency
import com.pabicalc.paisacalc.engine.PrepaymentPlan
import com.pabicalc.paisacalc.engine.PrepaymentStrategy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleGrouperTest {

    private val startMonth = LoanMonth(2026, 10)

    private fun summary(principal: Long, rateMicroPercent: Long, months: Int): LoanSummary =
        EmiEngine.calculate(LoanTerms(principal, rateMicroPercent, months), startMonth)

    @Test
    fun aTwentyYearLoanBecomesTwentyYearBlocks() {
        val groups = ScheduleGrouper.byLoanYear(summary(1_000_000L, 8_500_000L, 240))
        assertEquals(20, groups.size)
        assertEquals((1..20).toList(), groups.map { it.index })
        groups.forEach { group -> assertEquals(12, group.installments.size) }
        assertEquals("Oct-26", groups.first().firstMonth.shortLabel())
        assertEquals("Sep-27", groups.first().lastMonth.shortLabel())
        assertEquals("Sep-46", groups.last().lastMonth.shortLabel())
    }

    @Test
    fun theLastBlockMayBeShorter() {
        val groups = ScheduleGrouper.byLoanYear(summary(999_999L, 13_750_000L, 37))
        assertEquals(4, groups.size)
        assertEquals(listOf(12, 12, 12, 1), groups.map { it.installments.size })
        assertEquals("Oct-29", groups.last().lastMonth.shortLabel())
    }

    @Test
    fun blockTotalsAddUpToTheSummaryTotals() {
        val summary = summary(1_500_000L, 8_600_000L, 240)
        val groups = ScheduleGrouper.byLoanYear(summary)
        assertEquals(summary.principal, groups.sumOf { it.totalPrincipal })
        assertEquals(summary.totalInterest, groups.sumOf { it.totalInterest })
        groups.forEach { group ->
            assertEquals(group.installments.sumOf { it.principalComponent }, group.totalPrincipal)
            assertEquals(group.installments.sumOf { it.interestComponent }, group.totalInterest)
        }
    }

    @Test
    fun aPlainLoanHasNoPrepaidMoneyInAnyBlock() {
        ScheduleGrouper.byLoanYear(summary(1_000_000L, 8_500_000L, 240)).forEach { group ->
            assertEquals(0L, group.totalPrepaid)
        }
    }

    @Test
    fun blockTotalsCarryThePrepaymentSplitTheStoreReviewAskedFor() {
        val plan = PrepaymentPlan(
            listOf(Prepayment(PrepaymentFrequency.QUARTERLY, 13, 25_000L, PrepaymentStrategy.REDUCE_TENURE)),
        )
        val summary = EmiEngine.calculate(LoanTerms(1_000_000L, 8_500_000L, 240), startMonth, plan)
        val groups = ScheduleGrouper.byLoanYear(summary)

        assertEquals(summary.totalPrepaid, groups.sumOf { it.totalPrepaid })
        groups.forEach { group ->
            assertEquals(group.installments.sumOf { it.prepaymentComponent }, group.totalPrepaid)
        }

        // Every block: balance it started with = principal + prepaid + balance it ended with.
        var balanceBefore = summary.principal
        groups.forEach { group ->
            val closing = group.installments.last().closingBalance
            assertEquals(
                "block ${group.index} balance movement",
                balanceBefore - closing,
                group.totalPrincipal + group.totalPrepaid,
            )
            balanceBefore = closing
        }
    }

    @Test
    fun prepaidMoneyIsAttributedToTheBlocksItLandsIn() {
        val plan = PrepaymentPlan(
            listOf(Prepayment(PrepaymentFrequency.QUARTERLY, 13, 25_000L, PrepaymentStrategy.REDUCE_TENURE)),
        )
        val groups = ScheduleGrouper.byLoanYear(
            EmiEngine.calculate(LoanTerms(1_000_000L, 8_500_000L, 240), startMonth, plan),
        )
        // Quarterly from installment 13: four payments (13, 16, 19, 22) in year 2, none in year 1.
        assertEquals(0L, groups[0].totalPrepaid)
        assertEquals(100_000L, groups[1].totalPrepaid)
        assertEquals(0, groups[0].installments.count { it.prepaymentComponent > 0L })
        assertEquals(4, groups[1].installments.count { it.prepaymentComponent > 0L })
    }

    @Test
    fun everyInstallmentAppearsExactlyOnceAndInOrder() {
        val summary = summary(500_000L, 12_000_000L, 60)
        val flattened = ScheduleGrouper.byLoanYear(summary).flatMap { it.installments }
        assertEquals(summary.installments, flattened)
        assertTrue(flattened.zipWithNext().all { (a, b) -> a.month < b.month })
    }
}
