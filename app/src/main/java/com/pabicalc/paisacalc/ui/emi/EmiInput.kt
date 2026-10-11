package com.pabicalc.paisacalc.ui.emi

import com.pabicalc.paisacalc.engine.LoanTerms

/** Which input field an issue belongs to. */
enum class EmiField { AMOUNT, RATE, TENURE }

/**
 * What is wrong with a field. The parser returns these codes (testable without
 * Android); the UI maps each code to a localized message.
 */
enum class FieldIssue(val field: EmiField) {
    AMOUNT_NOT_POSITIVE(EmiField.AMOUNT),
    AMOUNT_TOO_LARGE(EmiField.AMOUNT),
    RATE_INVALID(EmiField.RATE),
    TENURE_NOT_POSITIVE(EmiField.TENURE),
    TENURE_TOO_LONG(EmiField.TENURE),
}

enum class TenureUnit { YEARS, MONTHS }

/**
 * The raw text state of the three input fields. Text (not numbers) so the
 * fields can be empty, partially typed, or invalid — exactly what the user sees.
 */
data class EmiInput(
    val amountText: String = "",
    val rateText: String = "",
    val tenureText: String = "",
    val tenureUnit: TenureUnit = TenureUnit.YEARS,
)

sealed interface EmiParseResult {
    /** At least one field is still blank: nothing to calculate yet. */
    data object Incomplete : EmiParseResult

    /** The user typed something invalid; show the issues, no results. */
    data class Invalid(val issues: List<FieldIssue>) : EmiParseResult

    /** Everything parsed into valid, validated [LoanTerms]. */
    data class Valid(val terms: LoanTerms) : EmiParseResult
}

/**
 * Keeps field text typeable: digits only where digits belong, one decimal
 * point for the rate, and caps that match the engine's legal ranges so the
 * error messages stay rare and meaningful.
 */
object EmiInputSanitizer {

    fun amount(text: String): String = text.filter { it.isDigit() }.take(MAX_AMOUNT_DIGITS)

    fun rate(text: String): String {
        val filtered = text.filter { it.isDigit() || it == '.' }
        val firstDot = filtered.indexOf('.')
        val singleDot =
            if (firstDot < 0) filtered
            else filtered.substring(0, firstDot + 1) +
                filtered.substring(firstDot + 1).filter { it.isDigit() }
        val integerPart = singleDot.substringBefore('.').take(MAX_RATE_INTEGER_DIGITS)
        if (!singleDot.contains('.')) return integerPart
        val fractionPart = singleDot.substringAfter('.').take(MAX_RATE_DECIMALS)
        return "$integerPart.$fractionPart"
    }

    fun tenure(text: String): String = text.filter { it.isDigit() }.take(MAX_TENURE_DIGITS)

    /** 12 digits: enough to type past ₹1,00,00,00,000 and see the error. */
    private const val MAX_AMOUNT_DIGITS = 12
    private const val MAX_RATE_INTEGER_DIGITS = 3
    private const val MAX_RATE_DECIMALS = 2
    private const val MAX_TENURE_DIGITS = 5
}

/**
 * Turns field text into validated [LoanTerms], or says exactly why not.
 *
 * Blank fields mean "not finished typing yet" ([EmiParseResult.Incomplete]);
 * non-blank but out-of-range values mean [EmiParseResult.Invalid].
 */
object EmiInputParser {

    fun parse(input: EmiInput): EmiParseResult {
        val amountDigits = input.amountText.filter { it.isDigit() }
        val rateText = input.rateText.trim().trimEnd('.')
        val tenureDigits = input.tenureText.filter { it.isDigit() }

        if (amountDigits.isEmpty() || rateText.isEmpty() || tenureDigits.isEmpty()) {
            return EmiParseResult.Incomplete
        }

        val issues = ArrayList<FieldIssue>(3)

        val principal = amountDigits.toLongOrNull()
            ?: return EmiParseResult.Invalid(listOf(FieldIssue.AMOUNT_TOO_LARGE))
        when {
            principal < 1L -> issues += FieldIssue.AMOUNT_NOT_POSITIVE
            principal > LoanTerms.MAX_PRINCIPAL -> issues += FieldIssue.AMOUNT_TOO_LARGE
        }

        val rateMicroPercent = rateMicroPercent(rateText)
        if (rateMicroPercent == null || rateMicroPercent > LoanTerms.MAX_RATE_MICRO_PERCENT) {
            issues += FieldIssue.RATE_INVALID
        }

        val tenureValue = tenureDigits.toLongOrNull()
        var tenureMonths = 0L
        if (tenureValue == null) {
            issues += FieldIssue.TENURE_TOO_LONG
        } else {
            tenureMonths = monthsOf(tenureValue, input.tenureUnit)
            when {
                tenureMonths < 1L -> issues += FieldIssue.TENURE_NOT_POSITIVE
                tenureMonths > LoanTerms.MAX_TENURE_MONTHS -> issues += FieldIssue.TENURE_TOO_LONG
            }
        }

        if (issues.isNotEmpty()) return EmiParseResult.Invalid(issues)

        return EmiParseResult.Valid(
            LoanTerms(
                principal = principal,
                rateMicroPercent = requireNotNull(rateMicroPercent),
                tenureMonths = tenureMonths.toInt(),
            ),
        )
    }

    /** Tenure text in the chosen unit, converted to months. */
    fun monthsOf(value: Long, unit: TenureUnit): Long =
        if (unit == TenureUnit.YEARS) value * 12L else value

    /**
     * Exact text -> millionths of a percent: `"8.75"` -> `8_750_000`.
     * No floating point, so `"8.75"` can never become 8749999.
     */
    fun rateMicroPercent(text: String): Long? {
        val clean = text.trim().trimEnd('.')
        if (clean.isEmpty()) return null
        val wholePart = clean.substringBefore('.').ifEmpty { "0" }
        val fractionPart = clean.substringAfter('.', missingDelimiterValue = "")
        if (fractionPart.length > 6) return null
        val whole = wholePart.toLongOrNull() ?: return null
        val fraction = fractionPart.padEnd(6, '0').toLongOrNull() ?: return null
        return whole * 1_000_000L + fraction
    }
}
