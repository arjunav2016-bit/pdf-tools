# PDF Tools

PDF Tools is a premium, offline-first Android app for common PDF workflows:
merge, split, convert, compress, secure, edit, scan, OCR, and more.

The app is built with Kotlin, Jetpack Compose, Material 3, Hilt, Room,
DataStore, Coroutines, and Navigation 3. PDF processing runs on-device; there is
no PPTX conversion backend or cloud upload path.

## Tool Catalog

| Category | Tools |
|---|---|
| Organize | Merge PDF, Split PDF, Remove Pages, Extract Pages, Reorder Pages, Scan to PDF |
| Optimize | Compress PDF, Repair PDF, OCR PDF |
| Convert to PDF | JPG to PDF, Word to PDF, PowerPoint to PDF, Excel to PDF, HTML to PDF |
| Convert from PDF | PDF to JPG, PDF to Word, PDF to PowerPoint, PDF to Excel, PDF/A Conversion |
| Edit | Rotate PDF, Add Page Numbers, Add Watermark, Crop PDF, Edit PDF, PDF Forms |
| Security | Unlock PDF, Protect PDF, Sign PDF, Redact PDF, Compare PDF |

## Architecture

```text
com.example.pdftools
├── data/
│   ├── PdfProcessor.kt          # Facade delegating to category processors
│   ├── processors/
│   │   ├── ConvertProcessor     # Format conversions, including local PPT rendering
│   │   ├── EditProcessor        # Text overlay, signatures, watermarks, numbering
│   │   ├── OptimizeProcessor    # Image resampling and compression
│   │   ├── OrganizeProcessor    # Merge, split, remove, extract, reorder
│   │   └── SecurityProcessor    # Encrypt, decrypt, redact, compare
│   ├── ToolRepository.kt        # Tool catalog with i18n resolution
│   ├── ToolCategory.kt          # Theme-aware category accent colors
│   └── db/                      # Room database for favorites and recents
├── ui/
│   ├── screens/                 # Compose screens and tool flows
│   ├── viewmodels/              # MVVM state holders
│   └── theme/                   # Material 3 light/dark theme
└── utils/                       # Shared helpers
```

Key design decisions:

- MVVM plus a `PdfProcessor` facade keeps UI state separate from processing.
- `WindowSizeClass` adapts the shell between bottom navigation and navigation rail.
- PDF work uses on-device libraries: PDFBox Android, Apache POI, and ML Kit.
- PPT to PDF is local-only and uses Apache POI plus PDFBox vector/text rendering.
- Navigation uses `androidx.navigation3`, not legacy fragments.

## Requirements

- Android Studio or Android SDK with Gradle support.
- JDK 17. The repo pins `org.gradle.java.home` in `gradle.properties`.
- Android SDK API 36 for compile/target, API 26 minimum.
- `local.properties` at the repo root with your SDK path:

```properties
sdk.dir=C:\\Users\\your-user\\AppData\\Local\\Android\\Sdk
```

## Build and Test

Use the Gradle wrapper from the repo root.

```bash
# Build debug APK
./gradlew :app:assembleDebug

# Run all unit tests
./gradlew :app:testDebugUnitTest

# Run a specific test class
./gradlew :app:testDebugUnitTest --tests "com.example.pdftools.PdfProcessorTest"

# Run a specific test method
./gradlew :app:testDebugUnitTest --tests "com.example.pdftools.PdfProcessorTest.testConvertPptToPdf"
```

There are no configured `lint`, `detekt`, `ktlint`, or GitHub Actions checks.

## Project Structure

```text
pdf-tools/
├── app/                        # Main Android application module
│   ├── src/main/               # Production source and resources
│   └── src/test/               # Unit tests
├── print-helper/               # Local helper module
├── stubs/                      # Stub module included by Gradle
├── gradle/
│   └── libs.versions.toml      # Version catalog
├── build.gradle.kts            # Root build script
├── settings.gradle.kts         # Module declarations
├── gradle.properties           # JDK path and Gradle config
└── AGENTS.md                   # Agent contribution guidance
```

## Theming Notes

The app uses Material 3 color schemes with full dark-mode support.

- Cards: `surfaceContainerHigh` or `surfaceContainer`.
- Borders and dividers: `outlineVariant` or `outline`.
- Primary text: `onSurface`.
- Secondary text: `onSurfaceVariant`.
- Inactive controls: `surfaceContainerHighest`.
- Selected state: `accentColor.copy(alpha = 0.15f)`.

Avoid hardcoded light colors in Compose UI. Tool config composables receive a
theme-aware `accentColor`; use it instead of hardcoding a brand color.

## Testing

The test suite uses JUnit 4, Robolectric 4.12, Mockito-Kotlin 5.2, and
`kotlinx-coroutines-test`. Runtime Android resources are enabled for unit tests.

Main test files:

- `PdfProcessorTest`
- `ToolViewModelTest`
- `FavoritesRepositoryTest`
- `RecentFilesRepositoryTest`
- `ToolRepositoryTest`
- `PdfPreviewRepositoryTest`
- `UserPreferencesRepositoryTest`
- `OnboardingPreferenceTest`
- `MainScreenViewModelTest`

## Gotchas

- `buildConfig = false`; do not reference `BuildConfig.*`.
- Room and Hilt use KSP, not kapt.
- Apache POI excludes `org.apache.xmlgraphics` and `xml-apis` to keep APK size down.
- Compose BOM `2026.03.01` is pinned; do not override individual Compose versions.
- `PDFBoxResourceLoader.init(this)` runs in `PdfToolsApplication.onCreate()` before PDF work.
- Cache cleanup deletes files older than 7 days under `cacheDir`.
- Keep sample PDFs under test resources, not at the repo root.

