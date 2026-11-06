from __future__ import annotations

import json
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

import fitz


def _classify_page(text_chars: int, image_coverage: float, image_count: int) -> tuple[str, str]:
    """Conservative heuristic; downstream stages must not treat this as ground truth."""
    has_text = text_chars >= 30
    has_large_image = image_coverage >= 0.45 or (image_count > 0 and image_coverage >= 0.15)
    if has_text and has_large_image:
        return "mixed", "Native text and substantial raster imagery were both detected."
    if has_text:
        return "digital", "A meaningful amount of native PDF text was detected."
    if has_large_image:
        return "scanned", "Little native text was found and raster imagery covers a substantial area."
    return "unknown", "Insufficient native text or image evidence for a reliable classification."


def _image_coverage(page: fitz.Page) -> tuple[float, int]:
    page_area = max(page.rect.width * page.rect.height, 1.0)
    covered_area = 0.0
    image_count = 0
    for image in page.get_images(full=True):
        xref = image[0]
        for rect in page.get_image_rects(xref):
            clipped = rect & page.rect
            if not clipped.is_empty:
                covered_area += clipped.width * clipped.height
                image_count += 1
    # Overlapping image rectangles can double count; cap coverage for classification.
    return min(covered_area / page_area, 1.0), image_count


def inspect_pdf(
    pdf_path: Path,
    output_dir: Path,
    *,
    dpi: int = 150,
    password: str | None = None,
) -> dict[str, Any]:
    pdf_path = Path(pdf_path).expanduser().resolve()
    output_dir = Path(output_dir).expanduser().resolve()
    if not pdf_path.exists() or not pdf_path.is_file():
        raise FileNotFoundError(f"Input PDF does not exist: {pdf_path}")
    if pdf_path.suffix.lower() != ".pdf":
        raise ValueError("Input file must have a .pdf extension.")
    if dpi not in (100, 150, 200, 250, 300):
        raise ValueError("DPI must be one of: 100, 150, 200, 250, 300.")

    output_dir.mkdir(parents=True, exist_ok=True)
    pages_dir = output_dir / "pages"
    pages_dir.mkdir(parents=True, exist_ok=True)

    try:
        document = fitz.open(pdf_path)
    except Exception as exc:
        raise ValueError(f"Could not open PDF: {exc}") from exc

    with document:
        if document.is_encrypted:
            if not password:
                raise ValueError("PDF is password-protected. Supply --password.")
            if not document.authenticate(password):
                raise ValueError("The supplied PDF password is incorrect.")
        if document.page_count == 0:
            raise ValueError("The PDF contains no pages.")

        page_reports: list[dict[str, Any]] = []
        scale = dpi / 72.0
        matrix = fitz.Matrix(scale, scale)
        for index in range(document.page_count):
            page = document.load_page(index)
            text_dict = page.get_text("dict")
            text_chars = sum(
                len(span.get("text", ""))
                for block in text_dict.get("blocks", [])
                if block.get("type") == 0
                for line in block.get("lines", [])
                for span in line.get("spans", [])
            )
            text_blocks = sum(1 for block in text_dict.get("blocks", []) if block.get("type") == 0)
            image_coverage, image_count = _image_coverage(page)
            drawings = len(page.get_drawings())
            classification, reason = _classify_page(text_chars, image_coverage, image_count)

            image_path = pages_dir / f"page-{index + 1:04d}.png"
            pixmap = page.get_pixmap(matrix=matrix, alpha=False, annots=True)
            pixmap.save(image_path)

            page_reports.append({
                "page_number": index + 1,
                "classification": classification,
                "classification_reason": reason,
                "width_points": round(page.rect.width, 2),
                "height_points": round(page.rect.height, 2),
                "rotation": page.rotation,
                "native_text_characters": text_chars,
                "native_text_blocks": text_blocks,
                "image_coverage_ratio": round(image_coverage, 4),
                "image_placements": image_count,
                "vector_drawing_count": drawings,
                "preview": str(image_path.relative_to(output_dir)),
            })

    report: dict[str, Any] = {
        "schema_version": "1.0",
        "created_at": datetime.now(timezone.utc).isoformat(),
        "input": str(pdf_path),
        "file_size_bytes": pdf_path.stat().st_size,
        "dpi": dpi,
        "page_count": len(page_reports),
        "pages": page_reports,
        "notes": [
            "Page classification is heuristic, not definitive.",
            "No text has been removed, translated, or overlaid in this inspection stage.",
        ],
    }
    report_path = output_dir / "report.json"
    report_path.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    return report
