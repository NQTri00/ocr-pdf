# OCR PDF for Android

Native Kotlin Android prototype targeting mid-range phones such as the Xiaomi Redmi Note 11S.

## Current milestone: offline PDF OCR inspection

Implemented:
- Pick a PDF through Android's Storage Access Framework.
- Open PDFs read-only with Android PdfRenderer.
- Render one page at a time at a configurable mobile DPI (default 150).
- Run bundled ML Kit Latin text recognition locally on the device.
- Save recognized text and OCR text-block bounding boxes as a JSON report.
- Plan expanded candidate text masks without modifying the source document.
- Define an offline translation interface that fails explicitly when no model is installed.

Not implemented yet:
- English-to-Vietnamese translation model and tokenizer.
- Safe text segmentation and protected-region detection.
- Background restoration/inpainting.
- Vietnamese layout fitting and final PDF export.
- Residual-text and visual-damage validation.

**This prototype does not yet translate PDFs.** It intentionally does not export a fake translated PDF or place Vietnamese over the original English.

## Build

Open the `android/` directory in Android Studio (JDK 17; Android SDK 35), allow Gradle sync, then run the `app` configuration.

Or, with Gradle installed:

```bash
gradle :app:assembleDebug
```

The first dependency resolution requires internet. Once dependencies are cached and the APK is installed, PDF rendering and OCR run locally. The manifest declares no network permission.

## Device profile

- Minimum SDK: Android 8.0 (API 26)
- Target SDK: 35
- Reference device: Redmi Note 11S, preferably 8 GB RAM
- OCR render DPI: 150 by default; 100–200 supported
- Processing: sequential, one page bitmap at a time

Advertised RAM is not entirely available to an app. Large PDFs should be processed page-by-page, and model memory must be measured on a physical device before enabling translation and restoration.

## Report location

The JSON report is saved in app-private storage at:

`files/reports/ocr-report.json`

It contains page dimensions, recognized text and OCR bounding boxes in rendered-bitmap pixel coordinates.
