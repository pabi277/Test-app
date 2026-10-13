package com.pabicalc.paisacalc.legal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Publish-readiness rule: the app links the privacy policy only when a real
 * http(s) URL is configured, so no build ships a dead privacy link.
 */
class PrivacyPolicyTest {

    @Test
    fun anUnconfiguredUrlHidesTheInAppLink() {
        assertNull(PrivacyPolicy.usableUrl(""))
        assertNull(PrivacyPolicy.usableUrl("   "))
        assertNull(PrivacyPolicy.url)
    }

    @Test
    fun onlyHttpOrHttpsUrlsAreAccepted() {
        assertNull(PrivacyPolicy.usableUrl("ftp://example.com/privacy"))
        assertNull(PrivacyPolicy.usableUrl("example.com/privacy"))
        assertNull(PrivacyPolicy.usableUrl("javascript:alert(1)"))
        assertNull(PrivacyPolicy.usableUrl("https://"))
    }

    @Test
    fun aRealPolicyUrlIsKeptAndTrimmed() {
        assertEquals(
            "https://example.com/paisacalc/privacy",
            PrivacyPolicy.usableUrl("  https://example.com/paisacalc/privacy  "),
        )
        assertEquals(
            "http://example.com/privacy",
            PrivacyPolicy.usableUrl("http://example.com/privacy"),
        )
    }
}
