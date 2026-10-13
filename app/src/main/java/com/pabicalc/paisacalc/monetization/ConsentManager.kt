package com.pabicalc.paisacalc.monetization

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

/**
 * Google's User Messaging Platform (UMP) behind a three-call surface: refresh
 * the consent state on every launch, read whether ads may be requested, and let
 * the user change their choice later.
 *
 * The app never decides for itself that consent is unnecessary: it asks UMP,
 * and a "no" (or an unanswered request) simply means no ad is requested.
 */
class ConsentManager(context: Context) {

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    /** True once the current privacy choices allow ad requests. */
    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    /** True when the user must be offered a privacy-options entry point. */
    val privacyOptionsRequired: Boolean
        get() = consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    /**
     * Refreshes consent information and shows the consent form only when the
     * current configuration requires one. [onComplete] runs either way, so a
     * consent failure never blocks the calculator.
     */
    fun gatherConsent(activity: Activity, onComplete: () -> Unit) {
        val params = ConsentRequestParameters.Builder().build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            { UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { _ -> onComplete() } },
            { _ -> onComplete() },
        )
    }

    /** Shows the privacy-options form; [onResult] receives false on failure. */
    fun showPrivacyOptions(activity: Activity, onResult: (Boolean) -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            onResult(formError == null)
        }
    }
}
