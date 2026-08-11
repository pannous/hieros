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
import re

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
ANNOTATED_LINES_TXT = ROOT / "texts" / "proto-elamite" / "subheader-lines-annotated.txt"
RECORD_ID_RE = re.compile(r"^&(P\d+)")

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


def annotate_token(tok: str, char2code: dict[str, str]) -> str:
    """Render a token as-is, but tag numerals and category signs with
    their code so nothing is silently dropped from the raw dump."""
    result = classify(tok, char2code)
    if result is None:
        return tok
    kind, code = result
    if kind == "numeral":
        return f"⟨{tok}:{code}⟩"
    if kind == "sign" and is_category_tainted(code):
        return f"‹{tok}:{code}›"
    return tok


def extract_annotated_subheader_lines(char2code: dict[str, str]) -> list[tuple[str, str, str]]:
    """One row per subheader line: (record id, header content, annotated
    subheader content) - nothing cut, category/numeral tokens tagged."""
    lines = read_lines()
    record_id = "?"
    rows: list[tuple[str, str, str]] = []
    for i, line in enumerate(lines):
        m_rec = RECORD_ID_RE.match(line)
        if m_rec:
            record_id = m_rec.group(1)
            continue
        prev = lines[i - 1].strip() if i > 0 else ""
        if not HEADER_MARK_RE.match(prev):
            continue
        header_m = LINE_RE.match(lines[i - 2]) if i >= 2 else None
        header_content = header_m.group(2) if header_m else ""
        m = LINE_RE.match(line)
        if not m:
            continue
        annotated = " ".join(annotate_token(tok, char2code) for tok in m.group(2).split())
        rows.append((record_id, header_content, annotated))
    return rows


def write_annotated_lines(char2code: dict[str, str]) -> None:
    rows = extract_annotated_subheader_lines(char2code)
    with ANNOTATED_LINES_TXT.open("w", encoding="utf-8") as f:
        f.write(
            "# Raw subheader lines (line right after \"# header\"), nothing cut.\n"
            "# ⟨glyph:CODE⟩ = numeral tally, ‹glyph:CODE› = Dahl's known category\n"
            "# sign (CDLI 2002:1 Table 2) - both excluded from the syllabary\n"
            "# frequency tables but shown here in place so sequences stay intact.\n\n"
        )
        for record_id, header, annotated in rows:
            f.write(f"{record_id}\theader={header}\t{annotated}\n")
    print(f"wrote {ANNOTATED_LINES_TXT} ({len(rows)} lines)")


NGRAM_TSV = ROOT / "texts" / "proto-elamite" / "case1-5grams.tsv"
NGRAM_LEN = 5


def extract_all_line_code_sequences(char2code: dict[str, str]) -> list[tuple[str, list[str]]]:
    """Per numbered line ANYWHERE in the corpus (not just subheaders):
    (record id, ordered code list), numerals dropped but category signs
    kept in place. Subheader lines alone are too few (660) to show any
    5-gram repeating twice; the leading "case 1" sign-string on every
    line of every text (~660 subheaders is a small fraction of the
    corpus) is the actual pool recurring sequences are drawn from."""
    lines = read_lines()
    record_id = "?"
    rows: list[tuple[str, list[str]]] = []
    for line in lines:
        m_rec = RECORD_ID_RE.match(line)
        if m_rec:
            record_id = m_rec.group(1)
            continue
        m = LINE_RE.match(line)
        if not m:
            continue
        codes = []
        for tok in m.group(2).split():
            result = classify(tok, char2code)
            if result is None or result[0] == "numeral":
                continue
            codes.append(result[1])
        rows.append((record_id, codes))
    return rows


def write_ngrams(char2code: dict[str, str], code2char: dict[str, str]) -> None:
    rows = extract_all_line_code_sequences(char2code)
    ngram_counts = collections.Counter()
    ngram_examples = collections.defaultdict(list)
    for record_id, codes in rows:
        for i in range(len(codes) - NGRAM_LEN + 1):
            gram = tuple(codes[i:i + NGRAM_LEN])
            ngram_counts[gram] += 1
            if len(ngram_examples[gram]) < 3:
                ngram_examples[gram].append(record_id)

    recurring = [(gram, n) for gram, n in ngram_counts.items() if n >= 2]
    recurring.sort(key=lambda gn: gn[1], reverse=True)

    with NGRAM_TSV.open("w", encoding="utf-8") as f:
        f.write("count\tglyphs\tcodes\texample_records\n")
        for gram, n in recurring:
            glyphs = " ".join(glyph_for(c, code2char) for c in gram)
            codes_str = " ".join(gram)
            examples = ", ".join(ngram_examples[gram])
            f.write(f"{n}\t{glyphs}\t{codes_str}\t{examples}\n")

    print(f"wrote {NGRAM_TSV} ({len(recurring)} recurring {NGRAM_LEN}-grams, count>=2)")


def main() -> None:
    char2code = load_char_to_code()
    code2char = code_to_char_map(char2code)
    write_annotated_lines(char2code)
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
            variant_str = ", ".join(f"{glyph_for(c, code2char)} {c}×{v}" for c, v in variants.most_common())
            f.write(f"{rank}\t{n}\t{glyph_for(base, code2char)}\t{base}\t{variant_str}\n")

    print()
    print(f"wrote {SYLLABARY_TSV}")
    print(f"wrote {SYLLABARY_GROUPED_TSV}")
    write_ngrams(char2code, code2char)


if __name__ == "__main__":
    main()
