# Unicode 18 Small Seal ⇔ oracle bone

- Unicode 18.0 (Sept 2026) encoded Small Seal 小篆 as its own script: U+3D000..U+3FC3F, 11,328 chars. Oracle bone is still unencoded (tentative Plane 3 slots only).
- UCD data: `dicts/unicode18/SealSources.txt`; `kSEAL_MCJK` = modern CJK equivalent (the join key).
- Font: Kaiyuan Small Seal (OFL, github.com/frankslin/kaiyuan-small-seal-font) has no releases; the font is a CI artifact (`gh run download <id> -R frankslin/kaiyuan-small-seal-font`). Covers 10,980/11,328. Installed to ~/Library/Fonts.
- LXGW Seal (github.com/lxgw/LxgwSeal) is alpha: only 105 seal codepoints.
- Oracle PUA font `fonts/oracle_bone_script_F5000.ttf`: glyph names `uniXXXX`/`uXXXXX`/`HXXXXX` name the modern hanzi; `glyphN`/`uF…` are unidentified.
- `scripts/align_seal_oracle.py` → `docs/oracle_seal.md` (986 hanzi with both forms). Visual check: `probes/render_oracle_seal_rows.py`.
