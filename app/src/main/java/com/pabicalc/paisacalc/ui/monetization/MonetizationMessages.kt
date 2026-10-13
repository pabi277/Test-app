package com.pabicalc.paisacalc.ui.monetization

import androidx.annotation.StringRes
import com.pabicalc.paisacalc.R
import com.pabicalc.paisacalc.monetization.MonetizationEvent

/** One-shot monetization results as the copy the user sees. */
@StringRes
fun MonetizationEvent.messageRes(): Int = when (this) {
    MonetizationEvent.REWARD_UNLOCKED -> R.string.ads_message_reward_unlocked
    MonetizationEvent.UNLOCK_GRANTED_WITHOUT_AD -> R.string.ads_message_unlock_without_ad
    MonetizationEvent.ALREADY_AD_FREE -> R.string.ads_message_already_ad_free
    MonetizationEvent.PURCHASE_COMPLETED -> R.string.ads_message_purchase_done
    MonetizationEvent.PURCHASE_PENDING -> R.string.ads_message_purchase_pending
    MonetizationEvent.PURCHASE_CANCELLED -> R.string.ads_message_purchase_cancelled
    MonetizationEvent.PURCHASE_FAILED -> R.string.ads_message_purchase_failed
    MonetizationEvent.RESTORE_COMPLETED -> R.string.ads_message_restore_done
    MonetizationEvent.RESTORE_NOTHING_FOUND -> R.string.ads_message_restore_empty
    MonetizationEvent.PRIVACY_CHOICES_UPDATED -> R.string.ads_message_privacy_updated
    MonetizationEvent.PRIVACY_OPTIONS_FAILED -> R.string.ads_message_privacy_failed
}
