# AGENTS.md

Quick context for working in this repo. Keep entries short: only what an agent
would otherwise get wrong.

## Project

Single Android app module `:app` (`com.example.pdftools`).
Kotlin + Jetpack Compose, Hilt, Room, DataStore, Coroutines, Navigation 3
(alpha). Offline-first; all PDF work runs on-device.

`PROJECT_OUTLINE.txt` at the repo root has a deeper architecture tour. Treat it
as background reading; the code is the source of truth.

## Build & Test

Use the wrapper. JDK 17 toolchain is pinned in `gradle.properties` via
`org.gradle.java.home` (Windows-specific path). Do not override it.

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

No `lint`, `detekt`, `ktlint`, or `ktlintCheck` tasks are configured. Do not add
CI steps that depend on them. There is no GitHub Actions config and no
pre-commit hooks.

## Architecture

- Entry point: `MainActivity.kt` (Hilt `@AndroidEntryPoint`) ->
  `MainNavigation()` -> `MainScreen.kt` (adaptive: bottom bar on mobile,
  `NavigationRail` on tablet).
- PDF work goes through the `PdfProcessor` facade (`data/PdfProcessor.kt`),
  which delegates to per-category processors under `data/processors/`.
- Per-tool state and the processing state machine live in
  `ui/viewmodels/ToolViewModel.kt`. Tool configs are in
  `ui/viewmodels/ToolConfigStates.kt`.
- Tool routing is string-id based: `merge_pdf`, `split_pdf`, `compress_pdf`,
  `ppt_to_pdf`, `pdf_to_ppt`, `pdf_to_jpg`, `protect_pdf`, `redact_pdf`,
  `compare_pdf`, etc. The full list is in `data/ToolRepository.kt`.
- Each tool's Compose config UI is in `ui/screens/tools/*ToolConfigs.kt`.
- Result previews, success states, and post-processing UI live in
  `ui/screens/tools/ResultDisplayConfigs.kt`.
- `MainScreen` switches between bottom-bar and rail layouts based on Material 3
  `WindowSizeClass`.

## Theme & Dark Mode

`MaterialTheme.colorScheme` is the only source of truth for surfaces, outlines,
and text. The dark/light color schemes are in `theme/Color.kt` and
`theme/Theme.kt`.

When adding UI:

- Cards: `containerColor = MaterialTheme.colorScheme.surfaceContainerHigh` (or
  `surfaceContainer` for less-elevated). Never hardcode `Color.White` or
  `Color(0xFFF4F6F9)`.
- Borders/dividers: `outlineVariant` or `outline`.
- Body text: `onSurface` for primary, `onSurfaceVariant` for muted.
- Inactive slider/track: `surfaceContainerHighest`.
- Text field background: `surfaceContainerHigh`.
- Tool config UIs receive `accentColor: Color`; use it instead of hardcoding a
  brand color.
- Selected tinted background: `accentColor.copy(alpha = 0.15f)`.

Many existing tool configs still have hardcoded light colors. Fix them inline
when you touch the file.

## PPT Conversion

`convertPptToPdf` and `convertPdfToPpt` are local-only. PPT to PDF uses the
Apache POI + PDFBox renderer. PDF to PPT extracts text with `PDFTextStripper`
or ML Kit OCR and writes a hand-built OPC zip (`.pptx`/`.otp`).

`preparePptPreview` in `ToolViewModel` does a full `convertPptToPdf` pass just
to render a preview. It is correct but slow for large decks; no progress
callback is plumbed through.

## Tests

- Stack: JUnit 4, Robolectric 4.12, Mockito-Kotlin 5.2,
  `kotlinx-coroutines-test` 1.10. Coroutine main is replaced via
  `MainDispatcherRule` (uses `UnconfinedTestDispatcher`).
- Test sources live in `app/src/test/java/com/example/pdftools/`.
- Runtime Android resources are enabled
  (`testOptions.unitTests.isIncludeAndroidResources = true`).
- Test files: `PdfProcessorTest`, `ToolViewModelTest`,
  `FavoritesRepositoryTest`, `RecentFilesRepositoryTest`,
  `ToolRepositoryTest`, `PdfPreviewRepositoryTest`,
  `UserPreferencesRepositoryTest`, `OnboardingPreferenceTest`,
  `MainScreenViewModelTest`.

## Repo Hygiene

- `.gitignore` excludes root-level `*.pptx`, `*.ppt`, `*.pdf`, and
  `PROJECT_OUTLINE.txt` so local scratch does not sneak into commits.
- If you need a sample PDF in the repo, put it under `app/src/test/resources/`
  or `app/src/androidTest/resources/`.
- `local.properties` is not in the repo. It must contain `sdk.dir=...`.
- Three drawer drawables (`onboarding_customize.jpg`, `onboarding_privacy.jpg`,
  `onboarding_tools.jpg`) were removed; onboarding now renders vector/code
  illustrations.

## Gotchas

- `compileSdk` and `targetSdk` are 36, `minSdk` 26.
- `buildConfig = false` in `app/build.gradle.kts`; `BuildConfig.*` constants are
  not generated.
- Room and Hilt use KSP, not kapt.
- Apache POI is included with explicit excludes (`org.apache.xmlgraphics`,
  `xml-apis`) to keep the APK small. Do not drop them.
- Compose BOM `2026.03.01` is pinned in `libs.versions.toml`.
- Navigation 3 is the active navigation library. Do not add navigation fragments.
- `PdfToolsApplication.onCreate` calls `PDFBoxResourceLoader.init(this)` before
  any PDF work. Do not reorder it.
- Cache cleanup runs on app start: files older than 7 days under `cacheDir` are
  deleted. Do not write important user files there.
- `tool.category.accentColor`, `darkAccentColor`, `containerColor`, and
  `darkContainerColor` are defined per category in `data/ToolCategory.kt`.
- `local.properties` is required (Android SDK path).

