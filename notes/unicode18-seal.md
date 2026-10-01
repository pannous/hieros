# Unicode 18 Small Seal ⇔ oracle bone

- Unicode 18.0 (Sept 2026) encoded Small Seal 小篆 as its own script: U+3D000..U+3FC3F, 11,328 chars. Oracle bone is still unencoded (tentative Plane 3 slots only).
- UCD data: `dicts/unicode18/SealSources.txt`; `kSEAL_MCJK` = modern CJK equivalent (the join key).
- macOS/CoreText dropped U+3D7D1..3E029 from Kaiyuan's fragmented format-12 cmap (boxes in Sublime for 可 就 …). `scripts/fix_seal_font.py` orders glyphs by code point and fills gaps with a hollow-box placeholder → one cmap group, all 11,328 accepted. Probe: `osascript -l JavaScript probes/coretext_file_charset.js FONT HEX…`.
- Font: Kaiyuan Small Seal (OFL, github.com/frankslin/kaiyuan-small-seal-font) has no releases; the font is a CI artifact (`gh run download <id> -R frankslin/kaiyuan-small-seal-font`). Covers 10,980/11,328. Installed to ~/Library/Fonts.
- LXGW Seal (github.com/lxgw/LxgwSeal) is alpha: only 105 seal codepoints.
- Oracle PUA font `fonts/oracle_bone_script_102B00.ttf` (U+102B00..10FFCF): glyph names `uniXXXX`/`uXXXXX`/`HXXXXX` name the modern hanzi; `glyphN`/`uF…` are unidentified.
- `scripts/align_seal_oracle.py` → `docs/oracle_seal.md` (986 hanzi with both forms). Visual check: `probes/render_oracle_seal_rows.py`.
- Editors (Sublime/CoreText) show boxes for U+3Dxxx: the OS text engine doesn't know Unicode 18 yet, so no font fallback. View via `fonts/oracle_seal.html` (embeds both fonts via @font-face), generated alongside the MD.
- 2026-10-01 moved the oracle PUA block U+F5000..1024CF → U+102B00..10FFCF (`scripts/move_oracle_pua.py font|text`):
  NewGardinerOmni maps U+F0000..F8361 (Aegyptus signs F3000..F4B92 + its pre-scaled composition glyphs, e.g. U+F50D7 =
  u13A4F at 75 %), so CoreText rendered 13,154 oracle code points as shrunk hieroglyphs. The rest of Plane 15 (31,900
  slots) is too small for 54,480 glyphs; the start of Plane 16 is Apple's (SFNS U+100BB7, SFCamera U+10045D, SF Symbols).
  Moved: abc/oracle_bone_script_102B00.{abc,codes} (were _0F5000, dicts/ symlinks follow), docs/oracle.md,
  docs/oracle_seal.md, fonts/oracle_seal.html. Egyptian and other docs untouched; they may still hold old-range oracle
  characters (only oracle files were moved). papers/si-child-worm-fetus still uses the old F5000 font, which stays in fonts/.
  Check: `osascript -l JavaScript probes/seal/coretext_fallback.js 102BD7` → Oracular.
