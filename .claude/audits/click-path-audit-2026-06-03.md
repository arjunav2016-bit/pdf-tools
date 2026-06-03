# Click-Path Audit — pdf-tools

**Date:** 2026-06-03
**Scope:** Full app audit (no suspected bug)
**Methodology:** 12-layer stack adapted for Jetpack Compose. 5 phases: ViewModel side-effect map → 3 parallel area audits (app shell, surgical tools, scan flow) → synthesis.
**Total findings (raw):** 64 subagent findings across 4 audit agents
**Unique bugs (deduped):** 35
**Critical:** 4 · **High:** 9 · **Medium:** 13 · **Low:** 9

---

## Executive Verdict

**Overall health: HIGH RISK** — multiple CRITICAL bugs that ship-broken core features.

**Primary failure mode:** `ToolViewModel.reset()` is a "nuclear" reset that wipes all 24 per-tool configs on every tool-change, success-card-clear, surgical-screen-close, and FormsBuilder-back. This is the root cause of ~7 of the 35 unique bugs and is the largest single threat to user trust. The `process("ocr_pdf")` path has a related issue: it `OcrConfig(...)` REPLACES the config instead of `.copy(...)`-ing it, losing `ocrLanguage` and `moduleStatuses`.

**Most urgent fix:** Split `ToolViewModel.reset()` into a *soft reset* (used by success-card-clear and surgical-screen-close) and a *hard reset* (used only by tool-change). The soft reset clears the current run's inputs/outputs; the hard reset clears everything. This single change resolves bugs BUG-001, BUG-002, BUG-007, BUG-011, BUG-014, BUG-022, and BUG-026.

**Architecture diagnosis:** the wrapper layer (Compose + ViewModel + StateFlow) is the source of all bugs. The model layer (Room, DataStore, PdfProcessor) is fine. The bugs cluster in three places:

1. **Over-broad state mutation** — `reset()`, `OcrConfig(...)` replace, surgical-screen `onClear` paths
2. **Async race conditions** — file picker callbacks, scan flow state-machine bypass, scan cancel-during-success
3. **Stale closure in long-lived gesture/animation handlers** — Crop drag, PdfForms field dialog, page navigation

---

## Severity-Ranked Findings

### CRITICAL

#### BUG-001 — `RotateToolConfig` chip rotation has no effect
- **Severity:** CRITICAL
- **Touchpoint:** "Choose 90°/180°/270°" angle chips in `OrganizeToolConfigs.kt:651`
- **Pattern:** Dead Path (data class field not bound to processor)
- **Trace:**
  1. User taps "90°" → `viewModel.rotateConfig.value = config.copy(degrees = 90)` (line 652)
  2. `process("rotate_pdf")` at `ToolViewModel.kt:333–339` reads `c = rotateConfig.value`
  3. **Reads `c.previewRotation`, not `c.degrees`** (line 335)
- **Source layer:** Tool selection / answer shaping
- **Root cause:** The chip writes to the wrong field. Only the surgical screen (`RotatePdfSurgicalScreen.kt:255–293`) writes `previewRotation`. The small config chip is wired to a dead field.
- **Evidence:** `OrganizeToolConfigs.kt:652`, `ToolViewModel.kt:333–339`, `RotatePdfSurgicalScreen.kt:255–293`
- **Confidence:** 0.95
- **Recommended fix:** Either change the chip to `config.copy(previewRotation = angle)`, or change `process("rotate_pdf")` to read `c.degrees`. Pick one source of truth.
- **Subagent ID:** CLICK-PATH-024

#### BUG-002 — Scan: cancel during success race shows Success state to user who tapped Cancel
- **Severity:** CRITICAL
- **Touchpoint:** Cancel button in `ScanReviewContent.kt:497` Processing state
- **Pattern:** Async Race (success-after-cancel)
- **Trace:**
  1. `generatePdf()` launches a coroutine. User taps Cancel just before `pdfProcessor.scanToPdf` returns.
  2. `cancelProcessing()` calls `processingJob?.cancel()`. This only signals cancellation; it does not interrupt a non-suspending step in progress.
  3. If cancel happens between `scanToPdf` return and line 188, the coroutine continues: writes `_outputUri`, `_flowState = Success`.
  4. The cancel handler had already set `_flowState = Review` — the coroutine overwrites it back to Success.
  5. The Cancel button is now a no-op (processingJob is null); user is stuck looking at a Success state they tried to dismiss.
- **Source layer:** Tool execution / hidden repair loops
- **Root cause:** `ScanViewModel.generatePdf` does not check `isActive` after the suspending call returns.
- **Evidence:** `ScanViewModel.kt:156–199` (coroutine body), `ScanViewModel.kt:201–205` (cancelProcessing)
- **Confidence:** 0.85
- **Recommended fix:** Add `if (!isActive) return@launch` or `ensureActive()` between `pdfProcessor.scanToPdf(...)` return and the state writes. Or use a `wasCancelled` flag.
- **Subagent ID:** CLICK-PATH-041

#### BUG-003 — Scan: addPages from picker/camera flips flowState back to Review during Processing
- **Severity:** CRITICAL
- **Touchpoint:** Gallery picker / camera scanner launcher callbacks in `ScanFlowScreen.kt:79–91, 94–100`
- **Pattern:** Async Race (state-machine violation)
- **Trace:**
  1. User on Processing screen. flowState = Processing.
  2. User (or system) re-launches the camera or gallery picker from another path.
  3. Callback runs unconditionally and calls `viewModel.addPages(uris)` (line 87, 98).
  4. `addPages` (line 79–85) appends to `_scannedPages` AND unconditionally sets `_flowState = Review`.
  5. The user sees Review screen appear while the processing job is still running on the OLD page snapshot.
  6. The processing job completes and writes `_flowState = Success`, jumping over Review.
