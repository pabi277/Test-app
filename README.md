<!-- SPDX-License-Identifier: MIT -->

# PaisaCalc

Bootstrapped from AppFlow @ c8856022ca57cdf23ae78b9491481543db89438d on 2026-10-11

PaisaCalc is an offline-first, privacy-respecting EMI/loan calculator for
India — with part-prepayments, correct ₹ lakh/crore formatting, an offline
GST tab, and PDF/image export. Built for anyone planning or repaying a loan
who is tired of ad walls and data-hungry calculator apps.

## What it does

- Calculates EMI, total interest, and total payable for any loan (reducing
  balance), with an amortization schedule labelled mmm-yy.
- Models part-prepayments (monthly/quarterly/half-yearly/yearly/one-time)
  and shows the interest saved.
- Formats Indian currency correctly (lakh/crore grouping).
- Offline GST tab: add or extract GST with CGST/SGST or IGST split, a dated
  rate-preset asset plus custom rate, paise-precision results, and a
  verify-against-CBIC disclaimer.
- Saves reusable loans (including the prepayment settings) and recent GST
  calculations in a private on-device Room database.
- Switches between light and dark themes.
- Exports the schedule as PDF or a shareable image — unlocked by one
  optional rewarded ad or by the one-time ad-free purchase.
- Works fully offline; collects no personal data and needs no location or
  account.

## Screenshots

[Add current screenshots from the released app. Do not show features or
claims that are not present in the current build. The capture list and
rules are in `docs/play/listing-draft.md`; capture from the release
variant (`app-release-qa-apk` in CI, or the signed build after release).]

## Privacy and ads

- Privacy policy: the publish-ready text is `docs/play/privacy-policy.md`;
  it is linked in the app only once it is hosted and the build is given
  the URL (`-Ppaisacalc.privacyPolicyUrl=…`) — until then no link is shown
- Data Safety: see the Google Play listing; it reflects this exact build and
  all included SDKs. The Google Mobile Ads SDK and the Google Play Billing
  library are the only components that can reach the network.
- Ads: one opt-in rewarded ad to unlock PDF/image export. No interstitials,
  no banners, never mid-task, and no ad request at all unless you tap the
  unlock. When no ad can be served (offline, no fill), export unlocks anyway
  for the same 24-hour window — a calculation is never held behind an ad.
- Ad-free purchase: one-time purchase removing all ads, with restore. Buying
  it stops every ad request.
- Accounts and backend: none — all your loan and GST data stays on the device.

## Build and test

Requirements: JDK 17, Android SDK, and the checked-in Gradle wrapper.

```sh
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Development builds use Google's test ad units and the `paisacalc_ad_free`
product ID only; live AdMob identifiers are injected through protected build
configuration (`-Ppaisacalc.admobAppId=… -Ppaisacalc.rewardedAdUnitId=…`) and
are never committed.

CI uploads two installable artifacts per push: `app-progress-apk` (debug) and
`app-release-qa-apk` (the release variant, debug-signed — for device QA only,
never for upload). The same run inspects the packaged release APK and fails on
any privacy-invariant permission or on an ad unit that is neither Google's test
unit nor an SDK placeholder; findings appear as check annotations.

Release artifacts are built by GitHub Actions. Signing credentials are stored
in protected Actions secrets, not in this repository. See
`.github/workflows/ci-build.yml` for the actual workflow.

## Permissions and data

The calculator itself needs no network: EMI, part-prepayments, the schedule
and the GST tab all work in airplane mode. PaisaCalc requests no location,
contacts, phone, or storage permission, and no runtime permission at all.

The optional rewarded ad and the ad-free purchase are the only network paths:
the Google Mobile Ads SDK declares `INTERNET`, `ACCESS_NETWORK_STATE` and
`AD_ID`, and they are used only after you tap the export unlock or open the
ad-free entry. The ads SDK's automatic startup component is removed in the
manifest, so the SDK is initialized on your tap and not before.

PDFs can be saved through Android's Storage Access Framework document picker or
shared alongside PNG schedules from private cache URIs through the Android
sharesheet. No broad storage permission is requested. Loan and GST history is
stored locally in the app's private database; Android cloud and device-transfer
backup are disabled for the app data.

## License and credits

- **App license:** MIT.
- **Third-party code and assets:** recorded in `docs/DECISIONS.md` as added.

## Store listing

- Google Play: [YOUR_PUBLIC_PLAY_LISTING_URL — after publish]
- Support contact: [YOUR_PUBLIC_SUPPORT_EMAIL]
- Target API: 36 (re-check the Play target-API policy before every release)
- Publish-readiness pack: `docs/play/` — privacy policy text, Data Safety and
  content-rating worksheets, closed-test plan, listing draft, the release
  checklist, and the release-variant QA record. Submission stops at the owner's
  gate: nothing is uploaded to Play by CI.

Do not leave bracketed placeholders in a public release. Never add signing
keys, service-account JSON, account IDs, private tester data, or real AdMob
IDs.
