#!/usr/bin/env python3
"""Add experimental Egyptian Hieratic mappings to a font."""

from __future__ import annotations

import argparse
from pathlib import Path

from fontTools.ttLib import TTFont
from fontTools.ttLib.tables._c_m_a_p import cmap_format_12


EGYPTIAN_HIEROGLYPHS_START = 0x13000
EGYPTIAN_HIEROGLYPHS_END = 0x1345F
TARGET_OFFSET = 0x20000
FONT_FAMILY = "Egyptian Hieratic 33000"
FONT_SUBFAMILY = "Regular"
POSTSCRIPT_NAME = "EgyptianHieratic33000-Regular"


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


def rename_font(font: TTFont) -> None:
    name_table = font["name"]
    full_name = f"{FONT_FAMILY} {FONT_SUBFAMILY}"
    unique_id = f"{POSTSCRIPT_NAME}; Egyptian signs remapped to U+33000"

    names = {
        1: FONT_FAMILY,
        2: FONT_SUBFAMILY,
        3: unique_id,
        4: full_name,
        6: POSTSCRIPT_NAME,
        16: FONT_FAMILY,
        17: FONT_SUBFAMILY,
    }
    for name_id, value in names.items():
        name_table.setName(value, name_id, 1, 0, 0)
        name_table.setName(value, name_id, 3, 1, 0x409)
        name_table.setName(value, name_id, 3, 10, 0x409)


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
        format12.cmap[codepoint + TARGET_OFFSET] = glyph_name

    rename_font(font)
    font.save(output_path)
    return len(source_mappings)


def main() -> int:
    parser = argparse.ArgumentParser(
        description=(
            "Duplicate Egyptian Hieroglyphs Unicode cmap entries into the "
            "experimental U+33000 Hieratic range."
        )
    )
    parser.add_argument("input", type=Path, help="Input TTF/OTF font")
    parser.add_argument(
        "output",
        nargs="?",
        type=Path,
        default=Path("fonts/EgyptianHieratic33000.ttf"),
        help="Output font path",
    )
    args = parser.parse_args()

    count = add_jsesh_pua_mappings(args.input, args.output)
    print(f"Mapped {count} Egyptian Hieroglyph codepoints")
    print(f"Saved to: {args.output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
