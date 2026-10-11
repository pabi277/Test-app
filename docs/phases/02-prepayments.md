<!-- SPDX-License-Identifier: MIT -->

# Phase 02 — Part-prepayments

- Status: ✅ · Date: 2026-10-11 · Branch: arena/3443f676-test-app
- CI: run [38125639352](https://github.com/pabi277/Test-app/actions/runs/38125639352)
  green (tip sha `2344192`) · fix history: run 38125376319 (1 failing
  phase-1 micro-loan assertion) → run 38125556038 (readable via check
  annotations) → green
- **APK:** artifact `app-progress-apk`, 10,365,092 bytes, from run
  38125639352 — *shows: the phase-1 calculator plus a **Prepayments** card. One
  tap on **Example** loads ₹10,00,000 at 8.5% for 20 years with ₹25,000 prepaid
  **quarterly from installment 13 (Oct-27, year 2)** — the Techinfornity
  review's exact flow — and the screen updates to a "You save ₹7,20,524 in
  interest" callout, an amortization schedule that closes in **85 installments
  instead of 240** (Principal · Interest · **Prepaid** per year block, and a
  `Prepaid ₹25,000` pill on every month the money lands), plus Total prepaid /
  Interest saved rows in the breakdown. Switching the strategy chip to "Reduce
  EMI" recomputes the EMI instead (that same scenario then saves ₹6,53,529 and
  ends with a ₹112 EMI in installment 115).*
- Commits: `3828cf6` (engine + UI + tests + reference tool), `486afaa` and
  `0485caf` (CI diagnostics), `2344192` (micro-loan assertion fix), then this
  record — all authored `pabi277 <pabitra27.7.06@gmail.com>` (repo-local git
  config, verified with `git var`).

