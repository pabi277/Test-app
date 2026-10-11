package com.pabicalc.paisacalc.export

import com.pabicalc.paisacalc.engine.LoanSummary
import com.pabicalc.paisacalc.format.IndianNumberFormat
import com.pabicalc.paisacalc.ui.emi.DisplayFormatters

/** A single headline value shared by the PDF and image renderers. */
data class LoanExportMetric(val label: String, val value: String)

/** One complete installment row, including any extra prepayment. */
data class LoanExportRow(
    val month: String,
    val emi: String,
    val principal: String,
    val interest: String,
    val prepayment: String,
    val balance: String,
    val hasPrepayment: Boolean,
)

/** Immutable, already-formatted export content; renderers do no loan math. */
data class LoanScheduleExportData(
    val title: String,
    val period: String,
    val metrics: List<LoanExportMetric>,
    val rows: List<LoanExportRow>,
    val note: String,
) {
    companion object {
        fun from(summary: LoanSummary): LoanScheduleExportData {
            val metrics = buildList {
                add(LoanExportMetric("Loan amount", IndianNumberFormat.rupees(summary.principal)))
                add(
                    LoanExportMetric(
                        "Annual rate",
                        "${DisplayFormatters.rateLabel(summary.terms.rateMicroPercent)} p.a.",
                    ),
                )
                add(LoanExportMetric("Original term", DisplayFormatters.tenureLabel(summary.terms.tenureMonths)))
                add(LoanExportMetric("Monthly EMI", IndianNumberFormat.rupees(summary.monthlyEmi)))
                if (summary.finalMonthlyEmi != summary.monthlyEmi) {
                    add(LoanExportMetric("Final EMI", IndianNumberFormat.rupees(summary.finalMonthlyEmi)))
                }
                add(LoanExportMetric("Total interest", IndianNumberFormat.rupees(summary.totalInterest)))
                add(LoanExportMetric("Total payable", IndianNumberFormat.rupees(summary.totalPayable)))
                if (summary.hasPrepayments) {
                    add(LoanExportMetric("Total prepaid", IndianNumberFormat.rupees(summary.totalPrepaid)))
                    add(LoanExportMetric("Interest saved", IndianNumberFormat.rupees(summary.interestSaved)))
                }
            }
            return LoanScheduleExportData(
                title = "Loan repayment schedule",
                period = "${summary.startMonth.shortLabel()} – ${summary.endMonth.shortLabel()}",
                metrics = metrics,
                rows = summary.installments.map { installment ->
                    LoanExportRow(
                        month = installment.month.shortLabel(),
                        emi = IndianNumberFormat.rupees(installment.amountPaid),
                        principal = IndianNumberFormat.rupees(installment.principalComponent),
                        interest = IndianNumberFormat.rupees(installment.interestComponent),
                        prepayment = if (installment.prepaymentComponent > 0L) {
                            IndianNumberFormat.rupees(installment.prepaymentComponent)
                        } else {
                            "—"
                        },
                        balance = IndianNumberFormat.rupees(installment.closingBalance),
                        hasPrepayment = installment.prepaymentComponent > 0L,
                    )
                },
                note = "Estimates only. A lender's schedule can differ because of its rounding policy.",
            )
        }
    }
}
