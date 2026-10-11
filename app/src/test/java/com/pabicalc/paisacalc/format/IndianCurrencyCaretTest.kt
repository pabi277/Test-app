package com.pabicalc.paisacalc.format

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Cursor mapping for the live "25,00,000 while you type" amount field.
 * If these break, the caret jumps while typing — the classic grouped-input bug.
 */
class IndianCurrencyCaretTest {

    @Test
    fun groupedLengthMatchesTheFormatter() {
        for (length in 0..14) {
            val raw = "1".repeat(length)
            assertEquals("length $length", IndianNumberFormat.group(raw).length, IndianCurrencyCaret.groupedLength(length))
        }
    }

    @Test
    fun caretOffsetRoundTripsForEveryPosition() {
        for (length in 1..12) {
            val raw = "123456789012".take(length)
            val grouped = IndianNumberFormat.group(raw)
            for (offset in 0..length) {
                val transformed = IndianCurrencyCaret.toTransformedOffset(length, offset)
                assertEquals(
                    "raw '$raw' offset $offset -> transformed $transformed",
                    offset,
                    IndianCurrencyCaret.toRawOffset(grouped, transformed),
                )
            }
        }
    }

    @Test
    fun transformedOffsetsAreMonotonicAndInBounds() {
        for (length in 1..12) {
            val grouped = IndianNumberFormat.group("123456789012".take(length))
            var previous = 0
            for (offset in 0..length) {
                val transformed = IndianCurrencyCaret.toTransformedOffset(length, offset)
                assertTrue("offset $offset of $length", transformed >= previous)
                assertTrue("offset $offset of $length", transformed <= grouped.length)
                previous = transformed
            }
        }
    }

    @Test
    fun knownPositionsMapAsExpected() {
        // "25,00,000": typing at the end leaves the caret at the end.
        assertEquals(9, IndianCurrencyCaret.toTransformedOffset(rawLength = 7, rawOffset = 7))
        // After 3 digits the grouped text has consumed one comma.
        assertEquals(4, IndianCurrencyCaret.toTransformedOffset(rawLength = 7, rawOffset = 3))
        // Short numbers have no commas at all.
        assertEquals(3, IndianCurrencyCaret.toTransformedOffset(rawLength = 3, rawOffset = 3))
        assertEquals(0, IndianCurrencyCaret.toTransformedOffset(rawLength = 7, rawOffset = 0))
    }

    @Test
    fun rawOffsetCountsDigitsOnly() {
        assertEquals(7, IndianCurrencyCaret.toRawOffset("25,00,000", 9))
        assertEquals(3, IndianCurrencyCaret.toRawOffset("25,00,000", 4))
        assertEquals(0, IndianCurrencyCaret.toRawOffset("25,00,000", 0))
    }
}
