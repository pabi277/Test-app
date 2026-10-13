package com.pabicalc.paisacalc.ui.emi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pabicalc.paisacalc.R
import com.pabicalc.paisacalc.engine.LoanSummary

@Composable
fun ExportActionsCard(
    summary: LoanSummary,
    onSavePdf: () -> Unit,
    onSharePdf: () -> Unit,
    onShareImage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionCard(modifier = modifier) {
        Text(
            text = stringResource(R.string.export_card_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.export_card_subtitle, summary.installments.size),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = onSharePdf, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.export_share_pdf))
                }
                OutlinedButton(onClick = onSavePdf, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.export_save_pdf))
                }
            }
            OutlinedButton(onClick = onShareImage, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.export_share_image))
            }
        }
    }
}
