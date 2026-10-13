package com.pabicalc.paisacalc.monetization

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Phase 5 acceptance suite for the ad policy: **an ad is requested only when
 * the policy says yes**, which means the user asked, consent allows it and the
 * user does not own ad-free. Every other answer blocks the ad — and, for the
 * export unlock, falls back to granting the unlock instead of charging the user
 * for an ad that was never shown.
 */
class AdPolicyTest {

    @Test
    fun adsAreBlockedWhenTheUserDidNotAskForOne() {
        val decision = RewardedOnlyAdPolicy.decide(
            AdSignals(userAskedForAd = false, consentAllowsAds = true),
        )

        assertEquals(AdDecision.Blocked(AdBlockReason.NOT_REQUESTED_BY_USER), decision)
    }

    @Test
    fun adsAreBlockedWhenConsentWithholdsThem() {
        val decision = RewardedOnlyAdPolicy.decide(
            AdSignals(userAskedForAd = true, consentAllowsAds = false),
        )

        assertEquals(AdDecision.Blocked(AdBlockReason.CONSENT_WITHHELD), decision)
    }

    @Test
    fun adsAreBlockedForAnAdFreeOwnerEvenWhenConsentAllowsThem() {
        val decision = RewardedOnlyAdPolicy.decide(
            AdSignals(
                userAskedForAd = true,
                consentAllowsAds = true,
                hasAdFreeEntitlement = true,
            ),
        )

        assertEquals(AdDecision.Blocked(AdBlockReason.AD_FREE_OWNER), decision)
    }

    @Test
    fun adsAreAllowedOnlyForTheOneOptInRewardedPlacement() {
        val decision = RewardedOnlyAdPolicy.decide(
            AdSignals(
                placement = AdPlacement.EXPORT_REWARD,
                userAskedForAd = true,
                consentAllowsAds = true,
                hasAdFreeEntitlement = false,
            ),
        )

        assertEquals(AdDecision.Allowed, decision)
    }

    @Test
    fun theOnlyPathToAnAdRequestIsTheUsersTapPlusConsent() {
        val plan = ExportUnlockPolicy.plan(
            AdSignals(userAskedForAd = true, consentAllowsAds = true),
        )

        assertEquals(UnlockPlan.ShowRewardedAd, plan)
    }

    @Test
    fun aConsentRefusalUnlocksExportInsteadOfBlockingIt() {
        val plan = ExportUnlockPolicy.plan(
            AdSignals(userAskedForAd = true, consentAllowsAds = false),
        )

        assertEquals(UnlockPlan.GrantWithoutAd(AdBlockReason.CONSENT_WITHHELD), plan)
    }

    @Test
    fun anAdFreeOwnerIsNeverAskedToWatchAnAd() {
        val plan = ExportUnlockPolicy.plan(
            AdSignals(
                userAskedForAd = true,
                consentAllowsAds = true,
                hasAdFreeEntitlement = true,
            ),
        )

        assertEquals(UnlockPlan.AlreadyAdFree, plan)
    }

    @Test
    fun aPolicyThatSaysNoIsObeyedEvenWhenTheUserAsks() {
        // Hosts (and any future build flag) can veto ads outright; the app
        // follows the policy, not its own convenience.
        val neverAds = AdPolicy { AdDecision.Blocked(AdBlockReason.NOT_REQUESTED_BY_USER) }

        val plan = ExportUnlockPolicy.plan(
            AdSignals(userAskedForAd = true, consentAllowsAds = true),
            policy = neverAds,
        )

        assertEquals(UnlockPlan.GrantWithoutAd(AdBlockReason.NOT_REQUESTED_BY_USER), plan)
    }
}
