<!-- SPDX-License-Identifier: MIT -->

# Phase 01 — EMI engine + core screen

- Status: ✅ · Date: 2026-10-11 · Branch: arena/3443f676-test-app
- CI: run [38124496188](https://github.com/pabi277/Test-app/actions/runs/38124496188)
  green (tip sha `b4f6414`) · Provenance: AppFlow @
  `c8856022ca57cdf23ae78b9491481543db89438d` (reference only)
- **APK:** artifact `app-progress-apk` (10.1 MB — a Compose debug build) from
  run 38124496188 — *shows: the
  PaisaCalc EMI calculator. Entering an amount, rate and tenure (typing or
  dragging the sliders) produces a monthly EMI, a principal-vs-interest
  donut, total interest/payable, and the full amortization schedule with
  mmm-yy month labels and year blocks — all calculated on-device, offline.*

## Parts

- 1.1 — Pure-Kotlin reducing-balance amortization engine — ✅
- 1.2 — Indian currency formatting (lakh/crore) + amount/rate/tenure inputs
  (fields, sliders, quick chips, years/months toggle) — ✅
- 1.3 — Results screen: EMI hero, principal-vs-interest donut, year-grouped
  schedule list, empty/loading/error/ready states — ✅

## Evidence (before the change)

- Acceptance test: `docs/ROADMAP.md` phase 1 — "Unit tests cover engine
  (incl. ₹10L+/₹1Cr formatting and known-bank sample loans); APK shows a
  working offline EMI calculation end-to-end".
- Why Indian formatting is the sharpest requirement: `docs/RESEARCH.md:41`
  (Techinfornity: "Indian number format broken above ₹10 lakh"),
  `docs/RESEARCH.md:55` ("₹ formatting breaks above 10 lakh"), and
  `docs/RESEARCH.md:42` (ARBApps reviews ask for "month as mmm-yy not
  number") → schedule labels are mmm-yy.
- Workspace reality (this session): `java`/`javac` absent, no `~/.gradle`,
  no Android SDK, and raw network is blocked for `dl.google.com` /
  `repo1.maven.org` (`curl` exit 000) while page fetching works — so CI is
  the executor of record (law 11) and every dependency below was verified
  through the web before being pinned.
- Dependency verification (2026-10-11): `compose-compiler-gradle-plugin`
  2.2.0 exists on Maven Central (published 2025-06-23, gradle80–gradle88
  variants) and matches the Kotlin 2.2.0 already proven green in phase 0;
  `compose-bom/2025.08.01` POM pins Compose 1.9.0 / Material3 1.3.2 /
  ui-test-junit4 1.9.0 (BOM metadata confirms up to 2026.09.00 exists, so
  2025.08.01 is a deliberate, not forced, choice); `activity-compose` 1.10.1
  and `kotlinx-coroutines-android` 1.10.2 are published.
- Golden values were computed *before* the Kotlin was written, by an
  independent Python implementation of the same policy
  (`tools/emi_reference.py`), then verified by a second, exact-decimal
  (60-digit `Decimal`) path: both paths agree on **every row of all 10
  cases**, and the integer policy equals the decimal control path.
- Published cross-checks (web, 2026-10-11): HomeFirst 10L @ 8.5% × 240 ≈
  ₹8,678; marketfeed ₹1L @ 10% × 12 ≈ ₹8,792; Deal4Loans 5L @ 8.35% × 120 =
  ₹6,159 and 15L @ 8.6% × 240 = ₹13,112 — all four match this engine exactly.
  Ujjivan's table prints 10L @ 8.75% × 120 = 12,532 (truncated; the rounded
  value is 12,533) and 10L @ 8.75% × 180 = 9,934, which contradicts its own
  quoted rate (the formula gives ≈9,994). Recorded here so no future phase
  copies a third-party number that is internally inconsistent.
- aapt2 hazard avoided: literal `%` characters in strings.xml can be read as
  format specifiers (non-positional substitutions are a hard aapt2 error;
  escapes then leak `%%` into plain `getString`). The rate field suffix is a
  Kotlin literal and `issue_rate_invalid` says "0 and 36 percent p.a."
  instead of `%` — verified by grep that only positional specifiers remain.

## What shipped

- `engine/` — `EmiEngine` (formula + rounding policy in KDoc), `LoanTerms`
  (rate stored as millionths of a percent; validated caps: ₹100 crore,
  36% p.a., 480 months), `Installment`, `LoanSummary` (shares,
  adjusted-final-installment flag), `LoanMonth` (pure-Kotlin month math,
  `Oct-26` labels, `now()` via `java.util.Calendar` — no `java.time`, so
  minSdk 24 needs no desugaring).
- `format/` — `IndianNumberFormat` (grouping, ₹ rendering, compact
  lakh/crore labels that truncate rather than overstate, parse helpers,
  `MAX_AMOUNT` locked to the engine's cap by test) and `IndianCurrencyCaret`
  (cursor math for the live-grouped amount field).
- `ui/` — Compose: `EmiCalculatorScreen` (parse → debounce 120 ms →
  `Dispatchers.Default` compute → Ready; stale-while-revalidate so numbers
  never blink), `InputCard`, `ResultSection` (four states), `BreakdownCard`
  (animated donut with TalkBack description), `ScheduleSection` (year blocks
  with per-block totals), theme, visual transformation.
- Rounding policy (also in `docs/DECISIONS.md`): EMI = half-up round of the
  published formula; monthly interest = half-up round of
  `balance × rateMicroPercent / 1_200_000_000` in exact `Long` arithmetic;
  months `1..n-1` pay `EMI − interest`; the final installment clears the
  remaining balance so the loan closes at exactly zero; totals are the sums
  of the schedule columns, so donut, totals and table can never disagree.
- Build: Compose via BOM 2025.08.01 + `org.jetbrains.kotlin.plugin.compose`
  2.2.0, `activity-compose` 1.10.1, `kotlinx-coroutines-android` 1.10.2.
  Phase-0 hello screen, `activity_main.xml` and `SmokeTest` removed.
- Git author aligned to the recorded DECIDE-ONCE answer
  (`docs/DECISIONS.md`: `pabitra27.7.06@gmail.com`); phase 0's commits used
  a different address.

## Tests

- `EmiEngineTest` — 10 golden cases (amounts from ₹1L to ₹1 crore, 0-rate,
  odd tenure, 37-month and 480-boundary shapes) each asserting EMI, total
  interest, total payable, first and final installment rows, plus
  invariants for every case: 240/360 rows present, principal column sums to
  the principal, interest column sums to the total, closing balance is zero,
  no negative component, `paid = principal + interest`, months advance one
  per row and labels are `Oct-26 … Sep-46`. Also: half-up rounding of EMI and
  of monthly interest (ties up), exact-decimal rate parsing, zero-rate
  behaviour, adjusted-final-installment flag, micro-loan (₹1 @ 36%) safety,
  `LoanTerms` validation, `emiExact` against the printed reference values.
- `IndianNumberFormatTest` — grouping from ₹999 to ₹1,00,00,00,000, negative
  numbers, ₹ rendering, compact lakh/crore (incl. `₹9.99 L` never shown as
  `₹10 L`), parse round-trips, cap crossover with `LoanTerms`.
- `IndianCurrencyCaretTest` — round-trip and monotonicity for every offset of
  every length 1–12 (this is what keeps the caret still while typing).
- `EmiInputParserTest` — incomplete vs invalid vs valid, unit conversion,
  boundary values (₹1 and ₹100 crore; 0% and 36%; 480 months), multi-issue
  reporting, state mapping, sanitizer behaviour.
- `DisplayFormattersTest`, `ScheduleGrouperTest` — labels, percentages, 20
  year blocks vs a short final block, block totals summing to the summary.
- Reference run (this workspace, 2026-10-11):
  `python3 tools/emi_reference.py` → `integer policy == exact-decimal
  reference on every row: True`; published deltas `+0, +0, +1, +0, +0`
  (the +1 is Ujjivan's truncation, documented above).
- CI run 38124496188 (head sha `b4f64144337101488ee287b26242e781910807b3`):
  every step green, including `testDebugUnitTest assembleDebug` and the
  `app-progress-apk` upload (10,332,452 bytes, expired=false, 30-day
  retention). The workflow's own log-download endpoint is outside this
  workspace's egress allowlist, so the step conclusions plus the uploaded
  artifact are the evidence recorded here.

## Follow-ups

- Owner items unchanged: on-device ten-1-star audit, CBIC slab verification,
  Play account + closed-test readiness.
- Deliberate scope notes for later phases: dark/light theme is roadmap 3.3
  (this phase ships the single light scheme — its colour tokens are the
  extension point); saved loans and tab navigation are phase 3; export
  (phase 4) will reuse `EmiEngine` and `LoanMonth`/`IndianNumberFormat`.
- If the owner wants the ₹100-crore cap, the 36% rate cap or the final-
  installment policy changed, each is a one-line change plus golden-data
  regeneration (`tools/emi_reference.py --kotlin`).
