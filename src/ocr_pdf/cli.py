from __future__ import annotations

import argparse
import json
from pathlib import Path

from ocr_pdf.inspection import inspect_pdf


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(prog="ocr-pdf", description="Inspect and prepare PDF translation jobs.")
    subparsers = parser.add_subparsers(dest="command", required=True)

    inspect_parser = subparsers.add_parser("inspect", help="Inspect a PDF and render page previews.")
    inspect_parser.add_argument("pdf", type=Path, help="Input PDF path")
    inspect_parser.add_argument("--output-dir", type=Path, required=True, help="Directory for report and previews")
    inspect_parser.add_argument("--dpi", type=int, default=150, choices=(100, 150, 200, 250, 300))
    inspect_parser.add_argument("--password", default=None, help="Password for an encrypted PDF")
    return parser


def main() -> int:
    args = build_parser().parse_args()
    if args.command == "inspect":
        try:
            report = inspect_pdf(args.pdf, args.output_dir, dpi=args.dpi, password=args.password)
        except (OSError, ValueError, RuntimeError) as exc:
            print(f"Error: {exc}")
            return 2
        print(json.dumps({
            "input": str(report["input"]),
            "pages": len(report["pages"]),
            "report": str(args.output_dir / "report.json"),
        }, ensure_ascii=False, indent=2))
        return 0
    return 2
