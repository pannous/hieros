#!/usr/bin/env python3
"""Candidate "kind of sheep/livestock" strings closed by M346.

M346 (𜈽) sits as the LAST non-numeral sign before the numeral tally in
90% of its 289 occurrences corpus-wide - the same structural role M288
plays for grain accounts. The sign most often immediately preceding it is
M387, Englund's documented reading of "100" in the Proto-Elamite DECIMAL
system, which per Dahl/Englund (and docs/proto-elamite.md) is specifically
"the system used for animals & laborers" as opposed to the sexagesimal
system for inanimate goods. So M346 plausibly reads as a livestock
classifier (matching the "...'s ewes: 98" note already in the docs),
parallel to M288 for grain.

This pulls every line-final "M346 preceded by N signs" string - the
candidate "kind of sheep" (or herder name, or flock location - we can't
tell which from structure alone) - the same way the M288 subheader cut
isolated candidate name strings.
"""
from __future__ import annotations

import collections

from pe_signs import base_number, code_to_char_map, glyph_for, load_char_to_code, ROOT
from analyze_subheader_syllabary import extract_all_line_code_sequences

MARKER = "M346"
OUT_TSV = ROOT / "texts" / "proto-elamite" / "livestock-kind-candidates.tsv"


def extract_kind_candidates(rows: list[tuple[str, list[str]]]) -> list[tuple[str, list[str]]]:
    """(record id, prefix codes) for every line where MARKER is the last
    non-numeral sign and at least one sign precedes it."""
    candidates = []
    for rid, codes in rows:
        for i, c in enumerate(codes):
            if base_number(c) == MARKER and i == len(codes) - 1 and i > 0:
                candidates.append((rid, codes[:i]))
    return candidates


def main() -> None:
    char2code = load_char_to_code()
    code2char = code_to_char_map(char2code)
    rows = extract_all_line_code_sequences(char2code)
    candidates = extract_kind_candidates(rows)

    print(f"{len(candidates)} lines end '...<kind> {MARKER}' with a non-empty kind string")

    lengths = collections.Counter(len(prefix) for _, prefix in candidates)
    print(f"prefix length distribution: {dict(sorted(lengths.items()))}")

    whole_string_freq = collections.Counter()
    examples = collections.defaultdict(list)
    for rid, prefix in candidates:
        key = tuple(prefix)
        whole_string_freq[key] += 1
        if len(examples[key]) < 3:
            examples[key].append(rid)

    print(f"distinct whole kind-strings: {len(whole_string_freq)}")
    recurring = sum(1 for n in whole_string_freq.values() if n >= 2)
    print(f"of which recur (n>=2): {recurring}")
    print()

    sign_freq = collections.Counter()
    for _, prefix in candidates:
        for c in prefix:
            sign_freq[base_number(c)] += 1
    print(f"distinct base signs used across all kind-strings: {len(sign_freq)}")
    print()

    print("=== whole kind-strings that recur (n>=2), i.e. real candidate 'sheep kinds' ===")
    recurring_strings = [(k, n) for k, n in whole_string_freq.items() if n >= 2]
    recurring_strings.sort(key=lambda kn: kn[1], reverse=True)
    for prefix, n in recurring_strings:
        glyphs = " ".join(glyph_for(c, code2char) for c in prefix)
        codes_str = " ".join(prefix)
        ex = ", ".join(examples[tuple(prefix)])
        print(f"{n:3d}  {glyphs}   ({codes_str})   e.g. {ex}")

    print()
    print("=== most frequent individual signs inside kind-strings (top 20) ===")
    for code, n in sign_freq.most_common(20):
        print(f"{n:3d}  {glyph_for(code, code2char)} {code}")

    with OUT_TSV.open("w", encoding="utf-8") as f:
        f.write("count\tglyphs\tcodes\texample_records\n")
        for prefix, n in sorted(whole_string_freq.items(), key=lambda kn: kn[1], reverse=True):
            glyphs = " ".join(glyph_for(c, code2char) for c in prefix)
            codes_str = " ".join(prefix)
            ex = ", ".join(examples[prefix])
            f.write(f"{n}\t{glyphs}\t{codes_str}\t{ex}\n")
    print()
    print(f"wrote {OUT_TSV}")


if __name__ == "__main__":
    main()
