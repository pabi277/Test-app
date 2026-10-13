package com.pabicalc.paisacalc.legal

import com.pabicalc.paisacalc.BuildConfig

/**
 * The published privacy policy, as far as the app is concerned.
 *
 * Play requires the policy URL to be linked from the app as well as the store
 * listing, but a link that goes nowhere is worse than no link: until the owner
 * hosts the policy and the build is given the URL
 * (`-Ppaisacalc.privacyPolicyUrl=…`), [url] is null and the menu entry that
 * opens it is not shown at all.
 *
 * The policy text that must be published lives in `docs/play/privacy-policy.md`.
 */
object PrivacyPolicy {

    /** The configured policy URL, or null when none is set (or it is not http/s). */
    val url: String? get() = usableUrl(BuildConfig.PRIVACY_POLICY_URL)

    /** Trims, then accepts only an http(s) URL — anything else is treated as unset. */
    fun usableUrl(raw: String): String? {
        val trimmed = raw.trim()
        val isHttp = trimmed.startsWith("https://") || trimmed.startsWith("http://")
        return trimmed.takeIf { isHttp && it.length > "https://".length }
    }
}