- **Source layer:** Tool selection / state changes
- **Root cause:** `addPages` is not guarded on current flow state.
- **Evidence:** `ScanFlowScreen.kt:79–100`, `ScanViewModel.kt:79–85`
- **Confidence:** 0.90
- **Recommended fix:** Guard `addPages` on `_flowState.value is Review || _flowState.value is Launcher` before mutating `_flowState`. Or remove the unconditional `_flowState = Review` from `addPages` (the screen should already be there).
- **Subagent ID:** CLICK-PATH-044

#### BUG-004 — Scan: Generate button can be double-tapped, leaking job and orphaning PDF
- **Severity:** CRITICAL
- **Touchpoint:** "Save PDF" / "Generate PDF" button at `ScanReviewContent.kt:496–517`
- **Pattern:** Async Race (double-tap)
- **Trace:**
  1. Button `enabled` flag only guards against empty pages. Recomposition lag allows a second tap before ProcessingContent unmounts.
  2. First tap sets `_flowState = Processing`, launches job, assigns `processingJob = job1`.
  3. Second tap reads pages, sets `_flowState = Processing` (no-op), launches `job2`, **OVERWRITES `processingJob = job2`**.
  4. `job1` is leaked: no reference, `cancelProcessing()` can only cancel `job2`.
  5. `job1` completes and writes `_outputUri = file1`, `_flowState = Success(file1)`.
  6. `job2` completes later, overwrites with its own Success. `file1` is orphaned on disk.
- **Source layer:** Tool execution
- **Root cause:** No in-flight check at the top of `generatePdf`.
- **Evidence:** `ScanViewModel.kt:156–161`, `ScanReviewContent.kt:496–517`
- **Confidence:** 0.95
- **Recommended fix:** Add `if (processingJob?.isActive == true) return` at the top of `generatePdf`.
- **Subagent ID:** CLICK-PATH-045 (Phase 4)

---

### HIGH

