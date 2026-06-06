<p align="center">
  <h1 align="center">📄 PDF Tools</h1>
  <p align="center">
    A premium, offline-first Android app for every PDF workflow — merge, split, convert, compress, secure, edit, and more.
    <br />
    Built with Kotlin · Jetpack Compose · Material 3 · Hilt · Room · Coroutines
  </p>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white" alt="Platform" />
  <img src="https://img.shields.io/badge/Min%20SDK-26%20(Oreo)-6200EE" alt="Min SDK" />
  <img src="https://img.shields.io/badge/Target%20SDK-36-1976D2" alt="Target SDK" />
  <img src="https://img.shields.io/badge/Kotlin-2.3-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Compose%20BOM-2026.03-4285F4?logo=jetpackcompose&logoColor=white" alt="Compose" />
  <img src="https://img.shields.io/badge/License-Proprietary-EF5350" alt="License" />
</p>

---

## ✨ Overview

**PDF Tools** is a modern Android application that handles common PDF tasks entirely on-device. No cloud uploads, no subscriptions, no data leaves your phone. The app ships **30 tools** across six categories, an adaptive Material 3 interface (phone ↔ tablet), a 3-page onboarding flow, favorites, recents, and configurable themes.

An optional **FastAPI micro-service** (`backend/pptx-converter/`) can be spun up alongside the emulator for higher-fidelity PowerPoint → PDF conversion using LibreOffice headless.

---

## 🛠️ Tool Catalog

| Category | Tools |
|---|---|
| **Organize** | Merge PDF · Split PDF · Remove Pages · Extract Pages · Reorder Pages · Scan to PDF |
| **Optimize** | Compress PDF · Repair PDF · OCR PDF |
| **Convert → PDF** | JPG to PDF · Word to PDF · PowerPoint to PDF · Excel to PDF · HTML to PDF |
| **Convert ← PDF** | PDF to JPG · PDF to Word · PDF to PowerPoint · PDF to Excel · PDF/A Conversion |
| **Edit** | Rotate PDF · Add Page Numbers · Add Watermark · Crop PDF · Edit PDF · PDF Forms |
| **Security** | Unlock PDF · Protect PDF · Sign PDF · Redact PDF · Compare PDF |

---

## 🏗️ Architecture

```
com.example.pdftools
├── data/                        # Data layer
│   ├── PdfProcessor.kt          # Facade → delegates to category processors
│   ├── processors/
│   │   ├── ConvertProcessor     # Format conversions (JPG, Word, PPT, Excel, HTML)
│   │   ├── EditProcessor        # Text overlay, signatures, watermarks, numbering
│   │   ├── OptimizeProcessor    # Image resampling & compression
│   │   ├── OrganizeProcessor    # Merge, split, remove, extract, reorder
│   │   └── SecurityProcessor    # Encrypt, decrypt, redact, compare
│   ├── ToolRepository.kt        # Tool catalog with i18n resolution
│   ├── ToolCategory.kt          # Six categories with theme-aware accent colors
│   └── db/                      # Room database (Favorites, Recents)
│
├── ui/
│   ├── screens/
│   │   ├── MainScreen.kt        # Adaptive shell (BottomBar ↔ NavigationRail)
│   │   ├── HomeScreen.kt        # Search, parallax header, category grids
│   │   ├── ToolScreen.kt        # File picker → config → process → result
│   │   └── tools/               # Per-tool config & result composables
│   ├── viewmodels/
│   │   └── ToolViewModel.kt     # Processing state machine with cancellation
│   └── theme/                   # Material 3 light/dark color schemes, Outfit typography
│
└── utils/                       # Helpers (page-range parsing, etc.)
```

**Key design decisions:**

- **MVVM + Facade pattern** — ViewModels observe repository Flows; `PdfProcessor` dispatches to specialized processors by category.
- **Adaptive layout** — `WindowSizeClass` switches between bottom bar and navigation rail, and reconfigures tool grids (2–4 columns).
- **Offline-first** — All PDF processing uses on-device libraries (PDFBox Android, Apache POI, ML Kit). No network calls required.
- **On-device OCR** — ML Kit Text Recognition supports Latin, Chinese, Devanagari, Japanese, and Korean scripts.
- **Camera scanning** — ML Kit Document Scanner provides edge detection and perspective correction.
- **Navigation 3** — Uses the latest `androidx.navigation3` alpha APIs (not legacy fragments).

---

## 📋 Requirements

| Requirement | Version |
|---|---|
| Android Studio | Latest stable (with AGP 9.0+) |
| JDK | **17** (pinned via `org.gradle.java.home` in `gradle.properties`) |
| Android SDK | API 36 (compile & target), API 26 (minimum) |

Create a `local.properties` file in the repo root with your SDK path:

```properties
sdk.dir=C:\\Users\\your-user\\AppData\\Local\\Android\\Sdk
```

---

## 🔨 Build & Test

Always use the Gradle wrapper from the repo root.

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

> [!NOTE]
> There are no `lint`, `detekt`, `ktlint`, or CI pipeline configurations in this repo.

