#!/usr/bin/env python3
"""Add the Proto-Elamite proposal cmap to the PDF-extracted font."""

from __future__ import annotations

import argparse
from pathlib import Path

from fontTools.ttLib import TTFont, newTable
from fontTools.ttLib.tables._c_m_a_p import CmapSubtable


# Extracted from the /ToUnicode CMap for Uni1BD00ProtoElamiteRefactored in
# Unicode L2/23-196, object 811.
CID_TO_CODEPOINT_RANGES = (
    (0x0003, 0x00FF, 0x1BD00),
    (0x0100, 0x0102, 0x1BDFD),
    (0x0103, 0x01FF, 0x1BE00),
    (0x0200, 0x0202, 0x1BEFD),
    (0x0203, 0x02FF, 0x1BF00),
    (0x0300, 0x0302, 0x1BFFD),
    (0x0303, 0x03FF, 0x1C000),
    (0x0400, 0x0402, 0x1C0FD),
    (0x0403, 0x04FF, 0x1C100),
    (0x0500, 0x0502, 0x1C1FD),
    (0x0503, 0x05FF, 0x1C200),
    (0x0600, 0x0602, 0x1C2FD),
    (0x0603, 0x0666, 0x1C300),
)


def build_cmap(font: TTFont) -> dict[int, str]:
    glyph_order = font.getGlyphOrder()
    cmap: dict[int, str] = {}
    for cid_start, cid_end, codepoint_start in CID_TO_CODEPOINT_RANGES:
        for cid in range(cid_start, cid_end + 1):
            if cid >= len(glyph_order):
                raise ValueError(f"CID {cid} is outside glyph order")
            cmap[codepoint_start + cid - cid_start] = glyph_order[cid]
    return cmap


def cmap_subtable(platform_id: int, plat_enc_id: int, cmap: dict[int, str]) -> CmapSubtable:
    table = CmapSubtable.newSubtable(12)
    table.platformID = platform_id
    table.platEncID = plat_enc_id
    table.language = 0
    table.cmap = cmap.copy()
    return table


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("source", type=Path)
    parser.add_argument("output", type=Path)
    args = parser.parse_args()

    font = TTFont(args.source, recalcBBoxes=True, recalcTimestamp=True)
    cmap = build_cmap(font)

    cmap_table = newTable("cmap")
    cmap_table.tableVersion = 0
    cmap_table.tables = [
        cmap_subtable(0, 6, cmap),
        cmap_subtable(3, 10, cmap),
    ]
    font["cmap"] = cmap_table

    args.output.parent.mkdir(parents=True, exist_ok=True)
    font.save(args.output)
    print(f"wrote {len(cmap)} cmap entries")
    print(args.output)


if __name__ == "__main__":
    main()
