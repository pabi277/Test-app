package com.pabicalc.paisacalc.monetization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The export unlock window and the ad-free entitlement, with an injected clock:
 * a 24-hour promise is tested in microseconds, and expiry is tested at the
 * boundary rather than by waiting.
 */
class ExportAccessStoreTest {

    private class FakeKeyValueStore : KeyValueStore {
        private val values = mutableMapOf<String, Any>()

        override fun getLong(key: String, default: Long): Long =
            values[key] as? Long ?: default

        override fun getBoolean(key: String, default: Boolean): Boolean =
            values[key] as? Boolean ?: default

        override fun putLong(key: String, value: Long) {
            values[key] = value
        }

        override fun putBoolean(key: String, value: Boolean) {
            values[key] = value
        }

        override fun remove(key: String) {
            values.remove(key)
        }
    }

    private var now = 1_760_000_000_000L
    private val preferences = FakeKeyValueStore()
    private val store = ExportAccessStore(preferences, clock = { now })

    @Test
    fun exportStartsLockedOnAFreshInstall() {
        assertEquals(ExportAccess.Locked, store.access())
        assertEquals(ExportEntitlement.NONE, store.entitlement())
        assertNull(store.rewardUnlockExpiresAtMillis())
    }

    @Test
    fun oneRewardedAdUnlocksExportUntilTheWindowEnds() {
        val expiresAt = store.grantRewardUnlock()

        assertEquals(now + AdsConfig.REWARD_UNLOCK_WINDOW_MILLIS, expiresAt)
        assertEquals(
            ExportAccess.Unlocked(ExportAccessSource.REWARDED_AD),
            store.access(),
        )

        now = expiresAt - 1
        assertEquals(
            ExportAccess.Unlocked(ExportAccessSource.REWARDED_AD),
            store.access(),
        )

        now = expiresAt
        assertEquals(ExportAccess.Locked, store.access())
    }

    @Test
    fun theAdFreePurchaseUnlocksExportForGood() {
        store.grantAdFreeEntitlement()

        assertEquals(
            ExportAccess.Unlocked(ExportAccessSource.AD_FREE_PURCHASE),
            store.access(),
        )

        now += 10L * 365L * 24L * 60L * 60L * 1000L
        assertEquals(
            ExportAccess.Unlocked(ExportAccessSource.AD_FREE_PURCHASE),
            store.access(),
        )
    }

    @Test
    fun adFreeOutranksAnAdWindowThatHasExpired() {
        store.grantRewardUnlock()
        store.grantAdFreeEntitlement()

        now += AdsConfig.REWARD_UNLOCK_WINDOW_MILLIS * 2

        assertEquals(
            ExportAccess.Unlocked(ExportAccessSource.AD_FREE_PURCHASE),
            store.access(),
        )
    }

    @Test
    fun theEntitlementSurvivesANewStoreOnTheSamePreferences() {
        store.grantAdFreeEntitlement()

        val reopened = ExportAccessStore(preferences, clock = { now })

        assertEquals(ExportEntitlement.AD_FREE, reopened.entitlement())
        assertEquals(
            ExportAccess.Unlocked(ExportAccessSource.AD_FREE_PURCHASE),
            reopened.access(),
        )
    }
}
