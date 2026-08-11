#!/usr/bin/env python3
"""Untangle the "kind" prefix before a closing classifier (M288/M346/M376):
how much of it is signs we already have a role-gloss for, versus a genuine
unexplained residue?

Every prior pass (header syllabary, subheader syllabary, livestock-kind)
treated the whole prefix as one undifferentiated "candidate name" pool.
But we've since pinned down glosses for several of its most frequent
members:
  - Dahl's CATEGORY_SIGNS (CDLB 2002:1 Table 2) - established, published.
  - M124 "person suffix" and M057 "water buffalo" - from
    docs/proto-elamite.md, explicitly flagged there as "highly tentative"
    personal notes, not a peer-reviewed source.

Splitting each prefix into (role-tagged signs) vs (residual signs) and
re-running frequency analysis on the residual only should get us closer
to an actual name/title candidate pool, separated from the semantic
scaffolding (worker-marker, person-marker, sub-classifiers) sitting
alongside it.
"""
from __future__ import annotations

import collections

from pe_signs import base_number, code_to_char_map, glyph_for, load_char_to_code, ROOT
from analyze_subheader_syllabary import extract_all_line_code_sequences, CATEGORY_SIGNS

# CATEGORY_SIGNS (Dahl, published) plus the two additional glosses this
# thread pinned down from docs/proto-elamite.md (tentative, personal notes).
ROLE_SIGNS = CATEGORY_SIGNS | {"M124", "M057"}
ROLE_GLOSS = {
    "M124": "person",
    "M057": "water buffalo",
    "M388": "kur/worker",
    "M218": "container/of",
    "M346": "livestock classifier",
    "M288": "grain classifier",
    "M376": "cattle(herder)",
}

CLASSIFIERS = ["M288", "M346", "M376"]


def extract_kind_strings(rows, marker: str) -> list[tuple[str, list[str]]]:
    out = []
    for rid, codes in rows:
        for i, c in enumerate(codes):
            if base_number(c) == marker and i == len(codes) - 1 and i > 0:
                out.append((rid, codes[:i]))
    return out


def split_role_residual(prefix: list[str]) -> tuple[list[str], list[str]]:
    role, residual = [], []
    for c in prefix:
        (role if base_number(c) in ROLE_SIGNS else residual).append(c)
    return role, residual


def main() -> None:
    char2code = load_char_to_code()
    code2char = code_to_char_map(char2code)
    rows = extract_all_line_code_sequences(char2code)

    all_residual_freq = collections.Counter()
    all_residual_base_freq = collections.Counter()
    all_residual_base_variants = collections.defaultdict(collections.Counter)
    total_signs = 0
    total_role = 0

    for marker in CLASSIFIERS:
        kind_strings = extract_kind_strings(rows, marker)
        print(f"=== {marker} ({glyph_for(marker, code2char)}, {ROLE_GLOSS.get(marker, '?')}) - {len(kind_strings)} kind-strings ===")
        pure_residual = 0
        for rid, prefix in kind_strings:
            role, residual = split_role_residual(prefix)
            total_signs += len(prefix)
            total_role += len(role)
            for c in residual:
                all_residual_freq[c] += 1
                base = base_number(c)
                all_residual_base_freq[base] += 1
                all_residual_base_variants[base][c] += 1
            if role and not residual:
                pure_residual += 1  # fully explained by known roles, nothing left over
        fully_explained = sum(1 for _, p in kind_strings if split_role_residual(p)[1] == [] and split_role_residual(p)[0])
        print(f"  strings fully accounted for by known roles (nothing residual): {fully_explained}/{len(kind_strings)}")
        # sample annotated breakdown
        for rid, prefix in kind_strings[:6]:
            role, residual = split_role_residual(prefix)
            tagged = " ".join(
                f"{glyph_for(c, code2char)}[{ROLE_GLOSS.get(base_number(c), base_number(c))}]"
                if base_number(c) in ROLE_SIGNS else glyph_for(c, code2char)
                for c in prefix
            )
            print(f"  {rid}: {tagged}")
        print()

    print(f"TOTAL across all 3 classifiers: {total_signs} signs in kind-strings, {total_role} ({100*total_role/total_signs:.0f}%) tagged as known roles, {total_signs-total_role} residual")
    print()
    print(f"distinct residual exact signs: {len(all_residual_freq)}, distinct residual base signs: {len(all_residual_base_freq)}")
    print()
    print("=== residual signs, grouped by base, top 30 ===")
    for base, n in all_residual_base_freq.most_common(30):
        variants = all_residual_base_variants[base]
        variant_str = ", ".join(f"{glyph_for(c, code2char)} {c}×{v}" for c, v in variants.most_common())
        print(f"{n:3d}  {glyph_for(base, code2char):3s} {base:8s} [{variant_str}]")

    out_tsv = ROOT / "texts" / "proto-elamite" / "kind-residual-signs-grouped.tsv"
    with out_tsv.open("w", encoding="utf-8") as f:
        f.write("rank\tcount\tglyph\tbase\tvariants\n")
        for rank, (base, n) in enumerate(all_residual_base_freq.most_common(), 1):
            variants = all_residual_base_variants[base]
            variant_str = ", ".join(f"{glyph_for(c, code2char)} {c}×{v}" for c, v in variants.most_common())
            f.write(f"{rank}\t{n}\t{glyph_for(base, code2char)}\t{base}\t{variant_str}\n")
    print()
    print(f"wrote {out_tsv}")


if __name__ == "__main__":
    main()
