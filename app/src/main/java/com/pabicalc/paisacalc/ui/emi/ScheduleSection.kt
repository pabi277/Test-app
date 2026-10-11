@file:OptIn(ExperimentalMaterial3Api::class)

package com.pabicalc.paisacalc.ui.emi

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pabicalc.paisacalc.R
import com.pabicalc.paisacalc.engine.Installment
import com.pabicalc.paisacalc.engine.LoanSummary
import com.pabicalc.paisacalc.format.IndianNumberFormat

private val MONTH_COLUMN_WIDTH = 52.dp
private const val PRINCIPAL_WEIGHT = 1.05f
private const val INTEREST_WEIGHT = 1.05f
private const val BALANCE_WEIGHT = 1.3f

/** Card above the table: what the schedule covers, and its column headers. */
@Composable
fun ScheduleHeaderCard(summary: LoanSummary, modifier: Modifier = Modifier) {
    SectionCard(modifier = modifier) {
        Text(
            text = stringResource(R.string.schedule_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(
                R.string.schedule_subtitle,
                DisplayFormatters.installmentCount(summary.installments.size),
                summary.startMonth.shortLabel(),
                summary.endMonth.shortLabel(),
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(
                    R.string.schedule_emi_label,
                    IndianNumberFormat.rupees(summary.monthlyEmi),
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(R.string.schedule_amounts_note),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(modifier = Modifier.fillMaxWidth()) {
            HeaderCell(
                text = stringResource(R.string.schedule_header_month),
                modifier = Modifier.width(MONTH_COLUMN_WIDTH),
                alignment = TextAlign.Start,
            )
            HeaderCell(
                text = stringResource(R.string.schedule_header_principal),
                modifier = Modifier.weight(PRINCIPAL_WEIGHT),
            )
            HeaderCell(
                text = stringResource(R.string.schedule_header_interest),
                modifier = Modifier.weight(INTEREST_WEIGHT),
            )
            HeaderCell(
                text = stringResource(R.string.schedule_header_balance),
                modifier = Modifier.weight(BALANCE_WEIGHT),
            )
        }
    }
}

/** One 12-month block: "Year 1 · Oct-26 – Sep-27" with the block's own totals. */
@Composable
fun ScheduleYearHeader(year: ScheduleYear, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(top = 8.dp, bottom = 2.dp)) {
        Text(
            text = stringResource(
                R.string.schedule_year_label,
                year.index,
                year.firstMonth.shortLabel(),
                year.lastMonth.shortLabel(),
            ),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(
                R.string.schedule_year_totals,
                IndianNumberFormat.rupees(year.totalPrincipal),
                IndianNumberFormat.rupees(year.totalInterest),
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** One installment row: month, then the principal/interest/balance columns. */
@Composable
fun ScheduleRow(installment: Installment, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
    ) {
        Text(
            text = installment.month.shortLabel(),
            modifier = Modifier.width(MONTH_COLUMN_WIDTH),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AmountCell(
            text = IndianNumberFormat.group(installment.principalComponent),
            modifier = Modifier.weight(PRINCIPAL_WEIGHT),
            color = MaterialTheme.colorScheme.primary,
        )
        AmountCell(
            text = IndianNumberFormat.group(installment.interestComponent),
            modifier = Modifier.weight(INTEREST_WEIGHT),
            color = MaterialTheme.colorScheme.tertiary,
        )
        AmountCell(
            text = IndianNumberFormat.group(installment.closingBalance),
            modifier = Modifier.weight(BALANCE_WEIGHT),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** Explains a last installment that differs from the EMI, when it happens. */
@Composable
fun FinalInstallmentNote(summary: LoanSummary, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(
            R.string.schedule_final_adjusted,
            IndianNumberFormat.rupees(summary.installments.last().amountPaid),
        ),
        modifier = modifier.padding(top = 4.dp),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** Honesty note: this table is exact for the policy, banks round their own way. */
@Composable
fun ScheduleEstimateNote(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.schedule_estimate_note),
        modifier = modifier.padding(top = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun HeaderCell(text: String, modifier: Modifier, alignment: TextAlign = TextAlign.End) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = alignment,
        maxLines = 1,
    )
}

@Composable
private fun AmountCell(text: String, modifier: Modifier, color: Color) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        textAlign = TextAlign.End,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
