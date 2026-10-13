<!-- SPDX-License-Identifier: MIT -->

# PaisaCalc — continuation prompt

> **Self-contained handoff. Paste this file as the first message of the next
> chat, then give one command** (e.g. `start phase 2` or a bug report).
> This repo is self-contained: `rule.md` holds the full rules, `docs/` the
> plan and decisions, THIS FILE the living state. AppFlow is provenance only —
> nothing here depends on it. Update this file at the end of every phase, in
> the same commit as the phase record.

## Identity (fixed)

- App: PaisaCalc (working name) · package: `com.pabicalc.paisacalc`
  (confirmed at exit gate — see `docs/DECISIONS.md`) · license: MIT
- What: offline-first modern EMI/loan calculator for India — part-prepayments,
  ₹ lakh/crore formatting, amortization schedule, PDF/image export, offline
  GST tab. English (India) only. Rewarded-first ads (export unlock) +
  one-time ad-free IAP.
- Official Play title: Not set (recorded in the portfolio register only when
  confirmed)
- Provenance: bootstrapped from AppFlow @
  `c8856022ca57cdf23ae78b9491481543db89438d` (reference only — this repo is
  self-contained)
- Git author: [designated at exit gate] · session branch: [arena/…] · never
  push `main`

## Rules digest (full rules: `./rule.md` in THIS repo — self-contained)

- Work one **phase** per chat (a phase = 2–4 parts, one whole capability).
  No work outside the current phase's "Done means".
- Never create/switch branches, push `main`, force-push, or `reset --hard`.
- No PR, merge, release, publish, deploy, or Play submission without my
  explicit command in chat. Green CI is not authorization.
- **Ask less, gate always** (`rule.md` §3): GATE items are asked once, in one
  Summary batch. DECIDE-ONCE answers live in `docs/DECISIONS.md` and are
  **never re-asked**. Everything else is researched or decided-and-logged.
- Evidence before hypothesis (`file:line`). CI green required, with the
  downloadable `app-progress-apk` artifact uploaded. Docs ride with the code
  commit: phase record, roadmap status, `prompt.md`.

## State (update at every phase end)

- Phase 0 ✅ bootstrap (2026-10-11) · CI run 38123389452 green · APK
  `app-progress-apk` (836 KB) shows the PaisaCalc hello screen
- Decisions digest: English (India) · rewarded-first + ad-free IAP ·
  4-week budget, no waiver · MIT confirmed · author
  pabitra27.7.06@gmail.com · engine rounding policy: half-up EMI, exact
  integer monthly interest, final installment closes the loan at exactly
  zero · phase-1 UI: Compose (BOM 2025.08.01, Kotlin 2.2.0 compiler plugin)
  · caps ₹100 crore / 36% / 480 months · phase-2 prepayment policy:
  prepay after the month's EMI, capped at the balance, schedule stops the
  month the loan closes; shorten-tenure keeps the EMI / reduce-EMI recomputes
  it; interest saved = no-plan interest − plan interest · phase-3 decisions:
  Room stores loans/GST history; a private preference stores theme, with
  Android backup disabled; GST uses exact paise math and dated non-exhaustive
  convenience rates that must be verified against CBIC, CGST receives the
  half-up half and SGST the remainder, two tabs and a persistent
  system-initialized theme — full log in `docs/DECISIONS.md`
  · phase-5 ads/IAP decisions: ONE opt-in rewarded placement behind a
  host-testable `AdPolicy` (allowed only when the user asked ∧ consent
  allows ∧ not ad-free); one completed ad unlocks export for 24 h and the
  ad-free purchase unlocks it forever; when no ad can be requested or
  served (offline, no fill, consent withheld) export unlocks anyway for the
  same window — a calculation is never held behind an ad; the ads SDK
  initializes only on the user's tap (its auto-init provider is removed in
  the manifest) and Play billing connects only when the ad-free
  entry/restore is used; the entitlement is a private `SharedPreferences`
  flag granted only on `PURCHASED` (PENDING grants nothing); only Google's
  test ad IDs are committed — live IDs are a GATE through protected build
  config; `privacy_pill` copy corrected to "Works offline · your data stays
  on this device"   · phase-6 publish-readiness decisions: store/legal answers live in
  `docs/play/` (privacy policy text, Data Safety + content-rating worksheets,
  closed-test plan, listing draft, the playbook checklist copy, release-QA
  record); the in-app policy link is config-driven and hidden until
  `-Ppaisacalc.privacyPolicyUrl=…` is set, so no dead link ships; CI now
  builds and uploads the release variant as `app-release-qa-apk`
  (debug-signed, QA only — signed artifacts stay main-only) and fails the run
  on any privacy-invariant permission or on a non-test ad unit that did not
  come from protected config, reporting findings as check annotations; the
  AdMob Data Safety rows stay marked unverified until checked in Console
  against Google's SDK guidance — full log in `docs/DECISIONS.md`
- Phase 1 ✅ EMI engine + core screen (2026-10-11) · CI run 38124496188
  green (tip sha `b4f6414`) · APK `app-progress-apk` (10.1 MB) shows a
  working offline EMI calculation: amount/rate/tenure fields + sliders →
  EMI, principal-vs-interest donut, totals, and the mmm-yy amortization
  schedule — record: `docs/phases/01-emi-engine.md`
