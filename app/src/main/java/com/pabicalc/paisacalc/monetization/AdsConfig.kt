package com.pabicalc.paisacalc.monetization

import com.pabicalc.paisacalc.BuildConfig

/**
 * Identifiers and policy constants for phase 5's single rewarded placement and
 * the one-time ad-free purchase.
 *
 * The committed values are **Google's official test values** (see
 * `docs/phases/05-ads-iap.md` for the source and the date it was checked). Live
 * AdMob units are only ever injected through protected build configuration, so
 * no real ad unit, account ID or price is stored in this repository.
 */
object AdsConfig {

    /** Google's sample AdMob app ID, used by Google's own rewarded sample. */
    const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"

    /** Google's sample rewarded ad unit (always serves a test ad). */
    const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    /**
     * The Play Console in-app product behind the ad-free purchase. The owner
     * creates a one-time (non-consumable) product with exactly this ID; the
     * price itself lives only in Play Console.
     */
    const val AD_FREE_PRODUCT_ID = "paisacalc_ad_free"

    /**
     * One completed rewarded ad unlocks export for this long. Long enough that
     * a user is not asked again mid-session, short enough that the reward is
     * tied to the ad that earned it.
     */
    const val REWARD_UNLOCK_WINDOW_MILLIS: Long = 24L * 60L * 60L * 1000L

    /** The rewarded unit the ads SDK is asked for (test unless overridden). */
    val rewardedAdUnitId: String = BuildConfig.REWARDED_AD_UNIT_ID

    /**
     * True while the build uses Google's test unit. A release with live units
     * must be a deliberate owner GATE, and this flag documents which build the
     * user is holding.
     */
    val usesTestAdUnits: Boolean = BuildConfig.USING_TEST_AD_UNITS
}
