#!/usr/bin/env python3
"""Make Kaiyuan Small Seal usable on macOS.

1. Spacing: every glyph was drawn flush left in a 1000-unit advance; give each equal side bearings and a tight advance.
2. Coverage: CoreText silently drops parts of a fragmented format-12 cmap (U+3D7D1..3E029 went missing, so 可 and 就
   fell back to LastResort in Sublime Text). Order glyphs by code point and fill the block's gaps with a hollow-box
   placeholder, so the whole Seal block is one contiguous cmap group.
Idempotent: safe to run on an already fixed font.
"""
import sys
from pathlib import Path
from fontTools.ttLib import TTFont
from fontTools.pens.ttGlyphPen import TTGlyphPen

ROOT = Path(__file__).resolve().parent.parent
FONT = ROOT / "fonts/seal-scripts/kaiyuan/fonts/KaiyuanSmallSeal-Regular.ttf"
SEAL_BLOCK = range(0x3D000, 0x3FC40)
SIDE_BEARING = 60
PLACEHOLDER_BOX = (120, 0, 480, 700)
PLACEHOLDER_STROKE = 40


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


def rectangle(pen, left, bottom, right, top, clockwise):
    corners = [(left, bottom), (left, top), (right, top), (right, bottom)]
    for index, point in enumerate(corners if clockwise else corners[::-1]):
        (pen.moveTo if index == 0 else pen.lineTo)(point)
    pen.closePath()


def placeholder_glyph():
    left, bottom, right, top = PLACEHOLDER_BOX
    pen = TTGlyphPen(None)
    rectangle(pen, left, bottom, right, top, clockwise=True)
    rectangle(pen, left + PLACEHOLDER_STROKE, bottom + PLACEHOLDER_STROKE, right - PLACEHOLDER_STROKE, top - PLACEHOLDER_STROKE, clockwise=False)
    return pen.glyph()


def make_block_contiguous(font):
    glyph_table, metrics = font["glyf"], font["hmtx"]
    by_codepoint = font.getBestCmap()
    left, _, right, _ = PLACEHOLDER_BOX
    names = []
    for codepoint in SEAL_BLOCK:
        name = by_codepoint.get(codepoint)
        if name is None:
            name = f"missing{codepoint:X}"
            glyph_table[name] = placeholder_glyph()
            metrics[name] = (right + left, left)
        names.append(name)
    others = [name for name in font.getGlyphOrder() if name not in set(names) and name != ".notdef"]
    order = [".notdef"] + names + others
    font.setGlyphOrder(order)
    glyph_table.glyphOrder = order
    for subtable in font["cmap"].tables:
        if subtable.format == 12:
            subtable.cmap = {codepoint: name for codepoint, name in zip(SEAL_BLOCK, names)} | {
                codepoint: name for codepoint, name in subtable.cmap.items() if codepoint not in SEAL_BLOCK}
    font["post"].formatType = 2.0
    font["post"].extraNames, font["post"].mapping = [], {}
    return len(SEAL_BLOCK) - sum(1 for codepoint in SEAL_BLOCK if codepoint in by_codepoint)


def main():
    font = TTFont(FONT)
    tighten(font)
    placeholders = make_block_contiguous(font)
    font["hhea"].advanceWidthMax = max(width for width, _ in font["hmtx"].metrics.values())
    font.save(FONT)
    font.flavor = "woff2"
    font.save(FONT.with_suffix(".woff2"))
    print(f"fixed {FONT.relative_to(ROOT)}: side bearing {SIDE_BEARING}, {placeholders} placeholders, one contiguous cmap group", file=sys.stderr)


if __name__ == "__main__":
    main()
