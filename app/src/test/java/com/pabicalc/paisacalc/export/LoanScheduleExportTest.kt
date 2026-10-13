package com.pabicalc.paisacalc.export

import android.graphics.BitmapFactory
import com.pabicalc.paisacalc.engine.EmiEngine
import com.pabicalc.paisacalc.engine.LoanMonth
import com.pabicalc.paisacalc.engine.LoanTerms
import com.pabicalc.paisacalc.engine.Prepayment
import com.pabicalc.paisacalc.engine.PrepaymentFrequency
import com.pabicalc.paisacalc.engine.PrepaymentPlan
import com.pabicalc.paisacalc.engine.PrepaymentStrategy
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LoanScheduleExportTest {

    @Test
    fun exportDataKeepsIndianRupeesAndEveryPrepaymentRow() {
        val summary = prepaymentSummary()
        val data = LoanScheduleExportData.from(summary)

        assertEquals(summary.installments.size, data.rows.size)
        assertEquals("Loan repayment schedule", data.title)
        assertEquals(
            "${summary.startMonth.shortLabel()} – ${summary.endMonth.shortLabel()}",
            data.period,
        )
        assertTrue(data.metrics.any { it.label == "Total prepaid" && it.value.startsWith("₹") })
        assertTrue(data.metrics.any { it.label == "Interest saved" && it.value.startsWith("₹") })
        assertTrue(data.rows.any { it.hasPrepayment && it.prepayment.startsWith("₹") })
        assertTrue(data.rows.all { it.emi.startsWith("₹") && it.balance.startsWith("₹") })
    }

    @Test
    fun pdfLayoutPaginatesEveryInstallmentAndKeepsPrepaymentRows() {
        val data = LoanScheduleExportData.from(prepaymentSummary())
        val rows = List(120) { data.rows[it % data.rows.size] }
        val pages = LoanSchedulePdfLayout.paginate(data.copy(rows = rows))

        assertTrue(pages.size > 1)
        assertEquals(rows, pages.flatMap { it.rows })
        assertTrue(pages.flatMap { it.rows }.any { it.hasPrepayment })
        var expectedFirstRowIndex = 0
        pages.forEachIndexed { index, page ->
            assertEquals(index + 1, page.number)
            assertEquals(expectedFirstRowIndex, page.firstRowIndex)
            assertTrue(page.rows.isNotEmpty())
            expectedFirstRowIndex += page.rows.size
        }
        assertEquals(rows.size, expectedFirstRowIndex)
    }

    @Test
    fun imageRendererWritesPngWithAllScheduleRows() {
        val summary = prepaymentSummary()
        val output = ByteArrayOutputStream()

        LoanScheduleImageRenderer.writePng(summary, output)

        val bytes = output.toByteArray()
        assertTrue(bytes.size > 1_000)
        assertArrayEquals(
            byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A),
            bytes.copyOfRange(0, 8),
        )
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        assertEquals(LoanScheduleImageRenderer.IMAGE_WIDTH, options.outWidth)
        assertEquals(expectedImageHeight(summary), options.outHeight)
    }

    private fun prepaymentSummary() = EmiEngine.calculate(
        terms = LoanTerms(
            principal = 250_000L,
            rateMicroPercent = 8_500_000L,
            tenureMonths = 24,
        ),
        startMonth = LoanMonth(2026, 10),
        plan = PrepaymentPlan(
            listOf(
                Prepayment(
                    frequency = PrepaymentFrequency.QUARTERLY,
                    fromInstallment = 4,
                    amount = 10_000L,
                    strategy = PrepaymentStrategy.REDUCE_TENURE,
                ),
            ),
        ),
    )

    private fun expectedImageHeight(summary: com.pabicalc.paisacalc.engine.LoanSummary): Int {
        val metricRows = (LoanScheduleExportData.from(summary).metrics.size + 1) / 2
        return 180 + metricRows * 40 + 24 + 34 + summary.installments.size * 25 + 74
    }
}
