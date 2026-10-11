package com.pabicalc.paisacalc.engine

/**
 * How often a recurring prepayment repeats, counted in installments from the
 * month the borrower picks.
 */
enum class PrepaymentFrequency(
    /** Months between applications; 0 for [ONE_TIME] (applies once, never repeats). */
    val stepMonths: Int,
    /** Short label for the UI. */
    val label: String,
) {
    MONTHLY(1, "Monthly"),
    QUARTERLY(3, "Quarterly"),
    HALF_YEARLY(6, "Half-yearly"),
    YEARLY(12, "Yearly"),
    ONE_TIME(0, "One-time"),
}

/**
 * What happens to the loan after a prepayment is applied.
 */
enum class PrepaymentStrategy(
    /** Short label for the UI. */
    val label: String,
) {
    /** Keep the EMI and close the loan earlier. Most home-loan lenders do this by default. */
    REDUCE_TENURE("Shorten tenure"),

    /** Recompute the EMI over the months that remain; the loan still ends on time. */
    REDUCE_EMI("Reduce EMI"),
}

/**
 * One prepayment instruction: pay [amount] every [frequency], starting at
 * installment [fromInstallment] (1 = the very first EMI), and apply the money
 * as [strategy] says.
 *
 * The amount is always a whole number of rupees, like every other money value
 * in the engine. An amount larger than the balance simply closes the loan —
 * the engine never lets the balance go below zero.
 */
data class Prepayment(
    val frequency: PrepaymentFrequency,
    /** 1-based installment number of the first application (within the original tenure). */
    val fromInstallment: Int,
    val amount: Long,
    val strategy: PrepaymentStrategy,
) {
    init {
        require(fromInstallment in 1..LoanTerms.MAX_TENURE_MONTHS) {
            "fromInstallment must be in 1..${LoanTerms.MAX_TENURE_MONTHS}, was $fromInstallment"
        }
        require(amount in 1..LoanTerms.MAX_PRINCIPAL) {
            "amount must be in 1..${LoanTerms.MAX_PRINCIPAL}, was $amount"
        }
    }

    /** True when this prepayment is due at [installmentNumber] (1-based). */
    fun appliesAt(installmentNumber: Int): Boolean = when (frequency) {
        PrepaymentFrequency.ONE_TIME -> installmentNumber == fromInstallment
        else ->
            installmentNumber >= fromInstallment &&
                (installmentNumber - fromInstallment) % frequency.stepMonths == 0
    }
}

/**
 * Everything the borrower plans to pay on top of the EMI, in the order entered.
 *
 * Application order across prepayments that fall on the same installment only
 * affects which instruction is credited once the balance runs out; the amount
 * applied to the loan in that month is the same either way, so per-month
 * schedule totals never depend on the order.
 */
data class PrepaymentPlan(val prepayments: List<Prepayment> = emptyList()) {

    val isEmpty: Boolean get() = prepayments.isEmpty()

    val isNotEmpty: Boolean get() = prepayments.isNotEmpty()

    companion object {
        /** No prepayments: the schedule is the plain EMI schedule. */
        val NONE = PrepaymentPlan()
    }
}
