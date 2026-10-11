package com.pabicalc.paisacalc.engine

import java.util.Calendar
import java.util.Locale

/**
 * A calendar month, used for amortization schedule labels and month arithmetic.
 *
 * Deliberately dependency-free (no `java.time`, which needs API 26 or core-library
 * desugaring at minSdk 24) so the engine stays pure Kotlin and runs in plain JVM
 * unit tests. Month labels use the short English form the research asked for
 * ("mmm-yy", e.g. `Oct-26`).
 */
data class LoanMonth(val year: Int, val month: Int) : Comparable<LoanMonth> {

    init {
        require(month in 1..12) { "month must be in 1..12, was $month" }
    }

    /** This month shifted by [count] months (negative counts shift backwards). */
    fun plusMonths(count: Int): LoanMonth {
        val total = year * 12 + (month - 1) + count
        return LoanMonth(Math.floorDiv(total, 12), Math.floorMod(total, 12) + 1)
    }

    /** e.g. `Oct-26` — the mmm-yy form requested in the store-review research. */
    fun shortLabel(): String =
        String.format(Locale.ROOT, "%s-%02d", ABBREVIATIONS[month - 1], Math.floorMod(year, 100))

    /** e.g. `Oct 2026`. */
    fun longLabel(): String =
        String.format(Locale.ROOT, "%s %d", ABBREVIATIONS[month - 1], year)

    override fun compareTo(other: LoanMonth): Int =
        compareValuesBy(this, other, { it.year }, { it.month })

    companion object {
        private val ABBREVIATIONS = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
        )

        /**
         * The current calendar month on the device. The amortization schedule
         * starts here; banks usually bill the first installment within a month
         * of disbursal, so "today" is the close-enough, predictable default.
         */
        fun now(): LoanMonth {
            val calendar = Calendar.getInstance()
            return LoanMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
        }
    }
}
