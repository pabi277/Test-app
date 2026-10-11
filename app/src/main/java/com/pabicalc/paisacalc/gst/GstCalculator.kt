package com.pabicalc.paisacalc.gst

import java.math.BigInteger

/** Whether the entered amount is before GST or already includes GST. */
enum class GstCalculationMode {
    ADD,
    REMOVE,
}

/** The tax split applied to a supply. */
enum class GstSupplyType {
    INTRA_STATE,
    INTER_STATE,
}

/**
 * GST amounts use paise internally so ordinary invoice values do not lose
 * precision. Rates are stored as millionths of a percentage point: 18% is
 * 18_000_000. The calculator rounds the total tax to the nearest paise,
 * half-up; for an intra-state supply CGST receives the half-up half and SGST
 * receives the remainder, so the split always equals the displayed total.
 */
data class GstCalculation(
    val mode: GstCalculationMode,
    val supplyType: GstSupplyType,
    val rateMicroPercent: Long,
    /** The amount the user typed, in paise (base value for ADD, gross for REMOVE). */
    val amountEnteredPaise: Long,
    /** Value before GST. */
    val taxableValuePaise: Long,
    val taxAmountPaise: Long,
    /** Value including GST. */
    val totalAmountPaise: Long,
    val cgstPaise: Long,
    val sgstPaise: Long,
    val igstPaise: Long,
)

/** Pure, exact GST add/remove math. No Android dependencies. */
object GstCalculator {

    /** 100 crore, shared with the loan engine's user-facing amount cap. */
    const val MAX_AMOUNT_RUPEES = 1_000_000_000L
    const val MAX_AMOUNT_PAISE = MAX_AMOUNT_RUPEES * PAISE_PER_RUPEE

    /** Custom rates are capped at 100% to keep the field useful and bounded. */
    const val MAX_RATE_MICRO_PERCENT = 100_000_000L

    private const val PAISE_PER_RUPEE = 100L
    private const val MICRO_PERCENT_DENOMINATOR = 100_000_000L
    private val RATE_DENOMINATOR = BigInteger.valueOf(MICRO_PERCENT_DENOMINATOR)
    private val ONE = BigInteger.ONE

    /**
     * Calculates GST on [amountEnteredPaise]. For [GstCalculationMode.ADD] the
     * input is the taxable value; for [GstCalculationMode.REMOVE] it is the
     * GST-inclusive total. No floating-point operations are used.
     */
    fun calculate(
        amountEnteredPaise: Long,
        rateMicroPercent: Long,
        mode: GstCalculationMode,
        supplyType: GstSupplyType,
    ): GstCalculation {
        require(amountEnteredPaise in 1..MAX_AMOUNT_PAISE) {
            "amountEnteredPaise must be in 1..$MAX_AMOUNT_PAISE, was $amountEnteredPaise"
        }
        require(rateMicroPercent in 0..MAX_RATE_MICRO_PERCENT) {
            "rateMicroPercent must be in 0..$MAX_RATE_MICRO_PERCENT, was $rateMicroPercent"
        }

        val amount = BigInteger.valueOf(amountEnteredPaise)
        val rate = BigInteger.valueOf(rateMicroPercent)
        val taxable: Long
        val tax: Long
        val total: Long
        when (mode) {
            GstCalculationMode.ADD -> {
                taxable = amountEnteredPaise
                tax = roundHalfUp(amount.multiply(rate), RATE_DENOMINATOR)
                total = Math.addExact(taxable, tax)
            }
            GstCalculationMode.REMOVE -> {
                taxable = roundHalfUp(
                    amount.multiply(RATE_DENOMINATOR),
                    RATE_DENOMINATOR.add(rate),
                )
                tax = amountEnteredPaise - taxable
                total = amountEnteredPaise
            }
        }

        val cgst: Long
        val sgst: Long
        val igst: Long
        when (supplyType) {
            GstSupplyType.INTRA_STATE -> {
                cgst = (tax + 1L) / 2L
                sgst = tax - cgst
                igst = 0L
            }
            GstSupplyType.INTER_STATE -> {
                cgst = 0L
                sgst = 0L
                igst = tax
            }
        }

        return GstCalculation(
            mode = mode,
            supplyType = supplyType,
            rateMicroPercent = rateMicroPercent,
            amountEnteredPaise = amountEnteredPaise,
            taxableValuePaise = taxable,
            taxAmountPaise = tax,
            totalAmountPaise = total,
            cgstPaise = cgst,
            sgstPaise = sgst,
            igstPaise = igst,
        )
    }

    /** Exact decimal percentage text -> millionths of a percent. */
    fun parseRateMicroPercent(text: String): Long? {
        val clean = text.trim().removeSuffix("%")
        if (clean.isEmpty() || clean.startsWith('-')) return null
        val dot = clean.indexOf('.')
        if (dot != clean.lastIndexOf('.')) return null
        val wholeText = if (dot >= 0) clean.substring(0, dot) else clean
        val fractionText = if (dot >= 0) clean.substring(dot + 1) else ""
        if (wholeText.isEmpty() && fractionText.isEmpty()) return null
        if (fractionText.length > 6) return null
        if (wholeText.isNotEmpty() && wholeText.any { !it.isDigit() }) return null
        if (fractionText.any { !it.isDigit() }) return null
        val whole = wholeText.ifEmpty { "0" }.toLongOrNull() ?: return null
        if (whole > MAX_RATE_MICRO_PERCENT / 1_000_000L) return null
        val fraction = fractionText.padEnd(6, '0').ifEmpty { "0" }.toLongOrNull() ?: return null
        val rate = whole * 1_000_000L + fraction
        return rate.takeIf { it <= MAX_RATE_MICRO_PERCENT }
    }

    /** Formats a rate without locale-dependent decimal separators. */
    fun rateText(rateMicroPercent: Long): String {
        val whole = rateMicroPercent / 1_000_000L
        val fraction = (rateMicroPercent % 1_000_000L)
            .toString()
            .padStart(6, '0')
            .trimEnd('0')
        return if (fraction.isEmpty()) whole.toString() else "$whole.$fraction"
    }

    private fun roundHalfUp(numerator: BigInteger, denominator: BigInteger): Long {
        val (quotient, remainder) = numerator.divideAndRemainder(denominator)
        val rounded = if (remainder.shiftLeft(1) >= denominator) quotient.add(ONE) else quotient
        return rounded.longValueExact()
    }
}
