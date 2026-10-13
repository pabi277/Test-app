<!-- SPDX-License-Identifier: MIT -->

# Play listing draft — PaisaCalc (phase 6)

**Status: draft for owner approval (gate).** Every claim below is a feature that
exists in the phase-5 build; nothing here promises more than the app does.
Character counts were checked on 2026-10-13 against Play's current limits (title
30, short description 80, full description 4000).

## Store identity

| Field | Draft | Notes |
| --- | --- | --- |
| App name (≤30) | **`PaisaCalc: Loan EMI & GST Calc`** (30) | Alternates: `PaisaCalc: EMI & GST Calc` (25), `PaisaCalc — Loan EMI & GST` (26). Owner picks at the gate; the name must stay "PaisaCalc" unless the gate changes the working name |
| Short description (≤80) | **`Offline EMI calculator: prepayments, ₹ lakh/crore numbers, PDF export, GST tab.`** (79) | Leads with the search term "EMI calculator" and the differentiators |
| Category | Finance (alternate: Tools) | Matches `docs/play/content-rating.md` |
| Contact email | **`pabitra27.7.06@gmail.com`** (gate answer 2026-10-13) | Must be a monitored address, shown publicly |
| Privacy policy URL | **`https://pabi277.github.io/Test-app/privacy-policy/`** (GitHub Pages; live once the owner enables Pages) | Also set in the app build (`-Ppaisacalc.privacyPolicyUrl=…`) |
| Contains ads | Yes | One opt-in rewarded ad (see `docs/play/data-safety.md`) |
| In-app purchases | Yes | One one-time ad-free purchase, `paisacalc_ad_free` |
| Website / other | none | Do not add a site that does not exist |

## Full description (draft, ~1,900 characters)

> **PaisaCalc — the EMI and GST calculator that works without the internet.**
>
> PaisaCalc is built for people planning or repaying a loan in India. Every
> number is calculated on your phone, offline, and your loan details never leave
> the device.
>
> **What it does**
>
> • **EMI, in one screen** — enter the amount, the interest rate and the tenure
> (or drag the sliders) and see your EMI, the principal-vs-interest split, the
> total interest and the total payable.
> • **Part-prepayments** — add a monthly, quarterly, half-yearly, yearly or
> one-time prepayment, starting from any installment, and see exactly what it
> saves you: the interest saved, the new closing month, or the lower EMI.
> • **Full amortization schedule** — every installment with the month
> (mmm-yy), EMI, principal, interest, any prepayment, and the closing balance.
> • **Correct Indian money formatting** — ₹ lakh and ₹ crore grouping that stays
> right past ₹10 lakh, where other calculators break.
> • **Export and share** — save the whole schedule as a PDF, or share it as a
> PDF or image through Android's share sheet, so it can go straight to a bank,
> an advisor or a family group.
> • **GST calculator tab** — add GST to a base price or extract GST from an
> inclusive amount, with CGST/SGST for intra-state and IGST for inter-state
> supplies, dated rate presets, and a custom rate. Exact paise results.
> • **Saved loans and history** — keep the loans you compare and your recent GST
> calculations on your phone (up to 50), and load them back any time.
> • **Light and dark themes**, and layouts that work on small phones and tablets.
>
> **Privacy, honestly**
>
> • No account, no sign-in, no server: your loan and GST data is stored only in
> the app's private database on your device, and Android backup is disabled for
> it. Uninstalling the app deletes it.
> • No location, contacts, camera, microphone or broad storage permissions.
> • The calculator, the schedule and the GST tab work in airplane mode.
> • Ads: **one optional rewarded ad** can be watched to unlock exporting, or
> you can buy the one-time **ad-free upgrade** and never see an ad. There are no
> banners, no pop-ups between you and a calculation, and no ads at all until you
> tap the unlock — if no ad can be served, export unlocks anyway.
>
> **Good to know**
>
> • The GST presets are a convenience list, not an HSN lookup, and rates change —
> verify the applicable rate against the CBIC portal for your supply.
> • PaisaCalc is a calculator, not financial advice, and it is not affiliated
> with any bank or government body.
> • Figures are estimates; a bank's schedule can differ by a rupee or two
> because of rounding.
>
> Made in India, for Indian loans — offline, ad-respectful, and no data collection
> beyond the optional ad described above.

## Screenshot shot-list (8 phone screenshots, in order)

Capture these from the **release-variant build** on a phone, in dark or light
theme but consistently, with the theme that shows the design best. Play requires
JPEG or 24-bit PNG without alpha; 320–3840 px, longest side ≤ 2× the shortest.

| # | Screen to capture | Suggested caption overlay (optional) |
| --- | --- | --- |
| 1 | EMI screen with the Example loaded — hero EMI, donut, totals | "EMI, interest and the full break-up in one screen" |
| 2 | Prepayments card with the savings callout ("You save ₹7,20,524…") | "See exactly what a prepayment saves you" |
| 3 | Schedule rows for a year (mmm-yy months, yearly totals) | "Every installment, with mmm-yy months" |
| 4 | A large loan (≥ ₹1 crore) showing lakh/crore grouping | "₹ lakh and crore formatting that stays correct" |
| 5 | Export card in its unlocked state (three actions visible) | "Export the whole schedule as PDF or image" |
| 6 | GST tab, intra-state, with the CGST/SGST split result | "GST with CGST/SGST or IGST — and exact paise" |
| 7 | GST presets plus the "verify against CBIC" note | "Dated presets, custom rate, honest disclaimers" |
| 8 | Saved loans list with a loaded loan | "Save the loans you compare — on your device only" |

Not allowed in screenshots: fabricated numbers the app cannot produce, a
"no ads" claim without the ad-free caveat, bank/government logos, or any rating
badge. Tablet screenshots (7"/10") are optional; capture them only if the owner
wants tablet featuring, and check the current Console requirements then.

## Graphic assets (specs; not yet produced)

| Asset | Requirement | Source |
| --- | --- | --- |
| App icon | 512 × 512 px, 32-bit PNG with alpha, ≤ 1 MiB | Export from the app's own launcher artwork (`app/src/main/res/drawable/ic_launcher.xml`) so store and device icons match |
| Feature graphic | 1024 × 500 px, JPEG or 24-bit PNG **without** alpha | Draw from the app's palette (`ui/theme/Theme.kt`) and the EMI/GST story; **deferred to the gate** — the owner approves the artwork before it is made |
| Phone screenshots | JPEG/PNG, per above | Shot-list above, from the release build |
| Promo video | optional | Skip unless the owner asks |

## "What's new" (first release)

> First release: offline EMI with part-prepayments and interest saved, full
> amortization schedule with mmm-yy months, ₹ lakh/crore formatting, PDF/image
> export, a GST calculator with CGST/SGST and IGST, saved loans and history,
> light/dark themes — and one optional rewarded ad (or the one-time ad-free
> upgrade) to unlock export.

## Owner approval needed at the gate

- [ ] App name (one of the three options above, or another ≤30-char name).
- [ ] Short and full description wording.
- [ ] Screenshot captions and the theme used for capture.
- [ ] Whether to produce the 512 icon and feature graphic now (and approve the
      artwork direction) or just before upload.
- [x] Developer name *Pabitra Chakrabortty*, contact `pabitra27.7.06@gmail.com`, policy URL `https://pabi277.github.io/Test-app/privacy-policy/` — answered at the gate 2026-10-13.
- [ ] Remaining at the gate: app name, description wording, screenshot captions/theme, artwork decision.
