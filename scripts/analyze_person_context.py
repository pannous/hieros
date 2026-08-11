#!/usr/bin/env python3
"""What sits between the "person" marker and the "worker/kur" marker?

docs/proto-elamite.md glosses two signs from independent lines of evidence:
  - M124 (𛻃) ~ "person suffix", compared to Sumerian LU and Egyptian
    person-determinatives (line 192).
  - M388 (𜌓) ~ "kurion"/"slave" (lines 183-184), matching Dahl's own
    identification of M388 as KUR, "male dependent laborer" (CDLB 2002:1).
    "𛻃𛿺 herder e.g. 𛷶 𛵅 𛿺 herder of water buffalo" (line 220) already shows
    M124 combining with a role marker this way.

This is exactly the structural slot Englund flagged as where proto-Elamite
personal names should live (CDLI 2004): dependent-worker/laborer accounts,
distinguished from parallel animal accounts by carrying a name. With
anchors on BOTH sides now glossed independently (not just inferred from
frequency), the content between an M124 and M388 occurrence on the same
line is the best candidate "name slot" we can currently isolate - so pull
it out and see what's actually there, in both orders.

NOTE the caveat from the prior turn stands: these glosses come from a
personal notes file, not a peer-reviewed source - "highly tentative."
"""
from __future__ import annotations

import collections

from pe_signs import base_number, code_to_char_map, glyph_for, load_char_to_code
from analyze_subheader_syllabary import extract_all_line_code_sequences

MARKER_A = "M124"  # person
MARKER_B = "M388"  # kur / worker

OUT_TSV = None  # set in main() once ROOT is imported


def find_spans(rows: list[tuple[str, list[str]]]) -> list[tuple[str, str, list[str], str]]:
    """(record id, direction, between-codes, ordered pair) for the nearest
    A->B or B->A span on each line. direction is 'person->kur' or
    'kur->person'."""
    spans = []
    for rid, codes in rows:
        for i, c in enumerate(codes):
            base = base_number(c)
            if base not in (MARKER_A, MARKER_B):
                continue
            other = MARKER_B if base == MARKER_A else MARKER_A
            for j in range(i + 1, len(codes)):
                if base_number(codes[j]) == other:
                    direction = "person->kur" if base == MARKER_A else "kur->person"
                    between = codes[i + 1:j]
                    spans.append((rid, direction, between, f"{c}..{codes[j]}"))
                    break
    return spans


def find_neighbors(rows: list[tuple[str, list[str]]], marker: str):
    """Every sign immediately before/after a bare `marker` occurrence,
    corpus-wide - a much bigger sample than requiring a second anchor."""
    before = collections.Counter()
    after = collections.Counter()
    before_examples = collections.defaultdict(list)
    after_examples = collections.defaultdict(list)
    for rid, codes in rows:
        for i, c in enumerate(codes):
            if base_number(c) != marker:
                continue
            if i > 0:
                b = codes[i - 1]
                before[b] += 1
                if len(before_examples[b]) < 3:
                    before_examples[b].append(rid)
            if i < len(codes) - 1:
                a = codes[i + 1]
                after[a] += 1
                if len(after_examples[a]) < 3:
                    after_examples[a].append(rid)
    return before, after, before_examples, after_examples


def report_neighbors(marker: str, code2char: dict[str, str], rows) -> None:
    before, after, before_ex, after_ex = find_neighbors(rows, marker)
    print(f"=== signs immediately BEFORE bare {marker} ({sum(before.values())} occurrences) ===")
    for code, n in before.most_common(20):
        print(f"{n:3d}  {glyph_for(code, code2char)} {code}   e.g. {', '.join(before_ex[code])}")
    print()
    print(f"=== signs immediately AFTER bare {marker} ({sum(after.values())} occurrences) ===")
    for code, n in after.most_common(20):
        print(f"{n:3d}  {glyph_for(code, code2char)} {code}   e.g. {', '.join(after_ex[code])}")
    print()

    from pe_signs import ROOT
    out_tsv = ROOT / "texts" / "proto-elamite" / f"{marker.lower()}-neighbors.tsv"
    with out_tsv.open("w", encoding="utf-8") as f:
        f.write("side\tcount\tglyph\tcode\texample_records\n")
        for side, counter, ex in (("before", before, before_ex), ("after", after, after_ex)):
            for code, n in counter.most_common():
                f.write(f"{side}\t{n}\t{glyph_for(code, code2char)}\t{code}\t{', '.join(ex[code])}\n")
    print(f"wrote {out_tsv}")


def main() -> None:
    from pe_signs import ROOT

    out_tsv = ROOT / "texts" / "proto-elamite" / "person-kur-spans.tsv"

    char2code = load_char_to_code()
    code2char = code_to_char_map(char2code)
    rows = extract_all_line_code_sequences(char2code)
    spans = find_spans(rows)

    by_direction = collections.Counter(d for _, d, _, _ in spans)
    by_len = collections.Counter(len(b) for _, _, b, _ in spans)
    content_freq = collections.Counter()
    content_examples = collections.defaultdict(list)
    for rid, direction, between, _ in spans:
        key = (direction, tuple(between))
        content_freq[key] += 1
        if len(content_examples[key]) < 3:
            content_examples[key].append(rid)

    print(f"{len(spans)} person<->kur spans found")
    print("by direction:", dict(by_direction))
    print("by gap length (signs between):", dict(sorted(by_len.items())))
    print()

    recurring = [(k, n) for k, n in content_freq.items() if n >= 2]
    recurring.sort(key=lambda kn: kn[1], reverse=True)
    print(f"=== recurring 'between' content (n>=2): {len(recurring)} ===")
    for (direction, between), n in recurring:
        glyphs = "".join(glyph_for(c, code2char) for c in between) or "(adjacent, nothing between)"
        codes_str = " ".join(between) or "-"
        examples = ", ".join(content_examples[(direction, between)])
        print(f"{n:3d}  [{direction}]  {glyphs}   ({codes_str})   e.g. {examples}")

    with out_tsv.open("w", encoding="utf-8") as f:
        f.write("count\tdirection\tglyphs\tcodes\texample_records\n")
        for (direction, between), n in sorted(content_freq.items(), key=lambda kn: kn[1], reverse=True):
            glyphs = "".join(glyph_for(c, code2char) for c in between)
            codes_str = " ".join(between)
            examples = ", ".join(content_examples[(direction, between)])
            f.write(f"{n}\t{direction}\t{glyphs}\t{codes_str}\t{examples}\n")
    print()
    print(f"wrote {out_tsv} ({len(content_freq)} distinct spans, {len(spans)} total)")

    print()
    print("=" * 60)
    print()
    report_neighbors(MARKER_A, code2char, rows)


if __name__ == "__main__":
    main()
