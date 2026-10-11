package com.pabicalc.paisacalc.ui.emi

import com.pabicalc.paisacalc.engine.EmiEngine
import com.pabicalc.paisacalc.engine.LoanMonth
import com.pabicalc.paisacalc.engine.LoanSummary
import com.pabicalc.paisacalc.engine.LoanTerms
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
    fun everyInstallmentAppearsExactlyOnceAndInOrder() {
        val summary = summary(500_000L, 12_000_000L, 60)
        val flattened = ScheduleGrouper.byLoanYear(summary).flatMap { it.installments }
        assertEquals(summary.installments, flattened)
        assertTrue(flattened.zipWithNext().all { (a, b) -> a.month < b.month })
    }
}
