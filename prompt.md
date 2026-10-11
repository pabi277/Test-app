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

- Phase 0 🚧 bootstrap — snapshot ✅ · plan docs ✅ · skeleton + CI pushed,
  awaiting green run (record run id here when green, then flip ROADMAP row)
- Decisions digest: English (India) · rewarded-first + ad-free IAP ·
  4-week budget, no waiver · MIT confirmed · author
  pabitra27.7.06@gmail.com — full log in `docs/DECISIONS.md`
- Next: once phase 0 CI is green → **phase 1 — EMI engine + core screen** —
  parts: 1.1 amortization engine, 1.2 Indian currency input UI, 1.3 results
  screen with donut + schedule
- Open risks / owner items: complete the on-device ten-1-star audit before
  submission · owner verifies GST slab asset against CBIC portal (law 14) ·
  Play account + closed-test (≥12 testers/14 days) readiness check

## How to continue

1. Read `docs/ROADMAP.md`, `docs/DECISIONS.md`, and the latest
   `docs/phases/` record — trust the repo, not memory.
2. Implement **phase [N]**, all its parts, evidence-first.
3. End with: CI green + the run's downloadable `app-progress-apk` artifact ·
   `docs/phases/[NN]-<slug>.md` (say what the APK shows) · roadmap row ✅ ·
   this file updated · short report (run id, tip sha, APK, what is now
   visible) · stop at the merge gate.