- Final branch run:
  [38125781632](https://github.com/pabi277/Test-app/actions/runs/38125781632)
  green (tip sha `da29854`) with `app-progress-apk` 10,365,093 bytes
- Merge to `main`: owner command in chat (2026-10-11) — PR #2
  (`arena/3443f676-test-app` → `main`)

## Parts

- 2.1 — Prepayment model: monthly / quarterly / half-yearly / yearly /
  one-time, from-installment, amount, shorten-tenure vs reduce-EMI, schedule
  recomputation — ✅
- 2.2 — Prepayment editor UI + updated schedule/donut + interest-saved
  callout — ✅

## Evidence (before the change)

- Acceptance test (`docs/ROADMAP.md` phase 2, verbatim): "Tests prove schedule
  recomputation for each frequency incl. the Techinfornity quarterly bug
  scenario; APK shows a loan with a prepayment and the interest saved."
- The bug this phase answers — Techinfornity review (Dec 22, 2023, still
  unresolved Dec 2025, `docs/RESEARCH.md`): *"Loan Pre-payment (Quarterly)
  function not working, it's bug. Even I add payment amount & starting month,
  save button doesn't work… Also month wise for each year break up is missing
  interms of how much amout principle, intrest, Pre-payment."* A Jun 13, 2024
  review asks for "Monthly, Quarterly, Half Yearly, Yearly, One-time"; Fincal
  (com.gp.emicalculator) already ships multiple part payments, so this is
  table stakes, not a differentiator.
- Workspace reality: no local JDK/Android SDK (`./gradlew` impossible here);
  CI is the executor of record, and the phase ran through one red build (see
  below) before green.
- Planned semantics were first proven in `tools/emi_reference.py`: the integer
  policy agrees row-by-row with a 60-digit exact-decimal control path on all
  21 cases (10 phase-1 + 11 prepayment), and the ₹50L published example below
  matched on every metric.

## What shipped

- `engine/Prepayment.kt` — `PrepaymentFrequency` (monthly 1 / quarterly 3 /
  half-yearly 6 / yearly 12 / one-time), `PrepaymentStrategy` (shorten tenure,
  reduce EMI), `Prepayment(frequency, fromInstallment, amount, strategy)` and
  `PrepaymentPlan` (a list; the engine already supports several at once).
- `engine/EmiEngine.kt` — `calculate(terms, startMonth, plan)`; the plan is
  applied **after** that month's regular EMI, against the remaining balance,
  capped at it (a prepayment never overpays), and the schedule **stops the
  month the balance reaches zero**. Shorten-tenure keeps the EMI; reduce-EMI
  recomputes it over the months that remain. `LoanSummary` gains
  `totalPrepaid`, `prepaymentApplications`, `interestSaved` (no-plan interest
  − plan interest), `monthsSaved`, `finalMonthlyEmi`; `Installment` gains
  `prepaymentComponent`/`totalPaid`. With no plan every number is
  byte-identical to phase 1 (tested).
- `ui/emi/PrepaymentInput.kt` — the editor's text model, sanitizer and parser
  (blank amount = no prepayment; out-of-range values map to the right field).
- `ui/emi/PrepaymentCard.kt` — amount field (₹ lakh/crore live grouping),
  cadence chips, starting installment with its mmm-yy month, strategy chips
  with a one-line explanation, a "never applies" warning when the start month
  is past the loan's end, and the interest-saved callout.
- `ui/emi/ScheduleSection.kt`, `ScheduleGrouper.kt`, `ResultSection.kt` —
  per-year `Principal · Interest · Prepaid` totals, a `Prepaid ₹X` pill on the
  months the money lands, an "EMI drops to ₹X after the prepayments" line, and
  Total prepaid / Interest saved rows in the breakdown.
- `ui/emi/EmiCalculatorScreen.kt` — prepayment state survives rotation
  (`rememberSaveable` + a `listSaver` that rebuilds the enums by name), the
  debounced recalculation now takes the plan, and an **Example** action loads
  the reviewed scenario in one tap.
- `tools/emi_reference.py` — mirrors the prepayment policy, cross-checks both
  numeric paths, and emits the Kotlin golden data with `--kotlin` (the source
  of every value asserted in `PrepaymentTest.kt`).

## Tests

- 77 tests total (was 53; +24): `PrepaymentTest.kt` (13 — 11-case golden
  table across all five frequencies plus reduce-EMI, capped, zero-rate,
  car-loan and mixed-strategy edges; per-frequency cadence; the
  Techinfornity quarterly-from-month-13 scenario; invariants
  `Σ principal + Σ prepaid == P`, closing balance 0, saved = baseline − plan;
  `PrepaymentPlan.NONE` == the phase-1 schedule), `PrepaymentInputParserTest`
  (8 — including the full editor flow that recomputes 240 → 85 rows and
  ₹7,20,524 saved), `ScheduleGrouperTest` (+3 — prepaid totals per year block
  and the per-block balance-movement invariant).
- Published cross-check, asserted to the rupee (`PrepaymentTest`): ₹50,00,000
  at 8.5% × 240 with ₹5,00,000 prepaid after 36 EMIs — reduce-tenure closes in
  199 installments, interest ₹40,94,274 (published ~₹40.94 L), saved
  ₹13,19,649 (published ~₹13.20 L); reduce-EMI keeps 240 months, interest
  ₹49,66,956 (published ~₹49.67 L), saved ₹4,46,967 (published ~₹4.47 L), new
  EMI ₹38,750 (published ~₹38,750). Baseline interest ₹54,13,923 (published
  ~₹54.14 L).
- One intentional phase-1 test change: the degenerate ₹1 micro-loan now
  produces **one** row instead of two — the second used to be a phantom
  zero-payment row after the balance had already reached zero. The new
  stop-at-zero rule is what lets a prepayment close a loan early; all 10
  phase-1 golden cases are unchanged.
- CI red → green trail (this is why the phase took two pushes): the first run
  failed exactly that micro-loan assertion. Because Actions log/artifact
  downloads resolve to `blob.core.windows.net` (outside this sandbox's egress
  allowlist), the workflow now also emits the failing lines as **check-run
  annotations** (`api.github.com`, reachable) and uploads a `ci-diagnostics`
  artifact on failure — a permanent debugging aid for future phases.

## Follow-ups (not this phase)

- The editor edits one prepayment at a time; the engine model already takes a
  list, so a multi-row editor is a later, user-visible feature.
- Phase 3's Room store will persist loans — the prepayment plan becomes part
  of a saved loan.
- Consider a prepayment slider/shortcut and "prepay vs invest" copy in a later
  polish pass (roadmap phase 4).