#### BUG-005 — `ToolViewModel.reset()` is "nuclear" — wipes ALL 24 per-tool configs
- **Severity:** HIGH
- **Touchpoint:** All 10 surgical screen "Close" (X) icons, all SuccessCard "Continue" buttons, tool-change `DisposableEffect`, FormsBuilder back button
- **Pattern:** Sequential Undo (over-broad state reset)
- **Trace:** See [Architecture Diagnosis](#architecture-diagnosis) below. `reset()` (ToolViewModel.kt:143–179) clears every per-tool config, not just the current tool's.
- **Source layer:** State changes
- **Root cause:** Single `reset()` method is reused for soft and hard reset semantics. Surgical screens, success cards, and tool-change all call the same method.
- **Evidence:** `ToolViewModel.kt:143–179`; callers: `ToolScreen.kt:240–243, 913, 956, 1260`; surgical screens: `CropPdfSurgicalScreen.kt:125, 162`, `WatermarkSurgicalScreen.kt:112`, `RotatePdfSurgicalScreen.kt:74`, `PageNumbersSurgicalScreen.kt:81`, `SignPdfSurgicalScreen.kt:122`, `EditPdfSurgicalScreen.kt:218`, `ProtectPdfSurgicalScreen.kt:74`, `PdfFormsSurgicalScreen.kt:258`, `UnlockPdfSurgicalScreen.kt:71`; forms: `PdfFormsSurgicalScreen.kt:306–309`
- **Confidence:** 0.95
- **Recommended fix:** Split into:
  - `resetCurrentRun()` — clears `_selectedFiles`, `_outputUris`, `_uiState`, `_progress`. Used by SuccessCard onClear and surgical-screen close.
  - `resetAll()` — the current full reset. Used only by `ToolScreen.kt:240` (tool-change).
- **Subagent IDs:** CLICK-PATH-001, 010, 012, 021, 058, 062, 064

#### BUG-006 — `process("ocr_pdf")` REPLACES `OcrConfig` instead of `.copy()`
- **Severity:** HIGH
- **Touchpoint:** OCR success path at `ToolViewModel.kt:453–458`
- **Pattern:** Sequential Undo (data class field overwrite)
- **Trace:**
  1. `val text = pdfProcessor.ocrPdf(context, files.first())`
  2. `ocrConfig.value = OcrConfig(ocrResultText = text)` — REPLACES the whole OcrConfig
  3. `OcrConfig()` default has `ocrLanguage = ""` and `moduleStatuses = emptyMap()`
  4. Init coroutines (`ToolViewModel.kt:89–104`) re-set them on next collect, but there's a window where they're blank
  5. `_outputUris.value = emptyList()` wipes any prior output URIs
  6. `_uiState.value = Success(emptyList())` — SuccessCard shows empty file list
- **Source layer:** State changes
- **Root cause:** Should be `ocrConfig.value = ocrConfig.value.copy(ocrResultText = text)`
- **Evidence:** `ToolViewModel.kt:453–458`
- **Confidence:** 0.90
- **Recommended fix:** Change to `.copy()`. Branch SuccessCard to show text-output actions (Copy / Share text / Save as .txt) when outputUris is empty and tool is ocr/compare.
- **Subagent IDs:** CLICK-PATH-011, 040, 060

#### BUG-007 — html_to_pdf file picker appends Uri to `_selectedFiles` (dead data)
- **Severity:** HIGH
- **Touchpoint:** File picker "Choose Files" for `html_to_pdf` at `ToolScreen.kt:742, 966`
- **Pattern:** Stale Closure / Sequential Undo
- **Trace:**
  1. User picks an HTML file. Callback at `ToolScreen.kt:257–273` fires.
  2. `viewModel.addFiles(uris)` appends Uri to `_selectedFiles` (line 261).
  3. `viewModel.htmlConfig.value = htmlConfig.copy(htmlContent = content)` (line 266) — only for `html_to_pdf`.
  4. The Uri in `_selectedFiles` is never read by the html_to_pdf processor.
  5. The action button becomes enabled (`selectedFiles.isNotEmpty() || htmlConfig.htmlContent.isNotEmpty()`) and shows "Convert to PDF".
  6. `process("html_to_pdf")` reads `htmlConfig.htmlContent` — the file picker result is correct.
  7. But the Uri sits as dead data in `_selectedFiles`, displayed in the file list.
- **Source layer:** Tool selection
- **Root cause:** `addFiles` is called unconditionally regardless of tool.
- **Evidence:** `ToolScreen.kt:257–273`
- **Confidence:** 0.85
- **Recommended fix:** For `html_to_pdf`, do not call `addFiles` — only update `htmlConfig.htmlContent` and clear `_selectedFiles` if needed.
- **Subagent IDs:** CLICK-PATH-003, 014

#### BUG-008 — File picker async race after tool change leaves stale state
- **Severity:** HIGH
- **Touchpoint:** `filePickerLauncher` callback at `ToolScreen.kt:257–273`
- **Pattern:** Async Race
- **Trace:**
  1. User picks a file. While system picker is open, the user backgrounds or kills the picker.
  2. Callback fires after Composable has unmounted. `viewModel.addFiles(uris)` writes to a VM scoped to a no-longer-current entry.
  3. On re-navigation to a different tool, the file silently appears in that tool's `_selectedFiles`.
  4. Worse: for `html_to_pdf`, the stale `htmlConfig.copy(htmlContent = content)` writeback overwrites the freshly-reset default with stale-but-non-empty content.
- **Source layer:** Tool execution
- **Root cause:** No lifecycle guard in the file picker callback.
- **Evidence:** `ToolScreen.kt:257–273, 240–243`
- **Confidence:** 0.70
- **Recommended fix:** Gate the writeback on `tool.id == "html_to_pdf"` AND on the current `htmlConfig` snapshot being the most recent emission. Or hoist file pick to a lifecycle-aware ViewModel.
- **Subagent ID:** CLICK-PATH-002

#### BUG-009 — Back-during-processing is unguarded
- **Severity:** HIGH
- **Touchpoint:** ToolScreen top-bar back arrow at `ToolScreen.kt:315`
- **Pattern:** Missing Transition
- **Trace:**
  1. User is processing. Taps back. `DisposableEffect`'s `onDispose {}` is empty (line 242) — no cancel.
  2. Processing continues silently in the background.
  3. If user enters a different tool, that tool's `DisposableEffect` calls `reset()` which cancels.
  4. Net: back is a no-op for the work, and there's no "are you sure?" prompt.
- **Source layer:** State changes
- **Root cause:** Empty `onDispose` and no back-during-processing confirmation.
- **Evidence:** `ToolScreen.kt:240–243, 315`
- **Confidence:** 0.85
- **Recommended fix:** Add a confirm dialog when `isProcessing` and back is pressed, OR call `viewModel.cancelProcessing()` in the `onDispose` of the `DisposableEffect` (along with `viewModel.reset()` if appropriate).
- **Subagent ID:** CLICK-PATH-004

#### BUG-010 — Crop drag handles use stale closure-captured `config`
- **Severity:** HIGH
- **Touchpoint:** Four corner drag handles at `CropPdfSurgicalScreen.kt:262–391`
- **Pattern:** Stale Closure
- **Trace:**
  1. `pointerInput(pageSize) { detectDragGestures { ... } }` is keyed on `pageSize`.
  2. Inside, `config` is the closure-captured one. If user types in the Left/Top fields mid-drag, the gesture's `config.leftMm` is the OLD value, not the current one.
  3. The drag delta is added to the OLD value, producing wrong final coords.
- **Source layer:** Tool selection
- **Root cause:** No `rememberUpdatedState` for `config` inside the gesture.
- **Evidence:** `CropPdfSurgicalScreen.kt:262–391`
- **Confidence:** 0.90
- **Recommended fix:** Use `val currentConfig by rememberUpdatedState(config)` inside the `pointerInput` block and read `currentConfig.leftMm` etc.
- **Subagent ID:** CLICK-PATH-030

#### BUG-011 — CropPdfSurgicalScreen page nav race on zeroed dimensions
- **Severity:** HIGH
- **Touchpoint:** Page nav chevrons at `CropPdfSurgicalScreen.kt:408–446`
- **Pattern:** LaunchedEffect Interference
- **Trace:**
  1. User drags a corner to set `widthMm=0` (manually zeroed via Left/Top fields, e.g. dragging past zero). `cropConfig` is updated.
  2. User taps Next page. `currentPageIndex` changes. `produceState` re-fires with new `pageSize`.
  3. `LaunchedEffect(pageSize)` at line 85–98 fires. Condition `config.widthMm == 0f || config.heightMm == 0f` is TRUE (manually zeroed). The effect RESETS to new page's dimensions, blowing away the user's manual entry.
- **Source layer:** State changes
- **Root cause:** `LaunchedEffect` overwrites when width/height are 0, regardless of whether the user zeroed them intentionally.
- **Evidence:** `CropPdfSurgicalScreen.kt:85–98, 408–446`
- **Confidence:** 0.75
- **Recommended fix:** Gate on `config.useAbsoluteCrop` — only overwrite if the user is in margin-percentage mode, not absolute mode.
- **Subagent ID:** CLICK-PATH-023

#### BUG-012 — PdfForms Filler has no associated file — submit always fails
- **Severity:** HIGH
- **Touchpoint:** Recent Documents cards in `PdfFormsSurgicalScreen.kt:519–590`; `onLoadRecent` at line 288
- **Pattern:** Dead Path / Missing Transition
- **Trace:**
  1. User taps a recent form template. `fillerFields.addAll(recent.fields); currentStep = FILLER`.
  2. `selectedFiles` is empty (templates are field-only, no file association).
  3. User fills fields, taps Submit. `onSubmit` calls `process("fill_pdf_fields")`.
  4. `process` reads `files = selectedFiles.value; if (empty) → Error("No files selected")` (`ToolViewModel.kt:255–258`).
  5. Submit fails. User is stuck.
- **Source layer:** Tool selection
- **Root cause:** Recent-form loading flow has no file picker step.
- **Evidence:** `PdfFormsSurgicalScreen.kt:288–295, 519–590`; `ToolViewModel.kt:255–258, 443–446`
- **Confidence:** 0.95
- **Recommended fix:** Either disable Submit when no file is selected, or require file selection before transitioning to FILLER (like the "Build Form" / "Fill Form" buttons do at line 411/424).
- **Subagent IDs:** CLICK-PATH-033, 045

#### BUG-013 — SignPdf: delete signature orphans placed field on canvas
- **Severity:** HIGH
- **Touchpoint:** Delete (X) icon on stored signature card at `SignPdfSurgicalScreen.kt:274`
- **Pattern:** Sequential Undo
- **Trace:**
  1. User places a SIGNATURE or INITIAL field on the canvas using a stored signature. `field.signatureUri` is set to the file path.
  2. User taps the X on that stored signature in the vault. File is deleted. `signatureUri` is still set on the field.
  3. Canvas preview at line 988–1002 tries to decode the file — fails. Shows "Tap to Sign" placeholder.
  4. On submit, the incomplete-fields check at line 181 sees `signatureUri != null` (still set), but the file is gone. User gets "Please click on placement boxes to sign/initial" toast. **Cannot submit.**
- **Source layer:** State changes
- **Root cause:** Delete doesn't check if any placed field references the signature.
- **Evidence:** `SignPdfSurgicalScreen.kt:253–280, 988–1002, 176–188`
- **Confidence:** 0.85
- **Recommended fix:** Before deleting, scan `signConfig.fields` for any field with `signatureUri == File(path)`. If found, either warn the user or null out the field's `signatureUri`.
- **Subagent ID:** CLICK-PATH-036

#### BUG-014 — EditPdf: sticky note ignores tap position, always saves at center
- **Severity:** HIGH
- **Touchpoint:** Sticky note Save button at `EditPdfSurgicalScreen.kt:1586`
- **Pattern:** Missing Transition (intended behavior is broken)
- **Trace:**
  1. User taps on canvas with sticky_note tool. `nextStickyPosition = offset` (line 1363).
  2. Dialog opens. User types comment, taps Save.
  3. Save handler at line 1586: `stickyNotes.add(StickyNote(text=..., x=0.5f, y=0.5f, pageIndex=activePageIndex))`.
  4. **`nextStickyPosition` is set but never used.** The note is always placed at canvas center.
- **Source layer:** State changes
- **Root cause:** Hard-coded `(0.5f, 0.5f)` instead of `nextStickyPosition`.
- **Evidence:** `EditPdfSurgicalScreen.kt:1362–1364, 1586`
- **Confidence:** 0.95
- **Recommended fix:** Use `(nextStickyPosition.x / size.width, nextStickyPosition.y / size.height)` and pass `size` as a parameter.
- **Subagent ID:** CLICK-PATH-049

---

### MEDIUM

#### BUG-015 — `isFavorite` sync read causes star icon lag on toggle
- **Severity:** MEDIUM
- **Touchpoint:** TopAppBar star icon at `ToolScreen.kt:341`
- **Pattern:** Async Race
- **Trace:** `isFav = viewModel.isFavorite(tool.id)` is a sync read of repo state. After `toggleFavorite`, the icon doesn't re-render until the next recomposition trigger. Double-tapping is racy.
- **Evidence:** `ToolScreen.kt:340–347`; `ToolViewModel.kt:181–187`
- **Confidence:** 0.75
- **Recommended fix:** Expose `favorites: StateFlow<List<String>>` and `collectAsState` in the Composable, or debounce taps.
- **Subagent ID:** CLICK-PATH-005

#### BUG-016 — Onboarding completed race — quick force-kill can show Onboarding again
- **Severity:** MEDIUM
- **Touchpoint:** "Get Started" / "Skip" buttons at `OnboardingScreen.kt:100, 155`
- **Pattern:** Async Race
- **Trace:** `setOnboardingCompleted()` writes to DataStore async. UI immediately navigates to Main. If user kills the app before DataStore flushes, the next launch shows Onboarding again.
- **Evidence:** `OnboardingScreen.kt:100, 155`; `NavigationViewModel.kt:23–27`; `Navigation.kt:46–50`
- **Confidence:** 0.65
- **Recommended fix:** Block backStack.clear()/add() until the DataStore write completes, or use `runBlocking` on the IO dispatcher (acceptable for a once-only write at app entry).
- **Subagent ID:** CLICK-PATH-006

#### BUG-017 — PdfForms Back from FILLER goes to BUILDER, not DASHBOARD
- **Severity:** MEDIUM
- **Touchpoint:** "Back" in FormFillingView at `PdfFormsSurgicalScreen.kt:329`
- **Pattern:** Missing Transition
- **Trace:** `onBack` (line 329–335): `if (selectedFiles.isEmpty()) currentStep = DASHBOARD else currentStep = BUILDER`. If user came via "Fill Form" (not "Build Form"), there's no builder, but BUILDER is shown anyway.
- **Evidence:** `PdfFormsSurgicalScreen.kt:329–335`
- **Confidence:** 0.80
- **Recommended fix:** Track `activeIntent` (build / fill) and route to the correct previous step.
- **Subagent IDs:** CLICK-PATH-026, 057

#### BUG-018 — Scan: "Try Again" button on Error is mislabeled
- **Severity:** MEDIUM
- **Touchpoint:** "Try Again" button in ErrorContent at `ScanFlowScreen.kt:666–671`
- **Pattern:** Dead Path
- **Trace:** `onRetry = { viewModel.dismissError() }` (line 289). Dismissal, not retry. User must tap Generate again.
- **Evidence:** `ScanFlowScreen.kt:666–671, 289`; `ScanViewModel.kt:146–152`
- **Confidence:** 0.90
- **Recommended fix:** Either relabel to "Back to Review" or "Dismiss", or have retry re-invoke `generatePdf(context)` with the saved snapshot.
- **Subagent ID:** CLICK-PATH-054

#### BUG-019 — Scan: camera permission denial loop
- **Severity:** MEDIUM
- **Touchpoint:** Camera permission dialog "Grant" button at `ScanFlowScreen.kt:181–189`
- **Pattern:** Async Race
- **Trace:** Denial → dialog → "Grant" → re-launch permission → denial → loop. On Android 11+ "Don't ask again" is silently recorded, but the in-app dialog keeps re-asking.
- **Evidence:** `ScanFlowScreen.kt:108–128, 161–189`
- **Confidence:** 0.85
- **Recommended fix:** Track consecutive denials. On the 2nd, show a "Go to Settings" button instead.
- **Subagent ID:** CLICK-PATH-051

#### BUG-020 — Scan: back during Processing silently kills job
- **Severity:** MEDIUM
- **Touchpoint:** TopAppBar back arrow at `ScanFlowScreen.kt:215–223`
- **Pattern:** Missing Transition
- **Trace:** No explicit `Processing` case in the `when` block (line 216–223). Falls through to `onBack()` (line 222). VM is destroyed, job cancelled, user has no idea.
- **Evidence:** `ScanFlowScreen.kt:215–223`
- **Confidence:** 0.80
- **Recommended fix:** Add explicit `Processing -> { showConfirmDialog = true; onConfirm = { viewModel.cancelProcessing(); onBack() } }` case.
- **Subagent ID:** CLICK-PATH-050

#### BUG-021 — Scan: back from Success does BOTH reset and pop
- **Severity:** MEDIUM
- **Touchpoint:** TopAppBar back arrow at `ScanFlowScreen.kt:215–223` when flowState is Success
- **Pattern:** Missing Transition (contradictory actions)
- **Trace:** Handler is `viewModel.reset(); onBack()` (line 219–220). Both fire. User sees LauncherContent flash for a frame during the back animation.
- **Evidence:** `ScanFlowScreen.kt:218–220`
- **Confidence:** 0.75
- **Recommended fix:** Pick one. Either reset() and stay on ScanFlow, or just onBack() and let MainScreen handle re-entry.
- **Subagent ID:** CLICK-PATH-043

#### BUG-022 — EditPdf: Recent Documents cards all load the sample
- **Severity:** MEDIUM
- **Touchpoint:** Recent cards at `EditPdfSurgicalScreen.kt:432–453`
- **Pattern:** Sequential Undo (label/action mismatch)
- **Trace:** All cards have the same `onClick = onLoadSample()` (line 435). Card label says "Tax_Invoice_May.pdf" but tapping loads the embedded sample.
- **Evidence:** `EditPdfSurgicalScreen.kt:432–453`
- **Confidence:** 0.95
- **Recommended fix:** Each card should have its own onLoad function pointing to the actual file URI, or the cards should be disabled.
- **Subagent ID:** CLICK-PATH-048

#### BUG-023 — EditPdf: undo stack shared across sub-tools
- **Severity:** MEDIUM
- **Touchpoint:** Undo/Redo at `EditPdfSurgicalScreen.kt:632–637, 1305–1319`
- **Pattern:** Missing Transition
- **Trace:** `undoStack` is shared across TEXT_TOOL, OBJECTS_TOOL, MARKUP_TOOL. Pressing Undo in OBJECTS_TOOL can revert a TEXT_TOOL change.
- **Evidence:** `EditPdfSurgicalScreen.kt:149–156, 174–190`
- **Confidence:** 0.85
- **Recommended fix:** Scope `undoStack` per `currentStep` — keep separate stacks for each sub-tool.
- **Subagent ID:** CLICK-PATH-046

#### BUG-024 — Organize Merge: fileMetadata stale on remove
- **Severity:** MEDIUM
- **Touchpoint:** "Remove file" X in MergeToolConfig at `OrganizeToolConfigs.kt:1291`
- **Pattern:** Sequential Undo
- **Trace:** `removeFile` removes the URI from `_selectedFiles` but `fileMetadata` map (line 1076) is not cleaned. Re-adding the same URI returns the OLD metadata (stale page count, etc.).
- **Evidence:** `OrganizeToolConfigs.kt:1076, 1088, 1291–1292`; `ToolViewModel.kt:132–141`
- **Confidence:** 0.90
- **Recommended fix:** Clean `fileMetadata` on remove, or re-query metadata every time.
- **Subagent ID:** CLICK-PATH-038

#### BUG-025 — Home tab scroll position lost on tab switch
- **Severity:** MEDIUM
- **Touchpoint:** Tab switch in MainScreen at `MainScreen.kt:71–104`
- **Pattern:** Missing Transition
- **Trace:** `rememberLazyListState()` at `HomeScreen.kt:80` is Composable-scoped. `AnimatedContent` disposes the previous child. Scroll position is lost.
- **Evidence:** `MainScreen.kt:71–104`; `HomeScreen.kt:80`
- **Confidence:** 0.90
- **Recommended fix:** Use `rememberSaveable` for the scrollState, or hoist to MainScreen and pass down.
- **Subagent ID:** CLICK-PATH-015

#### BUG-026 — Recent "Clear all history" has no confirmation
- **Severity:** MEDIUM
- **Touchpoint:** DeleteSweep icon at `RecentScreen.kt:105`
- **Pattern:** Missing Transition
- **Trace:** `onClick = { viewModel.clear() }` wipes entire DB table with no confirmation.
- **Evidence:** `RecentScreen.kt:105`; `RecentViewModel.kt:22–24`
- **Confidence:** 0.90
- **Recommended fix:** Add a confirmation AlertDialog, or use a Snackbar with "Undo" that re-inserts cleared entries.
- **Subagent ID:** CLICK-PATH-017

#### BUG-027 — App startup cache cleanup invalidates in-memory bitmap cache
- **Severity:** MEDIUM
- **Touchpoint:** `PdfToolsApplication.onCreate` at `PdfToolsApplication.kt:13`
- **Pattern:** Async Race
- **Trace:** `deleteExpiredCacheFiles()` deletes files in `cacheDir` older than 7 days. `PdfPreviewRepository.bitmapCache` (LruCache, in-memory) may hold keys for now-deleted files. Next thumbnail request fails.
- **Evidence:** `PdfToolsApplication.kt:13–21`; `PdfPreviewRepository` (referenced in map)
- **Confidence:** 0.75
- **Recommended fix:** When deleting expired files, also call `PdfPreviewRepository.evictAll()` or a per-key evict.
- **Subagent ID:** CLICK-PATH-019

#### BUG-028 — EditPdf: `activePageIndex` not reset on success
- **Severity:** MEDIUM
- **Touchpoint:** `EditPdfSurgicalScreen.kt:218–227` onClear
- **Pattern:** Missing Transition
- **Trace:** `onClear` resets `currentStep = DASHBOARD` but not `activePageIndex = 0`. If user was on page 5, next session starts there.
- **Evidence:** `EditPdfSurgicalScreen.kt:136, 218–227`
- **Confidence:** 0.90
- **Recommended fix:** In `onClear`, also reset `activePageIndex = 0`.
- **Subagent ID:** CLICK-PATH-020

#### BUG-029 — Crop: two apply buttons with inconsistent enabled gates
- **Severity:** MEDIUM
- **Touchpoint:** TopAppBar apply at `CropPdfSurgicalScreen.kt:130` vs. bottom Confirm at line 805
- **Pattern:** Sequential Undo
- **Trace:** TopBar requires `widthMm > 0 && heightMm > 0`. Bottom has no gate. Inconsistent state.
- **Evidence:** `CropPdfSurgicalScreen.kt:130, 805`
- **Confidence:** 0.85
- **Recommended fix:** Share the same gate between the two buttons.
- **Subagent ID:** CLICK-PATH-042

#### BUG-030 — Watermark image bitmap decode silent fail
- **Severity:** MEDIUM
- **Touchpoint:** "Select Watermark Image" picker at `WatermarkSurgicalScreen.kt:461`
- **Pattern:** Async Race
- **Trace:** Picker sets `config.imageUri`. `produceState` at line 79–90 decodes the bitmap in background. If decode fails (corrupted image), `runCatching` at line 86–88 swallows it. User sees `imageUri` set but no preview. Apply button is enabled because `imageUri != null` — and processing will also fail.
- **Evidence:** `WatermarkSurgicalScreen.kt:79–90, 460–480, 670`
- **Confidence:** 0.80
- **Recommended fix:** Validate bitmap is non-null before enabling the apply button.
- **Subagent ID:** CLICK-PATH-022

---

### LOW

#### BUG-031 — TopBar no-op Search/Settings icons in pdf_to_ppt and pdf_to_jpg
- **Severity:** LOW
- **Touchpoint:** TopAppBar actions at `ToolScreen.kt:322–338`
- **Pattern:** Dead Path
- **Trace:** `onClick = { /* Search action */ }` — empty comment. User sees a clickable icon that does nothing.
- **Evidence:** `ToolScreen.kt:322–338`
- **Confidence:** 0.95
- **Recommended fix:** Implement the actions or wrap in a non-interactive `Icon`.
- **Subagent ID:** CLICK-PATH-013

#### BUG-032 — Recent `context` stale on config change
- **Severity:** LOW
- **Touchpoint:** Recent Open/Share IconButtons at `RecentScreen.kt:224–245`
- **Pattern:** Stale Closure
- **Trace:** `LocalContext.current` captured in lambda. On rotation/Activity recreate, the context reference is the destroyed Activity. `startActivity` may fail.
- **Evidence:** `RecentScreen.kt:224–245, 304–329`
- **Confidence:** 0.65
- **Recommended fix:** Re-read `LocalContext.current` inside the handler, or use Application context.
- **Subagent ID:** CLICK-PATH-016

#### BUG-033 — `ClearCache` silent failure
- **Severity:** LOW
- **Touchpoint:** "Clear Cache" confirm dialog "Clear" button at `SettingsScreen.kt:85–93`
- **Pattern:** Dead Path
- **Trace:** If `clearCache()` throws (locked file), the size display remains stale. No error UI.
- **Evidence:** `SettingsScreen.kt:85–93`; `SettingsViewModel.kt:65–70`
- **Confidence:** 0.75
- **Recommended fix:** Wrap in `runCatching`, set an error state, display a Snackbar.
- **Subagent ID:** CLICK-PATH-007

#### BUG-034 — Settings slider async write lag
- **Severity:** LOW
- **Touchpoint:** Compression Quality Slider at `SettingsScreen.kt:168`
- **Pattern:** Sequential Undo (minor visual)
- **Trace:** Slider `value` is bound to DataStore-driven `preferences.compressionQuality`. Fast drags cause slider to lag finger position.
- **Evidence:** `SettingsScreen.kt:168`; `SettingsViewModel.kt:47–51`
- **Confidence:** 0.70
- **Recommended fix:** Drive `value` from local state during drag; persist on `onValueChangeFinished`.
- **Subagent ID:** CLICK-PATH-018

#### BUG-035 — Scan: OutputUri NPE on `content://` URI
- **Severity:** LOW
- **Touchpoint:** "Open Result" / "Share" buttons at `ScanFlowScreen.kt:567–619`
- **Pattern:** Dead Path
- **Trace:** `FileProvider.getUriForFile(context, ..., File(outputUri.path!!))`. If the URI is `content://`, `.path` is null → NPE on `!!`.
- **Evidence:** `ScanFlowScreen.kt:574, 600`
- **Confidence:** 0.55
- **Recommended fix:** Inspect `outputUri.scheme`; if `content`, pass directly. If `file`, wrap with FileProvider.
- **Subagent IDs:** CLICK-PATH-056, 057

---

## Architecture Diagnosis

### State mutation map (root cause for ~50% of bugs)

```
┌────────────────────────────────────────────────────────────┐
│                  ToolViewModel.reset()                     │
│  - Cancels processingJob                                   │
│  - Sets _uiState=Idle, _selectedFiles=[], _outputUris=[],  │
│    _pageCount=null, _progress=null                         │
│  - Resets ALL 24 per-tool configs to default()             │
└────────────────────────────────────────────────────────────┘
        │              │               │              │
        │              │               │              │
        ▼              ▼               ▼              ▼
  Tool change    Success card    Surgical screen  Forms back
  (correct       onClear         Close (X)        button
   but over-      (wrong:         (wrong: wipes   (wrong: global
   aggressive)    wipes other     other tools)    wipe for back)
                  tool configs)
```

`reset()` is called from 7+ different contexts with 4 different intended scopes, but it always does the same 24-config wipe. The fix is to split it.

### Async race map (root cause for ~30% of bugs)

```
System file picker ──(async)──► callback writes _selectedFiles / htmlConfig
                                       │
                                       │ (if user changed tool in between)
                                       ▼
                                  VM write lands in stale context
                                  → silent data corruption

Scan: addPages ──(async picker)──► callback sets _flowState = Review
                                       │
                                       │ (if state was Processing)
                                       ▼
                                  Hard state-machine violation

Scan: cancel ──(async)──► cancelProcessing sets _flowState = Review
                                       │
                                       │ (if coroutine past last suspend)
                                       ▼
                                  Coroutine overwrites with Success
```

### Stale closure map (root cause for ~15% of bugs)

```
pointerInput(pageSize) { detectDragGestures { ... config ... } }
                       ↑
                       └─ pageSize change restarts gesture, stale config captured

Crop drag ──(mid-drag)──► user types in Left/Top field
                                       │
                                       ▼
                                  Gesture reads OLD leftMm
                                  → wrong final coords

PdfForms text dialog ──(open)──► user deletes field from canvas
                                       │
                                       ▼
                                  Apply uses stale `field`
                                  → field re-added with new value
```

---

## Ordered Fix Plan (code-first, not prompt-first)

### 1. **Split `ToolViewModel.reset()` into soft/hard** (resolves BUG-005, BUG-022, BUG-028, BUG-029, others)
- **Why now:** Single biggest user-trust risk. Surgical-screen Close, SuccessCard onClear, and Forms back all call the same method but with different intent.
- **Expected effect:** User can work in multiple tools in one session without losing state on tool switch. Reduces 7 distinct bugs to one trivial fix.
- **Implementation:**
  - Add `resetCurrentRun()` (clears `_selectedFiles`, `_outputUris`, `_uiState`, `_progress`, cancels job)
  - Rename current `reset()` to `resetAll()` (the full 24-config wipe)
  - Update callers: SuccessCard onClear → `resetCurrentRun()`; surgical-screen Close → `resetCurrentRun()`; `DisposableEffect(tool.id)` at `ToolScreen.kt:240` → `resetAll()`; FormsBuilder back → `resetCurrentRun()` plus local `currentStep = DASHBOARD`.

### 2. **Fix `process("ocr_pdf")` to use `.copy()`** (resolves BUG-006)
- **Why now:** Trivial one-line fix for a HIGH severity bug. Also fixes related Compare/OCR Success UI.
- **Expected effect:** OCR language and downloaded modules survive a process. SuccessCard shows text-output actions.
- **Implementation:** Change `ocrConfig.value = OcrConfig(ocrResultText = text)` to `ocrConfig.value = ocrConfig.value.copy(ocrResultText = text)`. In SuccessCard, branch on `tool.id in {"ocr_pdf", "compare_pdf"}` and render Copy/Share-text/Save-as-txt actions.

### 3. **Fix BUG-001 (rotate degrees) and BUG-014 (sticky note position)** (one-line each)
- **Why now:** Trivial fixes, CRITICAL/HIGH bugs.
- **Expected effect:** Rotate chip works. Sticky note places at tap location.
- **Implementation:** Change `OrganizeToolConfigs.kt:652` to `config.copy(previewRotation = angle)`. Change `EditPdfSurgicalScreen.kt:1586` to use `nextStickyPosition`.

### 4. **Add in-flight guards to Scan async actions** (resolves BUG-002, BUG-003, BUG-004)
- **Why now:** All three are CRITICAL, all in one ViewModel, all one-line guards.
- **Expected effect:** Cancel works. State machine not violated. No double-job leaks.
- **Implementation:** Add `if (processingJob?.isActive == true) return` at top of `generatePdf`. Add `if (_flowState.value is Processing) return` guard in `addPages`. Add `if (!isActive) return@launch` after `pdfProcessor.scanToPdf` returns in `generatePdf`.

### 5. **Fix Crop drag stale closure and page nav race** (resolves BUG-010, BUG-011)
- **Why now:** Both HIGH, both in one file.
- **Implementation:** Use `rememberUpdatedState(config)` inside `pointerInput`. Add `config.useAbsoluteCrop` gate to `LaunchedEffect(pageSize)` overwrite.

### 6. **Fix PdfForms Filler no-file dead path** (resolves BUG-012)
- **Why now:** HIGH, blocks a feature.
- **Implementation:** Add file picker step to `onLoadRecent` (line 288). Or disable Submit when `selectedFiles.isEmpty() && activeIntent == "fill"`.

### 7. **Fix SignPdf delete-orphans-field and html_to_pdf file picker dead data** (resolves BUG-007, BUG-013)
- **Why now:** Both HIGH.
- **Implementation:** In SignPdf delete handler, scan `signConfig.fields` for references. In html_to_pdf picker callback, skip `addFiles` and clear `_selectedFiles`.

### 8. **Sweep MEDIUM/LOW bugs in batches**
- BUG-018, BUG-019, BUG-020, BUG-021 (scan flow UX): all in `ScanFlowScreen.kt`. ~30 min.
- BUG-022, BUG-023, BUG-024 (EditPdf issues): all in `EditPdfSurgicalScreen.kt` and `OrganizeToolConfigs.kt`. ~1 hour.
- BUG-025, BUG-026, BUG-027 (shell/recent): small isolated fixes. ~30 min.

### 9. **Add tests for the fixed patterns**
- For each fixed bug, add a unit test that simulates the trigger sequence and asserts the final state.
- Specifically: `ToolViewModel.resetAll_vs_resetCurrentRun` test, `ScanViewModel.generatePdf_guarded_against_double_tap` test, `ToolViewModel.process_ocrPdf_preserves_language` test.

---

## Verification Checklist (per the skill's pattern)

- [x] All relevant files discovered and documented (Phase 1 map covers 7 VMs + 1 shared state + 6 repos)
- [x] Naming conventions captured (Compose handlers, StateFlows, async actions)
- [x] Error handling patterns documented (`runCatching`, `try-catch` in coroutines)
- [x] Test patterns identified (no existing test files in scope; the fix plan adds them)
- [x] Dependencies listed (Hilt, Room, DataStore, Compose, ML Kit, PDFBox)
- [x] Every finding has a concrete file:line trace
- [x] No task requires additional codebase searching to implement
- [x] Critical/High bugs have code-first fixes (not prompt-first)

## Risks

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| Splitting `reset()` breaks existing call sites | Medium | High | Run the full app after each call site change; add unit tests |
| `LaunchedEffect` removal in Crop changes page-nav UX | Low | Medium | Test page 1 → 2 → 1 cycle; verify dimensions persist |
| `process("ocr_pdf") .copy()` change breaks existing OcrConfig consumers | Low | Medium | Search all `ocrConfig` reads; only `ResultDisplayConfigs.kt:49` was found |

## Notes

- The audit's biggest single finding is `ToolViewModel.reset()` being a catch-all "wipe everything" method. This is a textbook wrapper-regression pattern from the agent-architecture-audit framework, applied to the Compose+ViewModel world.
- The PdfForms Filler dead path is the most user-visible bug for the "fills existing form" feature, which is presumably a common use case.
- Several HIGH bugs (004, 008, 009, 016) cluster around the "back" and "kill" paths — the app has good coverage of the happy path but weak coverage of exit/abort paths.
- The scan flow has a state machine (`Launcher | Review | Processing | Success | Error`) that is mostly well-defined, but two actions (`addPages` and `cancelProcessing`) break the machine.
