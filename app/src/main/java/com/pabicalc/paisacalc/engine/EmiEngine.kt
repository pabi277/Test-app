package com.pabicalc.paisacalc.engine

/**
 * Pure reducing-balance EMI engine — no Android, no I/O, fully unit-testable.
 *
 * ## Rounding policy (mirrored by `tools/emi_reference.py`, see docs/DECISIONS.md)
 *
 * 1. `emiExact` is the standard reducing-balance formula
 *    `P·r·(1+r)^n / ((1+r)^n − 1)` with `r = annualRate / 1200`, evaluated in
 *    IEEE-754 doubles (the same formula every Indian lender publishes).
 *    For a 0% loan it degrades to `P / n`.
 * 2. `monthlyEmi` = half-up rounding of `emiExact` to whole rupees
 *    (Java `Math.round` semantics — ties go up, never a truncated number).
 * 3. Each month's interest = half-up rounding of
 *    `openingBalance × rateMicroPercent / 1_200_000_000`, computed with exact
 *    integer arithmetic so no floating-point drift can leak into the schedule.
 * 4. Months `1..n-1` pay `monthlyEmi − interest` towards principal; the final
 *    installment clears whatever balance remains, so the loan always closes at
 *    exactly zero (the last row may differ from the EMI by a few rupees — the
 *    UI says so).
 * 5. `totalInterest` = the sum of the monthly interest column and
 *    `totalPayable = principal + totalInterest`, so the schedule, the totals
 *    and the donut can never disagree.
 *
 * Numbers verified against an independent exact-decimal implementation and
 * published bank examples; see `docs/phases/01-emi-engine.md`.
 */
object EmiEngine {

    /** rateMicroPercent -> monthly rate on a whole-rupee balance. */
    private const val MICRO_PERCENT_DENOMINATOR = 1_200_000_000L

    /**
     * Calculates the full schedule for [terms], labelling installments from
     * [startMonth] (e.g. the current month).
     */
    fun calculate(terms: LoanTerms, startMonth: LoanMonth): LoanSummary {
        val emi = Math.round(emiExact(terms))
        val installments = ArrayList<Installment>(terms.tenureMonths)
        var balance = terms.principal
        var totalInterest = 0L

        for (number in 1..terms.tenureMonths) {
            val interest = monthlyInterest(balance, terms.rateMicroPercent)
            val scheduledPrincipal = emi - interest
            val principalComponent = when {
                // The final installment clears the loan.
                number == terms.tenureMonths -> balance
                // Degenerate micro-loans where rounding leaves the EMI unable
                // to cover the interest: keep the balance, clear it at the end.
                scheduledPrincipal <= 0L -> 0L
                else -> minOf(scheduledPrincipal, balance)
            }
            val paid = principalComponent + interest
            balance -= principalComponent
            totalInterest += interest
            installments += Installment(
                number = number,
                month = startMonth.plusMonths(number - 1),
                amountPaid = paid,
                principalComponent = principalComponent,
                interestComponent = interest,
                closingBalance = balance,
            )
        }

        return LoanSummary(
            terms = terms,
            startMonth = startMonth,
            monthlyEmi = emi,
            totalInterest = totalInterest,
            totalPayable = terms.principal + totalInterest,
            installments = installments,
        )
    }

    /** The unrounded EMI from the published reducing-balance formula. */
    fun emiExact(terms: LoanTerms): Double {
        if (terms.rateMicroPercent == 0L) {
            return terms.principal.toDouble() / terms.tenureMonths.toDouble()
        }
        val monthlyRate = terms.rateMicroPercent.toDouble() / MICRO_PERCENT_DENOMINATOR
        val growth = Math.pow(1.0 + monthlyRate, terms.tenureMonths.toDouble())
        return terms.principal * monthlyRate * growth / (growth - 1.0)
    }

    /**
     * Half-up rupee rounding of one month's interest on [balance].
     * Exact: both operands are integers well inside `Long` range
     * (balance ≤ ₹100 crore, rate ≤ 36%).
     */
    internal fun monthlyInterest(balance: Long, rateMicroPercent: Long): Long =
        halfUpDivide(balance * rateMicroPercent, MICRO_PERCENT_DENOMINATOR)

    /** Half-up division for non-negative operands: 1/2 -> 1, 2/4 -> 1, 1/3 -> 0. */
    internal fun halfUpDivide(numerator: Long, denominator: Long): Long {
        require(numerator >= 0L && denominator > 0L) { "halfUpDivide expects non-negative operands" }
        val quotient = numerator / denominator
        val remainder = numerator % denominator
        return if (2 * remainder >= denominator) quotient + 1 else quotient
    }
}
