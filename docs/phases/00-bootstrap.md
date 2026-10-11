<!-- SPDX-License-Identifier: MIT -->

# Phase 00 — bootstrap

- Status: ✅ · Date: 2026-10-11 · Branch: arena/666c81b5-test-app
- CI: run [38123389452](https://github.com/pabi277/Test-app/actions/runs/38123389452)
  green (tip sha `6e1a4e3`) · Provenance: AppFlow @
  `c8856022ca57cdf23ae78b9491481543db89438d` (reference only)
- **APK:** artifact `app-progress-apk` (836 KB) from run 38123389452 —
  *shows: a hello screen titled "PaisaCalc" with the bootstrap note; no
  calculator logic yet.*

## Parts

- 0.1 — Frozen `templates/` + `knowledge/` snapshot + README provenance — ✅
  (commit `aae35d7`)
- 0.2 — Plan docs (`docs/RESEARCH.md`, `ROADMAP.md`, `DECISIONS.md`) +
  `rule.md` + `prompt.md` — ✅ (commit `2fc537e`)
- 0.3 — MIT LICENSE, filled README, Gradle/AGP hello screen, ci-build.yml,
  unit test, CI with `app-progress-apk` — ✅ (green on the 2nd run)

Note: the first run (38123310494) failed at the `Set up Android SDK` step —
the pinned third-party action `android-actions/setup-android@v3` failed
during runner setup. Fix (commit `6e1a4e3`): replaced it with a plain
license-pre-acceptance step; the runner image already provides
`cmdline-tools`/`ANDROID_HOME` and AGP auto-installs required packages.

## Evidence (before the change)

- AppFlow `START.md` §2/2.5/3 (pinned playbook) prescribes the exact
  bootstrap order: snapshot → plan docs → LICENSE/README/skeleton/CI.
- `templates/ci-build.yml` expects an `app` module and root Gradle wrapper;
  skeleton matches both. Wrapper jar fetched from the official
  `gradle/gradle` repo at tag `v8.13.0` (GitHub contents API, 2026-10-11).
- No JDK/Android SDK in this workspace, so local builds are impossible — CI
  is the executor of record (law 11).

## What shipped

- `LICENSE` (MIT, Copyright (c) 2026 pabi277); `README.md` filled from
  `templates/readme-app.md` with provenance line kept.
- Gradle skeleton: `settings.gradle.kts`, root + `app/build.gradle.kts`,
  `gradle/libs.versions.toml` (AGP 8.11.1, Kotlin 2.2.0, JUnit 4.13.2),
  `gradle.properties`, wrapper (`gradlew`, `gradlew.bat`,
  `gradle/wrapper/*`, Gradle 8.13 distribution).
- App: namespace/applicationId `com.pabicalc.paisacalc`, compileSdk/
  targetSdk 36, minSdk 24, versionCode 1, versionName 0.1.0; plain
  `MainActivity` hello screen (no dependencies → smallest possible surface);
  release signing reads env secrets only, falls back to debug signing off-main.
- `.github/workflows/ci-build.yml` copied verbatim from the template.

## Tests

- `SmokeTest.arithmeticPipelineWorks` (placeholder; EMI suite in phase 1) —
  green in run 38123389452.

## Follow-ups
- Owner items unchanged: on-device ten-1-star audit, GST slab verification
  against CBIC, Play account + closed-test readiness.
- Agent decision logged: hello screen uses a framework-theme plain Activity
  (no AndroidX deps) to keep bootstrap minimal; UI stack for phase 1 is a
  reversible choice.
