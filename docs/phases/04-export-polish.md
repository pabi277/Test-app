<!-- SPDX-License-Identifier: MIT -->

# Phase 04 — Export + polish

- Status: 🚧 · Date: 2026-10-11 · Branch: `arena/4a0902f3-test-app`
- CI: green — [run 38132290384](https://github.com/pabi277/Test-app/actions/runs/38132290384), tip `bf6b8ae`; all 100 unit tests passed and `assembleDebug` succeeded.
- **APK:** `app-progress-apk`, 10,801,738 bytes (compressed), uploaded by that run with 30-day retention.
- The first run [38131528322](https://github.com/pabi277/Test-app/actions/runs/38131528322) exposed that Robolectric's `PdfDocument` is closed at construction in its JVM runtime (`document is closed!`); Android's native PDF backend is unavailable to that unit test. Full failure details were surfaced in [run 38131838704](https://github.com/pabi277/Test-app/actions/runs/38131838704). The PDF renderer now delegates page slicing to a pure Kotlin paginator, which is tested; the PNG renderer is covered by actual signature/dimensions rendering checks.
- Local `./gradlew testDebugUnitTest assembleDebug` remains blocked because this workspace has no JDK (`JAVA_HOME` unset and no `java` command).

## Acceptance

- 4.1 — Export a ₹-formatted amortization schedule as a PDF and a shareable image without broad storage permissions; tests cover rendering. The APK must share a real PDF containing a prepayment scenario through Android's sharesheet.
- 4.2 — Improve TalkBack labels/content descriptions and contrast; make the content adapt to tablet and narrow layouts; refine the icon and theme.

## Implemented

- `export/LoanScheduleExportData.kt` creates the shared, formatted export model from the displayed `LoanSummary`. It preserves every installment and the applied prepayment amount; the renderers do not recalculate loan math.
- `export/LoanSchedulePdfRenderer.kt` writes a multipage, printer-friendly PDF with rupee metrics, the complete installment table, prepayments, page numbers, and estimate caveat. **Save PDF** uses Android's `CreateDocument` Storage Access Framework flow; **Share PDF** writes to the app's private cache and hands a read-only `FileProvider` URI to the Android sharesheet.
- `export/LoanScheduleImageRenderer.kt` renders the full schedule as PNG; **Share image** uses the same private-cache/FileProvider sharesheet path. The provider exposes only `cache/exports/`; no broad or runtime storage permission was added.
- Export regression tests cover the ₹-formatted metrics/rows, prepayment inclusion, complete PDF pagination, and PNG signature/dimensions. Theme tests check WCAG AA text contrast for the light and dark schemes.
- All three EMI sliders expose a TalkBack label and current value. Schedule rows announce their month, amounts and prepayment as one semantic unit; the decorative privacy indicator is hidden from accessibility. Loan/GST delete controls announce the item they remove; the overflow action has a content description.
- EMI schedule headers and rows switch to a compact layout below 400 dp. EMI and GST content are capped at 720 dp and centered on wider screens. Theme typography/hero contrast and the vector launcher icon were polished.

## Verification and handoff

- CI tests/build and the `app-progress-apk` artifact are green (run 38132290384). PDF pagination and complete prepayment-row retention are unit-tested; the PNG renderer is exercised under Robolectric and its output signature/dimensions are checked. Robolectric cannot execute Android's native `PdfDocument`, so the PDF byte signature is not unit-tested in this environment.
- No emulator/device was available for visual tablet, low-density, TalkBack, PDF-reader, or real sharesheet QA. The responsive bounds and compact breakpoint are implemented, but manual device verification remains a phase-closure follow-up.
- Before marking this phase done, install the linked APK, load the built-in quarterly-prepayment example, share the PDF, verify it opens with all installments and prepayment rows, then separately exercise Save PDF and Share image. Check light/dark contrast and TalkBack with the target API.
