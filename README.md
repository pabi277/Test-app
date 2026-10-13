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
- Exports the schedule as PDF or a shareable image.
- Works fully offline; collects no personal data and needs no location or
  account.

## Screenshots

[Add current screenshots from the released app. Do not show features or
claims that are not present in the current build.]

## Privacy and ads

- Privacy policy: [YOUR_PUBLIC_PRIVACY_POLICY_URL — added at phase 6]
- Data Safety: see the Google Play listing; it reflects this exact build and
  all included SDKs (AdMob SDK declared once phase 5 lands).
- Ads: one opt-in rewarded ad to unlock PDF/image export. No interstitials,
  no banners, never mid-task.
- Ad-free purchase: one-time purchase removing all ads, with restore.
- Accounts and backend: none — all data stays on the device.

## Build and test

Requirements: JDK 17, Android SDK, and the checked-in Gradle wrapper.

```sh
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Release artifacts are built by GitHub Actions. Signing credentials are stored
in protected Actions secrets, not in this repository. See
`.github/workflows/ci-build.yml` for the actual workflow.

## Permissions and data

PaisaCalc requests no location, contacts, phone, or internet permission. PDFs
can be saved through Android's Storage Access Framework document picker or
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

Do not leave bracketed placeholders in a public release. Never add signing
keys, service-account JSON, account IDs, private tester data, or real AdMob
IDs.
