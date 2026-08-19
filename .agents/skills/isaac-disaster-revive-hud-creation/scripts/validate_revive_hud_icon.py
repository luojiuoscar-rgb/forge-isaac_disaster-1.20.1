#!/usr/bin/env python3
from __future__ import annotations

import argparse
import sys
from pathlib import Path

from PIL import Image


SOURCE_SIZE = (16, 16)
PREVIEW_SIZE = (8, 8)
try:
    NEAREST = Image.Resampling.NEAREST
except AttributeError:  # pragma: no cover - older Pillow fallback
    NEAREST = Image.NEAREST


def fail(message: str) -> None:
    print(f"error: {message}", file=sys.stderr)
    raise SystemExit(1)


def validate_source(source: Path) -> Image.Image:
    if not source.exists() or not source.is_file():
        fail(f"source file not found: {source}")

    try:
        with Image.open(source) as img:
            if img.format != "PNG":
                fail(f"source must be PNG: {source}")
            if img.size != SOURCE_SIZE:
                fail(f"source must be exactly {SOURCE_SIZE[0]}x{SOURCE_SIZE[1]} pixels: {source}")
            if "A" not in img.getbands() and "transparency" not in img.info:
                fail("source PNG must include transparency")

            rgba = img.convert("RGBA")
    except OSError as exc:
        fail(f"cannot read PNG: {exc}")

    alpha = rgba.getchannel("A")
    alpha_min, alpha_max = alpha.getextrema()
    if alpha_min != 0:
        fail("source must contain transparent pixels")
    if alpha_max == 0:
        fail("source cannot be fully transparent")

    return rgba


def write_preview(rgba: Image.Image, preview_path: Path) -> None:
    preview_path.parent.mkdir(parents=True, exist_ok=True)
    preview = rgba.resize(PREVIEW_SIZE, resample=NEAREST)
    preview.save(preview_path, format="PNG")

    with Image.open(preview_path) as check:
        if check.format != "PNG":
            fail(f"preview was not written as PNG: {preview_path}")
        if check.size != PREVIEW_SIZE:
            fail(f"preview must be exactly {PREVIEW_SIZE[0]}x{PREVIEW_SIZE[1]} pixels: {preview_path}")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Validate a revive HUD PNG and write an 8x8 preview.")
    parser.add_argument("source", type=Path, help="Path to the 16x16 source PNG.")
    parser.add_argument("--preview", required=True, type=Path, help="Path to write the 8x8 PNG preview.")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    source = args.source
    preview = args.preview

    if source.resolve() == preview.resolve():
        fail("preview path must differ from source path")

    rgba = validate_source(source)
    write_preview(rgba, preview)
    print(f"validated {source} and wrote preview {preview}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
