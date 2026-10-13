package com.pabicalc.paisacalc.ui.emi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.pabicalc.paisacalc.R
import com.pabicalc.paisacalc.data.SavedLoanEntity
import com.pabicalc.paisacalc.engine.PrepaymentFrequency
import com.pabicalc.paisacalc.engine.PrepaymentStrategy
import com.pabicalc.paisacalc.format.IndianNumberFormat

@Composable
fun SavedLoansCard(
    loans: List<SavedLoanEntity>,
    canSave: Boolean,
    onSave: () -> Unit,
    onLoad: (SavedLoanEntity) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingDelete by remember { mutableStateOf<SavedLoanEntity?>(null) }

    SectionCard(modifier = modifier) {
        Text(
            text = stringResource(R.string.saved_loans_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.saved_loans_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = onSave,
            enabled = canSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.action_save_loan))
        }

        if (loans.isEmpty()) {
            Text(
                text = stringResource(R.string.saved_loans_empty),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            loans.forEach { loan ->
                val loadDescription = stringResource(R.string.load_saved_loan_accessibility, loan.title)
                val removeDescription = stringResource(R.string.remove_saved_loan_accessibility, loan.title)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = loan.title,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(
                                R.string.saved_loan_details,
                                IndianNumberFormat.rupees(loan.principalRupees),
                                DisplayFormatters.rateLabel(loan.rateMicroPercent),
                                DisplayFormatters.tenureLabel(loan.tenureMonths),
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(
                        onClick = { onLoad(loan) },
                        modifier = Modifier.semantics { contentDescription = loadDescription },
                    ) {
                        Text(stringResource(R.string.action_load))
                    }
                    TextButton(
                        onClick = { pendingDelete = loan },
                        modifier = Modifier.semantics { contentDescription = removeDescription },
                    ) {
                        Text(stringResource(R.string.action_remove))
                    }
                }
            }
        }
    }

    val deleteTarget = pendingDelete
    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.delete_saved_loan_title)) },
            text = { Text(stringResource(R.string.delete_saved_loan_message, deleteTarget.title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(deleteTarget.id)
                        pendingDelete = null
                    },
                ) {
                    Text(stringResource(R.string.action_remove))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

/** Maps a database row back to the editable screen state. */
data class SavedLoanInputs(
    val loan: EmiInput,
    val prepayment: PrepaymentInput,
)

fun SavedLoanEntity.toInputs(): SavedLoanInputs {
    val usesYears = tenureMonths % 12 == 0
    val frequency = PrepaymentFrequency.entries.firstOrNull { it.name == prepaymentFrequency }
        ?: PrepaymentFrequency.QUARTERLY
    val strategy = PrepaymentStrategy.entries.firstOrNull { it.name == prepaymentStrategy }
        ?: PrepaymentStrategy.REDUCE_TENURE
    return SavedLoanInputs(
        loan = EmiInput(
            amountText = principalRupees.toString(),
            rateText = rateText(rateMicroPercent),
            tenureText = if (usesYears) (tenureMonths / 12).toString() else tenureMonths.toString(),
            tenureUnit = if (usesYears) TenureUnit.YEARS else TenureUnit.MONTHS,
        ),
        prepayment = if (prepaymentAmountRupees > 0L) {
            PrepaymentInput(
                amountText = prepaymentAmountRupees.toString(),
                fromInstallmentText = prepaymentFromInstallment.toString(),
                frequency = frequency,
                strategy = strategy,
            )
        } else {
            PrepaymentInput()
        },
    )
}

private fun rateText(rateMicroPercent: Long): String {
    val whole = rateMicroPercent / 1_000_000L
    val hundredths = (rateMicroPercent % 1_000_000L) / 10_000L
    return if (hundredths == 0L) {
        whole.toString()
    } else {
        "$whole.${hundredths.toString().padStart(2, '0').trimEnd('0')}"
    }
}
