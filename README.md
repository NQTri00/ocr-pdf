# OCR PDF — Local PDF Translation Pipeline

A local-first application for translating English PDFs into Vietnamese while preserving their layout.

**Non-negotiable scanned-PDF behavior:** the original English text must be removed from the page image before Vietnamese is drawn. The application must not merely place Vietnamese over the original. Text removal is performed with explicit masks and background reconstruction; uncertain regions are flagged for review rather than silently exported.

## Platform implementations

- **Windows desktop (Python):** [desktop pipeline](docs/ROADMAP.md)
- **Android (Kotlin):** [Android app and build instructions](android/README.md), with its [implementation roadmap](docs/ANDROID_ROADMAP.md).

The Android project is an early OCR foundation, not a complete translator yet. It can select PDFs, render pages sequentially and run bundled on-device OCR. Translation, safe background restoration and final PDF reconstruction remain future milestones.

## Windows desktop status

Milestone 1 is implemented: PDF inspection, per-page digital/scanned/mixed classification, page rendering, and a JSON report. It deliberately does not claim to translate or clean pages yet.

## Windows requirements

- Windows 10/11
- Python 3.11+
- 8 GB NVIDIA GPU / 16 GB RAM is supported as a target, but Milestone 1 uses CPU only.
- Internet is needed once to install Python packages and later download model weights. Runtime can be offline after dependencies and model files are cached.

## Install desktop version (PowerShell)

```powershell
py -3.11 -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install --upgrade pip
python -m pip install -e ".[dev]"
```

## Inspect a PDF

```powershell
ocr-pdf inspect "C:\\path\\document.pdf" --output-dir ".\\runs\\document" --dpi 150
```

Or run without installing the console entry point:

```powershell
python -m ocr_pdf inspect "C:\\path\\document.pdf" --output-dir ".\\runs\\document" --dpi 150
```

The run directory contains:
- `report.json`: metadata, page classification, text/image statistics, and render paths.
- `pages/page-0001.png`: rendered page previews.

Use 150 DPI for inspection. Final raster reconstruction will target 300 DPI, subject to source resolution and memory.

## Planned document pipeline

1. Inspect and classify every page.
2. Extract native PDF text or OCR scanned pages.
3. Build a unified region/layout representation.
4. Detect text masks and protect non-text structures.
5. Remove original text and validate the cleaned page.
6. Translate locally (English → Vietnamese).
7. Fit and render Vietnamese text.
8. Export a searchable PDF and validation report.

## Important limitations

- Classification is heuristic and reports its evidence; it is not a definitive document understanding model.
- A page can contain both native text and scanned images.
- No scanned-page text removal is included in the desktop Milestone 1 or Android OCR milestone. We will not fake this with an overlay.
- Inpainting cannot recover image details that were physically covered by printed text. The pipeline must expose low-confidence restoration and require review for photos, charts, and structured diagrams.
