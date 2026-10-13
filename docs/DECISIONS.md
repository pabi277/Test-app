# DECISIONS.md — PaisaCalc (working name)

The anti-re-ask register. Re-asking anything recorded here is a defect;
changing one is a GATE. All dates are 2026-10-11 (plan chat) unless noted.

## Owner decisions (DECIDE-ONCE, recorded when given)

| Date | Decision | Answer |
|---|---|---|
| 2026-10-11 | Trial-app context | "a very modern calculator" — treated as the seed; selection still made from Play evidence |
| 2026-10-11 | Preferred area | Roam free (evidence picks) |
| 2026-10-11 | Languages/locales | English (India) only |
| 2026-10-11 | Time budget | 4 weeks, idea → published or kill (NO law-3 waiver) |
| 2026-10-11 | Direction pick | Option A refined: modern EMI/loan calculator core + offline GST tab |
| 2026-10-11 | Monetization | Rewarded-first (opt-in rewarded ad unlocks export) + one-time ad-free IAP opt-out; no interstitials/banners/subscription |
| 2026-10-11 | Scope cuts | Recommended scope: EMI + part-prepayments + amortization + ₹ Indian formatting + PDF/image export + GST tab. Cut from v1: FD/RD/SIP, loan comparison, EMI reminders, multi-disbursement/variable-rate loans |

## Law waivers

None. (Law 3 four-week budget stands.)

## Agent decisions (reversible on objection)

| Date | Decision | Rationale |
|---|---|---|
| 2026-10-11 | Working name "PaisaCalc", proposed package `com.pabicalc.paisacalc` | Placeholder for the exit gate; owner may rename — package ID is permanent once published |
| 2026-10-11 | Playbook pinned at `c8856022ca57cdf23ae78b9491481543db89438d` (AppFlow main HEAD on 2026-10-11) | `<SHA>` placeholder in the launcher was unfilled; pinned to current main |
| 2026-10-11 | minSdk 24, targetSdk 36 | Current Play requirement (RULES.md checkpoint 2026-10-10) + sane floor covering ~99% of active IN devices |
| 2026-10-11 | GST slabs shipped as a versioned, dated asset with user-editable custom rate | Avoids backend (law 13) and policy-drift risk; owner verifies slabs (law 14) |
| 2026-10-11 | One refinement question asked after Batch A ("refine" was selected without details) | Needed to write the plan; it was an amendment clarification, not a new batch |
| 2026-10-11 | Phase 1 engine rounding policy: EMI = half-up round of the published formula; monthly interest = half-up round of `balance × rate / 12` in exact `Long` arithmetic (rate stored in millionths of a percent); months 1..n-1 pay `EMI − interest`; the final installment clears the remaining balance; totals are the sums of the printed columns | Removes floating-point drift from the schedule, so donut, totals and table can never disagree; verified row-by-row against an independent exact-decimal implementation (`tools/emi_reference.py`) and 4/5 published bank examples |
| 2026-10-11 | Phase 1 UI stack: Jetpack Compose (BOM 2025.08.01 → Compose 1.9.0 / Material3 1.3.2) with the Kotlin 2.2.0 compose compiler plugin; inputs kept in `rememberSaveable` (no ViewModel yet) | Smallest dependency set that stays on the Kotlin 2.2.0 proven green in phase 0; ViewModel/Room belong to phase 3's data layer; reversible |
| 2026-10-11 | Input caps: ₹100 crore amount, 36% p.a. rate, 480 months tenure | Comfortably above any retail Indian loan and keeps the schedule bounded; the error copy names each cap |
| 2026-10-11 | Schedule starts at the current month via `java.util.Calendar` (not `java.time`) and labels months `mmm-yy` | `java.time` needs API 26 or desugaring at minSdk 24; mmm-yy is the verified review request (`docs/RESEARCH.md:42`) |
| 2026-10-11 | `tools/emi_reference.py` is committed (independent reference + golden-data generator); its `emi_golden.json` output is gitignored | Anyone can regenerate and re-verify the numbers the unit tests assert; the tool is not shipped in the APK |
| 2026-10-11 | Git author identity set to the recorded exit-gate answer `pabitra27.7.06@gmail.com` (phase-0 commits had used a different address) | Brings commits in line with the recorded GATE decision; no re-ask for a recorded answer |
| 2026-10-11 | Phase 2 prepayment policy: a prepayment applies **after** that month's regular EMI, on the balance the EMI leaves behind, capped at it (never overpays); the schedule **stops the month the balance reaches zero**; one-time fires once, recurring frequencies fire every 1/3/6/12 installments from the chosen start installment; shorten-tenure keeps the EMI, reduce-EMI recomputes it over the months that remain; `interestSaved` = no-plan total interest − plan total interest | Matches the lender flow (prepay after the EMI) and keeps every phase-1 invariant (`Σ principal + Σ prepaid == P`, closing balance 0); proven row-by-row against the independent exact-decimal path on all 21 cases and against a published ₹50L worked example |
| 2026-10-11 | The schedule's stop-at-zero rule also removed a phantom zero-payment row in the degenerate ₹1 micro-loan case (one row now instead of two); that single phase-1 assertion was updated | Consistency: the rule has to hold with *and* without a prepayment, and a zero-payment row after the loan is closed is wrong on a statement; the 10 phase-1 golden cases are unchanged |

