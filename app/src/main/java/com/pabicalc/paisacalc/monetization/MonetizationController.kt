package com.pabicalc.paisacalc.monetization

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * The one place where the export unlock, the ads SDK, UMP consent and Play
 * billing meet. Everything it decides goes through [RewardedOnlyAdPolicy] and
 * [ExportUnlockPolicy], so the rules are unit-tested without any SDK present.
 *
 * Nothing here runs until the user acts: consent information is refreshed on
 * launch (required by UMP), but the ads SDK is initialized only when the user
 * asks for the rewarded unlock, and Play billing connects only when the user
 * opens the ad-free entry or asks to restore.
 */
class MonetizationController(
    context: Context,
    private val store: ExportAccessStore = ExportAccessStore(
        SharedPreferencesKeyValueStore(
            context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE),
        ),
    ),
    private val adPolicy: AdPolicy = RewardedOnlyAdPolicy,
) {

    private val appContext = context.applicationContext

    private val consentManagerLazy = lazy { ConsentManager(appContext) }
    private val adsClientLazy = lazy { RewardedAdsClient(appContext) }
    private val billingClientLazy = lazy {
        AdFreeBillingClient(appContext, AdsConfig.AD_FREE_PRODUCT_ID, billingCallbacks)
    }

    private val consentManager: ConsentManager get() = consentManagerLazy.value
    private val adsClient: RewardedAdsClient get() = adsClientLazy.value
    private val billingClient: AdFreeBillingClient get() = billingClientLazy.value

    private val _state = MutableStateFlow(
        MonetizationUiState(
            access = store.access(),
            rewardUnlockExpiresAtMillis = store.rewardUnlockExpiresAtMillis(),
            adFreeOwned = store.entitlement() == ExportEntitlement.AD_FREE,
        ),
    )
    val state: StateFlow<MonetizationUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<MonetizationEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<MonetizationEvent> = _events.asSharedFlow()

    /** Held only between "buy" and Play's answer, then released. */
    private var purchaseActivity: Activity? = null

    private val billingCallbacks = object : AdFreeBillingClient.Callbacks {

        override fun onBillingReady(priceText: String?) {
            _state.update {
                it.copy(billing = BillingPhase.Available, adFreePriceText = priceText)
            }
            val activity = purchaseActivity ?: return
            purchaseActivity = null
            _state.update { it.copy(billing = BillingPhase.Purchasing) }
            billingClient.buy(activity)
        }

        override fun onBillingUnavailable(reasonCode: Int, message: String) {
            purchaseActivity = null
            _state.update { it.copy(billing = BillingPhase.Unavailable(reasonCode)) }
        }

        override fun onPurchaseFlowResult(responseCode: Int, purchases: List<Purchase>) {
            when (responseCode) {
                BillingClient.BillingResponseCode.OK -> applyPurchases(purchases, isRestore = false)

                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    _state.update { it.copy(billing = BillingPhase.Available) }
                    emitEvent(MonetizationEvent.PURCHASE_CANCELLED)
                }

                // Play already has the purchase: fall through to the restore path,
                // which grants it (or reports that it is still pending).
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> billingClient.restore()

                else -> {
                    _state.update { it.copy(billing = BillingPhase.Available) }
                    emitEvent(MonetizationEvent.PURCHASE_FAILED)
                }
            }
        }

        override fun onRestoreResult(purchases: List<Purchase>) {
            applyPurchases(purchases, isRestore = true)
        }
    }

    /** Called once per launch by the host activity; UMP asks for an update every time. */
    fun gatherConsent(activity: Activity) {
        consentManager.gatherConsent(activity) { refresh() }
    }

    /** Opens the ad-free entry: connects to Play only now, on the user's request. */
    fun prepareAdFreePurchase() {
        refresh()
        if (store.entitlement() == ExportEntitlement.AD_FREE) return
        if (_state.value.billing is BillingPhase.Available) return
        _state.update { it.copy(billing = BillingPhase.Connecting) }
        billingClient.connect()
    }

    /** Buy: connects first when needed, then opens the Play purchase sheet. */
    fun buyAdFree(activity: Activity) {
        if (store.entitlement() == ExportEntitlement.AD_FREE) {
            emitEvent(MonetizationEvent.ALREADY_AD_FREE)
            return
        }
        if (_state.value.billing is BillingPhase.Available) {
            _state.update { it.copy(billing = BillingPhase.Purchasing) }
            billingClient.buy(activity)
            return
        }
        purchaseActivity = activity
        _state.update { it.copy(billing = BillingPhase.Connecting) }
        billingClient.connect()
    }

    fun restoreAdFreePurchase() {
        billingClient.restore()
    }

    fun showPrivacyOptions(activity: Activity) {
        consentManager.showPrivacyOptions(activity) { updated ->
            refresh()
            emitEvent(
                if (updated) {
                    MonetizationEvent.PRIVACY_CHOICES_UPDATED
                } else {
                    MonetizationEvent.PRIVACY_OPTIONS_FAILED
                },
            )
        }
    }

    /** The user tapped "watch an ad to unlock": ask the policy, then act on its plan. */
    fun requestRewardedExportUnlock(activity: Activity) {
        val signals = AdSignals(
            placement = AdPlacement.EXPORT_REWARD,
            userAskedForAd = true,
            consentAllowsAds = consentManager.canRequestAds,
            hasAdFreeEntitlement = store.entitlement() == ExportEntitlement.AD_FREE,
        )
        when (val plan = ExportUnlockPolicy.plan(signals, adPolicy)) {
            UnlockPlan.AlreadyAdFree -> emitEvent(MonetizationEvent.ALREADY_AD_FREE)

            is UnlockPlan.GrantWithoutAd -> grantUnlockWithoutAd()

            UnlockPlan.ShowRewardedAd -> showRewardedAd(activity)
        }
    }

    /** Releases the Play billing connection when the host goes away. */
    fun shutdown() {
        if (billingClientLazy.isInitialized()) billingClient.endConnection()
    }

    private fun showRewardedAd(activity: Activity) {
        _state.update { it.copy(adRequestInFlight = true) }
        adsClient.initialize {
            adsClient.load(
                onLoaded = {
                    var rewardEarned = false
                    adsClient.show(
                        activity = activity,
                        onReward = {
                            rewardEarned = true
                            // The SDK confirmed the reward: only now does the store move.
                            store.grantRewardUnlock()
                            refresh()
                            emitEvent(MonetizationEvent.REWARD_UNLOCKED)
                        },
                        onFinished = {
                            if (rewardEarned) {
                                _state.update { it.copy(adRequestInFlight = false) }
                                refresh()
                            } else {
                                // Loaded but never shown, or dismissed with no reward.
                                grantUnlockWithoutAd()
                            }
                        },
                    )
                },
                onFailed = { grantUnlockWithoutAd() },
            )
        }
    }

    /**
     * No ad could be delivered — offline, no fill, a show failure, or consent
     * withheld. Export unlocks for the same window and the user is told; an ad
     * that does not exist must not cost the user their own schedule.
     */
    private fun grantUnlockWithoutAd() {
        store.grantRewardUnlock()
        refresh()
        emitEvent(MonetizationEvent.UNLOCK_GRANTED_WITHOUT_AD)
    }

    private fun applyPurchases(purchases: List<Purchase>, isRestore: Boolean) {
        val owned = purchases.firstOrNull { purchase ->
            purchase.products.contains(AdsConfig.AD_FREE_PRODUCT_ID) &&
                purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        }
        if (owned != null) {
            store.grantAdFreeEntitlement()
            billingClient.acknowledge(owned)
            _state.update { it.copy(billing = BillingPhase.Available) }
            refresh()
            emitEvent(
                if (isRestore) MonetizationEvent.RESTORE_COMPLETED else MonetizationEvent.PURCHASE_COMPLETED,
            )
            return
        }

        val pending = purchases.firstOrNull { purchase ->
            purchase.products.contains(AdsConfig.AD_FREE_PRODUCT_ID) &&
                purchase.purchaseState == Purchase.PurchaseState.PENDING
        }
        if (pending != null) {
            _state.update { it.copy(billing = BillingPhase.Available) }
            emitEvent(MonetizationEvent.PURCHASE_PENDING)
            return
        }

        _state.update { it.copy(billing = BillingPhase.Available) }
        if (isRestore) emitEvent(MonetizationEvent.RESTORE_NOTHING_FOUND)
    }

    private fun refresh() {
        val access = store.access()
        _state.update { current ->
            current.copy(
                access = access,
                rewardUnlockExpiresAtMillis = store.rewardUnlockExpiresAtMillis(),
                adFreeOwned = store.entitlement() == ExportEntitlement.AD_FREE,
                consentAllowsAds = consentManager.canRequestAds,
                privacyOptionsRequired = consentManager.privacyOptionsRequired,
                adRequestInFlight = if (access.isUnlocked) false else current.adRequestInFlight,
            )
        }
    }

    private fun emitEvent(event: MonetizationEvent) {
        _events.tryEmit(event)
    }

    private companion object {
        const val PREFERENCES_NAME = "paisa_calc_monetization"
    }
}
