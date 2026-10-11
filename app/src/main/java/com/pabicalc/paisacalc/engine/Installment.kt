package com.pabicalc.paisacalc.engine

/**
 * One row of the amortization schedule. All money values are whole rupees.
 */
data class Installment(
    /** 1-based installment number within the schedule. */
    val number: Int,
    val month: LoanMonth,
    /** The regular EMI paid this month (possibly a reduced EMI, or an adjusted final one). */
    val amountPaid: Long,
    val principalComponent: Long,
    val interestComponent: Long,
    /** Balance after this installment; the final row is always zero. */
    val closingBalance: Long,
    /** Extra money prepaid this month, on top of [amountPaid]; zero on ordinary months. */
    val prepaymentComponent: Long = 0,
) {
    /** Everything that leaves the borrower's pocket this month. */
    val totalPaid: Long get() = amountPaid + prepaymentComponent
}
