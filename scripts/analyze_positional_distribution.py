#!/usr/bin/env python3
"""Positional distribution of every sign within its case-1 (pre-numeral)
sequence: does it prefer the start, the end, the middle, or standing
alone? Generalizes the ad-hoc check run by hand on M370/M124 (58% first,
25% sole, 12% middle) to every sign in the corpus.

For a line of N signs, sign at index i (0-based) is classified:
  - "sole"  if N == 1
  - "first" if i == 0 (and N > 1)
  - "last"  if i == N-1 (and N > 1)
  - "mid"   otherwise
Also records a continuous relative position i/(N-1) in [0,1] (sole lines
excluded from the average) - 0 = always first, 1 = always last, 0.5 =
no positional bias, so this is a single-number summary of "head sign"
vs "tail sign" tendency, independent of the categorical counts.
"""
from __future__ import annotations

import collections

from pe_signs import base_number, code_to_char_map, glyph_for, load_char_to_code, ROOT
from analyze_subheader_syllabary import extract_all_line_code_sequences

OUT_TSV = ROOT / "texts" / "proto-elamite" / "positional-distribution.tsv"
MIN_FREQ = 5  # skip signs too rare for the percentages to mean anything


def main() -> None:
    char2code = load_char_to_code()
    code2char = code_to_char_map(char2code)
    rows = extract_all_line_code_sequences(char2code)

    counts = collections.defaultdict(lambda: collections.Counter())
    rel_positions = collections.defaultdict(list)

    for _, codes in rows:
        bases = [base_number(c) for c in codes]
        n = len(bases)
        for i, b in enumerate(bases):
            if n == 1:
                counts[b]["sole"] += 1
            elif i == 0:
                counts[b]["first"] += 1
                rel_positions[b].append(0.0)
            elif i == n - 1:
                counts[b]["last"] += 1
                rel_positions[b].append(1.0)
            else:
                counts[b]["mid"] += 1
                rel_positions[b].append(i / (n - 1))

    results = []
    for b, c in counts.items():
        total = sum(c.values())
        if total < MIN_FREQ:
            continue
        avg_rel = sum(rel_positions[b]) / len(rel_positions[b]) if rel_positions[b] else None
        results.append((b, total, c, avg_rel))

    results.sort(key=lambda r: -r[1])

    print(f"{len(results)} signs with >= {MIN_FREQ} occurrences")
    print()
    print(f"{'sign':10s} {'glyph':4s} {'total':>6s} {'sole%':>6s} {'first%':>7s} {'mid%':>6s} {'last%':>6s} {'avg_rel_pos':>12s}")
    for b, total, c, avg_rel in results[:40]:
        pct = lambda k: 100 * c.get(k, 0) / total
        avg_str = f"{avg_rel:.2f}" if avg_rel is not None else "-"
        print(f"{b:10s} {glyph_for(b, code2char):4s} {total:6d} {pct('sole'):6.1f} {pct('first'):7.1f} {pct('mid'):6.1f} {pct('last'):6.1f} {avg_str:>12s}")

    with OUT_TSV.open("w", encoding="utf-8") as f:
        f.write("base\tglyph\ttotal\tsole_pct\tfirst_pct\tmid_pct\tlast_pct\tavg_relative_position\n")
        for b, total, c, avg_rel in results:
            pct = lambda k: 100 * c.get(k, 0) / total
            avg_str = f"{avg_rel:.3f}" if avg_rel is not None else ""
            f.write(f"{b}\t{glyph_for(b, code2char)}\t{total}\t{pct('sole'):.1f}\t{pct('first'):.1f}\t{pct('mid'):.1f}\t{pct('last'):.1f}\t{avg_str}\n")
    print()
    print(f"wrote {OUT_TSV}")


if __name__ == "__main__":
    main()
