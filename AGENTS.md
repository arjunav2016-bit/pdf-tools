# AGENTS.md

Quick context for working in this repo. Keep entries short — only what an
agent would otherwise get wrong.

## Project

Single Android app module `:app` (`com.example.pdftools`).
Kotlin + Jetpack Compose, Hilt, Room, DataStore, Coroutines, Navigation 3
(alpha). Offline-first; all PDF work runs on-device.

`backend/pptx-converter/` is a sibling Python FastAPI service (not wired into
Gradle) used by the app for high-fidelity PPT → PDF via LibreOffice.

`PROJECT_OUTLINE.txt` at the repo root has a deeper architecture tour.
Treat it as background reading — the code is the source of truth.

## Build & test

Use the wrapper. JDK 17 toolchain is pinned in `gradle.properties` via
`org.gradle.java.home` (Windows-specific path). Don't override it.

```bash
# Build
./gradlew :app:assembleDebug

# Run all unit tests
./gradlew :app:testDebugUnitTest

# Run a single test class
./gradlew :app:testDebugUnitTest --tests "com.example.pdftools.PdfProcessorTest"

# Run a single test method
./gradlew :app:testDebugUnitTest --tests "com.example.pdftools.PdfProcessorTest.testConvertPptToPdf"

# Multiple tests in one run
./gradlew :app:testDebugUnitTest \
  --tests "com.example.pdftools.PdfProcessorTest.testConvertPptToPdf" \
  --tests "com.example.pdftools.PdfProcessorTest.testConvertPdfToPpt_defaultOptions"
```

No `lint`, `detekt`, `ktlint`, or `ktlintCheck` tasks are configured —
don't add CI steps that depend on them. There is no GitHub Actions
config; no pre-commit hooks.

## Architecture

- Entry point: `MainActivity.kt` (Hilt `@AndroidEntryPoint`) → `MainNavigation()`
  → `MainScreen.kt` (adaptive: bottom bar on mobile, `NavigationRail` on tablet).
- PDF work goes through the `PdfProcessor` facade
  (`data/PdfProcessor.kt`) which delegates to per-category
  `*Processor` objects under `data/processors/`
  (`ConvertProcessor`, `EditProcessor`, `OptimizeProcessor`,
  `OrganizeProcessor`, `SecurityProcessor`).
- Per-tool state and the processing state machine live in
  `ui/viewmodels/ToolViewModel.kt`. Tool configs (state classes for each
  tool) are in `ui/viewmodels/ToolConfigStates.kt`.
- Tool routing is string-id based: `merge_pdf`, `split_pdf`,
  `compress_pdf`, `ppt_to_pdf`, `pdf_to_ppt`, `pdf_to_jpg`,
  `protect_pdf`, `redact_pdf`, `compare_pdf`, etc. The full list is
  defined in `data/ToolRepository.kt` and dispatched in
  `ToolViewModel.process(toolId, context)`.
- Each tool's Compose config UI is in
  `ui/screens/tools/*ToolConfigs.kt`. The `PdfToImageToolConfig`
  function is the rendered version of `pdf_to_jpg`.
- Result previews, success states, and post-processing UI live in
  `ui/screens/tools/ResultDisplayConfigs.kt`.
- `MainScreen` switches between bottom-bar and rail layouts based on
  Material 3 `WindowSizeClass`.

## Theme & dark mode

`MaterialTheme.colorScheme` is the only source of truth for surfaces,
outlines, and text. The dark/light color schemes are in
`theme/Color.kt` and `theme/Theme.kt`.

When adding UI:

- Cards: `containerColor = MaterialTheme.colorScheme.surfaceContainerHigh`
  (or `surfaceContainer` for less-elevated). **Never** hardcode
  `Color.White` or `Color(0xFFF4F6F9)` — that breaks dark mode.
- Borders / dividers: `outlineVariant` or `outline`.
- Body text: `onSurface` for primary, `onSurfaceVariant` for muted
  (replaces the old hardcoded `0xFF1A1F26` / `0xFF5F6368` / `0xFF9E9E9E`).
- Inactive slider/track: `surfaceContainerHighest` (replaces
  `0xFFE2E8F0`).
- Text field background: `surfaceContainerHigh` (replaces `0xFFF1F3F6`).
- Tool config UIs receive `accentColor: Color` as a parameter — use
  it (it's already theme-aware via `ToolCategory.darkAccentColor` vs
  `.accentColor`) instead of hardcoding a brand color.
- For a "selected" tinted background, use
  `accentColor.copy(alpha = 0.15f)` — it works in both themes.

Many existing tool configs (`ConvertToolConfigs.kt`,
`OptimizeToolConfigs.kt`, `OrganizeToolConfigs.kt`, `EditToolConfigs.kt`)
still have hardcoded light colors. Fix them inline when you touch the
file rather than deferring.

