package com.pabicalc.paisacalc.monetization

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * The only class in PaisaCalc that talks to the Google Mobile Ads SDK.
 *
 * It loads and shows one rewarded ad on request and reports what happened; the
 * decision to ask it at all belongs to [RewardedOnlyAdPolicy]. Nothing is
 * preloaded at launch, so a user who never taps the export unlock never
 * triggers a single ads-SDK network call.
 */
class RewardedAdsClient(private val context: Context) {

    @Volatile
    private var initialized = false

    private var loadedAd: RewardedAd? = null

    /** Initializes the SDK once; [onReady] runs on the calling thread. */
    fun initialize(onReady: () -> Unit) {
        if (initialized) {
            onReady()
            return
        }
        MobileAds.initialize(context) { _ ->
            initialized = true
            onReady()
        }
    }

    /** Loads the rewarded ad if one is not already waiting. */
    fun load(onLoaded: () -> Unit, onFailed: (String) -> Unit) {
        if (loadedAd != null) {
            onLoaded()
            return
        }
        if (!initialized) {
            onFailed("The ads SDK is not initialized")
            return
        }
        RewardedAd.load(
            context,
            AdsConfig.rewardedAdUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    loadedAd = ad
                    onLoaded()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    loadedAd = null
                    onFailed(adError.message)
                }
            },
        )
    }

    /**
     * Shows the loaded ad. [onReward] runs only when the SDK confirms the user
     * earned the reward; [onFinished] runs once the ad is gone (shown and
     * dismissed, or failed to show).
     */
    fun show(activity: Activity, onReward: () -> Unit, onFinished: () -> Unit) {
        val ad = loadedAd
        if (ad == null) {
            onFinished()
            return
        }
        loadedAd = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                onFinished()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                onFinished()
            }
        }
        ad.show(activity) { _ -> onReward() }
    }

    /** Drops a stale ad so the next request fetches a fresh one. */
    fun clearLoadedAd() {
        loadedAd = null
    }
}
