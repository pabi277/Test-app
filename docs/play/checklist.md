<!-- SPDX-License-Identifier: MIT -->

# PaisaCalc — Play release checklist (the app's copy of `templates/play-checklist.md`)

Status of every item of the playbook checklist as of 2026-10-13, phase 6.
**✅ = verified in this repository/CI · 🕒 = owner action in Play Console or
another private system · ⛔ = blocked until the owner answers the gate.**
A green checklist is not permission to publish: submission needs the owner's
explicit command (publish gate), and re-checking live Console requirements on
release day is part of the process, not optional.

## Build and policy

| Item | Status | Evidence / next step |
| --- | --- | --- |
| Green CI; tests pass; release artifacts available; upload key not in Git | ✅ | [run 38411379003](https://github.com/pabi277/Test-app/actions/runs/38411379003) (phase 6, tip `5f6a815`): unit tests, debug APK, **release variant** and the invariant guard green; `app-progress-apk` (15,865,176 bytes) + `app-release-qa-apk` (11,421,552 bytes) uploaded; no keystore in the repo (`ANDROID_KEYSTORE_*` Actions secrets, main-only job) |
| Signed release AAB + APK available | 🕒 | The main-only `signed-release` job needs the four `ANDROID_KEYSTORE_*` secrets; until they exist it fails at the preflight step. The debug-signed release-variant QA build is what exists today (`docs/play/release-qa.md`) |
| Target API 36+ (policy date 2026-08-31) | ✅ + 🕒 re-check | `targetSdk = 36`, `compileSdk = 36` in `app/build.gradle.kts`; re-read the live policy in Console on upload day |
| App genuinely distinct and functional | ✅ | `docs/RESEARCH.md` niche verdict + the nine verified competitor complaint themes this build answers; no reskin, no borrowed IP |
| Content-rating questionnaire completed honestly | 🕒 | Answers drafted in `docs/play/content-rating.md`; the form is a Console action |
| Every permission and SDK reviewed; nothing unneeded | ✅ | Phase-6 CI guard fails the release build on any privacy-invariant permission and reports the packaged permission list (10 entries, all accounted for) plus every ad id; SDK inventory in `docs/play/data-safety.md` and `docs/play/release-qa.md` |

## Listing assets and text

| Item | Status | Evidence / next step |
| --- | --- | --- |
| Icon 512×512 PNG ≤1 MiB | ⛔ | Must be exported from `ic_launcher.xml` artwork — an owner-approved asset (gate) |
| Feature graphic 1024×500 | ⛔ | Owner approves artwork direction first (gate) |
| Screenshots per current spec | ⛔ | Shot-list ready in `docs/play/listing-draft.md`; capture from the release build on a phone |
| Short description ≤80 chars | ✅ | 79 characters, verified 2026-10-13 (`docs/play/listing-draft.md`) |
| Long description accurate, no ranking/testimonial/affiliation claims | ✅ | Draft describes only shipped features; disclaimers included |
| No keyword stuffing or misleading official affiliation | ✅ | GST disclaimer and "not a bank/government app" notes included |

## Privacy, safety, and ads

| Item | Status | Evidence / next step |
| --- | --- | --- |
| Working privacy-policy URL, linked in Console **and** in the app | ⛔ | Text is publish-ready in `docs/play/privacy-policy.md`; hosting is a gate. The in-app link is implemented and config-driven (`-Ppaisacalc.privacyPolicyUrl=…`, hidden when unset) |
| Data Safety truthful for the app and every SDK | 🕒 | Worksheet in `docs/play/data-safety.md`; the AdMob rows must be confirmed against Google's published SDK guidance in Console |
| AdMob/analytics/billing behavior declared; consent and privacy options tested | ✅ + 🕒 | One opt-in rewarded placement, UMP consent on every launch, privacy-options entry in ⋮ → *Ads & ad-free*; consent flow needs a Play-installed device run (owner) |
| Content rating and target audience forms match the app | 🕒 | `docs/play/content-rating.md` |

## Testing gate

| Item | Status | Evidence / next step |
| --- | --- | --- |
| Closed test satisfies the live requirement (12 testers / 14 continuous days for newer personal accounts) | 🕒 | Plan ready in `docs/play/closed-test-plan.md`; account eligibility and opt-in counts are Console facts the owner must confirm |

## Payments, tax, and support

| Item | Status | Evidence / next step |
| --- | --- | --- |
| Play payments profile and tax forms complete | 🕒 | Owner-only, in Console; nothing to do in this repo |
| Ad-free digital feature uses Play Billing (or a verified exception) | ✅ | `com.android.billingclient:billing:8.3.0`, one non-consumable product |
| Reviews monitored and answered with verified facts; no rating incentives | 🕒 | Policy recorded; the owner monitors after publish (maintenance cadence) |
| Private record of test results; no tester identities/credentials in Git | ✅ | `docs/play/closed-test-plan.md` states the rule; nothing personal is committed |

## Remaining work before submission (ordered)

1. Owner answers the phase-6 gate: `[SUPPORT_EMAIL]`, developer name, and where the
   policy will be hosted.
2. Host the policy; set `[POLICY_URL]` in Console and in the release build config.
3. Produce the 512 icon + feature graphic (owner approves artwork).
4. Add the four `ANDROID_KEYSTORE_*` secrets; run the main-only signed release job.
5. Create the AdMob rewarded unit and the `paisacalc_ad_free` product; supply the
   live ad IDs through protected configuration at release time (gate).
6. Answer Data Safety, content rating, and target audience in Console.
7. Upload the signed build to the closed track; recruit testers; run the 14 days.
8. Apply for production access, then submit the release — only on the owner's
   explicit command.
