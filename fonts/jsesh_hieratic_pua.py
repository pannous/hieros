#!/usr/bin/env python3
"""Add JSesh Egyptian Hieroglyph PUA mappings to a font."""

from __future__ import annotations

import argparse
from pathlib import Path

from fontTools.ttLib import TTFont
from fontTools.ttLib.tables._c_m_a_p import cmap_format_12


EGYPTIAN_HIEROGLYPHS_START = 0x13000
EGYPTIAN_HIEROGLYPHS_END = 0x1345F
PUA_OFFSET = 0xE0000


def ensure_format_12_cmap(font: TTFont):
    cmap = font["cmap"]
    for table in cmap.tables:
        if table.format == 12 and table.platformID == 3 and table.platEncID == 10:
            return table

    table = cmap_format_12(12)
    table.platformID = 3
    table.platEncID = 10
    table.language = 0
    table.cmap = {}
    cmap.tables.append(table)
    return table


def add_jsesh_pua_mappings(input_path: Path, output_path: Path) -> int:
    font = TTFont(input_path, lazy=False)
    format12 = ensure_format_12_cmap(font)

    source_mappings: dict[int, str] = {}
    for table in font["cmap"].tables:
        if not hasattr(table, "cmap"):
            continue
        for codepoint, glyph_name in table.cmap.items():
            if EGYPTIAN_HIEROGLYPHS_START <= codepoint <= EGYPTIAN_HIEROGLYPHS_END:
                source_mappings[codepoint] = glyph_name

    for codepoint, glyph_name in source_mappings.items():
        format12.cmap[codepoint + PUA_OFFSET] = glyph_name

    font.save(output_path)
    return len(source_mappings)


def main() -> int:
    parser = argparse.ArgumentParser(
        description=(
            "Duplicate Egyptian Hieroglyphs Unicode cmap entries into the "
            "U+F3000 private-use range."
        )
    )
    parser.add_argument("input", type=Path, help="Input TTF/OTF font")
    parser.add_argument(
        "output",
        nargs="?",
        type=Path,
        default=Path("JSeshHieraticPUA.ttf"),
        help="Output font path",
    )
    args = parser.parse_args()

    count = add_jsesh_pua_mappings(args.input, args.output)
    print(f"Mapped {count} Egyptian Hieroglyph codepoints")
    print(f"Saved to: {args.output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
