#!/usr/bin/env python3
"""Sub-lettered row-groups within one tablet, compared column-by-column
down the tablet - the actual pattern behind:

    2.a. [[M362+X]]   2.b. [𜂧]   2.c. [𛺉]   2.d. 𛵓   2.e. 𛿃   2.f. 𛹱   2.g. 𜆾
    3.a. 𜊀            3.b. 𜃪     3.c. [𛺉]   3.d. [𛵓] 3.e. 𛿃   3.f. 𛹱   3.g. 𜆾
    4.a. 𜉳 𛴻          4.b. 𜃫     4.c. 𛺉     4.d. 𛵓   4.e. 𛿃   4.f. 𛹱   4.g. [𜆾]
    ...

Lines like "7.a1." / "3.g'." group under row 7 / row 3 with sub-key
"a1" / "g'". Within one tablet, group lines by their leading row number,
build a {sub-letter: sign} vector per row, then compare row vectors
pairwise on their SHARED sub-letter keys (so a bracket-damaged cell in
one row doesn't spuriously count as a mismatch against a present cell in
another - it's simply not compared). Reports near-identical row pairs
(few differing keys) the same way analyze_vertical_orders.py does for
whole lines, but at the right granularity this time.
"""
from __future__ import annotations

import collections
import re

from pe_signs import base_number, classify, code_to_char_map, glyph_for, load_char_to_code, ROOT, read_lines

RECORD_ID_RE = re.compile(r"^&(P\d+)")
SUBITEM_RE = re.compile(r"^(\d+)\.([a-z]\d*'?)\.\s*(.*?)\s*$")
MAX_DIFF = 2
MIN_SHARED_KEYS = 4  # need at least this many comparable columns for a match to mean anything
OUT_TSV = ROOT / "texts" / "proto-elamite" / "row-groups.tsv"


def leading_sign(content: str, char2code: dict[str, str]) -> str | None:
    for tok in content.split():
        result = classify(tok, char2code)
        if result is None:
            return None  # damaged/unresolved cell - not comparable, not a mismatch
        kind, code = result
        if kind == "numeral":
            return None
        return code
    return None


def extract_row_groups(char2code: dict[str, str]) -> dict[str, dict[str, dict[str, str]]]:
    """record id -> row number -> {sub-letter: sign code}"""
    record_id = "?"
    groups: dict[str, dict[str, dict[str, str]]] = collections.defaultdict(lambda: collections.defaultdict(dict))
    for line in read_lines():
        m_rec = RECORD_ID_RE.match(line)
        if m_rec:
            record_id = m_rec.group(1)
            continue
        m = SUBITEM_RE.match(line)
        if not m:
            continue
        row, sub, content = m.groups()
        sign = leading_sign(content, char2code)
        if sign is not None:
            groups[record_id][row][sub] = sign
    return groups


def main() -> None:
    char2code = load_char_to_code()
    code2char = code_to_char_map(char2code)
    groups = extract_row_groups(char2code)

    matches = []
    for rid, rows in groups.items():
        row_nums = list(rows.keys())
        for i in range(len(row_nums)):
            for j in range(i + 1, len(row_nums)):
                ra, rb = rows[row_nums[i]], rows[row_nums[j]]
                shared = sorted(set(ra) & set(rb), key=lambda k: (len(k), k))
                if len(shared) < MIN_SHARED_KEYS:
                    continue
                diffs = [k for k in shared if base_number(ra[k]) != base_number(rb[k])]
                if len(diffs) <= MAX_DIFF:
                    matches.append((rid, row_nums[i], row_nums[j], shared, diffs, ra, rb))

    matches.sort(key=lambda m: (len(m[4]), -len(m[3])))

    print(f"{len(matches)} near-identical row-group pairs (>={MIN_SHARED_KEYS} shared columns, <={MAX_DIFF} diffs)")
    print()
    for rid, ra_num, rb_num, shared, diffs, ra, rb in matches[:30]:
        ga = " ".join(glyph_for(ra[k], code2char) for k in shared)
        gb = " ".join(glyph_for(rb[k], code2char) for k in shared)
        print(f"{rid}  row {ra_num} vs row {rb_num}  cols={shared} diffs_at={diffs}")
        print(f"   {ga}")
        print(f"   {gb}")

    with OUT_TSV.open("w", encoding="utf-8") as f:
        f.write("record\trow_a\trow_b\tcolumns\tdiff_count\tglyphs_a\tglyphs_b\n")
        for rid, ra_num, rb_num, shared, diffs, ra, rb in matches:
            ga = " ".join(glyph_for(ra[k], code2char) for k in shared)
            gb = " ".join(glyph_for(rb[k], code2char) for k in shared)
            f.write(f"{rid}\t{ra_num}\t{rb_num}\t{','.join(shared)}\t{len(diffs)}\t{ga}\t{gb}\n")
    print()
    print(f"wrote {OUT_TSV}")


if __name__ == "__main__":
    main()
