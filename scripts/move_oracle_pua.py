#!/usr/bin/env python3
"""Move the Oracular oracle-bone PUA block from U+F5000..1024CF to U+102B00..10FFCF, out of NewGardinerOmni's range.

NewGardinerOmni maps U+F0000..F8361 (Aegyptus signs F3000..F4B92 plus its pre-scaled composition glyphs), so the
old block lost 13,154 code points to it in font fallback. The rest of Plane 15 is too small for the 54,480 glyphs, and
the start of Plane 16 is Apple's (SF Symbols; SFNS maps U+100BB7, SFCamera U+10045D), so the block ends Plane 16.

    python3 scripts/move_oracle_pua.py font            # fonts/oracle_bone_script_F5000.ttf → fonts/oracle_bone_script_102B00.ttf
    python3 scripts/move_oracle_pua.py text FILE…      # shifts the oracle code points of each file in place (also 0xf5abc and U+F5ABC)
"""
import re
import sys
from pathlib import Path
from fontTools.ttLib import TTFont

ROOT = Path(__file__).resolve().parent.parent
OLD_FONT = ROOT / "fonts/oracle_bone_script_F5000.ttf"
NEW_FONT = ROOT / "fonts/oracle_bone_script_102B00.ttf"
OLD_START = 0xF5000
OLD_END = 0x1024CF  # the font's last code point: one contiguous block of 54,480
NEW_START = 0x102B00  # ends at U+10FFCF
SHIFT = NEW_START - OLD_START
HEX_CODEPOINT = re.compile(r"\b(0x|U\+)([0-9a-fA-F]{5,6})\b")  # 0xf5abc or U+F5ABC


def moved(codepoint):
    return codepoint + SHIFT if OLD_START <= codepoint <= OLD_END else codepoint


def hex_moved(match):
    prefix, digits = match.groups()
    hex_digits = f"{moved(int(digits, 16)):x}"
    return prefix + (hex_digits if digits.islower() else hex_digits.upper())


def move_font():
    font = TTFont(OLD_FONT)
    for table in font["cmap"].tables:
        table.cmap = {moved(codepoint): glyph for codepoint, glyph in table.cmap.items()}
    font.save(NEW_FONT)
    print(f"wrote {NEW_FONT}")


def move_text(path):
    text = path.read_text()
    shifted = "".join(chr(moved(ord(character))) for character in text)
    shifted = HEX_CODEPOINT.sub(hex_moved, shifted)
    changed = sum(OLD_START <= ord(character) <= OLD_END for character in text)
    path.write_text(shifted)
    print(f"{path}: {changed} characters moved")


if __name__ == "__main__":
    if sys.argv[1:2] == ["font"]:
        move_font()
    elif sys.argv[1:2] == ["text"]:
        for name in sys.argv[2:]:
            move_text(Path(name))
    else:
        sys.exit(__doc__)
