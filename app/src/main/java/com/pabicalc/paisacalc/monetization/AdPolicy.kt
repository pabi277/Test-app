package com.pabicalc.paisacalc.monetization

/**
 * PaisaCalc has exactly **one** ad placement: a rewarded ad the user chooses to
 * watch so that export unlocks. There are no banners, no interstitials and no
 * app-open ads, and no ad ever stands between the user and a calculation.
 */
enum class AdPlacement { EXPORT_REWARD }

/** Why the app must not request or show an ad. */
enum class AdBlockReason {

    /** The app never requests an ad the user did not ask for. */
    NOT_REQUESTED_BY_USER,

    /** Consent (Google UMP) says ads may not be requested right now. */
    CONSENT_WITHHELD,

    /** The user bought ad-free: never request, never show. */
    AD_FREE_OWNER,

    /** The SDK could not initialize, fill or show an ad (offline, no fill, error). */
    NO_FILL,
}

/** The policy's answer: allowed, or blocked with the reason. */
sealed interface AdDecision {
    data object Allowed : AdDecision
    data class Blocked(val reason: AdBlockReason) : AdDecision
}

/**
 * Everything the ad policy needs to know, with no SDK types in sight — which is
 * what makes the policy testable on the host JVM, with no emulator and no
 * AdMob SDK on the test classpath.
 */
data class AdSignals(
    val placement: AdPlacement = AdPlacement.EXPORT_REWARD,
    val userAskedForAd: Boolean = false,
    val consentAllowsAds: Boolean = false,
    val hasAdFreeEntitlement: Boolean = false,
)

/** The seam that keeps the ad SDK out of the app's decision making. */
fun interface AdPolicy {
    fun decide(signals: AdSignals): AdDecision
}

/** Rewarded-only, opt-in-only, consent-first. */
object RewardedOnlyAdPolicy : AdPolicy {

    override fun decide(signals: AdSignals): AdDecision = when {
        !signals.userAskedForAd -> AdDecision.Blocked(AdBlockReason.NOT_REQUESTED_BY_USER)
        signals.hasAdFreeEntitlement -> AdDecision.Blocked(AdBlockReason.AD_FREE_OWNER)
        !signals.consentAllowsAds -> AdDecision.Blocked(AdBlockReason.CONSENT_WITHHELD)
        else -> AdDecision.Allowed
    }
}

/** What the app should do when the user asks to unlock export. */
sealed interface UnlockPlan {

    /** Ask the SDK for the rewarded ad: the user opted in and consent allows it. */
    data object ShowRewardedAd : UnlockPlan

    /** No ad can be requested, so unlock export without one and say so. */
    data class GrantWithoutAd(val reason: AdBlockReason) : UnlockPlan

    /** Already ad-free: nothing to unlock and nothing to watch. */
    data object AlreadyAdFree : UnlockPlan
}

/**
 * Turns a policy decision into an action for the export unlock.
 *
 * Export is never held hostage by an ad that cannot be shown: the rewarded ad
 * is the free alternative to paying, so when the SDK has nothing to offer (or
 * consent withholds ads) the export unlocks anyway for the same window. That
 * keeps "your schedule never needs a network connection" true, and it means a
 * user without internet is never locked out of their own data.
 */
object ExportUnlockPolicy {

    fun plan(signals: AdSignals, policy: AdPolicy = RewardedOnlyAdPolicy): UnlockPlan =
        when (val decision = policy.decide(signals)) {
            is AdDecision.Allowed -> UnlockPlan.ShowRewardedAd
            is AdDecision.Blocked -> when (decision.reason) {
                AdBlockReason.AD_FREE_OWNER -> UnlockPlan.AlreadyAdFree
                else -> UnlockPlan.GrantWithoutAd(decision.reason)
            }
        }
}
