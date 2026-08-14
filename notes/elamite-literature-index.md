# Elamite literature index (Proto-Elamite, Linear Elamite, Elamite cuneiform, Achaemenid Elamite/Old Persian)

Comprehensive inventory of every Elamite-related document found on this machine, across the whole
repo (not just `PDFs/`). This **supersedes/complements [dahl-proto-elamite-papers.md](dahl-proto-elamite-papers.md)**
for the Dahl-specific subset — that file has the full annotated summaries, diachronic-sign-change
discussion, and citation sources for the 6 confirmed Dahl papers; this file just lists them as rows
here for completeness and cross-references back rather than duplicating that analysis.

**Filename ≠ author/content is a proven gotcha in this project's `PDFs/` folder.** Confirmed cases:
files named `*proto-elamite*.pdf` (20192/21185/23196) are Unicode Consortium proposals by
**Anshuman Pandey**, not Dahl (though they cite him heavily); `21233-linear-elamite.pdf` and
`linear-elamite-unicode-proposal.pdf` are the same Pandey Unicode proposal for Linear Elamite;
`Proto_Elamite_60decimal.PDF` / `englund2004c.pdf` are both **Robert Englund**'s "State of
Decipherment of Proto-Elamite" (two versions of the same piece); `CH20-TheElamiteWorldversionfinale.pdf`
is **François Desset**'s *Linear Elamite* chapter from *The Elamite World* (2018), not a Dahl
Proto-Elamite chapter from the same book. Always verify with `pdftotext -l 3 <file> -` before trusting
a filename.

Several files here are the *same paper downloaded twice* under different names — flagged inline
in the table and summarized again below.

## Known duplicates / same-paper-twice

- `PDFs/The_Decipherment_of_Linear_Elamite_Writi.pdf` and `PDFs/desset2022.pdf` — identical paper (Desset et al. 2022, *Zeitschrift für Assyriologie* 112(1))
- `PDFs/ANEWWRITINGSYSTEMDISCOVEREDIN3rdMILLENNIUMBCEIRANTHEKONARSANDALGEOMETRICTABLETS.pdf` and `PDFs/A NEW WRITING SYSTEM DISCOVERED IN 3rd MILLENNIUM BCE IRAN THE KONAR SANDAL 'GEOMETRIC' TABLETS.pdf` — same Desset 2014 *Iranica Antiqua* article, two separate downloads (different file sizes/dates, same content)
- `PDFs/Roberts_PersianGrammarSketch.pdf` and `PDFs/Roberts_PersianGrammarSketch(1).pdf` — byte-identical (`diff` empty); modern Persian grammar sketch, not Elamite — listed here only as a duplicate-pair note, excluded from the table below as out of scope
- `PDFs/21233-linear-elamite.pdf` and `PDFs/linear-elamite-unicode-proposal.pdf` — same Pandey Unicode proposal L2/21-233, saved under two filenames

## Index

