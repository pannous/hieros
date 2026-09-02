# Comparing my_egyptian_dictionary.csv against external dictionaries

Two scripts, one shared library, all outputs under `probes/`.

| script | output | purpose |
| ------ | ------ | ------- |
| `scripts/egy_find_missing.py` | `probes/egyptian_missing_entries.md` + `.tsv` | words other dictionaries have that mine lacks, already transcribed to Unicode hieroglyphs |
| `scripts/egy_find_suspect.py` | `probes/egyptian_suspect_entries.md` + `.tsv` | entries of mine that no other dictionary corroborates |
| `scripts/egy_dict_common.py` | – | parsing, Gardiner/MdC transcription, source adapters |

## Sources
Downloaded into `probes/egy_dict_sources/` (gitignored, ~650 MB with the clones);
inventory with licenses in `probes/egy_dict_sources/SOURCES.md`. The lexical ones:
Vygus (47 947 entries, via cursedcoder/pharalex), TLA/AED (35 169 lemmas),
Wikidata Egyptian lexemes (14 077), Wiktionary via kaikki (4 507),
fayrose/MiddleEgyptianDataset (11 022), a Chinese lexicon (656).

## Gotchas found the hard way
- The dictionary is a **markdown pipe table**, not CSV: `glyphs | meaning | note`, with
  345 rows carrying an extra `phon` column. Take the arity from the raw pipe count.
- Only **`gardiner_unicode.tsv`** gives a complete Gardiner→Unicode map (4043 codes).
  `unicodedata.name()` alone covers just the 1072 signs of the base block; Extended-A/B
  characters are named by hex, and Unikemet's `kEH_Cat` is a catalog code, not Gardiner.
- Transliteration → hieroglyphs must **not** strip combining marks before mapping:
  NFD turns `ḥ` into `h`+dot, which silently swaps V28 𓎛 for O4 𓉔. Map the precomposed
  character first, drop leftover marks (`i̯`) afterwards.
- AED/TLA lemma dumps carry **no hieroglyphic writing** — those entries have to be spelled
  out uniliterally and are reported in their own section, never mixed with attested writings.
- Sign catalogues (Unikemet descriptions, pharalex sign meanings) must only *attest* a
  glyph sequence, never *contradict* a gloss, or they veto correct lexical meanings.
- Merging proposals per glyph sequence and sorting by number of independent sources puts
  the trustworthy additions (4 sources agreeing) at the top of a 14 500-row list.

## Dead ends
TLA's web app exposes no public lemma API. `hieroglyphicus` ships an SQL stub.
Ramsès needs a login; Faulkner is still in copyright.
