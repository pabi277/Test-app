@file:OptIn(ExperimentalMaterial3Api::class)

package com.pabicalc.paisacalc.ui.emi

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pabicalc.paisacalc.R
import com.pabicalc.paisacalc.engine.LoanMonth
import com.pabicalc.paisacalc.engine.LoanSummary
import com.pabicalc.paisacalc.engine.PrepaymentFrequency
import com.pabicalc.paisacalc.engine.PrepaymentStrategy
import com.pabicalc.paisacalc.format.IndianNumberFormat
import com.pabicalc.paisacalc.ui.IndianCurrencyVisualTransformation

/**
 * The prepayment editor: amount, how often, starting installment, and what the
 * extra money should do. It is always visible and only changes the numbers once
 * an amount is typed, so nothing is hidden behind a toggle.
 */
@Composable
fun PrepaymentCard(
    input: PrepaymentInput,
    issues: List<PrepaymentIssue>,
    startMonth: LoanMonth?,
    tenureMonths: Int?,
    onInputChange: (PrepaymentInput) -> Unit,
    modifier: Modifier = Modifier,
) {
    val issueByField = issues.associateBy { it.field }
    val amountIssue = issueByField[PrepaymentField.AMOUNT]
    val installmentIssue = issueByField[PrepaymentField.FROM_INSTALLMENT]
    val installment = input.fromInstallmentText.filter { it.isDigit() }.toIntOrNull()
    val neverApplies = amountIssue == null && installmentIssue == null &&
        installment != null && tenureMonths != null && installment > tenureMonths

    SectionCard(modifier = modifier) {
        Text(
            text = stringResource(R.string.prepayment_card_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.prepayment_card_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // ---- Amount ------------------------------------------------------
        OutlinedTextField(
            value = input.amountText,
            onValueChange = { onInputChange(input.copy(amountText = PrepaymentInputSanitizer.amount(it))) },
            label = { Text(stringResource(R.string.field_prepayment_amount_label)) },
            prefix = { Text(stringResource(R.string.currency_symbol)) },
            singleLine = true,
            isError = amountIssue != null,
            supportingText = if (amountIssue != null) {
                { Text(stringResource(amountIssue.messageRes())) }
            } else {
                null
            },
            visualTransformation = IndianCurrencyVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        // ---- How often ---------------------------------------------------
        Text(
            text = stringResource(R.string.prepayment_frequency_label),
            style = MaterialTheme.typography.labelLarge,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PrepaymentFrequency.entries.forEach { frequency ->
                FilterChip(
                    selected = input.frequency == frequency,
                    onClick = { onInputChange(input.copy(frequency = frequency)) },
                    label = { Text(frequency.label) },
                )
            }
        }

        // ---- Starting installment ---------------------------------------
        OutlinedTextField(
            value = input.fromInstallmentText,
            onValueChange = {
                onInputChange(input.copy(fromInstallmentText = PrepaymentInputSanitizer.installment(it)))
            },
            label = { Text(stringResource(R.string.field_prepayment_from_label)) },
            singleLine = true,
            isError = installmentIssue != null,
            supportingText = {
                Text(
                    text = when {
                        installmentIssue != null -> stringResource(installmentIssue.messageRes())
                        else -> fromInstallmentCaption(installment, tenureMonths, startMonth)
                            ?: stringResource(R.string.prepayment_from_hint)
                    },
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        // ---- What it should do ------------------------------------------
        Text(
            text = stringResource(R.string.prepayment_strategy_label),
            style = MaterialTheme.typography.labelLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PrepaymentStrategy.entries.forEach { strategy ->
                FilterChip(
                    selected = input.strategy == strategy,
                    onClick = { onInputChange(input.copy(strategy = strategy)) },
                    label = { Text(strategy.label) },
                )
            }
        }
        Text(
            text = stringResource(
                when (input.strategy) {
                    PrepaymentStrategy.REDUCE_TENURE -> R.string.prepayment_strategy_shorten_hint
                    PrepaymentStrategy.REDUCE_EMI -> R.string.prepayment_strategy_reduce_hint
                },
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (neverApplies) {
            Text(
                text = stringResource(
                    R.string.prepayment_never_applies,
                    requireNotNull(tenureMonths),
                    requireNotNull(installment),
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

/**
 * The interest-saved callout, shown only when money was actually prepaid.
 * Reduce-tenure ends with a shorter schedule; reduce-EMI with a lower EMI.
 */
@Composable
fun PrepaymentSavedCallout(summary: LoanSummary, modifier: Modifier = Modifier) {
    if (!summary.hasPrepayments) return

    SectionCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        borderColor = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text = if (summary.interestSaved > 0L) {
                stringResource(R.string.prepayment_saved_title, IndianNumberFormat.rupees(summary.interestSaved))
            } else {
                stringResource(R.string.prepayment_no_interest_saved)
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )

        if (summary.monthsSaved > 0) {
            Text(
                text = stringResource(
                    R.string.prepayment_saved_shorter,
                    DisplayFormatters.tenureLabel(summary.monthsSaved),
                    summary.installments.size,
                    summary.terms.tenureMonths,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        } else {
            Text(
                text = stringResource(
                    R.string.prepayment_saved_same_tenure,
                    IndianNumberFormat.rupees(summary.finalMonthlyEmi),
                    summary.endMonth.shortLabel(),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }

        Text(
            text = stringResource(
                R.string.prepayment_total_row_value,
                IndianNumberFormat.rupees(summary.totalPrepaid),
                summary.prepaymentApplications,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )

        if (summary.finalMonthlyEmi != summary.monthlyEmi) {
            Text(
                text = stringResource(R.string.prepayment_shorten_tip),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

/** "Installment 13 of 240 · Oct-27" — or null when there is nothing to describe. */
@Composable
private fun fromInstallmentCaption(
    installment: Int?,
    tenureMonths: Int?,
    startMonth: LoanMonth?,
): String? {
    if (installment == null || startMonth == null) return null
    val monthLabel = startMonth.plusMonths(installment - 1).shortLabel()
    return when (tenureMonths) {
        null, 0 -> stringResource(R.string.prepayment_from_caption_short, installment, monthLabel)
        else -> stringResource(R.string.prepayment_from_caption, installment, tenureMonths, monthLabel)
    }
}

/** Maps a prepayment issue code to the message shown under the field. */
fun PrepaymentIssue.messageRes(): Int = when (this) {
    PrepaymentIssue.AMOUNT_INVALID -> R.string.issue_prepayment_amount_invalid
    PrepaymentIssue.FROM_INSTALLMENT_INVALID -> R.string.issue_prepayment_from_invalid
}
