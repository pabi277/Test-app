package com.pabicalc.paisacalc.engine

/**
 * Immutable, validated loan inputs.
 *
 * The annual rate is stored as [rateMicroPercent] — millionths of a percent, so
 * 8.75% p.a. is `8_750_000` — because the schedule arithmetic rounds money to
 * whole rupees, and exact integer math on a whole-rupee balance removes an
 * entire class of floating-point drift from the amortization table.
 */
data class LoanTerms(
    val principal: Long,
    val rateMicroPercent: Long,
    val tenureMonths: Int,
) {
    init {
        require(principal in 1..MAX_PRINCIPAL) {
            "principal must be in 1..$MAX_PRINCIPAL, was $principal"
        }
        require(rateMicroPercent in 0..MAX_RATE_MICRO_PERCENT) {
            "rateMicroPercent must be in 0..$MAX_RATE_MICRO_PERCENT, was $rateMicroPercent"
        }
        require(tenureMonths in 1..MAX_TENURE_MONTHS) {
            "tenureMonths must be in 1..$MAX_TENURE_MONTHS, was $tenureMonths"
        }
    }

    /** The rate as a percentage, for display and the standard EMI formula. */
    val annualRatePercent: Double get() = rateMicroPercent / 1_000_000.0

    companion object {
        /** ₹100 crore — comfortably above any retail loan in India. */
        const val MAX_PRINCIPAL = 1_000_000_000L

        /** 36% p.a. — above any mainstream Indian lending rate. */
        const val MAX_RATE_MICRO_PERCENT = 36_000_000L

        /** 40 years. */
        const val MAX_TENURE_MONTHS = 480

        /** Builds terms from a percentage (up to 6 decimal places). */
        fun of(principal: Long, annualRatePercent: Double, tenureMonths: Int): LoanTerms =
            LoanTerms(
                principal = principal,
                rateMicroPercent = Math.round(annualRatePercent * 1_000_000.0),
                tenureMonths = tenureMonths,
            )
    }
}
