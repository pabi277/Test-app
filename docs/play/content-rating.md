<!-- SPDX-License-Identifier: MIT -->

# Content rating and target audience — answer worksheet (phase 6)

**Status: proposal, not a submitted answer.** Play's content-rating
questionnaire (IARC) must be answered honestly for the shipped build; these are
the answers the phase-5 build implies. Fill the form in Console and correct
anything that does not match, then record the resulting rating here.

## Category and audience

| Question | Answer | Why |
| --- | --- | --- |
| App category | App (not a game) | Utility/finance calculator |
| Primary category | Finance (alternative: Tools) | EMI + GST calculator; Finance matches the actual use |
| Target age group | 18+ / adults | Used for loan and tax planning; not designed for children |
| Families program | No | Not directed to children; no child-directed content or design |

## IARC content questions (draft answers)

| Question | Answer |
| --- | --- |
| Violence (realistic, fantasy, sexual, or otherwise) | No |
| Fear, horror, or threat | No |
| Sexuality or nudity | No |
| Profanity, crude humour, or discrimination | No |
| Controlled substances (drugs, alcohol, tobacco) | No |
| Gambling (real or simulated, including loot boxes) | No |
| User-generated content, chat, or social interaction between users | No |
| Does the app share the user's location? | No — the app requests no location permission and sends none |
| Does the app share personal information with third parties? | Yes — limited to the ad-related processing described in the privacy policy when the user opts into the rewarded ad; the app itself sends nothing. Answer the form consistently with Data Safety |
| Does the app allow users to make digital purchases? | Yes — one one-time ad-free purchase through Google Play |
| Does the app contain ads? | Yes — one opt-in rewarded ad |
| Does the app contain an unrestricted web browser or web-search access? | No — links open in the user's own browser app; there is no in-app browser |
| Does the app collect or transmit data over the internet? | Yes — only for the opt-in ad, consent check, and Play purchase |

## Expected outcome

A calculator with no violence, sexual content, gambling, or user interaction and
with ads and digital purchases disclosed normally lands in the lowest rating band
for each region (for example "Everyone"/"3+" style ratings) with an "Ads" and
"In-app purchases" disclosure. **The actual rating comes from the form, not from
expectation** — record the issued ratings per region after submitting.

## After submitting

- [ ] Save the issued rating strings here (per region) with the date.
- [ ] Confirm the Play listing shows the "Contains ads" and "In-app purchases"
      labels Play applies from these answers.
- [ ] Re-check the answers whenever the app adds ads formats, content, or
      features — a rewarded-only build and an interstitial build are not the same
      answer.