## PPT ↔ PDF backend

`ConvertProcessor.convertPptToPdf` is the only function with a backend
fast-path:

- URL is hardcoded: `http://10.0.2.2:8080/convert/presentation-to-pdf`
  (`ConvertProcessor.kt:63`). `10.0.2.2` is the Android emulator's
  loopback to the host. On a real device, the URL is unreachable and
  the function always falls back to the local POI renderer.
- `usesCleartextTraffic="true"` in the manifest is intentional — it
  allows the plain-HTTP backend call.
- Backend is used only when the user picks default options
  (`shouldUsePptBackend`: `slideRange="all"`, empty
  `selectedSlides`, `slidesPerPage=1`, `includeNotes=false`). Any
  custom selection bypasses the backend.
- Connect timeout is 5s, read timeout 120s. On failure it logs and
  falls back to the local renderer. Don't change this without
  confirming the local path still passes tests.
- `preparePptPreview` in `ToolViewModel` does a full `convertPptToPdf`
  pass just to render a preview. It's correct but slow for large
  decks — no progress callback is plumbed through.

`convertPdfToPpt` is purely local: it extracts text with
`PDFTextStripper` (or ML Kit OCR if `runOcr=true`) and writes a
hand-built OPC zip (`.pptx`/`.otp`). It has no backend.

## Tests

- Stack: JUnit 4, Robolectric 4.12, Mockito-Kotlin 5.2,
  `kotlinx-coroutines-test` 1.10. Coroutine main is replaced via
  `MainDispatcherRule` (uses `UnconfinedTestDispatcher`).
- Test sources live in `app/src/test/java/com/example/pdftools/`.
  Run-time Android resources are enabled
  (`testOptions.unitTests.isIncludeAndroidResources = true`), so
  Robolectric can resolve app strings/themes.
- Test files: `PdfProcessorTest` (heavy — covers every processor
  function), `ToolViewModelTest`, `FavoritesRepositoryTest`,
  `RecentFilesRepositoryTest`, `ToolRepositoryTest`,
  `PdfPreviewRepositoryTest`, `UserPreferencesRepositoryTest`,
  `OnboardingPreferenceTest`, `MainScreenViewModelTest`.
- **Pre-existing failure:** `ToolViewModelTest.processCompressPdfMapsQualityCorrectly`
  asserts a `quality: Int` argument, but the production
  `compress_pdf` path now reads `preferences.compressionQuality` (an
  Int from `UserPreferences`) and `c.quality` from a now-renamed
  config field. The test is stale; not caused by typical changes. Do
  not "fix" it by hacking the production type without also updating
  the production caller.

## Repo hygiene

- `.gitignore` excludes root-level `*.pptx`, `*.ppt`, `*.pdf`, and
  `PROJECT_OUTLINE.txt` so local scratch doesn't sneak into commits.
  If you legitimately need a sample PDF in the repo, put it under
  `app/src/test/resources/` or `app/src/androidTest/resources/`.
- `backend/pptx-converter/app/__pycache__/` is gitignored; never
  commit `*.pyc`.
- `local.properties` is **not** in the repo (gitignored). It must
  contain `sdk.dir=...` for the build to work.
- Three drawer drawables (`onboarding_customize.jpg`,
  `onboarding_privacy.jpg`, `onboarding_tools.jpg`) were removed; the
  onboarding screen now renders vector/code illustrations.

## Gotchas

- `compileSdk` and `targetSdk` are 36, `minSdk` 26.
- `buildConfig = false` in `app/build.gradle.kts` — `BuildConfig.*`
  constants are not generated. Don't reference them.
- Room and Hilt use **KSP** (not kapt). The KSP plugin is wired in
  `app/build.gradle.kts` and `gradle/libs.versions.toml`.
- Apache POI is included with two explicit excludes
  (`org.apache.xmlgraphics`, `xml-apis`) to keep the APK small. Don't
  drop those excludes.
- Compose BOM `2026.03.01` is pinned in `libs.versions.toml`. Don't
  override individual Compose library versions — let the BOM resolve
  them.
- Navigation 3 is the active navigation library
  (`androidx.navigation3:runtime` + `ui`). Don't add `navigation`
  fragments; they don't compose.
- `PdfToolsApplication.onCreate` calls `PDFBoxResourceLoader.init(this)`
  before any PDF work — required for `tom-roush:pdfbox-android`. Don't
  reorder.
- Cache cleanup runs on app start: files older than 7 days under
  `cacheDir` are deleted. Don't write important user files there.
- `tool.category.accentColor` / `darkAccentColor` /
  `containerColor` / `darkContainerColor` are defined per category in
  `data/ToolCategory.kt`. Add new categories there, not inline.
- `usesCleartextTraffic` is required for the local PPT backend. Don't
  remove it without also removing the HTTP call.
- `local.properties` is required (Android SDK path).
