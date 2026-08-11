#!/usr/bin/env python3
"""Build a candidate 'syllabary' from Proto-Elamite SUBheader lines.

The line right after "# header" (the header line itself is usually just one
or two signs - a responsible party marker) tends to carry the longest sign
run before the numeral tally: plausibly a name string. E.g.

    1. 𛽔
    # header
    2. 𛾤 𜌓 𜉉 𜀕 𛿺 𜄱  𛴓 𛴆𛴆𛴆𛴆𛴆𛴆 𛴀𛴀𛴀𛴀

𜄱 (M288) is the single most frequent sign in the whole corpus (Dahl, CDLB
2002:1) and shows up fused as a suffix in many ligatures (M157+M288,
M175+M288, M218+M288, M305+M288, ...) - it behaves like a measure/capacity
classifier that introduces the numeral tally rather than being part of the
name. So: cut the line at the first pure-numeral token OR the first bare
M288, and keep only what comes before it.
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

SYLLABARY_TSV = ROOT / "texts" / "proto-elamite" / "subheader-syllabary.tsv"
SYLLABARY_GROUPED_TSV = ROOT / "texts" / "proto-elamite" / "subheader-syllabary-grouped.tsv"

M288_CODE = "M288"


def extract_subheader_tokens(char2code: dict[str, str]) -> list[str]:
    lines = read_lines()
    tokens: list[str] = []
    for i, line in enumerate(lines):
        prev = lines[i - 1].strip() if i > 0 else ""
        if not HEADER_MARK_RE.match(prev):
            continue
        m = LINE_RE.match(line)
        if not m:
            continue
        for tok in m.group(2).split():
            result = classify(tok, char2code)
            if result is not None:
                kind, code = result
                if kind == "numeral" or code == M288_CODE:
                    break  # cut here: rest of the line is the quantity/classifier
            tokens.append(tok)
    return tokens


def main() -> None:
    char2code = load_char_to_code()
    code2char = code_to_char_map(char2code)
    tokens = extract_subheader_tokens(char2code)

    kind_counts = collections.Counter()
    sign_freq = collections.Counter()
    base_freq = collections.Counter()
    base_variants = collections.defaultdict(collections.Counter)

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

    print(f"subheader lines processed, tokens kept before cut: {len(tokens)}")
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
            variant_str = ", ".join(f"{c}×{v}" for c, v in variants.most_common())
            f.write(f"{rank}\t{n}\t{glyph_for(base, code2char)}\t{base}\t{variant_str}\n")

    print()
    print(f"wrote {SYLLABARY_TSV}")
    print(f"wrote {SYLLABARY_GROUPED_TSV}")


if __name__ == "__main__":
    main()
