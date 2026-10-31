# Hildegard's Lingua Ignota – dicts/GLOSSAE_HILDEGARDIS.tsv

Source: Steinmeyer/Sievers, Althochdeutsche Glossen III, nr. DCCCCXLIII "Glossae Hildegardis", pp. 390–404
(PDF: https://digital.ulb.hhu.de/download/pdf/308567.pdf; local copy probes/hildegard_pdf/glossae.pdf).
Manuscripts: a = Codex Wiesbadensis 2 (Riesencodex), b = Codex Cheltenhamensis 9303 (now Berlin).

The PDF's text layer is useless OCR (left column clipped by the page curve), so the table was
transcribed by reading 200 dpi page renders (pdftoppm) one page at a time. 1010 entries.

Columns: `ref` (page,line of the edition), `lingua_ignota`, `latin`, `german`, `note` (footnote variants).
- Sigla `ab` (gloss in both MSS) and `b` are dropped; they are kept only where a/b differ (`hůba a / huba b`)
  or a Latin word only appears in one MS (`linum b`).
- Many entries carry only a German gloss (written in the gloss slot in both MSS) → `latin` empty.
- Superscript vowels kept as ŏ ů (o/u with small e/o above).
- 398,36–37 repeat 398,10–11 (footnote confirms); 404,66–73 glosses in b are shifted by one (see note).

## dicts/GLOSSAE_HILDEGARDIS_modern.tsv
Same table + columns `english`, `deutsch` (modern meaning, my own identification from the Latin/MHG glosses).
`?` = plausible guess, `??` = highly uncertain (mostly obscure plant/bird/tool names with only an MHG gloss).
Source mapping files: probes/hildegard_pdf/modern_*.tsv (joined on `ref`).

## Spelling variants in `lingua_ignota`
Variant spellings from the footnotes are merged into the first column: `Aieganz / Aleganz b`.
Sigla: a = Wiesbaden Riesencodex, b = Cheltenham/Berlin codex, Denis = MDenis' print of the lost Vienna MS.
`a?` / `ab?` = editor's "oder X" (uncertain alternative reading). Unmarked first form = edition's main text.
Layout/correction notes (Son|ziz, Mis:kila, superscript insertions) stay in `note`.
