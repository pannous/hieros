#!/usr/bin/env python3
"""Build a candidate 'syllabary' from Proto-Elamite SUBheader lines.

The line right after "# header" (the header line itself is usually just one
or two signs - a responsible party marker) tends to carry the longest sign
run before the numeral tally: plausibly a name string. E.g.

    1. 𛽔
    # header
    2. 𛾤 𜌓 𜉉 𜀕 𛿺 𜄱  𛴓 𛴆𛴆𛴆𛴆𛴆𛴆 𛴀𛴀𛴀𛴀

A hard case boundary (the first pure-numeral run) ends the scan entirely -
past that point we're in the quantity case, not the name. But several of
the signs that dominate this line are themselves known category markers
rather than name material: Dahl (CDLB 2002:1, Table 2 + section 6) lists
the corpus's highest-frequency non-numerical signs and states that "except
for M157 and M346, all of the most frequent signs ... are signs of either
grain products, containers or persons." Those act as logographic
classifiers, not syllabic content - similar in function to M288 - but
unlike M288 they don't reliably sit at the end right before the number, so
instead of cutting the scan short we just skip them wherever they occur
and keep scanning for the surrounding name signs.
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

# Dahl's corpus-wide high-frequency signs (CDLB 2002:1, Table 2): "M305
# (107) M387 (206) M218 (453) M388 (528) M288 (709) M36 (128) M9 (213) M32
# (132) M297 (222) M66 (139) M157 (247) M1 (152) M346 (253) M263 (164) M54
# (266) M376 (172) M96 (194) M371 (290)". Per section 6, all but M157 and
# M346 are identified as signs for "grain products, containers or persons"
# - i.e. logographic/classifier use, not syllabic content. We exclude the
# full table (including the two unexplained-but-still-anomalously-frequent
# M157/M346) rather than cherry-pick, since the whole set is Dahl's own
# evidence-backed list, not signs we picked for being frequent in our data.
CATEGORY_SIGNS = {
    "M305", "M387", "M218", "M388", "M288", "M036", "M009", "M032", "M297",
    "M066", "M157", "M001", "M346", "M263", "M054", "M376", "M096", "M371",
}


def is_category_tainted(code: str) -> bool:
    """True if the sign itself, or any '+'-part of a ligature it's fused
    into (e.g. M218+M288, both of which are category signs on their own),
    is one of Dahl's category signs."""
    return any(base_number(part) in CATEGORY_SIGNS for part in code.split("+"))


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
                if kind == "numeral":
                    break  # hard case boundary: rest of the line is the quantity
                if is_category_tainted(code):
                    continue  # skip the classifier, keep scanning for name signs
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
