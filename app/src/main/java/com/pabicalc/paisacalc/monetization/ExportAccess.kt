package com.pabicalc.paisacalc.monetization

/** What the user owns, stored on the device only. */
enum class ExportEntitlement { NONE, AD_FREE }

/** How export got unlocked. */
enum class ExportAccessSource { AD_FREE_PURCHASE, REWARDED_AD }

/** Whether the export actions are available right now. */
sealed interface ExportAccess {

    /** Export is available; [source] says what unlocked it. */
    data class Unlocked(val source: ExportAccessSource) : ExportAccess

    /** Export needs one rewarded ad, or the one-time ad-free purchase. */
    data object Locked : ExportAccess
}

val ExportAccess.isUnlocked: Boolean
    get() = this is ExportAccess.Unlocked

/**
 * The ad-free purchase unlocks export forever; one rewarded ad unlocks it for
 * [AdsConfig.REWARD_UNLOCK_WINDOW_MILLIS]. The clock is a parameter, not a
 * static call, so expiry is testable without waiting a day.
 */
object ExportAccessPolicy {

    fun evaluate(
        entitlement: ExportEntitlement,
        rewardUnlockExpiresAtMillis: Long?,
        nowMillis: Long,
    ): ExportAccess = when {
        entitlement == ExportEntitlement.AD_FREE ->
            ExportAccess.Unlocked(ExportAccessSource.AD_FREE_PURCHASE)

        rewardUnlockExpiresAtMillis != null && nowMillis < rewardUnlockExpiresAtMillis ->
            ExportAccess.Unlocked(ExportAccessSource.REWARDED_AD)

        else -> ExportAccess.Locked
    }
}
