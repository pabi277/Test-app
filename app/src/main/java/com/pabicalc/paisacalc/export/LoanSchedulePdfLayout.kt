package com.pabicalc.paisacalc.export

/** One page's installment slice, with its original row offset for striping. */
internal data class LoanSchedulePdfPage(
    val number: Int,
    val firstRowIndex: Int,
    val rows: List<LoanExportRow>,
)

/** Pure layout math kept separate so pagination can be tested without Android's PDF native backend. */
internal object LoanSchedulePdfLayout {

    const val PAGE_WIDTH = 595
    const val PAGE_HEIGHT = 842
    const val FOOTER_TOP = 812f
    const val ROW_HEIGHT = 19f

    const val FIRST_PAGE_METRICS_TOP = 127f
    const val METRIC_ROW_HEIGHT = 32f
    const val FIRST_PAGE_METRICS_PADDING = 15f
    const val CONTINUATION_TABLE_TOP = 69f
    const val TABLE_HEADER_HEIGHT = 23f
    private const val FOOTER_CLEARANCE = 4f

    fun paginate(data: LoanScheduleExportData): List<LoanSchedulePdfPage> {
        require(data.rows.isNotEmpty()) { "A loan schedule must contain at least one installment" }

        val pages = mutableListOf<LoanSchedulePdfPage>()
        var rowIndex = 0
        var pageNumber = 1
        while (rowIndex < data.rows.size) {
            var y = if (pageNumber == 1) {
                FIRST_PAGE_METRICS_TOP + ((data.metrics.size + 1) / 2) * METRIC_ROW_HEIGHT +
                    FIRST_PAGE_METRICS_PADDING
            } else {
                CONTINUATION_TABLE_TOP
            }
            y += TABLE_HEADER_HEIGHT
            val firstRowIndex = rowIndex
            val pageRows = buildList {
                while (rowIndex < data.rows.size && y + ROW_HEIGHT < FOOTER_TOP - FOOTER_CLEARANCE) {
                    add(data.rows[rowIndex])
                    rowIndex++
                    y += ROW_HEIGHT
                }
            }
            check(pageRows.isNotEmpty()) { "Schedule header leaves no room for installment rows" }
            pages += LoanSchedulePdfPage(
                number = pageNumber,
                firstRowIndex = firstRowIndex,
                rows = pageRows,
            )
            pageNumber++
        }
        return pages
    }
}
