# PDF Tools

PDF Tools is an offline-first Android app for common PDF workflows such as
merging, splitting, compressing, protecting, redacting, previewing, and
converting documents. The app is built with Kotlin, Jetpack Compose, Hilt,
Room, DataStore, Coroutines, and Navigation 3.

All PDF processing runs on-device by default. A sibling FastAPI service under
`backend/pptx-converter/` can optionally provide higher-fidelity PowerPoint to
PDF conversion through LibreOffice when running the Android emulator.

## Project Structure

- `app/` - Main Android application module, package `com.example.pdftools`.
- `print-helper/` - Local helper module used by the app.
- `stubs/` - Local stub module included by Gradle.
- `backend/pptx-converter/` - Optional Python FastAPI conversion service for
  PowerPoint files.
- `PROJECT_OUTLINE.txt` - Deeper architecture notes for contributors.

Key Android entry points:

- `MainActivity.kt` starts the app and renders `MainNavigation()`.
- `MainScreen.kt` owns the adaptive shell, using bottom navigation on mobile and
  a navigation rail on wider screens.
- `data/PdfProcessor.kt` is the main PDF processing facade.
- `ui/viewmodels/ToolViewModel.kt` owns per-tool processing state.
- `data/ToolRepository.kt` defines the tool catalog and string IDs.

## Requirements

- Android Studio or Android SDK with Gradle support.
- JDK 17. The repo pins `org.gradle.java.home` in `gradle.properties`; do not
  override it unless you are intentionally changing the local setup.
- `local.properties` with an Android SDK path, for example:

```properties
sdk.dir=C:\\Users\\your-user\\AppData\\Local\\Android\\Sdk
```

## Build and Test

Use the Gradle wrapper from the repo root.

```bash
./gradlew :app:assembleDebug
```

Run all debug unit tests:

```bash
./gradlew :app:testDebugUnitTest
```

Run one test class:

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.pdftools.PdfProcessorTest"
```

There are no configured `lint`, `detekt`, `ktlint`, or GitHub Actions checks.

Known test note: `ToolViewModelTest.processCompressPdfMapsQualityCorrectly` is
documented as a pre-existing stale failure around compression quality mapping.

## Optional PPTX Converter Backend

The Android app has a backend fast-path for default PowerPoint to PDF conversion
options. In the emulator, it calls:

```text
http://10.0.2.2:8080/convert/presentation-to-pdf
```

Run the backend with Docker:

```bash
cd backend/pptx-converter
docker build -t pptx-converter .
docker run --rm -p 8080:8080 pptx-converter
```

Or run it locally after installing LibreOffice:

```bash
cd backend/pptx-converter
python -m venv .venv
. .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8080
```

On a real Android device, `10.0.2.2` is unreachable, so the app falls back to
the local Apache POI renderer.

## Development Notes

- Keep UI colors theme-aware through `MaterialTheme.colorScheme`.
- Use category accent colors passed into tool config composables instead of
  hardcoded brand colors.
- Do not reference `BuildConfig.*`; `buildConfig = false` is configured.
- Room and Hilt use KSP, not kapt.
- Apache POI excludes `org.apache.xmlgraphics` and `xml-apis` to limit APK size.
- Do not remove `usesCleartextTraffic` unless the local HTTP PPT backend path is
  removed too.

