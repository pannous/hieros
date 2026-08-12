#!/usr/bin/env python3
""""Vertical orders": pairs of case-1 sequences (same length) that are
near-identical, differing at only 1-2 positions - e.g.

    M309~a M362+M059    M269    M310~1 M009 M206~g M081
    M309~a M362+M384~a  M269~1  M310~1 M009 M206~g M081

Comparison is by base_number (variant-lenient: M269 vs M269~1 counts as a
match), so the count reported is genuine positions of difference, not
inflated by graphic sub-variants. This generalizes the earlier "A-B-A
where the second A is a variant" observation to full-sequence pairs of
any length.
"""
from __future__ import annotations

import collections

from pe_signs import base_number, code_to_char_map, glyph_for, load_char_to_code, ROOT
from analyze_subheader_syllabary import extract_all_line_code_sequences

MIN_LEN = 4       # shorter sequences produce too many coincidental near-matches
MAX_DIFF = 2       # keep pairs differing at 1 or 2 positions
OUT_TSV = ROOT / "texts" / "proto-elamite" / "vertical-orders.tsv"


def main() -> None:
    char2code = load_char_to_code()
    code2char = code_to_char_map(char2code)
    rows = extract_all_line_code_sequences(char2code)

    # (record id, exact-code tuple, base-number tuple), bucketed by length
    by_len: dict[int, list[tuple[str, tuple, tuple]]] = collections.defaultdict(list)
    for rid, codes in rows:
        n = len(codes)
        if n < MIN_LEN:
            continue
        by_len[n].append((rid, tuple(codes), tuple(base_number(c) for c in codes)))

    pairs = []
    for n, entries in by_len.items():
        for i in range(len(entries)):
            rid_a, exact_a, base_a = entries[i]
            for j in range(i + 1, len(entries)):
                rid_b, exact_b, base_b = entries[j]
                if exact_a == exact_b and rid_a == rid_b:
                    continue  # trivial: same line matched to itself isn't possible here, but guard anyway
                diff_positions = [k for k in range(n) if base_a[k] != base_b[k]]
                nd = len(diff_positions)
                if 0 < nd <= MAX_DIFF:
                    pairs.append((nd, rid_a, exact_a, rid_b, exact_b, diff_positions))

    pairs.sort(key=lambda p: (p[0], -len(p[2])))

    print(f"{len(pairs)} near-identical sequence pairs (length>={MIN_LEN}, diff<={MAX_DIFF})")
    print()
    for nd, rid_a, exact_a, rid_b, exact_b, diffs in pairs[:40]:
        ga = " ".join(glyph_for(c, code2char) for c in exact_a)
        gb = " ".join(glyph_for(c, code2char) for c in exact_b)
        print(f"diff={nd} len={len(exact_a)}  {rid_a} vs {rid_b}  @pos{diffs}")
        print(f"   {ga}   ({' '.join(exact_a)})")
        print(f"   {gb}   ({' '.join(exact_b)})")

    with OUT_TSV.open("w", encoding="utf-8") as f:
        f.write("diff_count\tlength\trecord_a\tglyphs_a\tcodes_a\trecord_b\tglyphs_b\tcodes_b\tdiff_positions\n")
        for nd, rid_a, exact_a, rid_b, exact_b, diffs in pairs:
            ga = " ".join(glyph_for(c, code2char) for c in exact_a)
            gb = " ".join(glyph_for(c, code2char) for c in exact_b)
            f.write(f"{nd}\t{len(exact_a)}\t{rid_a}\t{ga}\t{' '.join(exact_a)}\t{rid_b}\t{gb}\t{' '.join(exact_b)}\t{diffs}\n")
    print()
    print(f"wrote {OUT_TSV}")


if __name__ == "__main__":
    main()