## Exit-gate answers (asked exactly once, answered 2026-10-11)

| Date | Gate item | Answer |
|---|---|---|
| 2026-10-11 | Plan | Approved as written |
| 2026-10-11 | Repo creation | Authorized — public, MIT |
| 2026-10-11 | Git author identity | `pabitra27.7.06@gmail.com` designated for the app repo |
| 2026-10-11 | Working name + package ID | PaisaCalc · `com.pabicalc.paisacalc` (package ID now locked — permanent once published) |

## Agent decisions (post-gate, reversible on objection)

| Date | Decision | Rationale |
|---|---|---|
| 2026-10-11 | App built inside the existing Test-app workspace on session branch `arena/666c81b5-test-app` | Arena session is fixed to this branch; creating a separate GitHub repo is a later GATE action when the owner requests it. Provenance + all plan rules preserved |
| 2026-10-11 | Phase-0 CI: replaced `android-actions/setup-android@v3` with a plain SDK-license pre-acceptance step (`.github/workflows/ci-build.yml`); pinned template snapshot left unchanged | Third-party action failed at runner setup (run 38123310494); runner image + AGP auto-install make it unnecessary. Deviation from template recorded, reversible |
| 2026-10-11 | Hello screen uses a framework-theme plain Activity, zero dependencies | Smallest possible bootstrap; UI stack for phase 1 stays a reversible choice |
| 2026-10-11 | Phase 2: the prepayment editor edits **one** prepayment at a time (the engine model already accepts a list, `PrepaymentPlan`) | Exactly the acceptance flow ("add payment amount & starting month") without inventing a multi-row editor the roadmap did not ask for; a list editor is a later, visible feature |
| 2026-10-11 | Phase 2: an **Example** action in the top bar loads ₹10,00,000 @ 8.5% × 20y with ₹25,000 quarterly from installment 13 | Makes the acceptance ("APK shows a loan with a prepayment and the interest saved") visible in one tap for the owner and any reviewer; doubles as a preset; reversible |
| 2026-10-11 | CI now keeps a `build-log.txt`, uploads a `ci-diagnostics` artifact on failure, and emits the failing lines as **check-run annotations** | Actions log and artifact downloads resolve to `blob.core.windows.net`, which this sandbox cannot reach; annotations come from `api.github.com` and are readable, so a red run is diagnosable from here |
| 2026-10-11 | Phase 3 persistence uses Room 2.7.2 with saved loan inputs (including the one editable prepayment) and GST history; history displays the latest 50 rows | A local Room database supports offline reads/writes and survives process death. Android cloud and device-transfer backup are explicitly disabled/excluded so stored calculator inputs remain app-local |
| 2026-10-11 | GST arithmetic stores values in paise and rates as millionths of a percent; tax is rounded half-up to paise; for intra-state supplies CGST gets the half-up half and SGST gets the remainder; reverse calculation derives taxable value from inclusive amount and rate | Exact integer/`BigInteger` math avoids floating-point drift, retains paise precision, and guarantees the displayed split adds back to the displayed tax |
| 2026-10-11 | GST preset asset `2025-09-22-v1` is a dated convenience list based on CBIC Notifications 09/2025 and 10/2025-Central Tax (Rate), plus the 56th GST Council implementation note; it includes nil, 0.25%, 1.5%, 3%, 5%, 18%, specified/transition 28%, and 40% presets, plus custom rate input | Presets are not a complete or item/HSN-specific legal schedule; the screen says to verify each supply and current amendments against CBIC. The owner verification item remains open under law 14 |
| 2026-10-11 | Phase 3 UI adds two bottom tabs and a local light/dark toggle that initially follows system theme; theme preference is in private SharedPreferences | Keeps screen state while switching tabs and does not add accounts, network access, or new permissions |
| 2026-10-11 | Added Room 2.7.2 (Apache-2.0), AndroidX Test Core 1.6.1 (Apache-2.0), and Robolectric 4.14.1 (MIT) for persistence coverage in `testDebugUnitTest` | The database test closes and reopens a file-backed Room database under Robolectric, verifying saved loan and GST history rows remain present |
| 2026-10-11 | Added AndroidX Core 1.16.0 (Apache-2.0) for the AndroidX `FileProvider` used to share cache-backed PDF/PNG exports | Grants receiving apps read access only to the selected export URI and avoids broad storage access |
| 2026-10-11 | Phase 4 exports are rendered from the displayed `LoanSummary`; PDFs can be saved through `CreateDocument` or shared through a private-cache `FileProvider` URI, and PNGs are shared through the same provider. The provider exposes only `cache/exports/`; no broad storage permission is declared | Supports both system-mediated PDF saving and the roadmap's sharesheet acceptance while keeping exported files app-private until the user chooses a destination |
| 2026-10-11 | Wide calculator content is capped at 720 dp; the EMI schedule switches to compact rows below 400 dp | Prevents stretched tablet layouts and reduces column crowding on narrow phones without changing calculation behavior |
| 2026-10-13 | Owner confirmed Phase 4 manual device QA passed, including prepayment PDF sharing, SAF PDF saving, PNG sharing, TalkBack, light/dark contrast, and responsive layout checks | Records the owner's acceptance of on-device behavior that Robolectric cannot validate for Android's native PDF backend and closes Phase 4 |
| 2026-10-13 | Phase 5 SDK pins: `com.google.android.gms:play-services-ads:25.5.0` (Apache-2.0), `com.google.android.ump:user-messaging-platform:4.0.0` (Google UMP), `com.android.billingclient:billing:8.3.0` (Play Billing, Apache-2.0) | Ads and UMP versions are the pair Google's own `googleads-mobile-android-examples` (RewardedVideoExample + JetpackComposeDemo) pinned when read on 2026-10-13, so the load/show and consent calls in this repo are copied from sources that build against exactly these versions; Billing 8.3.0 is the version in two actively maintained shipped apps (Signal-Android, SD Maid SE) on the same date. Google's docs and Maven metadata are unreachable from this sandbox, so the pins were verified through `api.github.com` instead — evidence recorded in `docs/phases/05-ads-iap.md` |
| 2026-10-13 | Only Google's official TEST ad identifiers are committed (app ID `ca-app-pub-3940256099942544~3347511713`, rewarded unit `ca-app-pub-3940256099942544/5224354917`); live IDs enter a release only through protected build configuration (`-Ppaisacalc.admobAppId=… -Ppaisacalc.rewardedAdUnitId=…`, supplied from Actions secrets) | Keeps real AdMob account data out of Git and chat (law 5) while the committed build still serves real test ads; `AdsConfigTest` fails the build if a non-test unit is typed into the source. Switching to live units is an owner GATE |
| 2026-10-13 | Phase 5 export unlock policy: one completed rewarded ad unlocks export for 24 h; the ad-free purchase unlocks it permanently. When no ad can be requested or served — consent withheld, offline, no fill, show failure — export unlocks for the same window anyway, and the app says why | The rewarded ad is the free alternative to paying, not a toll gate: the app must never withhold a schedule the user already calculated because an ad did not exist. Keeps the offline-first invariant true in airplane mode and makes a consent refusal cost the user nothing |
| 2026-10-13 | The ads SDK's automatic startup component (`com.google.android.gms.ads.MobileAdsInitProvider`) is removed in the merged manifest, so `MobileAds.initialize` runs only on the user's tap; Play billing connects only when the ad-free entry or restore is used; UMP consent information is refreshed on every launch | Removes SDK work from the app's cold start, keeps the "nothing monetization-related happens before the user asks" claim true, and keeps the ads SDK out of the JVM unit-test runtime |
| 2026-10-13 | The ad-free entitlement is a boolean in a private `SharedPreferences` file (`paisa_calc_monetization`), granted only for a purchase Play reports as `PURCHASED` (and then acknowledged); `PENDING` is reported to the user and grants nothing, and a restore only reads Play's owned purchases | One-time non-consumable purchase with a stable local flag that survives process death; acknowledging keeps Play from refunding the purchase, and never granting on `PENDING` avoids the classic entitlement bug. Purchase tokens and order IDs are never logged |
| 2026-10-13 | The `privacy_pill` copy changed from "No internet needed · no data collected" to "Works offline · your data stays on this device" | With an ads SDK in the build, the old blanket claim is no longer accurate for a user who opts into an ad; the new copy stays true in both cases — the calculators do work offline and loan/GST data still never leaves the device. Reversible on objection |
| 2026-10-13 | The ad-free entry lives where ads can appear: the EMI screen's overflow menu and the locked export card; the GST tab has none | Ads only ever appear in the export unlock, so the ad-free offer belongs next to it rather than as a new app-wide surface. A reversible placement choice; a settings entry point is a later feature |
