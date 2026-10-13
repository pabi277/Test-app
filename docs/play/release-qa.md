<!-- SPDX-License-Identifier: MIT -->

# Release-variant QA record (phase 6)

The phase-6 CI job builds the **release variant** — the same build type that
will be uploaded to Play — and inspects the packaged APK. This file records what
that run proved, so the owner's device pass starts from verified facts instead of
assumptions.

## CI evidence

- Run: [38411379003](https://github.com/pabi277/Test-app/actions/runs/38411379003)
  (tip `5f6a815`); the same steps were green in runs
  [38410820083](https://github.com/pabi277/Test-app/actions/runs/38410820083)
  (`417fa38`) and [38409809270](https://github.com/pabi277/Test-app/actions/runs/38409809270)
  (`15ac34a`).
- Artifacts uploaded by that run: **`app-release-qa-apk`** (11,421,552 bytes
  compressed; APK 12,180,110 bytes) and `app-progress-apk` (15,865,176 bytes).
  The QA release APK is **debug-signed**: it is for device QA only and must never
  be uploaded to Play. Upload-grade signed artifacts come from the main-only
  `signed-release` job once the four `ANDROID_KEYSTORE_*` secrets exist.

## Verified from the packaged release APK (aapt2 badging + APK scan)

| Fact | Value |
| --- | --- |
| Package / version | `com.pabicalc.paisacalc` · versionCode 1 · versionName 0.1.0 |
| minSdk / targetSdk | 24 / 36 (target meets the Play requirement recorded 2026-10-10) |
| Permissions | `INTERNET`, `ACCESS_NETWORK_STATE`, `com.google.android.gms.permission.AD_ID`, `com.android.vending.BILLING`, `ACCESS_ADSERVICES_AD_ID`, `ACCESS_ADSERVICES_ATTRIBUTION`, `ACCESS_ADSERVICES_TOPICS`, `FOREGROUND_SERVICE`, `WAKE_LOCK`, `com.pabicalc.paisacalc.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` |
| Banned permissions (location, contacts, camera, microphone, phone, SMS, storage, media, calendar, sensors, `QUERY_ALL_PACKAGES`) | **none** — CI fails the run if any appears |
| Ad units in the APK | `ca-app-pub-3940256099942544/5224354917` (Google's **test** rewarded unit) and `ca-app-pub-3940256099942544~3347511713` (Google's **test** app ID) |
| Placeholder ad id | `ca-app-pub-0000000000000000~0000000000`, shipped by `com.google.android.ump:user-messaging-platform:4.0.0` (`com/google/android/gms/internal/consent_sdk/zzco.class`, `zzp.class`). It is the consent SDK's all-zero placeholder — it cannot serve ads. CI reports it and fails only on a non-test, non-placeholder publisher id |

The permission list is exactly four capabilities: the ads SDK's network access,
its Privacy Sandbox ad-services (Topics/Attribution/Ad ID), Play Billing, and
AndroidX's internal receiver guard. Nothing reads the user's files, location,
contacts, camera, microphone, phone or messages.

## What CI cannot check (owner device pass, before submission)

Install the `app-release-qa-apk` (or the Play-installed closed-test build) on a
real phone and confirm:

- [ ] First launch on a clean install: EMI screen renders, no crash, no consent
      prompt where consent is not required (India), and the ⋮ menu shows
      *Ads & ad-free* but **no** *Privacy policy* entry while no URL is configured.
- [ ] The Example scenario still produces EMI ₹8,678 and the ₹7,20,524 interest
      saving (the phase-2 numbers) in the release build.
- [ ] Airplane mode: EMI, prepayments, schedule and GST all work; export is
      locked until the unlock is tapped, and with no network the unlock is granted
      without an ad with the "no ad was available" message.
- [ ] Rewarded flow online: tapping the unlock requests Google's **test** ad,
      completing it unlocks export for 24 h, and a second export needs no ad.
- [ ] The three export actions still produce a real PDF (SAF save + share) and PNG.
- [ ] GST tab: preset and custom rate, CGST/SGST vs IGST, and the CBIC note.
- [ ] Dark theme, rotation, process death (Kill the app, reopen: saved loan and
      history survive), and TalkBack on the main screens.
- [ ] On a **Play-installed** build (closed track): the ad-free dialog shows the
      store price, a test purchase completes, ads stop, and *Restore purchase*
      re-grants it after a reinstall. Play test purchases only — never real money
      for QA.
- [ ] Privacy policy link appears once `-Ppaisacalc.privacyPolicyUrl=…` is set,
      and opens the hosted page.

Record the outcome here (or in the phase record of the next phase) with the
device, Android version, and any failures.
