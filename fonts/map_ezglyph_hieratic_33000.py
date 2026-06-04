#!/usr/bin/env python3
"""Map EZGlyph Hieratic glyphs to an experimental U+33000 range."""

from __future__ import annotations

import argparse
import re
from collections import defaultdict
from pathlib import Path
from xml.etree import ElementTree as ET
from zipfile import ZipFile

from fontTools.ttLib import TTFont
from fontTools.ttLib.tables._c_m_a_p import cmap_format_12


SOURCE_START = 0x13000
SOURCE_END = 0x1342E
TARGET_OFFSET = 0x20000
ORIGINAL_APPEND_START = 0x34000
FONT_FAMILY = "Egyptian Hieratic 33000"
FONT_SUBFAMILY = "Regular"
POSTSCRIPT_NAME = "EgyptianHieratic33000-Regular"
TRAY_SEPARATOR = 0xF861
DEFAULT_FONT_TRAY_PATHS = (
    Path("fonts/Font Tray - EZGlyph.docx"),
    Path("/tmp/ezdocmac/Font Tray - EZGlyph.docx"),
)
VISUAL_REJECTED_TRAY_MAPPINGS = {
    0x13042,  # A57: tray placeholder is not a standing king/offering figure.
    0x13046,  # A61: tray placeholder is horizontal, not a standing man.
    0x13049,  # A64: tray alias is standing, not seated with a bowl.
    0x13058,  # B8: tray placeholder renders as a dash, not a woman.
    0x130C6,  # D64: tray placeholder renders as a dash.
    0x130C7,  # D65: tray placeholder does not match the looped sign.
    0x130C8,  # D66: tray placeholder does not match the slanted sign.
    0x131A2,  # K8: tray placeholder renders as a dash, not a fish.
    0x132CD,  # R26: tray placeholder renders as a dash.
    0x13306,  # S46: tray placeholder renders as a dash.
    0x13332,  # T36: tray placeholder renders as a dash.
}


def normalized_sign_code(value: str) -> str:
    match = re.fullmatch(r"([A-Z][a-z]?)(\d+)([A-Za-z]*)", value)
    if not match:
        return value.upper()

    prefix, number, suffix = match.groups()
    return f"{prefix}{int(number)}{suffix}".upper()


def sign_parts(value: str) -> tuple[str, int, str] | None:
    match = re.fullmatch(r"([A-Z][a-z]?)(\d+)([A-Za-z]*)", value)
    if not match:
        return None

    prefix, number, suffix = match.groups()
    return prefix.upper(), int(number), suffix.upper()


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
        if property_name != "kEH_UniK":
            continue

        candidates = names_by_codepoint.setdefault(codepoint, [])
        candidates.append(value)
        candidates.append(normalized_sign_code(value))

    return names_by_codepoint


def read_unikemet_primary_names(path: Path) -> tuple[dict[int, str], dict[str, int]]:
    primary_by_codepoint: dict[int, str] = {}
    codepoint_by_name: dict[str, int] = {}

    for line in path.read_text(encoding="utf-8").splitlines():
        if not line.startswith("U+"):
            continue

        parts = line.split("\t")
        if len(parts) < 3:
            continue

        codepoint = int(parts[0].split()[0][2:], 16)
        if not SOURCE_START <= codepoint <= SOURCE_END:
            continue
        if parts[1] != "kEH_UniK":
            continue

        name = normalized_sign_code(parts[2].strip())
        primary_by_codepoint[codepoint] = name
        codepoint_by_name[name] = codepoint

    return primary_by_codepoint, codepoint_by_name


def read_docx_tray_chunks(path: Path, font: TTFont) -> list[list[tuple[int, str]]]:
    cmap = {}
    for table in font["cmap"].tables:
        cmap.update(table.cmap)

    namespaces = {"w": "http://schemas.openxmlformats.org/wordprocessingml/2006/main"}
    with ZipFile(path) as docx:
        root = ET.fromstring(docx.read("word/document.xml"))

    chunks: list[list[tuple[int, str]]] = []
    current: list[tuple[int, str]] = []

    for paragraph in root.findall(".//w:p", namespaces):
        text = "".join(
            node.text or "" for node in paragraph.findall(".//w:t", namespaces)
        )
        codepoints = [ord(char) for char in text if 0xE000 <= ord(char) <= 0xF8FF]
        if not codepoints:
            continue

        if all(codepoint == TRAY_SEPARATOR for codepoint in codepoints):
            if current:
                chunks.append(current)
                current = []
            continue

        for codepoint in codepoints:
            if codepoint == TRAY_SEPARATOR:
                continue
            current.append((codepoint, cmap.get(codepoint, "")))

    if current:
        chunks.append(current)

    return chunks


