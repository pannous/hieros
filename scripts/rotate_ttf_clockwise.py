#!/usr/bin/env python3
"""Create a clockwise-rotated copy of a TrueType font."""

from __future__ import annotations

import argparse
from pathlib import Path

from fontTools.misc.transform import Transform
from fontTools.pens.boundsPen import BoundsPen
from fontTools.pens.recordingPen import DecomposingRecordingPen
from fontTools.pens.transformPen import TransformPen
from fontTools.pens.ttGlyphPen import TTGlyphPen
from fontTools.ttLib import TTFont


def set_name(font: TTFont, name_id: int, value: str) -> None:
    name = font["name"]
    for platform_id, encoding_id, language_id in (
        (3, 1, 0x409),
        (1, 0, 0),
    ):
        name.setName(value, name_id, platform_id, encoding_id, language_id)


def rotate_clockwise_about_bounds(font: TTFont) -> int:
    glyph_set = font.getGlyphSet()
    glyf = font["glyf"]
    hmtx = font["hmtx"].metrics
    rotated = 0

    for glyph_name in font.getGlyphOrder():
        bounds_pen = BoundsPen(glyph_set)
        glyph_set[glyph_name].draw(bounds_pen)
        if bounds_pen.bounds is None:
            continue

        x_min, y_min, x_max, y_max = bounds_pen.bounds
        cx = (x_min + x_max) / 2
        cy = (y_min + y_max) / 2

        transform = Transform(0, -1, 1, 0, cx - cy, cy + cx)
        recording_pen = DecomposingRecordingPen(glyph_set)
        glyph_set[glyph_name].draw(recording_pen)

        glyph_pen = TTGlyphPen(None)
        recording_pen.replay(TransformPen(glyph_pen, transform))
        new_glyph = glyph_pen.glyph()
        glyf[glyph_name] = new_glyph
        new_glyph.recalcBounds(glyf)

        if glyph_name in hmtx:
            advance_width, _left_side_bearing = hmtx[glyph_name]
            hmtx[glyph_name] = (advance_width, getattr(new_glyph, "xMin", 0))

        rotated += 1

    return rotated


def rename_font(font: TTFont) -> None:
    family = "PCSL Rotated CW"
    subfamily = "Regular"
    full_name = f"{family} {subfamily}"
    postscript_name = "PCSL-RotatedCW-Regular"

    set_name(font, 1, family)
    set_name(font, 2, subfamily)
    set_name(font, 4, full_name)
    set_name(font, 6, postscript_name)
    set_name(font, 16, family)
    set_name(font, 17, subfamily)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("source", type=Path)
    parser.add_argument("output", type=Path)
    args = parser.parse_args()

    font = TTFont(args.source, recalcBBoxes=True, recalcTimestamp=True)
    if "glyf" not in font:
        raise SystemExit("Only TrueType glyf fonts are supported.")

    rotated = rotate_clockwise_about_bounds(font)
    rename_font(font)

    for table in ("fpgm", "prep", "cvt ", "DSIG"):
        if table in font:
            del font[table]

    args.output.parent.mkdir(parents=True, exist_ok=True)
    font.save(args.output)
    print(f"rotated {rotated} glyphs")
    print(args.output)


if __name__ == "__main__":
    main()
