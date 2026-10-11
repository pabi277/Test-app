package com.pabicalc.paisacalc.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class LoanMonthTest {

    @Test
    fun plusMonthsCrossesYearBoundariesCorrectly() {
        assertEquals(LoanMonth(2027, 1), LoanMonth(2026, 10).plusMonths(3))
        assertEquals(LoanMonth(2027, 10), LoanMonth(2026, 10).plusMonths(12))
        assertEquals(LoanMonth(2026, 10), LoanMonth(2026, 10).plusMonths(0))
        assertEquals(LoanMonth(2025, 8), LoanMonth(2026, 10).plusMonths(-14))
        assertEquals(LoanMonth(2046, 9), LoanMonth(2026, 10).plusMonths(239))
        assertEquals(LoanMonth(2046, 10), LoanMonth(2026, 10).plusMonths(240))
    }

    @Test
    fun plusMonthsHandlesDecemberToJanuary() {
        assertEquals(LoanMonth(2027, 1), LoanMonth(2026, 12).plusMonths(1))
        assertEquals(LoanMonth(2025, 12), LoanMonth(2026, 1).plusMonths(-1))
    }

    @Test
    fun labelsUseShortEnglishMonths() {
        assertEquals("Oct-26", LoanMonth(2026, 10).shortLabel())
        assertEquals("Jan-29", LoanMonth(2029, 1).shortLabel())
        assertEquals("Dec-00", LoanMonth(2000, 12).shortLabel())
        assertEquals("Sep 2046", LoanMonth(2046, 9).longLabel())
    }

    @Test
    fun monthsOrderChronologically() {
        assertTrue(LoanMonth(2026, 10) < LoanMonth(2026, 11))
        assertTrue(LoanMonth(2026, 12) < LoanMonth(2027, 1))
        assertEquals(0, LoanMonth(2026, 10).compareTo(LoanMonth(2026, 10)))
    }

    @Test
    fun invalidMonthIsRejected() {
        assertThrows(IllegalArgumentException::class.java) { LoanMonth(2026, 0) }
        assertThrows(IllegalArgumentException::class.java) { LoanMonth(2026, 13) }
    }

    @Test
    fun nowReturnsAPlausibleCurrentMonth() {
        val now = LoanMonth.now()
        assertTrue("year ${now.year}", now.year in 2024..2100)
        assertTrue("month ${now.month}", now.month in 1..12)
    }
}
