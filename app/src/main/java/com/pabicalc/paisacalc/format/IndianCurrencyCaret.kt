package com.pabicalc.paisacalc.format

/**
 * Cursor arithmetic for a text field that shows Indian-grouped digits while
 * the stored value stays digits-only (`2500000` displayed as `25,00,000`).
 *
 * Pure functions: the Compose `VisualTransformation` adapter is a thin shell,
 * so the fiddly cursor math is unit-tested without any UI framework.
 * `transformedToOriginal(originalToTransformed(offset)) == offset` holds for
 * every offset (see `IndianCurrencyCaretTest`).
 */
object IndianCurrencyCaret {

    /**
     * Offset in the grouped text for [rawOffset] digits into the raw value.
     * Commas are inserted after every third-from-the-right digit, so the
     * grouped offset is the raw offset plus the commas that precede it.
     */
    fun toTransformedOffset(rawLength: Int, rawOffset: Int): Int {
        val offset = rawOffset.coerceIn(0, rawLength)
        if (rawLength <= 3) return offset
        var commas = 0
        var boundary = rawLength - 3
        while (boundary > 0) {
            if (boundary < offset) commas++
            boundary -= 2
        }
        return (offset + commas).coerceAtMost(groupedLength(rawLength))
    }

    /** Length of the grouped form of [rawLength] digits. */
    fun groupedLength(rawLength: Int): Int = when {
        rawLength <= 0 -> 0
        rawLength <= 3 -> rawLength
        else -> rawLength + 1 + (rawLength - 4) / 2
    }

    /** Raw digit offset for a cursor at [transformedOffset] in [grouped]. */
    fun toRawOffset(grouped: CharSequence, transformedOffset: Int): Int {
        val end = transformedOffset.coerceIn(0, grouped.length)
        var digits = 0
        for (index in 0 until end) {
            if (grouped[index].isDigit()) digits++
        }
        return digits
    }
}
