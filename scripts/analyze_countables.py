#!/usr/bin/env python3
""""Countables": signs immediately preceding a numeral tally, corpus-wide.

Whatever sign sits right before a number is the best candidate for "the
thing being counted" - a unit/measure word, classifier, or the counted
noun itself (this is the same logic that identified M288/M346/M376 as
classifiers, generalized to every numeral in the corpus, not just the
three we picked by hand).
"""
from __future__ import annotations

import collections

from pe_signs import (
    LINE_RE,
    ROOT,
    base_number,
    classify,
    code_to_char_map,
    glyph_for,
    load_char_to_code,
    read_lines,
)

EXACT_TSV = ROOT / "texts" / "proto-elamite" / "countables.tsv"
GROUPED_TSV = ROOT / "texts" / "proto-elamite" / "countables-grouped.tsv"


def extract_countables(char2code: dict[str, str]) -> list[str]:
    """The code of the sign immediately before each numeral RUN (not each
    numeral token - "1(N14) 7(N01)" is one run, so the sign before it is
    counted once, not twice)."""
    countables = []
    for line in read_lines():
        m = LINE_RE.match(line)
        if not m:
            continue
        tokens = m.group(2).split()
        prev_kind = None
        prev_code = None
        for tok in tokens:
            result = classify(tok, char2code)
            kind = result[0] if result else None
            if kind == "numeral" and prev_kind != "numeral" and prev_code is not None:
                countables.append(prev_code)
            if result is not None:
                prev_kind, prev_code = kind, result[1]
            else:
                prev_kind, prev_code = None, None
    return countables


def main() -> None:
    char2code = load_char_to_code()
    code2char = code_to_char_map(char2code)
    countables = extract_countables(char2code)

    exact_freq = collections.Counter(countables)
    base_freq = collections.Counter()
    base_variants = collections.defaultdict(collections.Counter)
    for c in countables:
        b = base_number(c)
        base_freq[b] += 1
        base_variants[b][c] += 1

    print(f"{len(countables)} sign-before-numeral occurrences")
    print(f"distinct exact signs: {len(exact_freq)}, distinct base signs: {len(base_freq)}")
    print()
    print("=== top 30 grouped by base sign ===")
    for base, n in base_freq.most_common(30):
        variants = base_variants[base]
        variant_str = ", ".join(f"{glyph_for(c, code2char)}{c}×{v}" for c, v in variants.most_common())
        print(f"{n:4d}  {glyph_for(base, code2char):3s} {base:10s} [{variant_str}]")

    with EXACT_TSV.open("w", encoding="utf-8") as f:
        f.write("rank\tcount\tglyph\tcode\tbase\n")
        for rank, (code, n) in enumerate(exact_freq.most_common(), 1):
            f.write(f"{rank}\t{n}\t{glyph_for(code, code2char)}\t{code}\t{base_number(code)}\n")

    with GROUPED_TSV.open("w", encoding="utf-8") as f:
        f.write("rank\tcount\tglyph\tbase\tvariants\n")
        for rank, (base, n) in enumerate(base_freq.most_common(), 1):
            variants = base_variants[base]
            variant_str = ", ".join(f"{glyph_for(c, code2char)}{c}×{v}" for c, v in variants.most_common())
            f.write(f"{rank}\t{n}\t{glyph_for(base, code2char)}\t{base}\t{variant_str}\n")

    print()
    print(f"wrote {EXACT_TSV}")
    print(f"wrote {GROUPED_TSV}")


if __name__ == "__main__":
    main()
