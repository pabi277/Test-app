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
