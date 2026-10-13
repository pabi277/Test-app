package com.pabicalc.paisacalc.ui.monetization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pabicalc.paisacalc.R
import com.pabicalc.paisacalc.monetization.BillingPhase
import com.pabicalc.paisacalc.monetization.MonetizationUiState

/**
 * The ad-free entry point: what the app's ads are, what the one-time purchase
 * changes, and where to restore it. No price is invented here — Play supplies
 * one, and when it cannot, the dialog says so.
 */
@Composable
fun AdFreeDialog(
    state: MonetizationUiState,
    onBuy: () -> Unit,
    onRestore: () -> Unit,
    onShowPrivacyOptions: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val billingAvailable = state.billing is BillingPhase.Available
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.ads_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.ads_dialog_body),
                    style = MaterialTheme.typography.bodyMedium,
                )
                when {
                    state.adFreeOwned -> Text(
                        text = stringResource(R.string.ads_dialog_owned),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    state.billing is BillingPhase.Connecting -> Text(
                        text = stringResource(R.string.ads_dialog_connecting),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    state.billing is BillingPhase.Unavailable -> Text(
                        text = stringResource(R.string.ads_dialog_billing_unavailable),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    else -> Text(
                        text = state.adFreePriceText?.let { price ->
                            stringResource(R.string.ads_dialog_price, price)
                        } ?: stringResource(R.string.ads_dialog_price_unknown),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onRestore, enabled = !state.adFreeOwned) {
                        Text(stringResource(R.string.ads_action_restore))
                    }
                    if (state.privacyOptionsRequired) {
                        TextButton(onClick = onShowPrivacyOptions) {
                            Text(stringResource(R.string.ads_action_privacy_options))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onBuy,
                enabled = !state.adFreeOwned && billingAvailable,
            ) {
                Text(
                    text = if (state.billing is BillingPhase.Purchasing) {
                        stringResource(R.string.ads_action_purchasing)
                    } else {
                        stringResource(R.string.ads_action_buy)
                    },
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        },
    )
}
