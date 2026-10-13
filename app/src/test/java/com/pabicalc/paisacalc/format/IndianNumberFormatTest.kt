package com.pabicalc.paisacalc.format

import com.pabicalc.paisacalc.engine.LoanTerms
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The lakh/crore grouping suite — competitor bug #1 in the research, so it
 * gets exhaustive coverage including above ₹10 lakh and above ₹1 crore.
 */
class IndianNumberFormatTest {

    @Test
    fun groupsIndianStyle() {
        assertEquals("0", IndianNumberFormat.group(0L))
        assertEquals("9", IndianNumberFormat.group(9L))
        assertEquals("999", IndianNumberFormat.group(999L))
        assertEquals("1,000", IndianNumberFormat.group(1_000L))
        assertEquals("99,999", IndianNumberFormat.group(99_999L))
        assertEquals("1,00,000", IndianNumberFormat.group(100_000L))
        assertEquals("10,00,000", IndianNumberFormat.group(1_000_000L))
        assertEquals("25,00,000", IndianNumberFormat.group(2_500_000L))
        assertEquals("12,34,567", IndianNumberFormat.group(1_234_567L))
        assertEquals("1,00,00,000", IndianNumberFormat.group(10_000_000L))
        assertEquals("1,23,45,678", IndianNumberFormat.group(12_345_678L))
        assertEquals("1,00,00,00,000", IndianNumberFormat.group(1_000_000_000L))
    }

    @Test
    fun groupsDigitsStringsAndIgnoresNoise() {
        assertEquals("12,34,567", IndianNumberFormat.group("1234567"))
        assertEquals("12,34,567", IndianNumberFormat.group("₹12,34,567"))
        assertEquals("", IndianNumberFormat.group("abc"))
        assertEquals("-12,34,567", IndianNumberFormat.group("-1234567"))
    }

    @Test
    fun rendersRupees() {
        assertEquals("₹25,00,000", IndianNumberFormat.rupees(2_500_000L))
        assertEquals("₹0", IndianNumberFormat.rupees(0L))
        assertEquals("₹1,00,00,00,000", IndianNumberFormat.rupees(1_000_000_000L))
    }

    @Test
    fun paiseAmountsKeepTwoDecimalsAndIndianGrouping() {
        assertEquals("₹1,23,45,678.09", IndianNumberFormat.rupeesAndPaise(1_234_567_809L))
        assertEquals("₹0.05", IndianNumberFormat.rupeesAndPaise(5L))
        assertEquals("₹1,00,00,00,000.00", IndianNumberFormat.rupeesAndPaise(100_000_000_000L))
    }

    @Test
    fun compactRendersLakhAndCrore() {
        assertEquals("₹99,999", IndianNumberFormat.compactRupees(99_999L))
        assertEquals("₹1 L", IndianNumberFormat.compactRupees(100_000L))
        assertEquals("₹5 L", IndianNumberFormat.compactRupees(500_000L))
        assertEquals("₹12.5 L", IndianNumberFormat.compactRupees(1_250_000L))
        assertEquals("₹1 Cr", IndianNumberFormat.compactRupees(10_000_000L))
        assertEquals("₹1.23 Cr", IndianNumberFormat.compactRupees(12_345_678L))
        assertEquals("₹100 Cr", IndianNumberFormat.compactRupees(1_000_000_000L))
    }

    @Test
    fun compactTruncatesInsteadOfOverstating() {
        // 9,99,999 must never be shown as "₹10 L".
        assertEquals("₹9.99 L", IndianNumberFormat.compactRupees(999_999L))
        // 99,99,999 must never be shown as "₹1 Cr".
        assertEquals("₹99.99 L", IndianNumberFormat.compactRupees(9_999_999L))
    }

    @Test
    fun digitsOnlyCapsTheLength() {
        assertEquals("123", IndianNumberFormat.digitsOnly("12a3b", maxDigits = 12))
        assertEquals("123456789012", IndianNumberFormat.digitsOnly("1234567890123456", maxDigits = 12))
        assertEquals("", IndianNumberFormat.digitsOnly("abc", maxDigits = 12))
    }

    @Test
    fun parsesGroupedAmounts() {
        assertEquals(2_500_000L, IndianNumberFormat.parseGrouped("25,00,000"))
        assertEquals(2_500_000L, IndianNumberFormat.parseGrouped("₹25,00,000"))
        assertNull(IndianNumberFormat.parseGrouped(""))
        assertNull(IndianNumberFormat.parseGrouped("₹"))
    }

    @Test
    fun maxAmountMatchesTheEngineLimit() {
        assertEquals(LoanTerms.MAX_PRINCIPAL, IndianNumberFormat.MAX_AMOUNT)
        assertEquals("₹1,00,00,00,000", IndianNumberFormat.rupees(LoanTerms.MAX_PRINCIPAL))
    }
}
