package com.pabicalc.paisacalc.monetization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the phase-5 rule that only Google's test ad units are committed. A
 * live unit has to arrive through protected build configuration, and this test
 * fails the moment one is typed into the source instead.
 */
class AdsConfigTest {

    private companion object {
        /** Google's test publisher ID: the one AdMob test IDs all share. */
        const val GOOGLE_TEST_PUBLISHER_ID = "ca-app-pub-3940256099942544"
    }

    @Test
    fun theCommittedBuildUsesTestAdUnits() {
        assertTrue(AdsConfig.usesTestAdUnits)
        assertEquals(AdsConfig.TEST_REWARDED_AD_UNIT_ID, AdsConfig.rewardedAdUnitId)
    }

    @Test
    fun everyCommittedAdIdentifierBelongsToGooglesTestPublisher() {
        assertTrue(AdsConfig.TEST_APP_ID.startsWith("$GOOGLE_TEST_PUBLISHER_ID~"))
        assertTrue(AdsConfig.rewardedAdUnitId.startsWith("$GOOGLE_TEST_PUBLISHER_ID/"))
    }

    @Test
    fun theRewardWindowMatchesTheCopyTheUserReads() {
        assertEquals(24L * 60L * 60L * 1000L, AdsConfig.REWARD_UNLOCK_WINDOW_MILLIS)
        assertEquals("paisacalc_ad_free", AdsConfig.AD_FREE_PRODUCT_ID)
    }
}
