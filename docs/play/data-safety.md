<!-- SPDX-License-Identifier: MIT -->

# Play Console Data Safety — answer worksheet (phase 6)

**Status: proposal, not a submitted answer.** Play's Data Safety form must match
the *shipped* build and every SDK in it. This worksheet records what the phase-5
build does so the form can be filled quickly and truthfully at submission — but
the AdMob rows below must be confirmed against Google's own Data Safety guidance
for the SDK version in use, which Play Console links from the form's SDK list.
Do not submit these rows unverified.

Build facts this worksheet is based on (evidence: `docs/phases/05-ads-iap.md`,
the phase-6 CI run's release-APK annotations, and
`app/src/main/AndroidManifest.xml`):

| Fact | Value |
| --- | --- |
| Package / version | `com.pabicalc.paisacalc` · versionCode 1 · versionName 0.1.0 |
| Ads SDK | `com.google.android.gms:play-services-ads:25.5.0` — **one** opt-in rewarded placement (`EXPORT_REWARD`), no banners/interstitials/app-open |
| Consent | `com.google.android.ump:user-messaging-platform:4.0.0`, refreshed every launch; privacy-options entry in ⋮ → *Ads & ad-free* |
| Billing | `com.android.billingclient:billing:8.3.0`, one non-consumable product `paisacalc_ad_free`; payment handled by Google Play |
| Analytics / crash SDKs | none |
| Accounts / backend | none |
| App permissions | verified from the packaged release APK (run 38410820083): `INTERNET`, `ACCESS_NETWORK_STATE`, `com.google.android.gms.permission.AD_ID`, `com.android.vending.BILLING`, `ACCESS_ADSERVICES_AD_ID`, `ACCESS_ADSERVICES_ATTRIBUTION`, `ACCESS_ADSERVICES_TOPICS`, `FOREGROUND_SERVICE`, `WAKE_LOCK`, `…DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` — and none of the location/contacts/storage/media set (`docs/play/release-qa.md`) |

## Section 1 — Data collection and security

| Play question | Answer | Why |
| --- | --- | --- |
| Does your app collect or share any of the required user data types? | **Yes** — through the Google Mobile Ads SDK, only when the user opts into the rewarded ad | The app itself stores everything locally and sends nothing; the ads SDK is the only collection path. Declaring "no" would be false because an included SDK can collect |
| Is all of the user data collected by your app encrypted in transit? | **Yes** | The ads SDK and Play Billing use HTTPS/TLS |
| Do you provide a way for users to request that their data be deleted? | **Yes** | There is no account and no server-side data of ours to delete; data is deleted by clearing app storage or uninstalling, and the policy says so and gives the contact route for ad-related requests. Review this row against Play's current definition before submitting |

## Section 2 — Data types (draft rows)

| Data type | Collected | Shared | Purpose | Optional? | Notes |
| --- | --- | --- | --- | --- | --- |
| Device or other IDs (advertising ID) | Yes | Yes | Advertising or marketing; analytics (ad measurement) | Collection happens only when the user watches the rewarded ad | AdMob row — confirm exact sub-types against Google's AdMob Data Safety guidance |
| App activity (in-app actions — the ad interaction itself) | Yes | Yes | Advertising or marketing; analytics | As above | AdMob may record ad impressions/clicks; the app itself records nothing off-device |
| Approximate location | **Verify** | **Verify** | Advertising or marketing | As above | The app requests **no location permission**; if the ads SDK derives coarse location from IP, Google's current guidance decides how this row is declared. Confirm before submitting |
| App info and performance (diagnostics/crash logs) | No | No | — | — | No crash or analytics SDK is included |
| Personal info (name, email, phone, address, financial info) | No | No | — | — | No account, no form, no server |
| Financial info (user payment info, credit score, etc.) | No | No | — | — | Purchases are processed by Google Play, which does not require the developer to declare them here; verify against the current form and the Play Billing guidance |
| Location (precise), Contacts, Messages, Photos/videos, Audio, Files/docs, Calendar, Health, Web browsing, Installed apps | No | No | — | — | The packaged release APK declares none of these permissions and none of the storage/media permissions; CI fails the build if any appears (publish-invariant guard in `.github/workflows/ci-build.yml`) |
| Ad-services permissions (`ACCESS_ADSERVICES_AD_ID`/`ATTRIBUTION`/`TOPICS`) | Declared by the ads SDK | — | Privacy Sandbox ad services | — | Manifest declarations added by Google Mobile Ads 25.5.0; they gate the SDK's use of Android's Privacy Sandbox APIs and are not permissions the user is prompted for |

## Section 3 — Other declarations that must agree

- **Contains ads:** **Yes** (one opt-in rewarded ad; "yes" is the truthful
  answer even though there are no banners or interstitials).
- **In-app purchases:** **Yes** — one one-time non-consumable product.
- **Target audience:** 18+ / adults; not designed for children; no Families
  participation.
- **Government app:** No. PaisaCalc is not affiliated with any government body,
  and the GST presets say so in the app.
- **Data Safety must equal** `docs/play/privacy-policy.md` and the app's own
  behavior. If any of the three disagree, fix all three before submitting.

## Owner verification checklist (at submission)

- [ ] Open Play Console → App content → Data Safety with this worksheet beside it.
- [ ] Use the form's SDK list to read Google's current AdMob Data Safety guidance;
      confirm or correct the Device-or-other-IDs / App-activity / approximate-location rows.
- [ ] Confirm the release APK's permission list equals `docs/play/release-qa.md`.
- [ ] Answer "contains ads" = yes and "in-app purchases" = yes.
- [ ] Re-read the policy page against these answers; fix any drift.
