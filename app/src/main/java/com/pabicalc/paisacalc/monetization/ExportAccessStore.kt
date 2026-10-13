package com.pabicalc.paisacalc.monetization

import android.content.SharedPreferences

/**
 * The tiny persistence seam the entitlement store needs — three value types and
 * a remove. Tests fake it in memory; the app backs it with a private
 * `SharedPreferences` file (no account, no cloud, never backed up).
 */
interface KeyValueStore {
    fun getLong(key: String, default: Long): Long
    fun getBoolean(key: String, default: Boolean): Boolean
    fun putLong(key: String, value: Long)
    fun putBoolean(key: String, value: Boolean)
    fun remove(key: String)
}

/** [KeyValueStore] over a private `SharedPreferences` file. */
class SharedPreferencesKeyValueStore(
    private val preferences: SharedPreferences,
) : KeyValueStore {

    override fun getLong(key: String, default: Long): Long = preferences.getLong(key, default)

    override fun getBoolean(key: String, default: Boolean): Boolean =
        preferences.getBoolean(key, default)

    override fun putLong(key: String, value: Long) {
        preferences.edit().putLong(key, value).apply()
    }

    override fun putBoolean(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }

    override fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }
}

/**
 * Reads and writes the export entitlement and the rewarded unlock window.
 *
 * Writes happen in exactly two places — after the ad SDK confirms the user
 * earned the reward, and after Play reports an owned purchase — so a failed or
 * pending purchase can never grant anything.
 */
class ExportAccessStore(
    private val store: KeyValueStore,
    private val clock: () -> Long = System::currentTimeMillis,
) {

    fun entitlement(): ExportEntitlement =
        if (store.getBoolean(KEY_AD_FREE_ENTITLEMENT, false)) {
            ExportEntitlement.AD_FREE
        } else {
            ExportEntitlement.NONE
        }

    fun rewardUnlockExpiresAtMillis(): Long? =
        store.getLong(KEY_REWARD_UNLOCK_EXPIRES_AT, 0L).takeIf { it > 0L }

    fun access(): ExportAccess = ExportAccessPolicy.evaluate(
        entitlement = entitlement(),
        rewardUnlockExpiresAtMillis = rewardUnlockExpiresAtMillis(),
        nowMillis = clock(),
    )

    /** Called only after the SDK confirms the reward was earned (or was unavailable). */
    fun grantRewardUnlock(): Long {
        val expiresAt = clock() + AdsConfig.REWARD_UNLOCK_WINDOW_MILLIS
        store.putLong(KEY_REWARD_UNLOCK_EXPIRES_AT, expiresAt)
        return expiresAt
    }

    /** Called only for a purchase Play reports as PURCHASED. */
    fun grantAdFreeEntitlement() {
        store.putBoolean(KEY_AD_FREE_ENTITLEMENT, true)
    }

    private companion object {
        const val KEY_AD_FREE_ENTITLEMENT = "ad_free_entitlement"
        const val KEY_REWARD_UNLOCK_EXPIRES_AT = "rewarded_export_unlock_expires_at"
    }
}
