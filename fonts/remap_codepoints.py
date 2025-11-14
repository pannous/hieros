#!/usr/bin/env python3
import sys
from fontTools.ttLib import TTFont

def remap_font_codepoints(font_path, target_base, output_path):
    """
    Remap all glyph codepoints in a font to start at target_base.

    Args:
        font_path: Path to input TTF/OTF font
        target_base: Target base codepoint (e.g., 0x12F000)
        output_path: Path to save remapped font
    """
    tt = TTFont(font_path, lazy=False)

    # Get glyph order and cmap
    glyph_order = tt.getGlyphOrder()

    # Collect current codepoint mappings
    old_mappings = {}  # glyph_name -> [codepoints]
    for table in tt["cmap"].tables:
        if not hasattr(table, "cmap"):
            continue
        for cp, name in table.cmap.items():
            if name not in old_mappings:
                old_mappings[name] = []
            old_mappings[name].append(cp)

    # Build new codepoint mapping
    # Start from target_base and assign sequentially to each glyph
    new_mappings = {}
    current_cp = target_base

    for glyph_name in glyph_order:
        if glyph_name.startswith(".notdef"):
            continue
        # Skip glyphs that don't have codepoints
        if glyph_name not in old_mappings:
            continue
        new_mappings[glyph_name] = current_cp
        current_cp += 1

    # Update cmap tables - use format 12 for large codepoints (> 0xFFFF)
    cmap_table = tt["cmap"]

    # Remove old tables that can't handle large codepoints
    cmap_table.tables = [t for t in cmap_table.tables if hasattr(t, "format") and t.format >= 12]

    if not cmap_table.tables:
        # If no suitable table exists, create a new format 12 table
        from fontTools.ttLib.tables._c_m_a_p import table__c_m_a_p
        cmap_table.tables = []

    # Find or create a format 12 table (supports full Unicode range)
    format12_table = None
    for table in cmap_table.tables:
        if hasattr(table, "format") and table.format == 12:
            format12_table = table
            break

    if format12_table is None:
        # Create a new format 12 table
        from fontTools.ttLib.tables._c_m_a_p import cmap_format_12
        format12_table = cmap_format_12(12)
        format12_table.platformID = 3  # Windows
        format12_table.platEncID = 10  # Unicode full repertoire
        format12_table.language = 0
        format12_table.cmap = {}
        cmap_table.tables.append(format12_table)

    # Clear and update mappings
    format12_table.cmap.clear()
    for glyph_name, cp in new_mappings.items():
        format12_table.cmap[cp] = glyph_name

    # Save modified font
    tt.save(output_path)

    # Print summary
    print(f"Remapped {len(new_mappings)} glyphs")
    print(f"Target range: U+{target_base:05X} to U+{target_base + len(new_mappings) - 1:05X}")
    print(f"Saved to: {output_path}")

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("usage: python remap_codepoints.py INPUT.ttf TARGET_BASE [OUTPUT.ttf]")
        print("example: python remap_codepoints.py oracle.ttf 0x12F000 oracle_remapped.ttf")
        sys.exit(1)

    font_path = sys.argv[1]
    target_base = int(sys.argv[2], 0)  # Allow hex or decimal
    output_path = sys.argv[3] if len(sys.argv) > 3 else font_path.replace(".ttf", "_remapped.ttf")

    remap_font_codepoints(font_path, target_base, output_path)
