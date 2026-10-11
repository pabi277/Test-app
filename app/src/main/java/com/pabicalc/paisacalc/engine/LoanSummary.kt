package com.pabicalc.paisacalc.engine

/**
 * The full result of an EMI calculation: headline numbers plus the schedule.
 */
data class LoanSummary(
    val terms: LoanTerms,
    val startMonth: LoanMonth,
    /** The EMI the borrower starts with, rounded to whole rupees. */
    val monthlyEmi: Long,
    val totalInterest: Long,
    val totalPayable: Long,
    /** One entry per month, in order; the last entry's closing balance is zero. */
    val installments: List<Installment>,
    /** Total extra money prepaid across the whole loan; zero without a plan. */
    val totalPrepaid: Long = 0,
    /** How many installments carried a prepayment; zero without a plan. */
    val prepaymentApplications: Int = 0,
    /** Interest avoided versus the same loan with no prepayments; zero without a plan. */
    val interestSaved: Long = 0,
    /** The EMI in force on the last row (lower than [monthlyEmi] after a reduce-EMI prepayment). */
    val finalMonthlyEmi: Long = monthlyEmi,
) {
    val principal: Long get() = terms.principal

    val endMonth: LoanMonth get() = installments.last().month

    /** Share of everything you repay that is principal (0..1). */
    val principalShare: Double get() = principal.toDouble() / totalPayable.toDouble()

    /** Share of everything you repay that is interest (0..1). */
    val interestShare: Double get() = totalInterest.toDouble() / totalPayable.toDouble()

    val hasPrepayments: Boolean get() = totalPrepaid > 0

    /** Months cut from the original tenure (0 when the loan still runs its full course). */
    val monthsSaved: Int get() = terms.tenureMonths - installments.size

    /**
     * True when the final installment differs from the EMI in force just before
     * it, because that last row clears whatever balance is left.
     */
    val hasAdjustedFinalInstallment: Boolean
        get() = installments.last().amountPaid !=
            (installments.getOrNull(installments.size - 2)?.amountPaid ?: monthlyEmi)
}
