#!/usr/bin/env python3
"""How do the clean residual signs (name/title candidates, not hidden
classifiers - see analyze_kind_substructure.py) combine with their
neighbors?

For each clean residual sign, find its most common immediate before/after
neighbor corpus-wide, then flag RECIPROCAL pairs: A's top "after" is B AND
B's top "before" is A. That's a much stronger signal than either direction
alone - it means the two signs are consistently glued together as a unit,
not just individually frequent.
"""
from __future__ import annotations

import collections

from pe_signs import base_number, code_to_char_map, glyph_for, load_char_to_code, ROOT
from analyze_subheader_syllabary import extract_all_line_code_sequences

# boundary_like_pct < 30% from analyze_kind_substructure.py's residual
# table - signs that don't behave like hidden sub-classifiers.
CLEAN_RESIDUAL = [
    "M377", "M048", "M352", "M217", "M004", "M259", "M347",
    "M024", "M386", "M295", "M146", "M103", "M240",
]

OUT_TSV = ROOT / "texts" / "proto-elamite" / "residual-collocations.tsv"


def neighbor_counts(rows, marker: str) -> tuple[collections.Counter, collections.Counter]:
    before, after = collections.Counter(), collections.Counter()
    for _, codes in rows:
        for i, c in enumerate(codes):
            if base_number(c) != marker:
                continue
            if i > 0:
                before[base_number(codes[i - 1])] += 1
            if i + 1 < len(codes):
                after[base_number(codes[i + 1])] += 1
    return before, after


def main() -> None:
    char2code = load_char_to_code()
    code2char = code_to_char_map(char2code)
    rows = extract_all_line_code_sequences(char2code)

    neighbors = {m: neighbor_counts(rows, m) for m in CLEAN_RESIDUAL}

    print("=== top before/after neighbor per clean residual sign ===")
    rows_out = []
    for marker in CLEAN_RESIDUAL:
        before, after = neighbors[marker]
        top_before = before.most_common(3)
        top_after = after.most_common(3)
        bstr = ", ".join(f"{glyph_for(b, code2char)}{b}×{n}" for b, n in top_before)
        astr = ", ".join(f"{glyph_for(a, code2char)}{a}×{n}" for a, n in top_after)
        print(f"{glyph_for(marker, code2char)} {marker}:  BEFORE<< {bstr}   |   >>AFTER {astr}")
        rows_out.append((marker, "before", top_before))
        rows_out.append((marker, "after", top_after))

    print()
    print("=== reciprocal pairs (A's top-after == B AND B's top-before == A) ===")
    reciprocal = []
    seen = set()
    for a in CLEAN_RESIDUAL:
        _, a_after = neighbors[a]
        if not a_after:
            continue
        b, n_a = a_after.most_common(1)[0]
        b_before, _ = neighbor_counts(rows, b) if b not in neighbors else (neighbors[b][0], None)
        if not b_before:
            continue
        top_b_before = b_before.most_common(1)[0][0] if b_before else None
        if top_b_before == a and (a, b) not in seen and (b, a) not in seen:
            seen.add((a, b))
            n_b = b_before[a]
            reciprocal.append((a, b, n_a, n_b))
            print(f"{glyph_for(a, code2char)}{a} -> {glyph_for(b, code2char)}{b}   ({a}->{b}: {n_a}x, {b}<-{a}: {n_b}x)")

    with OUT_TSV.open("w", encoding="utf-8") as f:
        f.write("sign\tside\tneighbor\tneighbor_glyph\tcount\n")
        for marker, side, top in rows_out:
            for code, n in top:
                f.write(f"{marker}\t{side}\t{code}\t{glyph_for(code, code2char)}\t{n}\n")
    print()
    print(f"wrote {OUT_TSV}")


if __name__ == "__main__":
    main()
