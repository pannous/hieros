#!/usr/bin/env python3
"""Super-concept" signs: the sign heading a numbered line on the REVERSE
of a tablet, where it plausibly summarizes/totals what's itemized on the
obverse (e.g. "M346 , 2(N23) 9(N14) 3(N01)" glossed by CDLI's own
translators as "(total) ewes: 293" - the reverse line uses the same
commodity-classifier as the obverse entries it's summing).

Structurally identical to the M288/M346/M376 "kind" extraction, just
scoped to lines physically located after "@reverse" (including its
sub-sections like "@column N", "@top") rather than by which classifier
follows. This is the general case those three were specific instances of.
"""
from __future__ import annotations

import collections
import re

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

EXACT_TSV = ROOT / "texts" / "proto-elamite" / "reverse-summary-signs.tsv"
GROUPED_TSV = ROOT / "texts" / "proto-elamite" / "reverse-summary-signs-grouped.tsv"

RECORD_ID_RE = re.compile(r"^&(P\d+)")
SECTION_RE = re.compile(r"^@(\S+)")


def extract_reverse_leading_signs(char2code: dict[str, str]) -> list[tuple[str, str]]:
    """(record id, code) for the FIRST non-numeral sign of every numbered
    line physically on the reverse (or a sub-section of it: @column, @top,
    @seal, etc. all count as still "reverse" until @obverse reappears)."""
    on_reverse = False
    record_id = "?"
    out: list[tuple[str, str]] = []
    for line in read_lines():
        m_rec = RECORD_ID_RE.match(line)
        if m_rec:
            record_id = m_rec.group(1)
            on_reverse = False
            continue
        m_sec = SECTION_RE.match(line)
        if m_sec:
            tag = m_sec.group(1)
            if tag == "obverse":
                on_reverse = False
            elif tag == "reverse":
                on_reverse = True
            # @column/@top/@left/@seal etc. under reverse: leave state as-is
            continue
        if not on_reverse:
            continue
        m = LINE_RE.match(line)
        if not m:
            continue
        for tok in m.group(2).split():
            result = classify(tok, char2code)
            if result is None:
                break  # leading token unresolved/noise - skip this line
            kind, code = result
            if kind == "numeral":
                break  # line has no leading sign before its tally
            out.append((record_id, code))
            break  # only the first sign of the line
    return out


def main() -> None:
    char2code = load_char_to_code()
    code2char = code_to_char_map(char2code)
    hits = extract_reverse_leading_signs(char2code)

    exact_freq = collections.Counter()
    base_freq = collections.Counter()
    base_variants = collections.defaultdict(collections.Counter)
    examples = collections.defaultdict(list)
    for rid, code in hits:
        exact_freq[code] += 1
        b = base_number(code)
        base_freq[b] += 1
        base_variants[b][code] += 1
        if len(examples[b]) < 3:
            examples[b].append(rid)

    print(f"{len(hits)} reverse-side lines with a leading sign")
    print(f"distinct exact signs: {len(exact_freq)}, distinct base signs: {len(base_freq)}")
    print()
    print("=== top 30 grouped by base sign ===")
    for base, n in base_freq.most_common(30):
        variants = base_variants[base]
        variant_str = ", ".join(f"{glyph_for(c, code2char)}{c}×{v}" for c, v in variants.most_common())
        ex = ", ".join(examples[base])
        print(f"{n:4d}  {glyph_for(base, code2char):3s} {base:10s} e.g. {ex}   [{variant_str}]")

    with EXACT_TSV.open("w", encoding="utf-8") as f:
        f.write("rank\tcount\tglyph\tcode\tbase\n")
        for rank, (code, n) in enumerate(exact_freq.most_common(), 1):
            f.write(f"{rank}\t{n}\t{glyph_for(code, code2char)}\t{code}\t{base_number(code)}\n")

    with GROUPED_TSV.open("w", encoding="utf-8") as f:
        f.write("rank\tcount\tglyph\tbase\texample_records\tvariants\n")
        for rank, (base, n) in enumerate(base_freq.most_common(), 1):
            variants = base_variants[base]
            variant_str = ", ".join(f"{glyph_for(c, code2char)}{c}×{v}" for c, v in variants.most_common())
            ex = ", ".join(examples[base])
            f.write(f"{rank}\t{n}\t{glyph_for(base, code2char)}\t{base}\t{ex}\t{variant_str}\n")

    print()
    print(f"wrote {EXACT_TSV}")
    print(f"wrote {GROUPED_TSV}")


if __name__ == "__main__":
    main()
