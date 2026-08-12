#!/usr/bin/env python3
"""Which sign pairs tend to land in NEARBY lines (not necessarily the same
line) within the same document - the "personnel list" pattern where each
numbered line is one entry (one leading sign + its own tally), e.g.

    3. M376        6. M376
    4. M149~b      (P009043: M149 at line 5, M376 at lines 3 and 6 - gap 2)
    5. M149~a2
    6. M376

This is a different relation than token-adjacency (analyze_residual_
collocations.py) or row-group columns (analyze_row_groups.py): two
signs can be "list neighbors" while several other single-item lines
sit between them.
"""
from __future__ import annotations

import collections
import math

from pe_signs import base_number, code_to_char_map, glyph_for, load_char_to_code, ROOT
from analyze_subheader_syllabary import extract_all_line_code_sequences

MAX_GAP = 3          # how many lines apart still counts as "nearby"
MIN_DOCS = 2         # require the pair to co-occur closely in >=2 documents
MIN_FREQ = 3         # skip near-singleton signs, too little signal either way
OUT_TSV = ROOT / "texts" / "proto-elamite" / "list-neighbor-pairs.tsv"


def per_document_leading_signs(rows: list[tuple[str, list[str]]]) -> dict[str, list[tuple[str, str]]]:
    """record id -> ordered list of each line's leading sign as (base
    number, exact code) - exact code kept so examples can show the real
    glyph that occurs, not an arbitrary stand-in for the base (document
    order is preserved because `rows` is built by a single top-to-bottom
    pass over the file)."""
    docs: dict[str, list[tuple[str, str]]] = collections.defaultdict(list)
    for rid, codes in rows:
        if codes:
            exact = codes[0]
            docs[rid].append((base_number(exact), exact))
    return docs


def main() -> None:
    char2code = load_char_to_code()
    code2char = code_to_char_map(char2code)
    rows = extract_all_line_code_sequences(char2code)
    docs = per_document_leading_signs(rows)

    total_freq = collections.Counter()
    for seq in docs.values():
        for base, _exact in seq:
            total_freq[base] += 1

    pair_docs = collections.defaultdict(set)     # (a,b) -> {record ids where they're within MAX_GAP}
    pair_min_gap = collections.defaultdict(lambda: 999)
    pair_examples = collections.defaultdict(list)  # (a,b) -> [(rid, gap, exact_a, exact_b)]

    for rid, seq in docs.items():
        n = len(seq)
        for i in range(n):
            for j in range(i + 1, min(i + MAX_GAP + 1, n)):
                (a, exact_a), (b, exact_b) = seq[i], seq[j]
                if a == b:
                    continue
                key = tuple(sorted((a, b)))
                # keep exact codes in the same order as the sorted base-pair key
                ex_pair = (exact_a, exact_b) if key == (a, b) else (exact_b, exact_a)
                gap = j - i
                pair_docs[key].add(rid)
                if gap < pair_min_gap[key]:
                    pair_min_gap[key] = gap
                if len(pair_examples[key]) < 4:
                    pair_examples[key].append((rid, gap, *ex_pair))

    # Raw doc-count ranking is dominated by the handful of mega-common
    # classifiers (M157, M288, M387...) co-occurring with everything just
    # from sheer volume. Score by doc-count normalized against how often
    # each sign appears at all (~PMI), so a pair that's RARE individually
    # but SPECIFICALLY clusters together outranks two common signs that
    # cluster only because they're everywhere.
    candidates = []
    for (a, b), docset in pair_docs.items():
        ndocs = len(docset)
        fa, fb = total_freq[a], total_freq[b]
        if ndocs < MIN_DOCS or fa < MIN_FREQ or fb < MIN_FREQ:
            continue
        score = ndocs / math.sqrt(fa * fb)
        candidates.append((a, b, score, ndocs, fa, fb, pair_min_gap[(a, b)]))
    candidates.sort(key=lambda c: -c[2])

    def format_examples(a: str, b: str) -> str:
        # show the REAL glyph that occurred in each example, not a generic
        # stand-in for the base - a base with several graphic variants
        # (e.g. M251/M251~c, M384/M384~d) can otherwise display a glyph
        # that never actually appears in the cited lines, which reads as
        # the pairing being wrong when it's the display that's wrong.
        parts = []
        for rid, g, exact_a, exact_b in pair_examples[(a, b)]:
            parts.append(f"{rid}(gap{g}: {glyph_for(exact_a,code2char)}{exact_a}/{glyph_for(exact_b,code2char)}{exact_b})")
        return ", ".join(parts)

    print(f"{len(candidates)} sign pairs co-occurring within {MAX_GAP} lines in >={MIN_DOCS} documents (freq>={MIN_FREQ} each)")
    print("(glyph_a/glyph_b are ONE representative variant of the base sign - see the")
    print(" examples column for the actual glyph attested in each specific occurrence)")
    print()
    for a, b, score, ndocs, fa, fb, mingap in candidates[:40]:
        ex = format_examples(a, b)
        print(f"score={score:.3f}  {ndocs:3d} docs  {glyph_for(a,code2char)}{a}(freq{fa}) <-> {glyph_for(b,code2char)}{b}(freq{fb})  min_gap={mingap}   e.g. {ex}")

    with OUT_TSV.open("w", encoding="utf-8") as f:
        f.write("score\tnum_documents\tmin_gap\tsign_a\tglyph_a\tfreq_a\tsign_b\tglyph_b\tfreq_b\texamples_with_actual_glyphs\n")
        for a, b, score, ndocs, fa, fb, mingap in candidates:
            ex = format_examples(a, b)
            f.write(f"{score:.4f}\t{ndocs}\t{mingap}\t{a}\t{glyph_for(a,code2char)}\t{fa}\t{b}\t{glyph_for(b,code2char)}\t{fb}\t{ex}\n")
    print()
    print(f"wrote {OUT_TSV}")


if __name__ == "__main__":
    main()
