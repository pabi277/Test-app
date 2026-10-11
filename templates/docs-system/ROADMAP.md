<!-- SPDX-License-Identifier: MIT -->

# Roadmap — [APP]

Bootstrapped from AppFlow @ <sha> on <date>

> One phase = one chat (`rule.md` §4). A phase is a decent chunk: one whole
> capability, 2–4 parts listed in its `docs/phases/NN-<slug>.md` record.
> v1 should be ~5–8 phases — if this table passes ~10 rows, phases are too
> small; merge them. Status: 📋 planned · 🚧 in progress · ✅ done · ❌
> cancelled (keep the row). "Done means" always includes a green CI run whose
> **`app-progress-apk` artifact installs and shows the phase's increment**.

| # | Phase (2–4 parts) | Done means | Status |
|---|---|---|---|
| 0 | Bootstrap + hello (P2/P3) | snapshot + plan docs + LICENSE + README + CI green; APK opens on the hello screen | 📋 |
| 1 | [core capability A] | [observable behavior]; APK shows [what] | 📋 |
| 2 | [core capability B] | [observable behavior]; APK shows [what] | 📋 |
| 3 | [core capability C] | [observable behavior]; APK shows [what] | 📋 |
| 4 | Ads + ad-free purchase (P4), only if approved | test-ID ads behind Policy, consent, IAP + restore; APK shows the flow | 📋 |
| 5 | Polish + store listing draft (P6) | [polish items]; listing text + shot list, owner-verified | 📋 |
| 6 | Release prep (P5) | signed artifact, checklist green, stopped before submit | 📋 |

## Held / cancelled (keep visible)

- [phase] — ❌ [why, date]
