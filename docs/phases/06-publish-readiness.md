<!-- SPDX-License-Identifier: MIT -->

# Phase 06 — Publish readiness

- Status: ✅ · Date: 2026-10-13 · Branch: `arena/0c89305a-test-app`
- CI: green — [run 38411379003](https://github.com/pabi277/Test-app/actions/runs/38411379003),
  tip `5f6a815`. Earlier phase-6 runs: `417fa38`
  ([38410820083](https://github.com/pabi277/Test-app/actions/runs/38410820083)),
  `15ac34a` ([38409809270](https://github.com/pabi277/Test-app/actions/runs/38409809270)),
  `bbcd174` ([38408903198](https://github.com/pabi277/Test-app/actions/runs/38408903198) —
  the guard's first run, which failed on purpose and taught it to tell an SDK
  placeholder from a live ad unit).
- **Artifacts:** `app-release-qa-apk` — **11,421,552 bytes**, the *release*
  variant (debug-signed; device QA only), and `app-progress-apk` — 15,865,176
  bytes. The packaged release APK is 12,180,110 bytes, versionCode 1,
  versionName 0.1.0.
- **This phase stops at the publish gate.** Nothing was hosted, submitted,
  purchased, or uploaded to Play; every item that needs the owner's accounts is
  listed in `docs/play/checklist.md` and in the gate batch at the end of the
  phase.

## Acceptance

- 6.1 — privacy policy, Data Safety answers, content rating, target API
  re-check, closed-test plan.
- 6.2 — listing draft (short/long description, screenshot shot-list) + final QA
  of the release build.
- Done means: every `templates/play-checklist.md` item green **or** explicitly
  blocked on an owner action, and a downloadable release APK. No submission
  without an explicit owner command.

## Implemented — 6.1 publish pack (`docs/play/`)

- `privacy-policy.md` — publish-ready text for the actual phase-5 build: what
  stays on the device, the three network paths (opt-in rewarded ad, consent
  check, Play purchase), what Google's SDKs may process, retention/deletion,
  children, contact. Owner fields (`EFFECTIVE_DATE`, `PUBLIC_DEVELOPER_NAME`,
  `SUPPORT_EMAIL`, `POLICY_URL`) are marked for the gate.
- `data-safety.md` — the build facts the answers must match, the four Play
  sections mapped to them, and the AdMob rows explicitly marked "verify against
  Google's SDK guidance in Console" rather than guessed. Records
  contains-ads = yes and in-app-purchases = yes.
- `content-rating.md` — IARC questionnaire draft (no violence/sex/drugs/
  gambling/UGC; ads yes; digital purchases yes; no unrestricted web access) and
  target audience = adults, not Families.
- `closed-test-plan.md` — 25-slot plan around the live rule (12 testers opted in
  continuously for 14 days on newer personal accounts), the nine flows testers
  should exercise, the "never click ads, never put tester identity in Git" rules,
  and an invitation draft that discloses the ads SDK.
- `listing-draft.md` — title options with character counts verified on
  2026-10-13, a 79-character short description, the full description, an
  eight-shot screenshot list with capture rules, asset specs, and "what's new".
- `checklist.md` — the app's copy of the playbook checklist with every item
  marked ✅ verified / 🕒 owner Console action / ⛔ blocked at the gate, plus the
  remaining work in order.
- **In-app policy link:** `-Ppaisacalc.privacyPolicyUrl=…` →
  `BuildConfig.PRIVACY_POLICY_URL` → `legal/PrivacyPolicy.kt`, which accepts only
  an http(s) URL. Until it is set, the ⋮ menu shows no *Privacy policy* entry, so
  no build can ship a dead privacy link (`PrivacyPolicyTest`).

## Implemented — 6.2 release QA in CI

- CI now assembles the **release variant** on every push and uploads it as
  `app-release-qa-apk` (debug-signed while no keystore secrets exist), so the
  phase's deliverable is the build type that will actually ship. Signed
  upload-grade artifacts remain main-only.
- A new guard step reads the packaged APK and refuses to pass a build that:
  requests any privacy-invariant permission (location, contacts, camera,
  microphone, phone, SMS, storage, media, calendar, sensors,
  `QUERY_ALL_PACKAGES`), or contains an ad unit that is neither Google's test
  publisher nor an all-zero SDK placeholder while no ad unit was supplied through
  protected configuration. Unexpected permissions warn; every finding is emitted
  as a check annotation, because Actions log downloads resolve to a host this
  sandbox cannot reach.
- Evidence the guard produced for the shipped release APK (also in
  `docs/play/release-qa.md`):
  - permissions: `INTERNET`, `ACCESS_NETWORK_STATE`, `AD_ID`, `BILLING`,
    `ACCESS_ADSERVICES_AD_ID/ATTRIBUTION/TOPICS`, `FOREGROUND_SERVICE`,
    `WAKE_LOCK`, `…DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` — **no** location,
    storage, media, contacts, camera, microphone, phone or SMS permission;
  - ad ids: the two Google **test** ids, plus
    `ca-app-pub-0000000000000000~0000000000`, which the guard traced to
    `com.google.android.ump:user-messaging-platform:4.0.0`
    (`com/google/android/gms/internal/consent_sdk/zzco.class`, `zzp.class`) — the
    consent SDK's all-zero placeholder, which cannot serve ads.

## Verification and honest limits

- Unit tests: 121 methods (118 in phase 5, `+3` for the privacy-policy URL rule).
- The release variant compiles and packages, and its permission/ad-id surface is
  verified by CI from the APK itself — not from the source.
- **Not verifiable here:** ad delivery, the consent form, the Play purchase
  sheet, SAF/share flows and TalkBack behavior need a real device; the checklist
  in `docs/play/release-qa.md` is written for the owner's pass. The signed,
  upload-grade build still needs the four `ANDROID_KEYSTORE_*` secrets.
- Data Safety's AdMob rows and the content-rating form are **not** answered here
  on purpose: guessing them would be invented fact, and the playbook marks them
  as Console/owner verification.

## Gate items this phase surfaced (owner, in `docs/play/checklist.md`)

1. Where the privacy policy will be hosted (deploy = gate) and the resulting
   `[POLICY_URL]`.
2. `[SUPPORT_EMAIL]` and the public developer name.
3. The 512×512 store icon and 1024×500 feature graphic (artwork approval).
4. The four `ANDROID_KEYSTORE_*` secrets, so the main-only signed-release job can
   produce the upload artifact.
5. AdMob rewarded unit + `paisacalc_ad_free` product, and the live-id release
   switch through protected configuration.
6. Data Safety, content rating, target audience, and the closed-test run in
   Console.
7. The explicit publish command (and the PR/merge that precede it).
