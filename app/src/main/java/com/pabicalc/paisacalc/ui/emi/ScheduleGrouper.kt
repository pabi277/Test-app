package com.pabicalc.paisacalc.ui.emi

import com.pabicalc.paisacalc.engine.Installment
import com.pabicalc.paisacalc.engine.LoanMonth
import com.pabicalc.paisacalc.engine.LoanSummary

/**
 * One 12-month block of the amortization schedule, with that block's totals.
 * A 240-month loan yields 20 of these; the last block may be shorter.
 */
data class ScheduleYear(
    val index: Int,
    val firstMonth: LoanMonth,
    val lastMonth: LoanMonth,
    val totalPrincipal: Long,
    val totalInterest: Long,
    val installments: List<Installment>,
)

object ScheduleGrouper {

    private const val MONTHS_PER_GROUP = 12

    fun byLoanYear(summary: LoanSummary): List<ScheduleYear> =
        summary.installments
            .chunked(MONTHS_PER_GROUP)
            .mapIndexed { index, chunk ->
                ScheduleYear(
                    index = index + 1,
                    firstMonth = chunk.first().month,
                    lastMonth = chunk.last().month,
                    totalPrincipal = chunk.sumOf { it.principalComponent },
                    totalInterest = chunk.sumOf { it.interestComponent },
                    installments = chunk,
                )
            }
}
