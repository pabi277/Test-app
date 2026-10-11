package com.pabicalc.paisacalc.engine

/**
 * The full result of an EMI calculation: headline numbers plus the schedule.
 */
data class LoanSummary(
    val terms: LoanTerms,
    val startMonth: LoanMonth,
    /** The fixed monthly installment, rounded to whole rupees. */
    val monthlyEmi: Long,
    val totalInterest: Long,
    val totalPayable: Long,
    /** One entry per month, in order; the last entry's closing balance is zero. */
    val installments: List<Installment>,
) {
    val principal: Long get() = terms.principal

    val endMonth: LoanMonth get() = installments.last().month

    /** Share of everything you repay that is principal (0..1). */
    val principalShare: Double get() = principal.toDouble() / totalPayable.toDouble()

    /** Share of everything you repay that is interest (0..1). */
    val interestShare: Double get() = totalInterest.toDouble() / totalPayable.toDouble()

    /**
     * True when the final installment differs from the regular EMI because it
     * clears whatever balance is left, so the loan closes at exactly zero.
     */
    val hasAdjustedFinalInstallment: Boolean
        get() = installments.last().amountPaid != monthlyEmi
}
