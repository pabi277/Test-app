package com.pabicalc.paisacalc.export

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.pabicalc.paisacalc.engine.LoanSummary
import java.io.OutputStream

/** Paginates every monthly row onto a printer-friendly A4-like PDF page. */
object LoanSchedulePdfRenderer {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val LEFT = 34f
    private const val RIGHT = 561f
    private const val FOOTER_TOP = 812f
    private const val ROW_HEIGHT = 19f

    private val emerald = 0xFF0B6E4F.toInt()
    private val darkInk = 0xFF19211D.toInt()
    private val mutedInk = 0xFF53615B.toInt()
    private val divider = 0xFFD7E1DC.toInt()
    private val paleGreen = 0xFFEAF5EF.toInt()
    private val white = 0xFFFFFFFF.toInt()

    fun write(summary: LoanSummary, output: OutputStream) = write(LoanScheduleExportData.from(summary), output)

    fun write(data: LoanScheduleExportData, output: OutputStream) {
        require(data.rows.isNotEmpty()) { "A loan schedule must contain at least one installment" }
        val document = PdfDocument()
        try {
            var rowIndex = 0
            var pageNumber = 1
            while (rowIndex < data.rows.size) {
                val page = document.startPage(
                    PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create(),
                )
                val canvas = page.canvas
                canvas.drawColor(white)
                var y = if (pageNumber == 1) drawFirstPageHeader(canvas, data) else drawContinuationHeader(canvas)
                drawTableHeader(canvas, y)
                y += 23f

                while (rowIndex < data.rows.size && y + ROW_HEIGHT < FOOTER_TOP - 4f) {
                    drawRow(canvas, data.rows[rowIndex], y, rowIndex)
                    y += ROW_HEIGHT
                    rowIndex++
                }
                drawFooter(canvas, pageNumber)
                document.finishPage(page)
                pageNumber++
            }
            document.writeTo(output)
        } finally {
            document.close()
        }
    }

    private fun drawFirstPageHeader(canvas: Canvas, data: LoanScheduleExportData): Float {
        val banner = fillPaint(emerald)
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 58f, banner)
        canvas.drawText("PaisaCalc", LEFT, 37f, textPaint(20f, white, bold = true))
        canvas.drawText(data.title, LEFT, 86f, textPaint(17f, darkInk, bold = true))
        canvas.drawText(data.period, LEFT, 104f, textPaint(10f, mutedInk))

        val metricsTop = 127f
        val metricRows = (data.metrics.size + 1) / 2
        val halfWidth = (RIGHT - LEFT) / 2f
        data.metrics.forEachIndexed { index, metric ->
            val column = index % 2
            val row = index / 2
            val x = LEFT + column * halfWidth
            val top = metricsTop + row * 32f
            canvas.drawText(metric.label, x, top, textPaint(8.5f, mutedInk))
            canvas.drawText(metric.value, x, top + 13f, textPaint(10f, darkInk, bold = true))
        }
        return metricsTop + metricRows * 32f + 15f
    }

    private fun drawContinuationHeader(canvas: Canvas): Float {
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 48f, fillPaint(emerald))
        canvas.drawText("PaisaCalc · Loan schedule (continued)", LEFT, 31f, textPaint(14f, white, bold = true))
        return 69f
    }

    private fun drawTableHeader(canvas: Canvas, top: Float) {
        canvas.drawRect(LEFT, top, RIGHT, top + 22f, fillPaint(emerald))
        canvas.drawText("Month", 39f, top + 15f, textPaint(8.5f, white, bold = true))
        val header = textPaint(8.5f, white, bold = true).apply { textAlign = Paint.Align.RIGHT }
        canvas.drawText("EMI", 174f, top + 15f, header)
        canvas.drawText("Principal", 264f, top + 15f, header)
        canvas.drawText("Interest", 354f, top + 15f, header)
        canvas.drawText("Prepaid", 445f, top + 15f, header)
        canvas.drawText("Balance", RIGHT - 8f, top + 15f, header)
    }

    private fun drawRow(canvas: Canvas, row: LoanExportRow, baseline: Float, index: Int) {
        val top = baseline - 13f
        if (row.hasPrepayment) {
            canvas.drawRect(LEFT, top, RIGHT, baseline + 5f, fillPaint(paleGreen))
        } else if (index % 2 == 1) {
            canvas.drawRect(LEFT, top, RIGHT, baseline + 5f, fillPaint(0xFFF7F9F8.toInt()))
        }
        val left = textPaint(8.3f, darkInk)
        val right = textPaint(8.3f, darkInk).apply { textAlign = Paint.Align.RIGHT }
        canvas.drawText(row.month, 39f, baseline, left)
        canvas.drawText(row.emi, 174f, baseline, right)
        canvas.drawText(row.principal, 264f, baseline, right)
        canvas.drawText(row.interest, 354f, baseline, right)
        canvas.drawText(row.prepayment, 445f, baseline, right)
        canvas.drawText(row.balance, RIGHT - 8f, baseline, right)
        canvas.drawLine(LEFT, baseline + 5f, RIGHT, baseline + 5f, strokePaint(divider, 0.45f))
    }

    private fun drawFooter(canvas: Canvas, pageNumber: Int) {
        canvas.drawLine(LEFT, FOOTER_TOP, RIGHT, FOOTER_TOP, strokePaint(divider, 0.7f))
        canvas.drawText(
            "Estimates only · lender rounding may differ",
            LEFT,
            FOOTER_TOP + 16f,
            textPaint(8f, mutedInk),
        )
        val page = textPaint(8f, mutedInk).apply { textAlign = Paint.Align.RIGHT }
        canvas.drawText("Page $pageNumber", RIGHT, FOOTER_TOP + 16f, page)
    }

    private fun fillPaint(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }

    private fun strokePaint(color: Int, width: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        strokeWidth = width
        style = Paint.Style.STROKE
    }

    private fun textPaint(size: Float, color: Int, bold: Boolean = false) =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = size
            typeface = Typeface.create(Typeface.SANS_SERIF, if (bold) Typeface.BOLD else Typeface.NORMAL)
            style = Paint.Style.FILL
        }
}
