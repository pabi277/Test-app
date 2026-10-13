package com.pabicalc.paisacalc.ui.emi

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pabicalc.paisacalc.R
import com.pabicalc.paisacalc.engine.LoanSummary
import com.pabicalc.paisacalc.monetization.ExportAccess
import com.pabicalc.paisacalc.monetization.ExportAccessSource
import java.util.Date

/**
 * Export, with phase 5's single rewarded placement in front of it.
 *
 * Locked: one opt-in rewarded ad (or the one-time ad-free purchase) unlocks the
 * three export actions. Unlocked: the actions, plus a line saying what unlocked
 * them and until when. The calculation above this card never depends on any of it.
 */
@Composable
fun ExportActionsCard(
    summary: LoanSummary,
    access: ExportAccess,
    adRequestInFlight: Boolean,
    rewardUnlockExpiresAtMillis: Long?,
    onSavePdf: () -> Unit,
    onSharePdf: () -> Unit,
    onShareImage: () -> Unit,
    onWatchRewardedAd: () -> Unit,
    onGoAdFree: () -> Unit,
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

        when (access) {
            ExportAccess.Locked -> ExportLockedActions(
                adRequestInFlight = adRequestInFlight,
                onWatchRewardedAd = onWatchRewardedAd,
                onGoAdFree = onGoAdFree,
            )

            is ExportAccess.Unlocked -> {
                ExportActions(
                    onSavePdf = onSavePdf,
                    onSharePdf = onSharePdf,
                    onShareImage = onShareImage,
                )
                ExportUnlockStatus(
                    access = access,
                    rewardUnlockExpiresAtMillis = rewardUnlockExpiresAtMillis,
                    onGoAdFree = onGoAdFree,
                )
            }
        }
    }
}

@Composable
private fun ExportActions(
    onSavePdf: () -> Unit,
    onSharePdf: () -> Unit,
    onShareImage: () -> Unit,
) {
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

@Composable
private fun ExportLockedActions(
    adRequestInFlight: Boolean,
    onWatchRewardedAd: () -> Unit,
    onGoAdFree: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.export_locked_body),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = onWatchRewardedAd,
            enabled = !adRequestInFlight,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(
                    if (adRequestInFlight) {
                        R.string.export_unlock_loading
                    } else {
                        R.string.export_unlock_watch
                    },
                ),
            )
        }
        TextButton(onClick = onGoAdFree, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.export_unlock_buy))
        }
    }
}

@Composable
private fun ExportUnlockStatus(
    access: ExportAccess.Unlocked,
    rewardUnlockExpiresAtMillis: Long?,
    onGoAdFree: () -> Unit,
) {
    val context = LocalContext.current
    when (access.source) {
        ExportAccessSource.AD_FREE_PURCHASE -> Text(
            text = stringResource(R.string.export_unlocked_owned),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )

        ExportAccessSource.REWARDED_AD -> {
            val unlockUntil = remember(rewardUnlockExpiresAtMillis) {
                rewardUnlockExpiresAtMillis?.let { expiresAt ->
                    DateFormat.getTimeFormat(context).format(Date(expiresAt))
                }.orEmpty()
            }
            Text(
                text = stringResource(R.string.export_unlocked_reward, unlockUntil),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onGoAdFree) {
                Text(stringResource(R.string.export_unlock_buy))
            }
        }
    }
}
