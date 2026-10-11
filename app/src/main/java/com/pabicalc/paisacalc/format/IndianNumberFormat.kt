package com.pabicalc.paisacalc.format

/**
 * Indian digit grouping and rupee rendering, in one tested place.
 *
 * This is competitor bug #1 in the research (`docs/RESEARCH.md`): other EMI
 * calculators break Indian formatting above ₹10 lakh. Grouping here is the
 * Indian system — the last three digits, then groups of two:
 * `1234567` renders as `12,34,567`, and `1000000000` as `1,00,00,00,000`.
 */
object IndianNumberFormat {

    /** ₹100 crore — must match `LoanTerms.MAX_PRINCIPAL`. */
    const val MAX_AMOUNT = 1_000_000_000L

    private const val LAKH = 100_000L
    private const val CRORE = 10_000_000L

    /** Groups a numeric string; any character that is not a digit is ignored. */
    fun group(digits: String): String {
        val stripped = digits.filter { it.isDigit() }
        if (stripped.isEmpty()) return ""
        val negative = digits.trimStart().startsWith("-")
        val body = if (stripped.length <= 3) stripped else groupAboveThousand(stripped)
        return if (negative) "-$body" else body
    }

    /** Groups a whole-rupee amount: `2500000` -> `25,00,000`. */
    fun group(value: Long): String = group(value.toString())

    /** Renders rupees with the ₹ sign: `2500000` -> `₹25,00,000`. */
    fun rupees(value: Long): String = "₹" + group(value)

    /**
     * Compact lakh/crore rendering for tight labels (slider ends, chips):
     * `100000` -> `₹1 L`, `1250000` -> `₹12.5 L`, `12345678` -> `₹1.23 Cr`.
     * Fractions are truncated (never rounded up), so the label can only ever
     * understate an amount, not overstate it.
     */
    fun compactRupees(value: Long): String = when {
        value >= CRORE -> "₹" + compactMagnitude(value, CRORE) + " Cr"
        value >= LAKH -> "₹" + compactMagnitude(value, LAKH) + " L"
        else -> rupees(value)
    }

    /** Digits only, capped at [maxDigits]. Used by the input fields. */
    fun digitsOnly(text: String, maxDigits: Int): String =
        text.filter { it.isDigit() }.take(maxDigits)

    /** Parses a grouped/sign-free amount string; null when there is no digit. */
    fun parseGrouped(text: String): Long? =
        text.filter { it.isDigit() }.takeIf { it.isNotEmpty() }?.toLongOrNull()

    private fun groupAboveThousand(stripped: String): String {
        val tail = stripped.takeLast(3)
        val head = stripped.dropLast(3)
        val chunks = ArrayList<String>()
        var end = head.length
        while (end > 0) {
            val start = (end - 2).coerceAtLeast(0)
            chunks += head.substring(start, end)
            end = start
        }
        return chunks.asReversed().joinToString(separator = ",") + "," + tail
    }

    private fun compactMagnitude(value: Long, divisor: Long): String {
        val whole = value / divisor
        val hundredths = (value % divisor) * 100 / divisor
        if (hundredths == 0L) return whole.toString()
        val fraction = hundredths.toString().padStart(2, '0').trimEnd('0')
        return "$whole.$fraction"
    }
}