def infer_tray_mappings(
    font: TTFont, tray_path: Path, unikemet_path: Path
) -> dict[int, str]:
    primary_by_codepoint, codepoint_by_name = read_unikemet_primary_names(unikemet_path)
    codepoint_by_prefix_number: dict[tuple[str, int], int] = {}
    for codepoint, name in primary_by_codepoint.items():
        parts = sign_parts(name)
        if parts is None:
            continue
        prefix, number, suffix = parts
        if not suffix:
            codepoint_by_prefix_number[(prefix, number)] = codepoint

    tray_mappings: dict[int, str] = {}
    filled_from_placeholders: dict[int, str] = {}

    for chunk in read_docx_tray_chunks(tray_path, font):
        chunk_parts = [sign_parts(normalized_sign_code(name)) for _, name in chunk]
        prefix_counts: dict[str, int] = defaultdict(int)
        for parts in chunk_parts:
            if parts is None:
                continue
            prefix, _, _ = parts
            prefix_counts[prefix] += 1

        if not prefix_counts:
            continue

        chunk_prefix = max(prefix_counts, key=prefix_counts.get)
        if prefix_counts[chunk_prefix] < 2:
            continue

        explicit_positions: list[tuple[int, int]] = []
        for index, ((_, glyph_name), parts) in enumerate(zip(chunk, chunk_parts)):
            if parts is None:
                continue
            prefix, number, suffix = parts
            if prefix != chunk_prefix:
                continue

            codepoint = codepoint_by_name.get(normalized_sign_code(glyph_name))
            if codepoint is not None:
                tray_mappings[codepoint] = glyph_name

            if not suffix:
                explicit_positions.append((index, number))

        explicit_positions.sort()
        for (left_index, left_number), (right_index, right_number) in zip(
            explicit_positions, explicit_positions[1:]
        ):
            if right_number <= left_number + 1:
                continue

            missing_numbers = list(range(left_number + 1, right_number))
            candidate_slots = [
                index
                for index in range(left_index + 1, right_index)
                if chunk_parts[index] is None
                or (
                    chunk_parts[index][0] == chunk_prefix
                    and chunk_parts[index][2]
                )
            ]

            for number, slot_index in zip(missing_numbers, candidate_slots):
                codepoint = codepoint_by_prefix_number.get((chunk_prefix, number))
                if codepoint is None:
                    continue
                glyph_name = chunk[slot_index][1]
                if glyph_name:
                    filled_from_placeholders[codepoint] = glyph_name

        first_index, first_number = explicit_positions[0] if explicit_positions else (-1, 0)
        if first_number > 1:
            leading_slots = [
                index
                for index in range(first_index)
                if chunk_parts[index] is None
            ]
            missing_numbers = list(
                range(max(1, first_number - len(leading_slots)), first_number)
            )
            for number, slot_index in zip(missing_numbers, leading_slots[-len(missing_numbers) :]):
                codepoint = codepoint_by_prefix_number.get((chunk_prefix, number))
                if codepoint is None:
                    continue
                glyph_name = chunk[slot_index][1]
                if glyph_name:
                    filled_from_placeholders[codepoint] = glyph_name

    tray_mappings.update(filled_from_placeholders)
    return tray_mappings


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


def read_font_cmap(font: TTFont) -> dict[int, str]:
    cmap: dict[int, str] = {}
    for table in font["cmap"].tables:
        cmap.update(table.cmap)
    return cmap


def add_original_append_mappings(font: TTFont, cmap: dict[int, str]) -> int:
    original_cmap = read_font_cmap(font)
    append_codepoint = ORIGINAL_APPEND_START

    for _, glyph_name in sorted(original_cmap.items()):
        cmap[append_codepoint] = glyph_name
        append_codepoint += 1

    encoded_glyphs = set(original_cmap.values())
    for glyph_name in font.getGlyphOrder():
        if glyph_name == ".notdef" or glyph_name in encoded_glyphs:
            continue
        cmap[append_codepoint] = glyph_name
        append_codepoint += 1

    return append_codepoint - ORIGINAL_APPEND_START


def resolve_font_tray_path(value: Path | None) -> Path | None:
    if value is not None:
        if str(value).lower() == "none":
            return None
        return value

    for path in DEFAULT_FONT_TRAY_PATHS:
        if path.exists():
            return path

    return None


def map_font(
    input_path: Path, unikemet_path: Path, output_path: Path, tray_path: Path | None
) -> tuple[dict[int, tuple[str, str]], int]:
    font = TTFont(input_path, lazy=False)
    source_glyphs = {glyph_name.upper(): glyph_name for glyph_name in font.getGlyphOrder()}
    candidates_by_codepoint = read_unikemet_sign_names(unikemet_path)
    tray_mappings = (
        infer_tray_mappings(font, tray_path, unikemet_path) if tray_path is not None else {}
    )

    matched: dict[int, tuple[str, str]] = {}
    cmap: dict[int, str] = {
        0x0000: ".null",
        0x0008: ".null",
        0x0009: "nonmarkingreturn",
        0x000D: "nonmarkingreturn",
        0x001D: ".null",
        0x0020: "space",
    }
    original_append_count = add_original_append_mappings(font, cmap)

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

        if codepoint in matched:
            continue

        tray_glyph = tray_mappings.get(codepoint)
        if tray_glyph is not None:
            if codepoint in VISUAL_REJECTED_TRAY_MAPPINGS:
                continue
            target = codepoint + TARGET_OFFSET
            cmap[target] = tray_glyph
            matched[codepoint] = (target, tray_glyph)
            continue

    font["cmap"].tables = [make_format_12_cmap(cmap)]
    set_font_names(font)
    font.save(output_path)
    return matched, original_append_count


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
    parser.add_argument(
        "--font-tray",
        type=Path,
        default=None,
        help="EZGlyph Font Tray DOCX. Use 'none' to disable tray-order mapping.",
    )
    args = parser.parse_args()

    tray_path = resolve_font_tray_path(args.font_tray)
    matched, original_append_count = map_font(
        args.input, args.unikemet, args.output, tray_path
    )
    if tray_path is None:
        print("Font Tray DOCX not found; used glyph-name mapping only")
    else:
        print(f"Used Font Tray order: {tray_path}")
    print(f"Mapped {len(matched)} EZGlyph Hieratic glyphs")
    print(
        f"Appended {original_append_count} original glyph entries starting at "
        f"U+{ORIGINAL_APPEND_START:05X}"
    )
    print(f"Saved to: {args.output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
