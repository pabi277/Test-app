package com.pabicalc.paisacalc.monetization

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

/** The real `SharedPreferences` backing store, exercised on the JVM. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SharedPreferencesKeyValueStoreTest {

    private fun freshStore(): SharedPreferencesKeyValueStore {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "paisa_calc_monetization_${UUID.randomUUID()}"
        return SharedPreferencesKeyValueStore(
            context.getSharedPreferences(name, Context.MODE_PRIVATE),
        )
    }

    @Test
    fun valuesRoundTripThroughPrivatePreferences() {
        val store = freshStore()

        assertFalse(store.getBoolean("ad_free_entitlement", false))
        assertEquals(0L, store.getLong("rewarded_export_unlock_expires_at", 0L))

        store.putBoolean("ad_free_entitlement", true)
        store.putLong("rewarded_export_unlock_expires_at", 1_760_000_000_000L)

        assertTrue(store.getBoolean("ad_free_entitlement", false))
        assertEquals(1_760_000_000_000L, store.getLong("rewarded_export_unlock_expires_at", 0L))

        store.remove("ad_free_entitlement")
        assertFalse(store.getBoolean("ad_free_entitlement", false))
    }

    @Test
    fun theEntitlementStoreReadsWhatItWroteThroughPreferences() {
        val store = freshStore()
        val clock = { 1_760_000_000_000L }

        val entitlementStore = ExportAccessStore(store, clock)
        entitlementStore.grantAdFreeEntitlement()

        val reopened = ExportAccessStore(freshStore(), clock)
        assertEquals(ExportAccess.Locked, reopened.access())

        val sameFile = ExportAccessStore(store, clock)
        assertEquals(ExportEntitlement.AD_FREE, sameFile.entitlement())
        assertEquals(
            ExportAccess.Unlocked(ExportAccessSource.AD_FREE_PURCHASE),
            sameFile.access(),
        )
    }
}
