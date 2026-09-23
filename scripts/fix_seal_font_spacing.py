#!/usr/bin/env python3
"""Kaiyuan Small Seal draws every glyph flush left in a 1000-unit advance; give each glyph equal side bearings and a tight advance."""
import sys
from pathlib import Path
from fontTools.ttLib import TTFont

ROOT = Path(__file__).resolve().parent.parent
FONT_DIR = ROOT / "fonts/seal-scripts/kaiyuan/fonts"
FONT = FONT_DIR / "KaiyuanSmallSeal-Regular.ttf"
SIDE_BEARING = 60


def tighten(font):
    glyph_table, metrics = font["glyf"], font["hmtx"]
    for name in font.getGlyphOrder():
        glyph = glyph_table[name]
        if glyph.numberOfContours <= 0:
            continue
        glyph.recalcBounds(glyph_table)
        glyph.coordinates.translate((SIDE_BEARING - glyph.xMin, 0))
        glyph.recalcBounds(glyph_table)
        metrics[name] = (glyph.xMax - glyph.xMin + 2 * SIDE_BEARING, SIDE_BEARING)
    font["hhea"].advanceWidthMax = max(width for width, _ in metrics.metrics.values())


def main():
    font = TTFont(FONT)
    tighten(font)
    font.save(FONT)
    font.flavor = "woff2"
    font.save(FONT.with_suffix(".woff2"))
    print(f"tightened {FONT.relative_to(ROOT)} (side bearing {SIDE_BEARING})", file=sys.stderr)


if __name__ == "__main__":
    main()