- Phase 2 ✅ part-prepayments (2026-10-11) · CI run 38125639352 green (tip
  sha `2344192`) · APK `app-progress-apk` (10.4 MB) shows the calculator with
  a Prepayments card: one tap on "Example" loads ₹10,00,000 @ 8.5% × 20y with
  ₹25,000 quarterly from installment 13, and the screen shows "You save
  ₹7,20,524 in interest", a schedule that closes in 85 installments (Prepaid
  per year block + per-month pills) and Total prepaid / Interest saved rows
  — record: `docs/phases/02-prepayments.md`
- Phase 3 ✅ GST tab + offline data layer (2026-10-11) · CI [run
  38129095902](https://github.com/pabi277/Test-app/actions/runs/38129095902)
  green at tip `af8752a` · uploaded `app-progress-apk` artifact (10,735,264
  compressed bytes) builds the saved-loan flow, GST history and add/remove
  calculator with CGST/SGST or IGST, dated presets plus custom rates and CBIC
  disclaimer, EMI/GST tabs, and persistent light/dark theme; Room close/reopen
  and GST math tests pass
  — record: `docs/phases/03-gst-tab.md`
- Phase 5 ✅ Ads + ad-free IAP (respectful) (2026-10-13) · CI [run
  38396702777](https://github.com/pabi277/Test-app/actions/runs/38396702777)
  green at tip `f774b12` (first try, 3m25s) · uploaded `app-progress-apk`
  artifact (15,863,439 compressed bytes) builds the export card locked behind
  "Watch an ad to unlock" + "Go ad-free", the "Ads & ad-free" dialog, Google's
  TEST rewarded unit (`ca-app-pub-3940256099942544/5224354917`) and the
  `paisacalc_ad_free` Play product entry; 118 unit-test methods pass, including
  the 8-test policy suite that proves ads are blocked when the Policy says no
  — record: `docs/phases/05-ads-iap.md`
- Phase 6 ✅ Publish readiness (2026-10-13) · CI [run
  38411379003](https://github.com/pabi277/Test-app/actions/runs/38411379003)
  green at tip `5f6a815` · uploaded `app-release-qa-apk` (11,421,560 bytes — the
  **release** variant, debug-signed for QA) and `app-progress-apk`
  (15,865,174 bytes) · the release APK is 12,180,110 bytes, versionCode 1,
  versionName 0.1.0, minSdk 24, targetSdk 36, and CI verified its packaged
  permission list (INTERNET, ACCESS_NETWORK_STATE, AD_ID, BILLING, ads Privacy
  Sandbox services, FOREGROUND_SERVICE, WAKE_LOCK, AndroidX receiver permission)
  and ad ids (Google test units only, plus the UMP consent SDK's all-zero
  placeholder) — no location/storage/contacts/camera/mic/phone/SMS permission ·
  121 unit tests pass · publish pack in `docs/play/`, record:
  `docs/phases/06-publish-readiness.md`
- Next: **the launch sequence** (owner-gated): host the policy and give its URL ·
  support email + developer name · 512 icon + 1024×500 feature graphic · the four
  `ANDROID_KEYSTORE_*` secrets (then the main-only signed release job builds the
  upload artifact) · AdMob rewarded unit + `paisacalc_ad_free` product and the
  live-id switch through protected config · Data Safety, content rating, target
  audience in Console · upload to the closed track, recruit 12+ testers for 14
  continuous days, then apply for production access · submit only on the owner's
  explicit command. After release: measure the `docs/RESEARCH.md` kill test four
  weeks after the public release date, and follow
  `knowledge/maintenance-cadence.md`
- Owner actions from phase 6 (all gates): decide where the privacy policy is
  hosted and provide its URL · provide the public support email and developer
  name for the policy and listing · approve the store artwork, then produce the
  512×512 icon and 1024×500 feature graphic · add the four `ANDROID_KEYSTORE_*`
  Actions secrets · create the AdMob rewarded unit and the `paisacalc_ad_free`
  product · answer Data Safety, content rating and target audience in Console ·
  run the closed test (12+ testers, 14 continuous days) · give the explicit
  merge and publish commands (`docs/play/checklist.md` orders all of this)
- Owner actions from phase 5: create the AdMob app + the one rewarded ad unit
  (live IDs are a GATE — they ship only through protected build configuration,
  never Git) · create the one-time non-consumable `paisacalc_ad_free` product in
  Play Console (Play owns the price) · sideload the phase-5 APK and confirm the
  test rewarded ad plays, the 24 h unlock holds, the offline/no-fill fallback
  unlocks and says so, and the ad-free dialog reads correctly · phase 6 answers
  the "contains ads" declaration and Data Safety for the ads SDK
- Owner action: add the four `ANDROID_KEYSTORE_*` Actions secrets — the
  main-only signed-release job fails without them (`Missing required Actions
  secret: ANDROID_KEYSTORE_BASE64`, first seen on the PR #1 merge run
  38123662318); the unit-test/`app-progress-apk` job is green regardless
- Open risks / owner items: complete the on-device ten-1-star audit before
  submission · owner verifies GST preset asset against current CBIC
  notifications (law 14; the list is not complete or item/HSN-specific) ·
  on-device airplane-mode/process-death/theme smoke test · Play account +
  closed-test (≥12 testers/14 days) readiness check

## How to continue

1. Read `docs/ROADMAP.md`, `docs/DECISIONS.md`, and the latest
   `docs/phases/` record — trust the repo, not memory.
2. Implement **phase [N]**, all its parts, evidence-first.
3. End with: CI green + the run's downloadable `app-progress-apk` artifact ·
   `docs/phases/[NN]-<slug>.md` (say what the APK shows) · roadmap row ✅ ·
   this file updated · short report (run id, tip sha, APK, what is now
   visible) · stop at the merge gate.
