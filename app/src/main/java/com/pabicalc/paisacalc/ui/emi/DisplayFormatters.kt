package com.pabicalc.paisacalc.ui.emi

import java.util.Locale

/**
 * Small, pure display helpers. Locale.ROOT everywhere: the app ships
 * English (India) only, and formatting must not change with device locale.
 */
object DisplayFormatters {

    /** "1 month", "18 months", "1 year 6 months", "20 years". */
    fun tenureLabel(months: Int): String {
        val years = months / 12
        val rest = months % 12
        val yearsText = when {
            years == 0 -> ""
            years == 1 -> "1 year"
            else -> "$years years"
        }
        val monthsText = when {
            rest == 0 -> ""
            rest == 1 -> "1 month"
            else -> "$rest months"
        }
        return listOf(yearsText, monthsText)
            .filter { it.isNotEmpty() }
            .joinToString(separator = " ")
    }

    /** 0.8963 -> "89.6%". */
    fun percent(share: Double, decimals: Int = 1): String =
        String.format(Locale.ROOT, "%.${decimals}f%%", share * 100.0)

    /** `8_750_000` -> "8.75%", `12_000_000` -> "12%", `0` -> "0%". */
    fun rateLabel(rateMicroPercent: Long): String {
        val whole = rateMicroPercent / 1_000_000L
        val hundredths = (rateMicroPercent % 1_000_000L) / 10_000L
        if (hundredths == 0L) return "$whole%"
        val fraction = hundredths.toString().padStart(2, '0').trimEnd('0')
        return "$whole.$fraction%"
    }

    /** Slider value (percent, up to 2 decimals) -> canonical field text. */
    fun sliderRateText(ratePercent: Float): String =
        String.format(Locale.ROOT, "%.2f", ratePercent).trimEnd('0').trimEnd('.')

    /** "240 installments" style count label. */
    fun installmentCount(count: Int): String = if (count == 1) "1 installment" else "$count installments"
}