| filename | author | year | title | script/topic | one-line summary | path |
|---|---|---|---|---|---|---|
| `Dahl_2002_proto-elamite-sign-frequencies.pdf` | Jacob L. Dahl | 2002 | Proto-Elamite Sign Frequencies | Proto-Elamite | Earliest quantitative PE sign-frequency survey; see [dahl-proto-elamite-papers.md](dahl-proto-elamite-papers.md) | `PDFs/Dahl_2002_proto-elamite-sign-frequencies.pdf` |
| `cdlj2005_003.pdf` | Jacob L. Dahl | 2005 | Complex Graphemes in Proto-Elamite (CDLJ 2005:3) | Proto-Elamite | Defines CCS/CG grapheme taxonomy underlying the sign-list numbering; see Dahl notes | `PDFs/cdlj2005_003.pdf` |
| `protoelamite_signlist.pdf` | Jacob L. Dahl | 2006 | Proto-Elamite sign list (drawings) | Proto-Elamite | Canonical drawn sign catalog (M001–M400+); see Dahl notes | `PDFs/protoelamite_signlist.pdf` |
| `Dahl_ProtoElamite.pdf` | Jacob L. Dahl | 2007 | Deciphering proto-Elamite / Un-deciphering linear-Elamite | Proto-Elamite / Linear Elamite | Beijing Univ. lecture slide deck, PE/LE overview; see Dahl notes | `PDFs/Dahl_ProtoElamite.pdf` |
| `Dahl_2023_proto-elamite-linear-elamite-misunderstood-relationship.pdf` | Jacob L. Dahl | 2023 | Proto-Elamite and Linear Elamite, a Misunderstood Relationship? (*Akkadica* 144.2) | Proto-Elamite / Linear Elamite | Argues LE is an archaising re-invention, not a lineal PE descendant; see Dahl notes | `PDFs/Dahl_2023_proto-elamite-linear-elamite-misunderstood-relationship.pdf` |
| `Yeganeh_Dahl_2025_complexity-proto-elamite-administration.pdf` | Yeganeh, Holakooei, Nokandeh, Piran, Dahl (co-author) | 2025 | Complexity of Proto-Elamite Administration System: Insights from Compositional Data from Sealings and Tablets | Proto-Elamite | Archaeometric/compositional analysis of sealings + tablets from Susa, Malyan, Tepe Yahya; Dahl is a co-author, not lead | `PDFs/Yeganeh_Dahl_2025_complexity-proto-elamite-administration.pdf` |
| `Dahl_2025_early-development-cuneiform-writing-system.pdf` | Jacob L. Dahl | 2025 | The Early Development of the Cuneiform Writing System, and Its Regional Adaptation | Proto-Elamite / proto-cuneiform (comparative) | Compares PE vs proto-cuneiform adaptation trajectories; see Dahl notes | `PDFs/Dahl_2025_early-development-cuneiform-writing-system.pdf` |
| `20192-proto-elamite.pdf` | Anshuman Pandey | 2020 | Preliminary proposal to encode Proto-Elamite in Unicode (L2/20-192) | Proto-Elamite | Unicode encoding proposal, sign repertoire overview, cites Dahl heavily — NOT a Dahl paper | `PDFs/20192-proto-elamite.pdf` |
| `21185-proto-elamite.pdf` | Anshuman Pandey | 2021 | Proto-Elamite: Comparison of Sign Images and Glyphs (L2/21-185) | Proto-Elamite | Font/glyph QA doc comparing CDLI sign images to a proposed Unicode font | `PDFs/21185-proto-elamite.pdf` |
| `23196-proto-elamite.pdf` | Anshuman Pandey | 2023 | Proposal to encode Proto-Elamite in Unicode (L2/23-196) | Proto-Elamite | Revised/final Unicode encoding proposal | `PDFs/23196-proto-elamite.pdf` |
| `Proto_Elamite_60decimal.PDF` | Robert Englund | 2001 | The State of Decipherment of Proto-Elamite | Proto-Elamite | MPIWG preprint; sexagesimal/numerical-sign focus | `PDFs/Proto_Elamite_60decimal.PDF` |
| `englund2004c.pdf` | Robert Englund | 2004 | The State of Decipherment of Proto-Elamite (published version) | Proto-Elamite | Same piece as `Proto_Elamite_60decimal.PDF`, published version | `PDFs/englund2004c.pdf` |
| `ANEWWRITINGSYSTEMDISCOVEREDIN3rdMILLENNIUMBCEIRANTHEKONARSANDALGEOMETRICTABLETS.pdf` | François Desset | 2014 | A New Writing System Discovered in 3rd Millennium BCE Iran: The Konar Sandal "Geometric" Tablets (*Iranica Antiqua* 49) | Proto-Elamite-adjacent (undeciphered "geometric" script) | Reports a distinct undeciphered geometric sign system from Konar Sandal (Jiroft), compared to PE; duplicate download exists (see above) | `PDFs/ANEWWRITINGSYSTEMDISCOVEREDIN3rdMILLENNIUMBCEIRANTHEKONARSANDALGEOMETRICTABLETS.pdf` |
| `A NEW WRITING SYSTEM DISCOVERED IN 3rd MILLENNIUM BCE IRAN THE KONAR SANDAL 'GEOMETRIC' TABLETS.pdf` | François Desset | 2014 | (same as above) | Proto-Elamite-adjacent | Duplicate of the file above (different filename/download) | `PDFs/A NEW WRITING SYSTEM DISCOVERED IN 3rd MILLENNIUM BCE IRAN THE KONAR SANDAL 'GEOMETRIC' TABLETS.pdf` |
| `The_Decipherment_of_Linear_Elamite_Writi.pdf` | Desset, Tabibzadeh, Kervran, Basello, Marchesi | 2022 | The Decipherment of Linear Elamite Writing (*Zeitschrift für Assyriologie* 112(1), 11–60) | Linear Elamite | The core 2022 decipherment paper — full sign inventory, phonology, biscriptualism discussion | `PDFs/The_Decipherment_of_Linear_Elamite_Writi.pdf` |
| `desset2022.pdf` | Desset, Tabibzadeh, Kervran, Basello, Marchesi | 2022 | The Decipherment of Linear Elamite Writing | Linear Elamite | Duplicate of `The_Decipherment_of_Linear_Elamite_Writi.pdf` | `PDFs/desset2022.pdf` |
| `Breaking_the_Code_Ancient_Irans_Linear_E.pdf` | Desset, Tabibzadeh, Kervran, Basello, Marchesi | 2022 | Breaking the Code: Ancient Iran's Linear Elamite Script Deciphered | Linear Elamite | Popular-audience summary article by the same 5 authors, announcing the ZA 2022 decipherment result | `PDFs/Breaking_the_Code_Ancient_Irans_Linear_E.pdf` |
| `franois-desset-on-the-decipherment-of-linear-elamite-writing.pdf` | François Desset | 2021 | François Desset: On The Decipherment Of Linear Elamite Writing (The Postil Magazine) | Linear Elamite | Popular-magazine interview/op-ed by Desset himself, pre-dating the 2022 formal paper | `PDFs/franois-desset-on-the-decipherment-of-linear-elamite-writing.pdf` |
| `A_critical_analysis_of_Francois_Dessets.pdf` | Unknown/uncredited (anonymous critique) | undated (post-2020) | A critical analysis of Francois Desset's decipherment of Linear Elamite (Gutian) | Linear Elamite (dissenting view) | Argues Puzur-Inshushinak's inscriptions are Gutian, not Elamite; disputes Desset's readings | `PDFs/A_critical_analysis_of_Francois_Dessets.pdf` |
| `21233-linear-elamite.pdf` | Anshuman Pandey | 2021 | Preliminary proposal to encode Linear Elamite in Unicode (L2/21-233) | Linear Elamite | Unicode encoding proposal; duplicate content of `linear-elamite-unicode-proposal.pdf` | `PDFs/21233-linear-elamite.pdf` |
| `linear-elamite-unicode-proposal.pdf` | Anshuman Pandey | 2021 | Preliminary proposal to encode Linear Elamite in Unicode (L2/21-233) | Linear Elamite | Same document as `21233-linear-elamite.pdf`, different filename | `PDFs/linear-elamite-unicode-proposal.pdf` |
| `CH20-TheElamiteWorldversionfinale.pdf` | François Desset | 2018 | Linear Elamite Writing (ch. 20 in *The Elamite World*, Routledge) | Linear Elamite | Book chapter, pre-decipherment overview of LE corpus and prior attempts | `PDFs/CH20-TheElamiteWorldversionfinale.pdf` |
| `Linear_Elamite.pdf` | Michael Mäder | 2021 | Linear Elamite (entry in *The Encyclopedia of Ancient History: Asia and Africa*, Wiley) | Linear Elamite | Encyclopedia overview article; corpus, dating, PE-vs-LE relationship hypotheses | `PDFs/Linear_Elamite.pdf` |
| `Ein_baktrisches_Siegel_mit_elamischer_St.pdf` | Michael Mäder | 2021 | Ein baktrisches Siegel mit elamischer Strichschrift und die Suche nach dem Land Šimaški (*Archiv für Orientforschung* Bd. 54) | Linear Elamite | German-language article on a Bactrian seal bearing Linear Elamite script, tied to locating the land of Šimaški | `PDFs/Ein_baktrisches_Siegel_mit_elamischer_St.pdf` |
| `Elamite_Sources.pdf` | Matthew W. Stolper | forthcoming (in *A Companion to the Achaemenid Empire*, Wiley-Blackwell) | Elamite Sources | Elamite cuneiform / Achaemenid Elamite | Survey of Achaemenid-period Elamite text genres (royal inscriptions, admin docs, seals) | `PDFs/Elamite_Sources.pdf` |
| `stolper2004original.pdf` | Matthew W. Stolper | 2004 | Elamite (ch. 3, in Woodard ed., *The Cambridge Encyclopedia of the World's Ancient Languages*) | Elamite language/cuneiform, general | Broad reference chapter: historical/cultural context, corpus geography, chronology | `PDFs/stolper2004original.pdf` |
| `Khaikjan1998TheElamiteLanguagedocumentaAsianaIv.pdf` | Margaret Khačikjan (per filename; likely) | 1998 | *The Elamite Language* (Documenta Asiana IV) | Elamite language, general | Scanned, no extractable text layer (pdffonts empty) — title/author inferred from filename only, matches a known real monograph; **not independently verified from page content**, worth a manual check | `PDFs/Khaikjan1998TheElamiteLanguagedocumentaAsianaIv.pdf` |
| `Sprache_und_Schriften_in_Elam.pdf` | author not given in PDF metadata/text (2-page popular piece) | undated | Sprache und Schriften in Elam | Elamite language/cuneiform, general | Short (2pp) German popular-science overview of Elamite language and its cuneiform writing phases | `PDFs/Sprache_und_Schriften_in_Elam.pdf` |
| `Development_of_the_Elamite_Cuneiform_Fon.pdf` | Tytus Mikołajczak (Persepolis Fortification Archive Project / OCHRE) | undated | Development of the Elamite Cuneiform Font | Achaemenid Elamite cuneiform | Documents design of a computer font for Achaemenid Elamite cuneiform in the OCHRE database system | `PDFs/Development_of_the_Elamite_Cuneiform_Fon.pdf` |
| `fonts/Old Persian sign list.pdf` | Unicode Consortium (Unicode Standard 5.1 chart) | — | Old Persian sign list | Old Persian (companion script to Achaemenid Elamite) | Old Persian cuneiform sign chart (Bisitun/Persepolis forms); not Elamite itself but the standard companion script in Achaemenid trilingual inscriptions | `fonts/Old Persian sign list.pdf` |

## Checked and excluded as out of scope

- `PDFs/ebrahim-2019-Mathematics_of_Uruk_and_Susa_3500-3000_BCE.pdf` — Assad Ebrahim, popular blog post on early Mesopotamian/Susa numeracy; mentions Susa but is about proto-cuneiform/token numeracy generally, not Elamite script — left out of the table as marginal; flag if the project wants a "numeracy/tokens" adjacent category later.
- `PDFs/Roberts_PersianGrammarSketch.pdf` (+ `(1)` duplicate), `PDFs/grammarofpersian00joneiala_bw.pdf`, `PDFs/Lexique_comparatif_de_onze_langues_irani.pdf` — modern/comparative Persian and Iranian-language grammars, unrelated to Elamite or Old Persian cuneiform specifically.
- `abc/CuneiformSignList.pdf`, `dicts/CuneiformSignList.pdf` (identical filenames, likely same file, not independently re-checked), `dicts/sumerian_dictionary_transliterated.pdf` — general Sumerian/Akkadian cuneiform sign lists (Kateřina Šašková, Pilsen 2021); no Elamite content found (`grep -i elamite` on extracted text: no hits).
- `fonts/Akkadian Assyrian.pdf`, `fonts/Hittite.pdf`, `fonts/Neo-assyrien.pdf`, `fonts/Paleo-Babylonien.pdf`, `fonts/Symbola.pdf`, `fonts/Fonts sample.pdf` — cuneiform/font specimens for other scripts, not Elamite.
- No `texts/PDFs/` or `texts/PDFs 2/` directories exist in the current repo state (an earlier session's search result referencing them did not reproduce here — only `texts/A Sheinem Cholem...pdf`, `texts/voynich/Voynich-Manuscript.pdf`, and `texts/Bibles/pdfs/gen{1,2,3}.pdf` exist under `texts/`, none Elamite-related).
- The remaining ~600 files in `PDFs/` were screened by filename only (per task instructions, not opened individually) — none matched Elamite/Elam/Susa/Achaemenid/Persepolis/Anshan/Khuzestan patterns beyond what's listed above.

## Notes for future maintenance

- Update this table (don't create a second index) when new Elamite-related PDFs are added to the repo.
- If `Khaikjan1998...pdf` is ever needed for content (not just cataloging), it will require OCR — it has no text layer at all.
- `A_critical_analysis_of_Francois_Dessets.pdf` has no visible author name in the extracted text; if that matters later, check the PDF metadata/original hosting URL (not captured in this pass).
