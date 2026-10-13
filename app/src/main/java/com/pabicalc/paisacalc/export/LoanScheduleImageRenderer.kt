package com.pabicalc.paisacalc.export

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import com.pabicalc.paisacalc.engine.LoanSummary
import java.io.OutputStream

/** Renders the complete amortization schedule into a tall, share-ready PNG. */
object LoanScheduleImageRenderer {

    const val IMAGE_WIDTH = 720
    private const val HORIZONTAL_MARGIN = 28f
    private const val HEADER_HEIGHT = 180
    private const val METRIC_ROW_HEIGHT = 40
    private const val TABLE_TOP_GAP = 24
    private const val TABLE_HEADER_HEIGHT = 34
    private const val INSTALLMENT_ROW_HEIGHT = 25
    private const val FOOTER_HEIGHT = 74
    private const val MAX_BITMAP_HEIGHT = 32_000

    private val emerald = 0xFF0B6E4F.toInt()
    private val paleGreen = 0xFFEAF5EF.toInt()
    private val ink = 0xFF19211D.toInt()
    private val muted = 0xFF53615B.toInt()
    private val divider = 0xFFD7E1DC.toInt()
    private val white = 0xFFFFFFFF.toInt()

    fun writePng(summary: LoanSummary, output: OutputStream) =
        writePng(LoanScheduleExportData.from(summary), output)

    fun writePng(data: LoanScheduleExportData, output: OutputStream) {
        require(data.rows.isNotEmpty()) { "A loan schedule must contain at least one installment" }
        val metricRows = (data.metrics.size + 1) / 2
        val tableTop = HEADER_HEIGHT + metricRows * METRIC_ROW_HEIGHT + TABLE_TOP_GAP
        val height = tableTop + TABLE_HEADER_HEIGHT + data.rows.size * INSTALLMENT_ROW_HEIGHT + FOOTER_HEIGHT
        require(height <= MAX_BITMAP_HEIGHT) { "Schedule image is too tall to render safely" }

        val bitmap = Bitmap.createBitmap(IMAGE_WIDTH, height, Bitmap.Config.RGB_565)
        try {
            val canvas = Canvas(bitmap)
            canvas.drawColor(white)
            drawHeader(canvas, data)
            drawMetrics(canvas, data.metrics)
            drawTable(canvas, data.rows, tableTop)
            drawFooter(canvas, height, data.note)
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                "Could not encode schedule image"
            }
        } finally {
            bitmap.recycle()
        }
    }

    private fun drawHeader(canvas: Canvas, data: LoanScheduleExportData) {
        canvas.drawRect(0f, 0f, IMAGE_WIDTH.toFloat(), HEADER_HEIGHT.toFloat(), fill(emerald))
        canvas.drawText("PaisaCalc", HORIZONTAL_MARGIN, 46f, text(22f, white, bold = true))
        canvas.drawText(data.title, HORIZONTAL_MARGIN, 91f, text(29f, white, bold = true))
        canvas.drawText(data.period, HORIZONTAL_MARGIN, 122f, text(17f, white))
        canvas.drawText("Offline loan schedule · amounts in Indian rupees", HORIZONTAL_MARGIN, 154f, text(15f, white))
    }

    private fun drawMetrics(canvas: Canvas, data: List<LoanExportMetric>) {
        val top = HEADER_HEIGHT + 19f
        val halfWidth = (IMAGE_WIDTH - HORIZONTAL_MARGIN * 2) / 2f
        data.forEachIndexed { index, metric ->
            val column = index % 2
            val row = index / 2
            val left = HORIZONTAL_MARGIN + column * halfWidth
            val y = top + row * METRIC_ROW_HEIGHT
            canvas.drawText(metric.label, left, y, text(13f, muted))
            canvas.drawText(metric.value, left, y + 19f, text(16f, ink, bold = true))
        }
    }

    private fun drawTable(canvas: Canvas, rows: List<LoanExportRow>, top: Int) {
        val tableRight = IMAGE_WIDTH - HORIZONTAL_MARGIN
        val headerTop = top.toFloat()
        canvas.drawRect(HORIZONTAL_MARGIN, headerTop, tableRight, headerTop + TABLE_HEADER_HEIGHT, fill(emerald))

        val rightEdges = floatArrayOf(100f, 210f, 320f, 430f, 540f, tableRight)
        val headerPaint = text(12f, white, bold = true).apply { textAlign = Paint.Align.RIGHT }
        canvas.drawText("Month", HORIZONTAL_MARGIN + 4f, headerTop + 22f, text(12f, white, bold = true))
        listOf("EMI", "Principal", "Interest", "Prepaid", "Balance").forEachIndexed { index, label ->
            canvas.drawText(label, rightEdges[index + 1] - 5f, headerTop + 22f, headerPaint)
        }

        val leftPaint = text(13f, ink)
        val rightPaint = text(13f, ink).apply { textAlign = Paint.Align.RIGHT }
        rows.forEachIndexed { index, row ->
            val rowTop = headerTop + TABLE_HEADER_HEIGHT + index * INSTALLMENT_ROW_HEIGHT
            val baseline = rowTop + 18f
            if (row.hasPrepayment) {
                canvas.drawRect(HORIZONTAL_MARGIN, rowTop, tableRight, rowTop + INSTALLMENT_ROW_HEIGHT, fill(paleGreen))
            } else if (index % 2 == 1) {
                canvas.drawRect(HORIZONTAL_MARGIN, rowTop, tableRight, rowTop + INSTALLMENT_ROW_HEIGHT, fill(0xFFF7F9F8.toInt()))
            }
            canvas.drawText(row.month, HORIZONTAL_MARGIN + 4f, baseline, leftPaint)
            canvas.drawText(row.emi, rightEdges[1] - 5f, baseline, rightPaint)
            canvas.drawText(row.principal, rightEdges[2] - 5f, baseline, rightPaint)
            canvas.drawText(row.interest, rightEdges[3] - 5f, baseline, rightPaint)
            canvas.drawText(row.prepayment, rightEdges[4] - 5f, baseline, rightPaint)
            canvas.drawText(row.balance, rightEdges[5] - 5f, baseline, rightPaint)
            canvas.drawLine(HORIZONTAL_MARGIN, rowTop + INSTALLMENT_ROW_HEIGHT, tableRight, rowTop + INSTALLMENT_ROW_HEIGHT, stroke(divider))
        }
    }

    private fun drawFooter(canvas: Canvas, height: Int, note: String) {
        val top = (height - FOOTER_HEIGHT).toFloat()
        canvas.drawLine(HORIZONTAL_MARGIN, top, IMAGE_WIDTH - HORIZONTAL_MARGIN, top, stroke(divider))
        canvas.drawText(note, HORIZONTAL_MARGIN, top + 25f, text(13f, muted))
        canvas.drawText("Created with PaisaCalc · Verify figures with your lender", HORIZONTAL_MARGIN, top + 49f, text(12f, muted))
    }

    private fun fill(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }

    private fun stroke(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    private fun text(size: Float, color: Int, bold: Boolean = false) =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = size
            typeface = Typeface.create(Typeface.SANS_SERIF, if (bold) Typeface.BOLD else Typeface.NORMAL)
            style = Paint.Style.FILL
        }
}
