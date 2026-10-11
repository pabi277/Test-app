package com.pabicalc.paisacalc.gst

/** Raw, editable fields for the GST screen. */
data class GstInput(
    val amountText: String = "",
    val rateText: String = "18",
    val mode: GstCalculationMode = GstCalculationMode.ADD,
    val supplyType: GstSupplyType = GstSupplyType.INTRA_STATE,
)

enum class GstFieldIssue {
    AMOUNT_NOT_POSITIVE,
    AMOUNT_TOO_LARGE,
    RATE_INVALID,
}

sealed interface GstParseResult {
    data object Incomplete : GstParseResult
    data class Invalid(val issues: List<GstFieldIssue>) : GstParseResult
    data class Valid(val calculation: GstCalculation) : GstParseResult
}

/** Digits and one decimal point only; money inputs are stored in paise. */
object GstInputSanitizer {

    fun amount(text: String): String {
        val filtered = text.filter { it.isDigit() || it == '.' }
        val firstDot = filtered.indexOf('.')
        val oneDot = if (firstDot < 0) {
            filtered
        } else {
            filtered.substring(0, firstDot + 1) +
                filtered.substring(firstDot + 1).filter { it.isDigit() }
        }
        val whole = oneDot.substringBefore('.').take(MAX_AMOUNT_INTEGER_DIGITS)
        if (!oneDot.contains('.')) return whole
        val fraction = oneDot.substringAfter('.').take(2)
        return "$whole.$fraction"
    }

    fun rate(text: String): String {
        val filtered = text.filter { it.isDigit() || it == '.' }
        val firstDot = filtered.indexOf('.')
        val oneDot = if (firstDot < 0) {
            filtered
        } else {
            filtered.substring(0, firstDot + 1) +
                filtered.substring(firstDot + 1).filter { it.isDigit() }
        }
        val whole = oneDot.substringBefore('.').take(MAX_RATE_INTEGER_DIGITS)
        if (!oneDot.contains('.')) return whole
        val fraction = oneDot.substringAfter('.').take(MAX_RATE_DECIMALS)
        return "$whole.$fraction"
    }

    private const val MAX_AMOUNT_INTEGER_DIGITS = 12
    private const val MAX_RATE_INTEGER_DIGITS = 3
    private const val MAX_RATE_DECIMALS = 6
}

/** Text parser and validation for the GST form. Pure Kotlin for unit tests. */
object GstInputParser {

    fun parse(input: GstInput): GstParseResult {
        if (input.amountText.filter { it.isDigit() }.isEmpty()) return GstParseResult.Incomplete

        val issues = ArrayList<GstFieldIssue>(2)
        val amountPaise = parseAmountPaise(input.amountText)
        when {
            amountPaise == null -> issues += GstFieldIssue.AMOUNT_TOO_LARGE
            amountPaise < 1L -> issues += GstFieldIssue.AMOUNT_NOT_POSITIVE
            amountPaise > GstCalculator.MAX_AMOUNT_PAISE -> issues += GstFieldIssue.AMOUNT_TOO_LARGE
        }

        val rateMicroPercent = GstCalculator.parseRateMicroPercent(input.rateText)
        if (rateMicroPercent == null || rateMicroPercent !in 0..GstCalculator.MAX_RATE_MICRO_PERCENT) {
            issues += GstFieldIssue.RATE_INVALID
        }

        if (issues.isNotEmpty()) return GstParseResult.Invalid(issues)

        return GstParseResult.Valid(
            GstCalculator.calculate(
                amountEnteredPaise = requireNotNull(amountPaise),
                rateMicroPercent = requireNotNull(rateMicroPercent),
                mode = input.mode,
                supplyType = input.supplyType,
            ),
        )
    }

    /** Parses a rupee amount with up to two decimal places into exact paise. */
    fun parseAmountPaise(text: String): Long? {
        val clean = text.trim().replace(",", "").replace("₹", "")
        if (clean.isEmpty() || clean.startsWith('-')) return null
        val dot = clean.indexOf('.')
        if (dot != clean.lastIndexOf('.')) return null
        val wholeText = if (dot >= 0) clean.substring(0, dot) else clean
        val fractionText = if (dot >= 0) clean.substring(dot + 1) else ""
        if (wholeText.isEmpty() && fractionText.isEmpty()) return null
        if (fractionText.length > 2) return null
        if (wholeText.isNotEmpty() && wholeText.any { !it.isDigit() }) return null
        if (fractionText.any { !it.isDigit() }) return null
        val whole = wholeText.ifEmpty { "0" }.toLongOrNull() ?: return null
        val fraction = fractionText.padEnd(2, '0').ifEmpty { "0" }.toLongOrNull() ?: return null
        return runCatching { Math.addExact(Math.multiplyExact(whole, 100L), fraction) }.getOrNull()
    }
}
