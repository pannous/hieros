#!/usr/bin/env python3
"""Build a candidate 'syllabary' from Proto-Elamite header lines.

Header lines are ATF cases explicitly annotated "# header" (or "# (header)")
by CDLI editors - conventionally the line naming the responsible party or
heading commodity for an administrative entry, as opposed to the numbered
tally lines beneath it.

This pulls every sign that occurs in such a header line, drops pure numeral
tallies and numeral-bearing "complex capacity sign" ligatures (M-sign fused
with an N-count, e.g. M036+1(N30D) - Dahl's CCS, see CDLJ 2005:3), and
reports frequency plus a similarity grouping by base M-number (i.e. folding
~a/~b/~c graphic variants of the same catalogued sign together).
"""
from __future__ import annotations

import collections

from pe_signs import (
    HEADER_MARK_RE,
    LINE_RE,
    ROOT,
    base_number,
    classify,
    code_to_char_map,
    glyph_for,
    load_char_to_code,
    read_lines,
)

SYLLABARY_TSV = ROOT / "texts" / "proto-elamite" / "header-syllabary.tsv"
SYLLABARY_GROUPED_TSV = ROOT / "texts" / "proto-elamite" / "header-syllabary-grouped.tsv"


def extract_header_tokens() -> list[str]:
    lines = read_lines()
    tokens: list[str] = []
    for i, line in enumerate(lines):
        m = LINE_RE.match(line)
        if not m:
            continue
        content = m.group(2)
        nxt = lines[i + 1].strip() if i + 1 < len(lines) else ""
        if not HEADER_MARK_RE.match(nxt):
            continue
        tokens.extend(content.split())
    return tokens


def main() -> None:
    char2code = load_char_to_code()
    code2char = code_to_char_map(char2code)
    tokens = extract_header_tokens()

    kind_counts = collections.Counter()
    sign_freq = collections.Counter()   # exact code -> count
    base_freq = collections.Counter()   # base M-number -> count
    base_variants = collections.defaultdict(collections.Counter)  # base -> {code: count}

    for tok in tokens:
        result = classify(tok, char2code)
        if result is None:
            kind_counts["dropped"] += 1
            continue
        kind, code = result
        kind_counts[kind] += 1
        if kind != "sign":
            continue
        sign_freq[code] += 1
        base = base_number(code)
        base_freq[base] += 1
        base_variants[base][code] += 1

    print(f"header lines processed, tokens: {len(tokens)}")
    print("token kinds:", dict(kind_counts))
    print(f"distinct sign codes in syllabary: {len(sign_freq)}")
    print(f"distinct base (variant-grouped) signs: {len(base_freq)}")
    print()

    print("=== top 40 signs by exact code ===")
    for code, n in sign_freq.most_common(40):
        print(f"{n:4d}  {code:20s} {glyph_for(code, code2char)}")

    print()
    print("=== top 40 grouped by base sign (variants folded together) ===")
    for base, n in base_freq.most_common(40):
        variants = base_variants[base]
        variant_str = ", ".join(f"{glyph_for(c, code2char)} {c}×{v}" for c, v in variants.most_common())
        print(f"{n:4d}  {glyph_for(base, code2char):3s} {base:15s} [{variant_str}]")

    with SYLLABARY_TSV.open("w", encoding="utf-8") as f:
        f.write("rank\tcount\tglyph\tcode\tbase\n")
        for rank, (code, n) in enumerate(sign_freq.most_common(), 1):
            f.write(f"{rank}\t{n}\t{glyph_for(code, code2char)}\t{code}\t{base_number(code)}\n")

    with SYLLABARY_GROUPED_TSV.open("w", encoding="utf-8") as f:
        f.write("rank\tcount\tglyph\tbase\tvariants\n")
        for rank, (base, n) in enumerate(base_freq.most_common(), 1):
            variants = base_variants[base]
            variant_str = ", ".join(f"{glyph_for(c, code2char)} {c}×{v}" for c, v in variants.most_common())
            f.write(f"{rank}\t{n}\t{glyph_for(base, code2char)}\t{base}\t{variant_str}\n")

    print()
    print(f"wrote {SYLLABARY_TSV}")
    print(f"wrote {SYLLABARY_GROUPED_TSV}")


if __name__ == "__main__":
    main()
