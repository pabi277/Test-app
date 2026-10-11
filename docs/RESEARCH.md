# RESEARCH.md — PaisaCalc (working name)

> Plan-chat deliverable, created 2026-10-11. All Play Store evidence fetched
> on 2026-10-11 from India (hl=en_IN). Facts and inferences are separated;
> uncertainties are stated. AppFlow provenance: playbook pinned at
> `c8856022ca57cdf23ae78b9491481543db89438d`.

## Niche verdict line

Demand: proven — five India-facing EMI-calculator listings at 10L+ downloads
each, leader at 50L+ (CRED, checked 2026-10-11). Staleness: two top-five
listings last updated Jan 2024 and Jan 2023 while still ranking. Complaints:
9+ recurring verified themes on leader listings (ad walls, fake ad close
buttons, broken ₹10L+ number formatting, missing/broken part-prepayment, no
PDF export, location collection by calculators). Owner advantage:
deterministic math (no content-accuracy duty), modern-UI ambition, India-care
details (lakh/crore formatting, prepayments) and an offline zero-data privacy
story. Decision: BUILD v1 — with the kill test below pre-committed; complete
on-device ten-1-star audit recommended before Play submission.

## Search method (knowledge/niche-method.md)

Seeded from owner context: trial app, "very modern calculator", roam-free
area, English (India). Search phrases run on 2026-10-11 (country India):
`EMI calculator`, `GST calculator`, `best calculator app India Play Store
2026 smart calculator`, `EMI calculator SIP calculator app India Play Store
one star reviews problems`, `date calculator app India`, `unit converter app
India offline`. Top listings fetched directly from play.google.com (en_IN).

Evidence limit (honesty): Play serves ~3 reviews per listing server-side, so
the "ten recent 1-star reviews per listing" audit is PARTIAL — complaint
themes below are verified but fewer than ten per listing. Finish the audit
on-device before submission. Missing evidence ⇒ that part stays
"validate further", not "proven".

## Top-5 evidence — chosen option (EMI core)

