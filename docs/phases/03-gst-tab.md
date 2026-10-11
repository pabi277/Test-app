<!-- SPDX-License-Identifier: MIT -->

# Phase 03 — GST tab + offline data layer

- Status: ✅ · Date: 2026-10-11 · Branch: `arena/4a0902f3-test-app`
- CI: green — [run 38129095902](https://github.com/pabi277/Test-app/actions/runs/38129095902), tip `af8752a`; hosted JDK 17 ran unit tests and assembled the debug APK. Initial runs caught and fixed the Kapt plugin-resolution conflict, GST cap constant initialization, and Room test database closing.
- **APK:** CI uploaded the compressed `app-progress-apk` artifact (10,735,264 bytes). It is built from the phase 3 source and includes the saved-loan flow, GST add/remove screen and history, EMI/GST tabs, and persistent light/dark theme. The artifact is available from the linked Actions run.
- Local `./gradlew testDebugUnitTest assembleDebug` remains unavailable because this workspace has no JDK (`JAVA_HOME` unset and no `java` command).

## Parts

- 3.1 — Saved loans and GST history in an offline Room store; no app permissions — ✅
- 3.2 — GST add/remove math, intra-state CGST/SGST and inter-state IGST, dated offline rate presets, custom rate, CBIC verification disclaimer — ✅
- 3.3 — EMI/GST tab navigation and light/dark theme — ✅

## Evidence (before the change)

- Acceptance test: `docs/ROADMAP.md:11` — tests cover GST math both directions, split types, history persistence; the APK shows both tabs and a saved loan surviving process death.
- Before phase 3, `app/src/main/AndroidManifest.xml` had no network permission but enabled Android backup; `MainActivity` displayed only `EmiCalculatorScreen`; `ui/theme/Theme.kt` exposed only a light scheme.
- The recorded rule requires dated GST data plus CBIC verification and leaves rate accuracy with the owner: `rule.md:83` and `docs/DECISIONS.md:29`.
- GST preset source references are pinned in `app/src/main/assets/gst-rate-slabs.txt`: CBIC Notifications 09/2025 and 10/2025-Central Tax (Rate), effective 2025-09-22, plus the PIB 56th GST Council implementation note. The asset is explicitly a convenience list, not a complete or HSN-specific schedule.

## What shipped

- `data/` — Room 2.7.2 database, entities, DAOs, singleton builder, and a local-only repository. Saved loans retain principal, rate, tenure, and the current single prepayment instruction. GST history records the exact mode, split, rate, entered amount, taxable value, tax, and total; the screen reads the latest 50 rows.
- Android cloud and device-transfer backup are disabled/excluded for database and preference data. The app still declares no permissions, including `INTERNET`.
- `gst/` — exact paise-based add/remove calculation, rate parsing and input validation, and a parser for the dated pipe-delimited asset. Half-up paise rounding is used; intra-state CGST is the half-up half of tax and SGST is the remainder, preserving the displayed total.
- `ui/gst/` — GST amount/rate controls, intra/inter-state choice, result breakdown, explicit save-to-history action and removable history rows. The rates notice tells users to verify their supply against CBIC.
- `ui/` — bottom navigation between EMI and GST, state retained while switching tabs, and a locally persisted light/dark toggle that initially follows the system setting.
- `ui/emi/SavedLoansCard.kt` — save/load/remove actions for loans. Loading reconstructs the original loan and prepayment fields from Room.
- `gradle/libs.versions.toml` — Room 2.7.2; Robolectric 4.14.1 and AndroidX Test Core 1.6.1 enable a file-backed Room reopen test in the existing unit-test task.

## Tests and verification

- CI run 38129095902 passed unit tests and the debug build. GST tests cover add/remove in both supply modes, split reconciliation, zero-rate behavior, caps, and paise half-up rounding.
- Input/catalog tests cover exact rupee-to-paise parsing, custom-rate validation, sanitizer behavior, dated asset metadata, and malformed/duplicate preset rejection.
- Saved-loan mapper tests reload loan and prepayment settings. `PaisaDatabaseTest` writes a loan and GST calculation, closes the file-backed Room database, reopens it, and asserts both rows remain.
- `IndianNumberFormatTest` verifies paise output keeps two decimals and Indian digit grouping.
- No physical-device/emulator airplane-mode smoke test was available. Offline evidence is no app permissions/network code plus Room-backed local reads/writes; owner QA still needs to exercise airplane mode and process death on a device.

## Follow-ups

- Owner verifies the GST preset asset against the current CBIC notifications before release (law 14); the asset is dated and is not an item/HSN lookup.
- Manual device smoke test (including airplane mode, saved loan after process death, dark theme and narrow screens) remains part of owner QA.
