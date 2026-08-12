#!/usr/bin/env python3
"""Recurring n-grams (length >= 3) across the whole corpus's leading
(pre-numeral) sign sequences - the general case of case1-5grams.tsv,
covering every length instead of just 5.

Only MAXIMAL recurring n-grams are kept: if a 3-gram is fully contained
inside a recurring 5-gram, the 3-gram is dropped as redundant (it's
implied by the longer match, and trivially recurs at least as often).
Without this, short n-grams would flood the output - almost every
recurring 5-gram trivially produces 3 recurring 4-grams and 2 recurring
3-grams as sub-sequences.
"""
from __future__ import annotations

import collections

from pe_signs import base_number, code_to_char_map, glyph_for, load_char_to_code, ROOT
from analyze_subheader_syllabary import extract_all_line_code_sequences

MIN_LEN = 3
OUT_TSV = ROOT / "texts" / "proto-elamite" / "case1-ngrams.tsv"


def all_ngrams_by_length(rows: list[tuple[str, list[str]]]) -> dict[int, collections.Counter]:
    """length -> Counter[gram tuple] -> occurrence count, across the whole corpus."""
    by_len: dict[int, collections.Counter] = collections.defaultdict(collections.Counter)
    for _, codes in rows:
        bases = [base_number(c) for c in codes]
        for n in range(MIN_LEN, len(bases) + 1):
            for i in range(len(bases) - n + 1):
                by_len[n][tuple(bases[i:i + n])] += 1
    return by_len


def is_substring(short: tuple, long: tuple) -> bool:
    ls, ll = len(short), len(long)
    return any(long[i:i + ls] == short for i in range(ll - ls + 1))


def main() -> None:
    char2code = load_char_to_code()
    code2char = code_to_char_map(char2code)
    rows = extract_all_line_code_sequences(char2code)

    by_len = all_ngrams_by_length(rows)
    max_len = max(by_len) if by_len else 0
    print(f"n-gram lengths present: {MIN_LEN}..{max_len}")

    # recurring grams per length, longest first
    recurring_by_len = {}
    for n in range(max_len, MIN_LEN - 1, -1):
        recurring_by_len[n] = [g for g, c in by_len[n].items() if c >= 2]
        print(f"  length {n}: {len(recurring_by_len[n])} distinct recurring grams (of {len(by_len[n])} total)")

    # keep only MAXIMAL recurring grams: drop a gram if it's a substring of
    # any kept gram at a longer length
    kept: list[tuple[int, tuple]] = []
    for n in range(max_len, MIN_LEN - 1, -1):
        for g in recurring_by_len[n]:
            if not any(is_substring(g, kg) for kn, kg in kept if kn > n):
                kept.append((n, g))

    # examples + counts
    examples = collections.defaultdict(list)
    for rid, codes in rows:
        bases = [base_number(c) for c in codes]
        seq_str = tuple(bases)
        for n, g in kept:
            gl = len(g)
            for i in range(len(bases) - gl + 1):
                if tuple(bases[i:i + gl]) == g:
                    if len(examples[(n, g)]) < 4:
                        examples[(n, g)].append(rid)
                    break

    kept.sort(key=lambda ng: (-by_len[ng[0]][ng[1]], -ng[0]))

    print()
    print(f"=== {len(kept)} maximal recurring n-grams (length >= {MIN_LEN}) ===")
    for n, g in kept:
        count = by_len[n][g]
        glyphs = " ".join(glyph_for(c, code2char) for c in g)
        codes_str = " ".join(g)
        ex = ", ".join(examples[(n, g)])
        print(f"{count:3d}  len={n}  {glyphs}   ({codes_str})   e.g. {ex}")

    with OUT_TSV.open("w", encoding="utf-8") as f:
        f.write("count\tlength\tglyphs\tcodes\texample_records\n")
        for n, g in kept:
            count = by_len[n][g]
            glyphs = " ".join(glyph_for(c, code2char) for c in g)
            codes_str = " ".join(g)
            ex = ", ".join(examples[(n, g)])
            f.write(f"{count}\t{n}\t{glyphs}\t{codes_str}\t{ex}\n")
    print()
    print(f"wrote {OUT_TSV}")


if __name__ == "__main__":
    main()
