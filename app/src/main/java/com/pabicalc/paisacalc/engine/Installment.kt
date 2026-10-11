package com.pabicalc.paisacalc.engine

/**
 * One row of the amortization schedule. All money values are whole rupees.
 */
data class Installment(
    /** 1-based installment number within the schedule. */
    val number: Int,
    val month: LoanMonth,
    /** The amount actually paid this month (the EMI, except possibly the last row). */
    val amountPaid: Long,
    val principalComponent: Long,
    val interestComponent: Long,
    /** Balance after this installment; the final row is always zero. */
    val closingBalance: Long,
)
