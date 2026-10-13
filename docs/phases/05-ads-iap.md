<!-- SPDX-License-Identifier: MIT -->

# Phase 05 — Ads + ad-free IAP (respectful)

- Status: ✅ · Date: 2026-10-13 · Branch: `arena/0c89305a-test-app`
- CI: green — [run 38396702777](https://github.com/pabi277/Test-app/actions/runs/38396702777),
  tip `f774b12` (3m25s). First try: the run resolved the three new SDK
  dependencies, compiled the whole integration and ran the full suite.
- **APK:** `app-progress-apk`, 15,863,439 bytes (compressed), uploaded by that
  run with 30-day retention. The suite now has 118 unit-test methods (100 in
  phase 4, 18 added here).
- Local `./gradlew testDebugUnitTest assembleDebug` is still impossible in this
  workspace (no JDK; Maven hosts are unreachable), so CI remains the executor of
  record and this run was the first compile of the ads/billing/UMP code.

## Acceptance

- 5.1 — AdMob behind a host-testable Policy interface; **one** opt-in rewarded
  placement that unlocks export; UMP consent; test units only.
- 5.2 — one-time ad-free IAP with restore and a local entitlement flag.
- Done means: a test proves ads are blocked when the Policy says no, and the APK
  shows the opt-in rewarded export plus the ad-free purchase entry (test IDs).

## Implemented

- `monetization/AdPolicy.kt` — the decision layer, with no SDK type in sight:
  `AdSignals` in, `AdDecision`/`AdBlockReason` out. `RewardedOnlyAdPolicy`
  allows an ad only when the user asked, consent allows it, and the user does not
  own ad-free. `ExportUnlockPolicy` turns that into the action: show the ad, or
  grant the unlock anyway (`GrantWithoutAd`) when no ad can be requested.
  `AdPolicy` is a `fun interface`, so a host (or a test) can veto ads outright.
- `monetization/AdsConfig.kt` — Google's official **test** app ID
  (`ca-app-pub-3940256099942544~3347511713`) and rewarded unit
  (`ca-app-pub-3940256099942544/5224354917`), the `paisacalc_ad_free` product ID
  and the 24-hour reward window. Live identifiers can only arrive through
  `-Ppaisacalc.admobAppId=… / -Ppaisacalc.rewardedAdUnitId=…`; the manifest takes
  the app ID as `${admobAppId}` and `AdsConfigTest` fails if a non-test unit is
  ever committed.
- `monetization/RewardedAdsClient.kt` (play-services-ads 25.5.0) and
  `monetization/ConsentManager.kt` (UMP 4.0.0) are the only classes that touch
  those SDKs. The SDK initializes on the user's tap, and the manifest removes the
  SDK's automatic startup provider, so a session that never taps the unlock never
  starts the ads SDK.
- `monetization/ExportAccessStore.kt` + `ExportAccess.kt` — the entitlement
  (`KeyValueStore` seam, `SharedPreferences` in the app) and the access policy:
  ad-free is permanent, one rewarded ad lasts 24 h, and the clock is injected.
- `monetization/AdFreeBillingClient.kt` (Play Billing 8.3.0) — one non-consumable
  product: connect on request, query the product, open the purchase sheet,
  acknowledge what Play reports as `PURCHASED`, restore from Play's owned
  purchases. `PENDING` grants nothing and is reported to the user; purchase
  tokens and order IDs are never logged.
- `monetization/MonetizationController.kt` — the glue the screens talk to:
  `StateFlow<MonetizationUiState>` plus one-shot `MonetizationEvent`s (snackbars),
  UMP consent refreshed on launch, and nothing else running before the user acts.
- UI: `ui/emi/ExportActionsCard.kt` is locked until one ad or the purchase
  unlocks it, then shows the three export actions plus what unlocked them and
  until when; `ui/monetization/AdFreeDialog.kt` carries buy / restore /
  privacy-options with Play's price (never a hard-coded one) and honest copy when
  Play billing is unavailable; `ui/monetization/MonetizationMessages.kt` maps
  events to copy; the EMI overflow menu gains "Ads & ad-free".

## Evidence for the pins and the API shapes

Google's docs, `dl.google.com` and Maven Central are unreachable from this
sandbox, so every SDK decision was taken from source that builds against the
pinned versions, read through `api.github.com` on 2026-10-13:

- `googleads/googleads-mobile-android-examples` (Google's own samples):
  `kotlin/admob/RewardedVideoExample/app/build.gradle` pins
  `play-services-ads:25.5.0` + `user-messaging-platform:4.0.0`; its
  `MainActivity.kt` is the load/show pattern used here (`RewardedAd.load` +
  `RewardedAdLoadCallback`, `fullScreenContentCallback`,
  `show(activity, OnUserEarnedRewardListener)`, `MobileAds.initialize`), and its
  manifest carries the test app ID. `kotlin/advanced/JetpackComposeDemo` +
  `GoogleMobileAdsConsentManager.kt` is the UMP pattern
  (`requestConsentInfoUpdate` on every launch, `loadAndShowConsentFormIfRequired`,
  `canRequestAds()`, `privacyOptionsRequirementStatus`, `showPrivacyOptionsForm`).
- Play Billing 8.3.0 is what `signalapp/Signal-Android` and
  `d4rken-org/sdmaid-se` ship; their billing packages supplied the 8.x call
  shapes (`enablePendingPurchases(PendingPurchasesParams)`,
  `queryProductDetailsAsync` → `QueryProductDetailsResult.productDetailsList`,
  `BillingFlowParams.ProductDetailsParams.setProductDetails`,
  `queryPurchasesAsync`, `acknowledgePurchase`, `Purchase.PurchaseState`).
- The `MobileAdsInitProvider` removal uses the exact authority
  (`${applicationId}.mobileadsinitprovider`) that other production apps remove,
  as the class itself is only visible from decompiled sources.

## What the APK shows

Open the app and press **Example**: the schedule appears as before, and the
export card is now locked — "Watch an ad to unlock" over a "Go ad-free" action,
with the copy that the EMI, the schedule and the GST tab never need an ad. The
overflow menu (⋮) has **Ads & ad-free**, which opens the purchase dialog and
connects to Play only then. Tapping the unlock loads Google's test rewarded ad
and, once it completes, the three export actions appear above "Unlocked by an ad
until <time>". In airplane mode (or with no fill) there is no ad to show, so
export unlocks anyway and the snackbar says why. With Play billing unavailable in
a sideloaded build, the dialog states that purchases work after installing from
Google Play; on a Play-installed build the entry shows the store price and the
one-time purchase removes ads for good (restore included).

## Owner actions this phase surfaced (no gate triggered)

- Create the AdMob app + the one rewarded ad unit in the owner's account; the
  live IDs are a **GATE** — they enter a build only through protected build
  configuration, never through Git.
- Create the `paisacalc_ad_free` in-app product (one-time, non-consumable) in
  Play Console; its price is chosen there and shown by Play, not by the app.
- Phase 6 must answer Play Console's "contains ads" declaration and Data Safety
  for the ads SDK (`INTERNET`, `ACCESS_NETWORK_STATE`, `AD_ID`) — recorded in
  `docs/DECISIONS.md`; the README was updated this phase so no public claim
  overstates privacy.

## Verification

- `AdPolicyTest` (8 tests) is the phase's acceptance test: ads are blocked when
  the user did not ask, when consent withholds them, and for an ad-free owner;
  the only path to an ad request is the user's tap plus consent; a consent
  refusal and a policy veto grant the unlock instead of blocking export.
- `ExportAccessStoreTest` (5) covers the 24 h window at its boundary, the
  permanent ad-free entitlement, ordering, and persistence; the fake clock means
  nothing waits.
- `SharedPreferencesKeyValueStoreTest` (2) runs the real preference-backed store
  under Robolectric; `AdsConfigTest` (3) guards the test-unit-only rule and the
  window the copy promises.
- Device QA for the ad flow itself is outstanding (it cannot be exercised by
  Robolectric): owner to sideload the artifact and confirm the test ad plays,
  the unlock lasts, the offline fallback unlocks, and the ad-free entry reads
  correctly on a Play-installed build.
