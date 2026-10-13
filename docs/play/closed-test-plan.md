<!-- SPDX-License-Identifier: MIT -->

# Closed-test plan — PaisaCalc (phase 6)

Adapted from `templates/testers-plan.md` for this app. It is a plan for genuine
volunteers, not a review campaign. Never buy installs, ask for ratings, or put a
tester's name, email, or phone number in this repository or in chat.

## What the rule actually is

For personal developer accounts created after 2023-11-13, Google's current page
requires **at least 12 testers continuously opted in for the preceding 14 days**
before the owner can apply for production access (checked 2026-10-10 in this
repo's playbook). The "20 testers" figure circulating online is not the current
rule. **Owner must confirm account type and the live requirement in Play
Console** — a company account, or an older personal account with earlier
releases, may not need a closed test at all. This plan targets 25 invited
volunteers so 12+ stay continuously opted in with room for dropouts.

## Preconditions (owner actions, all Play-side)

- [ ] Play Console account verified; payments profile and tax info completed.
- [ ] `paisacalc_ad_free` product created (one-time, non-consumable); price set; the
      ad-free flow tested end-to-end in an internal-test build.
- [ ] AdMob account verified; the rewarded unit created and its ID supplied to the
      release build through protected configuration (never committed, never chat).
- [ ] Privacy policy hosted; URL linked in Console and set in the app build.
- [ ] Data Safety, content rating, and target audience answered (see the sibling
      worksheets).
- [ ] Release build signed (`ANDROID_KEYSTORE_*` Actions secrets) and uploaded to
      the closed track — the closed-test build is the *release* build, not the
      debug APK.

## Recruiting (25 slots, anonymous in the tracker)

| Slots | Group | Why |
| --- | --- | --- |
| T01–T05 | Friends/family who actually have a loan | Real prepayment and EMI questions |
| T06–T10 | Work or study peers | Can exercise GST and export flows |
| T11–T15 | Local-language/community contacts | Check English (India) copy and ₹ formatting sense |
| T16–T20 | Opt-in community where the rules allow inviting | Outside perspective |
| T21–T25 | Backups | Replace anyone who cannot continue |

The private tracker may hold slot, consent date, opt-in status, and a summary of
feedback. Contact details live outside Git, in the owner's own private store,
and are deleted when the test ends.

## What testers are asked to do

Use the app naturally for at least 14 continuous days, and report problems
truthfully — not to rate it. Specific flows worth exercising (this is the list
the agent cannot run on a device):

1. EMI calculation for a real loan, including an amount above ₹10 lakh and one
   above ₹1 crore (₹ formatting must stay correct).
2. Add a prepayment (monthly/quarterly/half-yearly/yearly/one-time) and check the
   interest-saved figure and the shortened schedule.
3. Save a loan, force-stop the app, reopen it, and load the saved loan.
4. Airplane mode: confirm EMI, schedule and GST still work.
5. Export: share the schedule as PDF and as an image; save a PDF through the
   document picker; check the exported file's rupee formatting.
6. Rewarded export unlock: watch the ad, confirm export unlocks, then confirm a
   second export within 24 h needs no ad. **Report what the ad showed; do not tap
   ads deliberately** — AdMob policy forbids inducing or clicking ads.
7. Ad-free purchase (testers on a Play-installed build): buy, confirm ads stop,
   uninstall/reinstall, and confirm *Restore purchase* brings it back. Use Play's
   license-tester/test-purchase setup; never real money for QA.
8. GST tab: add and remove GST, CGST/SGST vs IGST, a preset and a custom rate.
9. Light/dark theme, rotation, and TalkBack on the main screens.

## Rules for the test

- Testers opt in voluntarily and can leave at any time; nobody is pressured to
  continue or to rate.
- The ads SDK is in the build: the app requests test ads in QA builds, and an ad
  request only happens when a tester taps the unlock. Disclose that the build
  contains an ads SDK before inviting (the invite text below does).
- Bugs are recorded without names, emails, phone numbers, purchase tokens, or
  screenshots that contain personal data.
- The Play clock starts when real users are opted in to the closed track, not
  when the track is created. Record the first opt-in date in the private tracker.

## Invitation draft (owner sends; personalize before sending)

> Hi! I'm getting a small Android app ready for a Google Play closed test — it's
> PaisaCalc, an offline EMI/loan calculator for India with prepayments, PDF/image
> export and a GST tab. Would you opt in, install it, and use it honestly for at
> least 14 days? It's not a request for a rating or a positive review, and you
> can say no or leave the test any time. The build includes Google's ads SDK: one
> optional rewarded ad can be watched to unlock export, and there's a one-time
> ad-free purchase. Your loan and GST data stays on your phone. I'll send the
> opt-in link and a private way to report problems.

## After the 14 days

- [ ] Record the opt-in count and continuity from Play Console (private tracker).
- [ ] File every reported bug with reproduction steps; fix crashes and wrong
      numbers first.
- [ ] Apply for production access only when the Console status and the live rule
      are both satisfied — and only on the owner's explicit command (publish gate).
- [ ] Start the post-release measurement in `docs/RESEARCH.md` (kill test) from
      the public release date, not from the closed-test date.
