package com.pabicalc.paisacalc.monetization

/** Google Play billing's state, as the ad-free dialog needs to render it. */
sealed interface BillingPhase {

    /** Nothing has asked Play yet: the app does not connect on launch. */
    data object Idle : BillingPhase

    data object Connecting : BillingPhase

    /** Play answered and the product is known; the purchase sheet can open. */
    data object Available : BillingPhase

    /** The purchase sheet is open (or its result is being processed). */
    data object Purchasing : BillingPhase

    /**
     * Play billing cannot be used here — a sideloaded build, no Play Store, or
     * a product the account cannot buy. [reasonCode] is a
     * `BillingClient.BillingResponseCode`.
     */
    data class Unavailable(val reasonCode: Int) : BillingPhase
}

/** Everything the export card and the ad-free dialog render. */
data class MonetizationUiState(
    val access: ExportAccess = ExportAccess.Locked,
    val rewardUnlockExpiresAtMillis: Long? = null,
    val adFreeOwned: Boolean = false,
    val consentAllowsAds: Boolean = false,
    val privacyOptionsRequired: Boolean = false,
    /** True while a rewarded ad is being fetched or shown. */
    val adRequestInFlight: Boolean = false,
    val billing: BillingPhase = BillingPhase.Idle,
    /** The localized price Play reported for the ad-free product, when it did. */
    val adFreePriceText: String? = null,
)

/** One-shot results the screens turn into snackbar copy. */
enum class MonetizationEvent {
    /** The ad completed; export is unlocked for the window. */
    REWARD_UNLOCKED,

    /** No ad could be requested, filled or shown, so export was unlocked anyway. */
    UNLOCK_GRANTED_WITHOUT_AD,

    /** The user already owns ad-free. */
    ALREADY_AD_FREE,

    PURCHASE_COMPLETED,
    PURCHASE_PENDING,
    PURCHASE_CANCELLED,
    PURCHASE_FAILED,
    RESTORE_COMPLETED,
    RESTORE_NOTHING_FOUND,
    PRIVACY_CHOICES_UPDATED,
    PRIVACY_OPTIONS_FAILED,
}
