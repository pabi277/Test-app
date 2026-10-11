<!-- SPDX-License-Identifier: MIT -->

# [APP] — rules

> The operating manual for THIS repository — **self-contained**: follow it even
> if no other repo is reachable. Derived from AppFlow @ `<sha>` (provenance
> only; the text below is authoritative for this app). Owner answers and
> waivers live in `docs/DECISIONS.md`; the living state lives in `prompt.md`.

## 1. Identity

- App: [working name] · package: [id] · license: MIT/GPL-3.0
- Git author: [designated address for THIS repo] · session branch only
- Time budget: [4 weeks — or the owner-approved waiver recorded in
  `docs/DECISIONS.md`, with date]

## 2. Branching and the merge gate (law)

- Work only on the assigned session branch. Never create/switch branches,
  push `main`, force-push, or `git reset --hard`.
- Commit and push the session branch freely. Before committing, verify
  author/committer identity with `git var` and inspect the commit before
  pushing. Never use an agent's personal email/account.
- The agent never opens a PR, merges, pushes `main`, tags, releases,
  publishes, deploys, or submits to Play without my explicit command in chat.
  Green CI is evidence, not authorization. "Open a PR" does not mean "merge".
- One phase at a time; never a second PR while one is open.

## 3. Questions — ask less, gate always

**GATE — always ask; never act without an explicit yes:**
repo creation · app name + package ID (permanent once published) ·
git author identity · license if unfixed · PR · merge · `main` push ·
release · publish · deploy · Play submission · money in any form ·
legal/tax/store-account actions · tester personal data · waiving a law of
this file · changing an answer recorded in `docs/DECISIONS.md`.
→ One Summary batch per gate moment, ≤ 4 questions, each naming its gate.

**DECIDE-ONCE — ask once (normally the plan chat), record in
`docs/DECISIONS.md` with date:** languages/locales · monetization model ·
scope cuts · time budget · design direction.
→ Never re-ask a recorded answer — that is a defect, not caution. Changing
one is a GATE.

**DEFAULT — never ask:** researchable facts (research, cite source + date) ·
reversible implementation choices (log under Agent decisions) · anything
already answered in chat or docs · taste with an obvious default.

## 4. Work cycle — one phase per chat

1. Verify state first: `git status`, `git log`, PRs, CI runs. Trust the
   repository, not memory.
2. Evidence before hypothesis: `file:line` reads against the current tree.
   No fix on a guess.
3. Implement the current phase only (2–4 parts; one whole capability). Its
   "Done means" cell in `docs/ROADMAP.md` is the acceptance test. Nothing
   outside it.
4. Tests + CI green. CI is the executor of record. The run must upload the
   downloadable **`app-progress-apk`** artifact — every phase must be visible
   to me as a sideloadable build.
5. Docs ride with the code commit: `docs/phases/NN-<slug>.md` record
   (including what the APK shows), roadmap status, `docs/DECISIONS.md` if
   anything new, and `prompt.md` updated.
6. Report (run id, tip sha, APK artifact name, what is now visible) — then
   **stop at the merge gate** (§2).

## 5. Invariants — do not break (append app-specific ones as phases decide)

- Offline-first core; no backend, login, or analytics without recorded
  evidence of need.
- No secrets, real ad IDs, account IDs, or tester personal data in git or chat.
- Ads: rewarded-first, never mid-task; the ad-free purchase is always offered;
  test ad units only until the owner authorizes live units (GATE).
- Agent-invented facts are never shipped; content claims are the owner's to
  verify.
- The privacy policy and Data Safety answers stay truthful to the shipped
  build. Licenses of vendored code are recorded in `docs/DECISIONS.md`.
- Kill fast: the pre-written kill test in `docs/RESEARCH.md` decides
  ship-next vs iterate — flat retention/downloads after the first weeks means
  the next app, not endless polishing. Never rewrite the test after seeing
  results.
- [app-specific invariants appended here as decided]

## 6. Definition of done (any change)

CI green · tests · docs updated · `prompt.md` current · `app-progress-apk`
uploaded · short report — then stop. A release additionally needs every item
on `templates/play-checklist.md` (or the app's `docs/` copy) green and my
explicit command.
