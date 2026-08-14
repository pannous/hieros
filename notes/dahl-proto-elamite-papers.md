# Jacob L. Dahl — Proto-Elamite papers: inventory + summaries

Task: gather every locally-available and freely-downloadable Dahl paper on Proto-Elamite, summarize
for internal reference. Read-only research task, no code/data files touched.

## Key correction to task premise

There is **no Oxford DPhil thesis** by Dahl on Proto-Elamite. His PhD is from **UCLA, 2003**
(under the CDLI orbit, Robert Englund's program), followed by a 2003–2005 CNRS postdoc in Paris
working on the Louvre Proto-Elamite tablets. He later became a permanent Oxford faculty member
(Faculty of Asian and Middle Eastern Studies / Wolfson College), which is presumably why an Oxford
DPhil was assumed. No such thesis exists in ORA.

## Filename-vs-author corrections (files that looked Dahl-related but aren't)

| File | Actual author | Notes |
|---|---|---|
| `20192-proto-elamite.pdf` | Anshuman Pandey | Unicode encoding proposal for Proto-Elamite (cites Dahl heavily) |
| `21185-proto-elamite.pdf` | Anshuman Pandey | Unicode glyph/sign-image comparison doc |
| `23196-proto-elamite.pdf` | Anshuman Pandey | Unicode encoding proposal, 2023 revision |
| `21233-linear-elamite.pdf` | Anshuman Pandey | Unicode proposal for Linear Elamite (not Proto-Elamite, not Dahl) |
| `Proto_Elamite_60decimal.PDF` | Robert Englund | "The State of Decipherment of Proto-Elamite" (2001 MPIWG preprint) |
| `englund2004c.pdf` | Robert Englund | Same "State of Decipherment" piece, published version (2004) |
| `CH20-TheElamiteWorldversionfinale.pdf` | **François Desset** | "Linear Elamite Writing" chapter in *The Elamite World* (2018) — not Dahl, and not even Proto-Elamite (it's the Linear Elamite chapter) |

## Confirmed Dahl papers (local + newly downloaded)

| Filename | Year | Title | One-line summary | Local path |
|---|---|---|---|---|
| `cdlj2005_003.pdf` | 2005 | Complex Graphemes in Proto-Elamite (CDLJ 2005:3) | Defines CCS/CG grapheme-combination taxonomy; introduces the collated sign-list underlying most later Dahl numbering | `PDFs/cdlj2005_003.pdf` |
| `protoelamite_signlist.pdf` | 2006 | Proto-Elamite sign list (drawings), © Jacob L. Dahl | Pure sign-catalog of drawn glyphs and their `~` sub-variants (M001…M400+), the canonical numbering this project's `abc/proto-elamite-allographs.tsv` cites | `PDFs/protoelamite_signlist.pdf` |
| `Dahl_ProtoElamite.pdf` | 2007 | "Deciphering proto-Elamite / Un-deciphering linear-Elamite" (Beijing Univ. lecture slides) | Conference-talk slide deck, high-level overview of writing origins, PE paleography/chronology questions, and PE vs LE relationship; light on hard data (slide format) | `PDFs/Dahl_ProtoElamite.pdf` |
| `Dahl_2023_proto-elamite-linear-elamite-misunderstood-relationship.pdf` | 2023 | Proto-Elamite and Linear Elamite, a Misunderstood Relationship? (*Akkadica* 144.2, 107–126) | Argues Linear Elamite is **not** a lineal descendant of Proto-Elamite but a deliberately archaising new script ("schismogenesis") built by late-3rd-millennium scribes drawing on recovered PE tablets + Old Akkadian cuneiform knowledge. **Most relevant paper found for the diachronic-sign-change question — see flags below.** | `PDFs/Dahl_2023_proto-elamite-linear-elamite-misunderstood-relationship.pdf` |
| `Dahl_2025_early-development-cuneiform-writing-system.pdf` | 2025 | The Early Development of the Cuneiform Writing System, and Its Regional Adaptation (National Museum of World Writing Academic Series, Incheon conf. 2024, ORA CC-BY) | Traces tokens → numerical tablets → numero-ideographic tablets in Mesopotamia; compares PE vs proto-cuneiform adaptation; argues writing invention was a response to economic/resource pressure, not bureaucratic growth; discusses why PE went obsolete while proto-cuneiform evolved | `PDFs/Dahl_2025_early-development-cuneiform-writing-system.pdf` |
| `Yeganeh_Dahl_2025_complexity-proto-elamite-administration.pdf` | 2025 | Complexity of Proto-Elamite Administration System: Insights from Compositional Data from Sealings and Tablets (Yeganeh, Holakooei, Nokandeh, Piran, **Dahl**) | Dahl is a co-author, not lead — archaeometric/compositional-analysis paper on sealings + tablets from Susa, Malyan, Tepe Yahya; scanned/image-based PDF, largely outside sign-paleography scope | `PDFs/Yeganeh_Dahl_2025_complexity-proto-elamite-administration.pdf` |
| `Dahl_2002_proto-elamite-sign-frequencies.pdf` | 2002 | Proto-Elamite Sign Frequencies (CDLB 2002:1) | Earliest quantitative sign-frequency survey (~1,900 non-numerical signs from ~1,600 texts, Meriggi-based numbering); ~1,050 signs are hapax, ~300 attested twice; shows M36's variant-merging jumps its count from 128→221; **explicitly states "we have found no internal development in the writing system"** — directly relevant to (and in tension with) this project's positional/frequency work | `PDFs/Dahl_2002_proto-elamite-sign-frequencies.pdf` |
| `Early_Writing_in_Iran_a_Reappraisal.pdf` | 2009 | Early Writing in Iran, a Reappraisal (*Iran* 47: 23–31) | Argues Proto-Elamite is decipherable as a true proto-writing system, while Linear Elamite is not (contra earlier assumptions); traces the Uruk-expansion origins of PE accounting tools. Byline confirmed by `pdftotext -l 2`: "By Jacob l. Dahl, University of Oxford" — matches this file's title/page range exactly. **Previously flagged as paywalled/academia.edu-only; now confirmed and downloaded via the Academia.edu bundle.** | `PDFs/Academia.edu_Bundle_-_Proto_Elamite_writing_in_Iran/most_similar_papers_to_this_one/Early_Writing_in_Iran_a_Reappraisal.pdf` |
| `Dahl_Petrie_and_Potts_2013_Chronological.pdf` | 2013 | Chronological parameters of the earliest writing system in Iran (ch. 18, pp. 353–378, in Petrie, C.A. (ed.), *Ancient Iran and Its Neighbours: Local Developments and Long-Range Interactions in the Fourth Millennium BC*, Oxbow) | Jacob Dahl, Cameron A. Petrie & D.T. Potts, co-authored — not previously known to either note file. Establishes the chronological framework (14C + stratigraphy) for the earliest PE writing horizon in Iran, alongside the Godin Tepe/Susa/Malyan comparanda in the same volume. | `PDFs/Academia.edu_Bundle_-_Proto_Elamite_writing_in_Iran/most_similar_papers_to_this_one/Dahl_Petrie_and_Potts_2013_Chronological.pdf` |

## Downloaded, with exact sources

- **Dahl 2025**, "The Early Development of the Cuneiform Writing System, and Its Regional Adaptation" — Oxford Research Archive, CC BY 4.0. Source: `https://ora.ox.ac.uk/objects/uuid:4e617379-45da-444b-a9aa-1ca2d01f10bf/files/s9s161833v`
- **Dahl 2023**, "Proto-Elamite and Linear Elamite, a Misunderstood Relationship?" — Oxford Research Archive, accepted-manuscript / CC BY. Source: `https://ora.ox.ac.uk/objects/uuid:51adb1c2-61de-438c-9b29-125addf3d2a1/files/r6w924c626`
- **Yeganeh et al. 2025** (Dahl co-author) — Oxford Research Archive, CC BY. Source: `https://ora.ox.ac.uk/objects/uuid:a8f083aa-bb85-458b-bb40-915206867912/files/rcz30pv50c`
- **Dahl 2002**, "Proto-Elamite Sign Frequencies" — Cuneiform Digital Library Bulletin 2002:1, open access via CDLI. Source: `https://cdli.earth/articles/cdlb/2002-1.pdf`

## Found but NOT downloaded (paywalled / gated, per instructions to skip rather than bypass)

- Dahl, Hawkins & Kelley 2018, "Labour Administration in Proto-Elamite Iran", in Garcia-Ventura (ed.) *What's in a Name?*, Ugarit-Verlag, 15–44 — academia.edu only (403).
- Dahl, Hessari & Yousefi 2013, "The Proto-Elamite Tablets from Tepe Sofalin", *Iranian Journal of Archaeological Studies* — academia.edu only (403).
- Dahl 2018, "The Proto-Elamite Writing System", in Álvarez-Mon/Basello/Wicks (eds.) *The Elamite World*, Routledge, 383–396 — Taylor & Francis paywall, no open copy found. (Note: the locally-present `CH20-TheElamiteWorldversionfinale.pdf` is a *different* chapter from the same book, by Desset, not this one.)
- "Images Hidden in Script: The Invention of Writing in Ancient Iran" — academia.edu only (403), unclear year/venue, not pursued further.
- Dahl 2019, *Tablettes et Fragments Proto-Élamites* (Textes Cunéiformes Musée du Louvre 32), Éditions Khéops — a physical Louvre catalog volume, not a web-distributable PDF; not pursued. (Cited repeatedly in the 2023 paper as the source of the clearest early/standard/late sign-form figures — see below, worth chasing physically if this project wants that data.)

Not attempted: general Google Scholar / Sci-Hub-style bypass — out of scope per task instructions ("skip anything paywalled... rather than trying to bypass access").

## Flagged: diachronic sign-change / paleographic-development passages

This is the most actionable finding. Nothing found explicitly mentions **M206** changing into another
glyph by name, but the 2023 paper ("Misunderstood Relationship") contains Dahl's most direct
published statement of a general **within-Proto-Elamite diachronic sign-simplification model**, plus
one concrete case of a sign being **renumbered** after re-collation:

1. **General diachronic model** (`Dahl_2023...pdf`, running text ~p.9–10 of the PDF extraction):
   > "we can observe significant change in the sign forms, often going from complex, almost decorated, versions in the earliest texts, to standard simpler forms... with a further modification in the very latest texts involving a clear 'cuneiformication'"

   Dahl states PE sign forms move through **three phases**: early/decorated → standard/simplified →
   very-late/cursive-"cuneiformicized". He cites his own **Dahl 2019** (Louvre catalog, figs. 3–4, pp.
   62–65) and **Dahl et al. 2013** (fig. 18.18, p. 371) as where this is actually plotted out — those
   are the primary sources to chase next if this project wants the actual glyph-stage drawings, and
   neither is in this project's PDFs/ folder or freely downloadable online.

2. **Sign renumbering case (M483 → M346d)**: a sign originally catalogued as **M483** in Dahl's 2005
   list was "abandoned when I realised the correct graphical visualisation of the sign — two
   overlapping M346" and "then renamed **M346d**." This is a documented instance of Dahl's own sign
   list mutating a sign's identity/label between his 2005 and later ordinated versions — relevant
   precedent for how this project should treat sign-ID churn across Dahl sign-list versions, though it
   is a cataloging correction, not an attested scribal-diachronic change.

3. **M346/M347/M352n "cuneiformication" open question**: Dahl explicitly asks (but leaves unanswered,
   citing the PE→LE gap as unbridged) how these stylus-impressed circular signs would have evolved
   "when moving to an ever more cursive script" — directly the kind of "sign X evolving into sign Y"
   question the project's M206 hypothesis is chasing, just for different sign numbers.

4. **No direct M206 hit**: `cdlj2005_003.pdf` §3.7 discusses **M206g / M206j** only in a tablet-join
   argument (M206j "perhaps a graphic variant of M206g, a sign representing an animal by-product"),
   not a diachronic claim — this is allography within one archival moment, not sign change over time.
   `protoelamite_signlist.pdf` lists variants `M206~b, ~d, ~f, ~fa, ~g, ~i, ~j` as bare drawings with
   no accompanying discussion (it's a catalog, not prose). **No paper found so far states M206
   specifically changes into another glyph over time** — if the project's working hypothesis
   attributes that claim to Dahl, the source is either Dahl 2019 (Louvre catalog, not obtained) or a
   verbal/lecture source not captured in these six papers, and should be re-verified before being
   treated as established.

## Tension worth flagging: Dahl's own position shifted 2002 → 2023

- **2002** (`Dahl_2002_proto-elamite-sign-frequencies.pdf`, §1): *"we have found no internal
  development in the writing system"* — Dahl explicitly denies diachronic development in PE's sign
  repertoire (contrasted with proto-cuneiform, which Damerow/Englund had shown *does* evolve).
- **2023** (`Dahl_2023...pdf`, see above): now states *"we can observe significant change in the sign
  forms, often going from complex, almost decorated, versions in the earliest texts, to standard
  simpler forms... with a further modification in the very latest texts involving a clear
  'cuneiformication'."*

These are not strictly contradictory (2002 talks about the size/structure of the *sign repertoire*
showing no phase-based growth; 2023 talks about *glyph-form* simplification within attested signs),
but they read as a real shift in emphasis over two decades of work, and should be cited as **Dahl 2023**,
not "Dahl" undated, when this project claims he documented diachronic sign change — the 2002 paper
would read as contradicting that claim if not sequenced correctly.

## Bibliography note

Dahl's CDLI author page (`https://cdli.earth/authors/590`) is the best index of his output if a more
exhaustive future pass is wanted; it lists items (e.g. Dahl 2002 "Proto-Elamite Sign Frequencies" in
CDLB 2002:1) not chased down in this pass — that CDLB piece in particular sounds directly relevant to
this project's sign-frequency work (`texts/proto-elamite/positional-distribution.tsv` etc.) and is
worth a targeted follow-up.