| # | Listing | Rating/reviews | Downloads | Updated | Verified signals |
|---|---|---|---|---|---|
| 1 | [CRED — EMI Calculator: Calculate Loan](https://play.google.com/store/apps/details?id=com.finance.loan.emicalculator&hl=en_IN) | 4.4★ / 1.35L | 50L+ | 2026-10-01 | 1★: "four ads just to get to the interface"; "close button is fake — leads you to download the advertised app"; "a subscription… for a calculator app… Uninstall!!" (40 helpful). Dev replies 2026-09-30/10-01 claim "now completely ad-free". Data safety: collects personal info, messages; shares app activity |
| 2 | [Techinfornity — EMI Calculator](https://play.google.com/store/apps/details?id=com.finance.emi.calculate&hl=en_IN) | 4.5★ / 61.3K | 10L+ | 2025-12-20 | Indian number format broken above ₹10 lakh; no PDF/Excel export; quarterly prepayment save broken (Dec 2023, open Dec 2025); part-payment requested Jun 2024, still absent. Data safety: collects AND shares Location; "data can't be deleted" |
| 3 | [ARBApps — EMI Calculator - Loan Planner](https://play.google.com/store/apps/details?id=com.arbapps.loanemicalc&hl=en_IN) | 4.9★ / 14K | 10L+ | 2024-01-28 STALE | Requests: multi-disbursement home loans, variable rates, part-payment with updated amortization, month as mmm-yy not number. Data safety: "no data collected"; monetizes via bank/NBFC loan leads |
| 4 | [Easy EMI Loan Calculator](https://play.google.com/store/apps/details?id=com.swarajk1.easyemicalculator&hl=en_IN) (solo dev) | 5.0★ / 2.78K | 10L+ | 2026-04-05 | Requests: PDF export of amortization, % sign in rate field. Data safety: collects AND shares Location; "data can't be deleted" |
| 5 | [Loan EMI Calculator (Nilesh Harde)](https://play.google.com/store/apps/details?id=nilesh.loanemicalculator&hl=en&gl=US) | 4.3★ / 1.26K | 100K+ | 2023-01-02 STALE | Feature-rich but unmaintained 2+ years |

Adjacent signals: [EMI Calculator Premium](https://play.google.com/store/apps/details?id=com.finance.emi.calculate.paid&hl=en_US)
($2.49, 4.2★ — paid bugs unfixed 4 months), [Wisdomlogix EMI-GST-SIP](https://play.google.com/store/apps/details?id=com.wisdomlogix.emi.calculator.gst.sip.age)
(4.1★ — "too many ads… keep coming again").

## Verified complaint themes (the free product spec)

1. Ad wall before the interface (CRED)
2. Fake/manipulative ad close buttons (CRED)
3. Subscription for a calculator app (CRED)
4. ₹ formatting breaks above 10 lakh (Techinfornity)
5. No PDF/Excel export (Techinfornity, Easy EMI)
6. Part-prepayment broken or missing (Techinfornity, ARBApps)
7. Multi-disbursement/variable-rate home loans unsupported (ARBApps — deferred post-v1)
8. Months as numbers instead of mmm-yy (ARBApps)
9. Location/personal data collected by calculators (Techinfornity, Easy EMI, CRED)

Inference (flagged): CRED removing ads in late Sep 2026 narrows the pure
"no-ads" gap — differentiation must also rest on themes 4–8 and privacy.

## GST tab evidence (refine: A + GST)

Leader [HDS GST Calculator India](https://play.google.com/store/apps/details?id=net.smartlogic.indiagst&hl=en_IN):
4.1★ / 22K / 10L+, verified 1★: "ad on every step", "first thing it asked for
is precise location… deserve negative rating", "worst App only ads seen"
(Jan 2026). Cleaner newcomer [Vinson GST](https://play.google.com/store/apps/details?id=com.vinsontech.gstcalculator&hl=en)
shows the gap is already contested — our GST tab stays a v1 value-add inside
the EMI app (slabs as a versioned, dated offline asset; user-editable custom
rate), NOT a separate app (RULES law 5: no reskins).

## Rejected options (never re-litigate without new evidence)

- **Generic modern calculator (simple/scientific)** — Google Calculator 4.3★
  and Samsung 3.9★ are preinstalled defaults; clone swarm (Dlight Soft 10L+,
  RV AppStudios 4.8★, SMARTWHO 4.6★…) competes via "after-call" hooks/AI
  scanners. Giant-owned, cosmetic-only differentiation.
- **Date/working-days calculator** — top listing 4.6★/500K+ with only minor,
  old complaints; thin per-app demand signal.
- **Offline unit converter** — fragmented; privacy-clean niche already served
  (apps declaring no ads/tracking/internet permission); weak monetization.
- **Standalone GST app** — ongoing duty to keep slabs current with policy
  changes (law 14 makes accuracy the owner's job) and smaller pool than EMI;
  folded in as a tab instead.

## Privacy / licensing / policy risks

- Play target API 36 required for new apps (RULES.md policy checkpoint,
  checked 2026-10-10) — re-check on release day.
- AdMob: UMP consent, Data Safety must declare the SDK; rewarded ads must
  never gate the core answer (RULES law 9).
- GST slabs are government-published facts: ship them as a dated asset with a
  "verify against CBIC portal" disclaimer; never claim official affiliation.
- No financial advice: app is a calculator; disclaimers in-app and listing.
- No copyrighted content involved; all math and UI owned.

## Monetization (plausible — no revenue promises)

Rewarded-first: one opt-in rewarded ad to unlock PDF/image export of the
schedule. One-time ad-free IAP (initial experiment ₹49–99) as the opt-out.
No interstitials, no banners mid-task, no subscription.

## Kill test (pre-committed BEFORE results)

Measured 4 weeks after public release from Play Console:
1. ≥ 1,000 total installs;
2. ≥ 50 organic installs in the final week, and not flat/declining for two
   straight weeks;
3. Crash-free sessions ≥ 99.5%.
Any miss → exactly ONE 2-week iteration on one verified blocker, re-measure
once; still failing → kill and start the next app. Thresholds are frozen;
never edited after seeing results.