---

## 🔌 Optional: PPTX Converter Backend

The app includes a backend fast-path for default PowerPoint → PDF settings. In the emulator, it calls `http://10.0.2.2:8080/convert/presentation-to-pdf`. On a real device this address is unreachable and the app silently falls back to the local Apache POI renderer.

### Docker (recommended)

```bash
cd backend/pptx-converter
docker build -t pptx-converter .
docker run --rm -p 8080:8080 pptx-converter
```

### Local (requires LibreOffice)

```bash
cd backend/pptx-converter
python -m venv .venv
source .venv/bin/activate        # Windows: .venv\Scripts\activate
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8080
```

On Windows, set `LIBREOFFICE_PATH` if `soffice` is not on `PATH`:

```powershell
$env:LIBREOFFICE_PATH = "C:\Program Files\LibreOffice\program\soffice.exe"
```

### API Reference

| Endpoint | Method | Description |
|---|---|---|
| `/health` | GET | Health check |
| `/convert/presentation-to-pdf` | POST | Convert `.ppt` / `.pptx` / `.odp` → PDF |

```bash
# Health check
curl http://localhost:8080/health

# Convert a file
curl -F "file=@presentation.pptx" http://localhost:8080/convert/presentation-to-pdf -o output.pdf
```

---

## 🎨 Theming & UI Guidelines

The app uses **Material 3 dynamic color schemes** with full dark mode support.

| Element | Token |
|---|---|
| Card backgrounds | `surfaceContainerHigh` / `surfaceContainer` |
| Borders & dividers | `outlineVariant` / `outline` |
| Primary text | `onSurface` |
| Secondary text | `onSurfaceVariant` |
| Inactive controls | `surfaceContainerHighest` |
| Text field fill | `surfaceContainerHigh` |
| Selected state | `accentColor.copy(alpha = 0.15f)` |

> [!IMPORTANT]
> Never hardcode colors like `Color.White` or hex literals — always reference `MaterialTheme.colorScheme` tokens. Tool config composables receive a theme-aware `accentColor` parameter.

Typography uses [**Outfit**](https://fonts.google.com/specimen/Outfit) from Google Fonts.

---

## 📂 Project Structure

```
pdf-tools/
├── app/                        # Main Android application module
│   ├── src/main/               # Production source & resources
│   └── src/test/               # Unit tests (JUnit 4, Robolectric, Mockito-Kotlin)
├── print-helper/               # Local helper module
├── stubs/                      # Stub module included by Gradle
├── backend/
│   └── pptx-converter/         # Python FastAPI service (LibreOffice)
├── gradle/
│   └── libs.versions.toml      # Version catalog
├── build.gradle.kts            # Root build script
├── settings.gradle.kts         # Module declarations
├── gradle.properties           # JDK path & Gradle config
└── AGENTS.md                   # AI agent contribution guidelines
```

---

## 🧪 Testing

The test suite uses **JUnit 4**, **Robolectric 4.12**, **Mockito-Kotlin 5.2**, and `kotlinx-coroutines-test`. Tests are optimized for Windows environments with safe temp directories and file-locking guards.

Test files cover:
- `PdfProcessorTest` — All processor functions across every category
- `ToolViewModelTest` — Processing state machine transitions
- `FavoritesRepositoryTest` / `RecentFilesRepositoryTest` — Room DAO operations
- `ToolRepositoryTest` — Tool catalog integrity
- `PdfPreviewRepositoryTest` — Preview bitmap generation
- `UserPreferencesRepositoryTest` / `OnboardingPreferenceTest` — DataStore persistence
- `MainScreenViewModelTest` — Navigation and screen state

---

## ⚠️ Gotchas

- **`buildConfig = false`** — `BuildConfig.*` constants are not generated. Don't reference them.
- **KSP, not kapt** — Room and Hilt annotation processing uses KSP.
- **Apache POI excludes** — `org.apache.xmlgraphics` and `xml-apis` are excluded to reduce APK size. Don't remove them.
- **`usesCleartextTraffic`** — Required for the local HTTP PPT backend. Don't remove without also removing the HTTP call.
- **Navigation 3** — The app uses `androidx.navigation3` (alpha). Don't add legacy navigation fragment dependencies.
- **PDFBox init** — `PDFBoxResourceLoader.init(this)` runs in `Application.onCreate()` before any PDF work. Don't reorder.
- **Cache cleanup** — Files older than 7 days under `cacheDir` are deleted on app start.
- **Compose BOM pinned** — Don't override individual Compose library versions; let the BOM resolve them.

---

## 🤝 Contributing

1. Read [`AGENTS.md`](AGENTS.md) for AI-agent-specific contribution rules.
2. Keep all UI colors theme-aware — use `MaterialTheme.colorScheme` tokens and the `accentColor` parameter.
3. Run `./gradlew :app:testDebugUnitTest` before submitting changes.
4. Place test resources under `app/src/test/resources/`.

---

<p align="center">
  <sub>Built with ❤️ using Kotlin and Jetpack Compose</sub>
</p>
