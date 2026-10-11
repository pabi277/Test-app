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
 * 2. The EMI is half-up rounded to whole rupees (Java `Math.round` semantics —
 *    ties go up, never a truncated number).
 * 3. Each month's interest = half-up rounding of
 *    `openingBalance × rateMicroPercent / 1_200_000_000`, computed with exact
 *    integer arithmetic so no floating-point drift can leak into the schedule.
 * 4. Months pay `EMI − interest` towards principal; the final installment
 *    clears whatever balance remains, so the loan always closes at exactly
 *    zero (the last row may differ from the EMI by a few rupees — the UI says
 *    so).
 * 5. `totalInterest` = the sum of the monthly interest column and
 *    `totalPayable = principal + totalInterest`, so the schedule, the totals
 *    and the donut can never disagree.
 *
 * ## Prepayments (phase 2)
 *
 * A [PrepaymentPlan] is applied after each month's regular EMI, against the
 * balance that the EMI leaves behind — the same order Indian lenders use.
 * [PrepaymentStrategy.REDUCE_TENURE] keeps the EMI and closes the loan early;
 * [PrepaymentStrategy.REDUCE_EMI] recomputes the EMI over the months that
 * remain. A prepayment never overpays: it is capped at the outstanding balance
 * and the schedule stops the month the loan reaches zero. [LoanSummary.interestSaved]
 * is the difference against the same loan with no plan.
 *
 * Numbers verified against an independent exact-decimal implementation, a
 * fully worked published example and published bank examples; see
 * `docs/phases/01-emi-engine.md` and `docs/phases/02-prepayments.md`.
 */
object EmiEngine {

    /** rateMicroPercent -> monthly rate on a whole-rupee balance. */
    private const val MICRO_PERCENT_DENOMINATOR = 1_200_000_000L

    /**
     * Calculates the full schedule for [terms], labelling installments from
     * [startMonth] (e.g. the current month), with an optional [plan] of
     * prepayments to apply along the way.
     */
    fun calculate(
        terms: LoanTerms,
        startMonth: LoanMonth,
        plan: PrepaymentPlan = PrepaymentPlan.NONE,
    ): LoanSummary {
        var emi = Math.round(emiExact(terms.principal, terms.rateMicroPercent, terms.tenureMonths))
        val initialEmi = emi
        val installments = ArrayList<Installment>(terms.tenureMonths)
        var balance = terms.principal
        var totalInterest = 0L
        var totalPrepaid = 0L
        var applications = 0

        for (number in 1..terms.tenureMonths) {
            if (balance == 0L) break
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
            var remaining = balance - principalComponent

            var prepaid = 0L
            var reduceEmi = false
            for (prepayment in plan.prepayments) {
                if (remaining == 0L) break
                if (!prepayment.appliesAt(number)) continue
                val applied = minOf(prepayment.amount, remaining)
                prepaid += applied
                remaining -= applied
                if (prepayment.strategy == PrepaymentStrategy.REDUCE_EMI) reduceEmi = true
            }
            if (prepaid > 0L) applications++

            totalInterest += interest
            totalPrepaid += prepaid
            balance = remaining
            installments += Installment(
                number = number,
                month = startMonth.plusMonths(number - 1),
                amountPaid = principalComponent + interest,
                principalComponent = principalComponent,
                interestComponent = interest,
                closingBalance = balance,
                prepaymentComponent = prepaid,
            )
            if (balance == 0L) break
            if (reduceEmi && number < terms.tenureMonths) {
                emi = Math.round(
                    emiExact(balance, terms.rateMicroPercent, terms.tenureMonths - number),
                )
            }
        }

        return LoanSummary(
            terms = terms,
            startMonth = startMonth,
            monthlyEmi = initialEmi,
            totalInterest = totalInterest,
            totalPayable = terms.principal + totalInterest,
            installments = installments,
            totalPrepaid = totalPrepaid,
            prepaymentApplications = applications,
            interestSaved =
                if (plan.isEmpty) 0L
                else calculate(terms, startMonth).totalInterest - totalInterest,
            finalMonthlyEmi = emi,
        )
    }

    /** The unrounded EMI from the published reducing-balance formula. */
    fun emiExact(terms: LoanTerms): Double =
        emiExact(terms.principal, terms.rateMicroPercent, terms.tenureMonths)

    /** The same formula for a loan whose balance and tenure have moved on. */
    fun emiExact(principal: Long, rateMicroPercent: Long, months: Int): Double {
        if (rateMicroPercent == 0L) {
            return principal.toDouble() / months.toDouble()
        }
        val monthlyRate = rateMicroPercent.toDouble() / MICRO_PERCENT_DENOMINATOR
        val growth = Math.pow(1.0 + monthlyRate, months.toDouble())
        return principal * monthlyRate * growth / (growth - 1.0)
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
