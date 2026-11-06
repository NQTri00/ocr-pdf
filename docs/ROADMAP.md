# Development roadmap

## Milestone 1 — PDF intake (implemented)
- Validate PDF input, encryption, and page count.
- Inspect native text, images, drawings, page geometry, and rotation.
- Classify each page as digital, scanned, mixed, or unknown.
- Render preview images and write a machine-readable report.

## Milestone 2 — Text extraction
- Native extraction with word/span geometry and font metadata.
- OCR backend behind an interface; evaluate PaddleOCR on the target Windows/CUDA environment before pinning it.
- Reading order and coordinate transforms.
- Confidence and extraction provenance for every region.

## Milestone 3 — Unified document representation
- Stable page and region IDs.
- Text, polygon/bounding box, source, style, reading order, and confidence.
- Protected regions for lines, chart geometry, images, equations, and logos.

## Milestone 4 — Scanned-page text removal
- Character-aware segmentation and polygon masks, not OCR rectangles alone.
- Mask refinement and protected-object subtraction.
- Route solid, gradient, texture, photo, and structured backgrounds separately.
- Preserve original page images and write all intermediate masks.
- Validate residual source text and damage; mark uncertain regions for human review.

## Milestone 5 — Local translation
- English → Vietnamese model adapter, terminology glossary, context windows, and local cache.
- Benchmark translation quality and memory use on 8 GB VRAM.
- No remote translation API.

## Milestone 6 — Layout reconstruction and export
- Font fallback, Vietnamese line wrapping, text fitting, and searchable text layer.
- Preserve digital PDF vectors where possible.
- Export page previews, final PDF, and validation report.

## Acceptance requirements
- Never leave original English visible beneath or beside translated text.
- Never silently export a page when text removal or layout fitting failed.
- Keep source PDF immutable.
- Preserve table borders, chart axes/lines, diagram connectors, and non-text image content.
- Record region-level extraction, translation, removal, and validation status.
