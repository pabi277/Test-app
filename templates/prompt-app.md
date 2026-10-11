<!-- SPDX-License-Identifier: MIT -->

# [APP] — continuation prompt

> **Self-contained handoff. Paste this file as the first message of the next
> chat, then give one command** (e.g. `start phase 2` or a bug report).
> This repo is self-contained: `rule.md` holds the full rules, `docs/` the
> plan and decisions, THIS FILE the living state. AppFlow is provenance only —
> nothing here depends on it. Update this file at the end of every phase, in
> the same commit as the phase record.

## Identity (fixed)

- App: [working name] · package: [id] · license: MIT/GPL-3.0
- Official Play title: Not set (recorded in the portfolio register only when
  confirmed)
- Provenance: bootstrapped from AppFlow @ [full 40-char SHA] (reference only —
  this repo is self-contained)
- Git author: [designated address] · session branch: [arena/…] · never push
  `main`

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

- Phase [N] ✅ [one line] · CI run [id] · APK `app-progress-apk` shows [what]
- Decisions digest: [languages · monetization · time budget · license — or
  "see docs/DECISIONS.md"]
- Next: **phase [N+1] — [title]** — parts: [N+1].1 … [N+1].3
- Open risks / owner items: [ ]

## How to continue

1. Read `docs/ROADMAP.md`, `docs/DECISIONS.md`, and the latest
   `docs/phases/` record — trust the repo, not memory.
2. Implement **phase [N+1]**, all its parts, evidence-first.
3. End with: CI green + the run's downloadable `app-progress-apk` artifact ·
   `docs/phases/[NN]-<slug>.md` (say what the APK shows) · roadmap row ✅ ·
   this file updated · short report (run id, tip sha, APK, what is now
   visible) · stop at the merge gate.
