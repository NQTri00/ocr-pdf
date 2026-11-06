# Android implementation roadmap

## A1 — Kotlin OCR foundation (current)
- [x] Native Android project structure
- [x] PDF picker and read-only PDF rendering
- [x] Sequential page processing
- [x] Offline bundled OCR
- [x] OCR JSON report and text-region coordinates
- [x] Translation interface with explicit missing-model failure
- [ ] Build and run on a physical Redmi Note 11S

## A2 — Translation model
- [ ] Select a permissively licensed English-to-Vietnamese model
- [ ] Convert/quantize for an Android runtime
- [ ] Implement tokenizer and chunking
- [ ] Benchmark peak RAM, latency and translation quality on Note 11S
- [ ] Verify offline behavior in airplane mode

## A3 — Safe text removal
- [ ] Refine OCR boxes to character/word masks
- [ ] Detect and protect tables, chart axes, borders, equations and illustrations
- [ ] Add conservative solid/gradient restoration
- [ ] Add inpainting for supported texture regions
- [ ] Flag photo/structured/low-confidence regions for review

## A4 — Vietnamese reconstruction
- [ ] Font selection and glyph coverage
- [ ] Layout-aware wrapping and font fitting
- [ ] Rebuild PDF pages without retaining source English
- [ ] Add selectable/searchable Vietnamese text layer

## A5 — Validation and release
- [ ] Residual English OCR validation
- [ ] Protected-region and visual-damage checks
- [ ] Large-document and low-memory tests
- [ ] Export only pages that pass validation or are explicitly approved
