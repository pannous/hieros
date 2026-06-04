#!/usr/bin/env python3
"""Map EZGlyph Hieratic glyphs to an experimental U+33000 range."""

from __future__ import annotations

import argparse
import re
from pathlib import Path

from fontTools.ttLib import TTFont
from fontTools.ttLib.tables._c_m_a_p import cmap_format_12


SOURCE_START = 0x13000
SOURCE_END = 0x1342E
TARGET_OFFSET = 0x20000
FONT_FAMILY = "Egyptian Hieratic 33000"
FONT_SUBFAMILY = "Regular"
POSTSCRIPT_NAME = "EgyptianHieratic33000-Regular"


def normalized_sign_code(value: str) -> str:
    match = re.fullmatch(r"([A-Z][a-z]?)(\d+)([A-Za-z]*)", value)
    if not match:
        return value.upper()

    prefix, number, suffix = match.groups()
    return f"{prefix}{int(number)}{suffix}".upper()


def read_unikemet_sign_names(path: Path) -> dict[int, list[str]]:
    names_by_codepoint: dict[int, list[str]] = {}

    for line in path.read_text(encoding="utf-8").splitlines():
        if not line.startswith("U+"):
            continue

        parts = line.split("\t")
        if len(parts) < 3:
            continue

        codepoint = int(parts[0].split()[0][2:], 16)
        if not SOURCE_START <= codepoint <= SOURCE_END:
            continue

        property_name = parts[1]
        value = parts[2].strip()
        if property_name not in {"kEH_JSesh", "kEH_UniK", "kEH_HG"}:
            continue

        candidates = names_by_codepoint.setdefault(codepoint, [])
        if property_name == "kEH_JSesh" and " " in value:
            continue

        candidates.append(value)
        candidates.append(normalized_sign_code(value))

    return names_by_codepoint


def set_font_names(font: TTFont) -> None:
    name_table = font["name"]
    full_name = f"{FONT_FAMILY} {FONT_SUBFAMILY}"
    unique_id = f"{POSTSCRIPT_NAME}; EZGlyph Hieratic remapped to U+33000"
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

    if "CFF " in font:
        top_dict = font["CFF "].cff.topDictIndex[0]
        top_dict.FamilyName = FONT_FAMILY
        top_dict.FullName = full_name
        top_dict.FontName = POSTSCRIPT_NAME
        top_dict.Weight = FONT_SUBFAMILY


def make_format_12_cmap(mappings: dict[int, str]):
    table = cmap_format_12(12)
    table.platformID = 3
    table.platEncID = 10
    table.language = 0
    table.cmap = mappings
    return table


def map_font(input_path: Path, unikemet_path: Path, output_path: Path) -> dict[int, tuple[str, str]]:
    font = TTFont(input_path, lazy=False)
    source_glyphs = {glyph_name.upper(): glyph_name for glyph_name in font.getGlyphOrder()}
    candidates_by_codepoint = read_unikemet_sign_names(unikemet_path)

    matched: dict[int, tuple[str, str]] = {}
    cmap: dict[int, str] = {
        0x0000: ".null",
        0x0008: ".null",
        0x0009: "nonmarkingreturn",
        0x000D: "nonmarkingreturn",
        0x001D: ".null",
        0x0020: "space",
    }

    for codepoint, candidates in sorted(candidates_by_codepoint.items()):
        for candidate in candidates:
            glyph_name = source_glyphs.get(candidate.upper())
            if glyph_name is None:
                glyph_name = source_glyphs.get(normalized_sign_code(candidate))
            if glyph_name is None:
                continue

            target = codepoint + TARGET_OFFSET
            cmap[target] = glyph_name
            matched[codepoint] = (target, glyph_name)
            break

    font["cmap"].tables = [make_format_12_cmap(cmap)]
    set_font_names(font)
    font.save(output_path)
    return matched


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("input", type=Path, help="Input EZGlyph Hieratic OTF")
    parser.add_argument(
        "output",
        nargs="?",
        type=Path,
        default=Path("fonts/EgyptianHieratic33000.ttf"),
        help="Output mapped font",
    )
    parser.add_argument(
        "--unikemet",
        type=Path,
        default=Path("abc/UCD/Unikemet.txt"),
        help="Unicode Unikemet.txt source",
    )
    args = parser.parse_args()

    matched = map_font(args.input, args.unikemet, args.output)
    print(f"Mapped {len(matched)} EZGlyph Hieratic glyphs")
    print(f"Saved to: {args.output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
