#!/usr/bin/env python3
"""Build a candidate 'syllabary' from Proto-Elamite header lines.

Header lines are ATF cases explicitly annotated "# header" (or "# (header)")
by CDLI editors - conventionally the line naming the responsible party or
heading commodity for an administrative entry, as opposed to the numbered
tally lines beneath it.

This pulls every sign that occurs in such a header line, drops pure numeral
tallies and numeral-bearing "complex capacity sign" ligatures (M-sign fused
with an N-count, e.g. M036+1(N30D) - Dahl's CCS, see CDLJ 2005:3), and
reports frequency plus a similarity grouping by base M-number (i.e. folding
~a/~b/~c graphic variants of the same catalogued sign together).
"""
from __future__ import annotations

import collections
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TSV_PATH = ROOT / "abc" / "proto-elamite.tsv"
UNICODE_PATH = ROOT / "texts" / "proto-elamite" / "cdli-proto-elamite-unicode.txt"

HEADER_MARK_RE = re.compile(r"^#\s*\(?header\)?\s*$")
LINE_RE = re.compile(r"^(\d+[A-Za-z']*)\.\s*(.*?)\s*$")
NOISE_TOKENS = {"x", "…", "n"}


def load_char_to_code() -> dict[str, str]:
    """Reverse of load_code_map(): each tsv row's char -> its primary code."""
    char2code: dict[str, str] = {}
    with TSV_PATH.open(encoding="utf-8") as f:
        for line in f:
            line = line.rstrip("\n")
            if not line.strip():
                continue
            cols = line.split("\t")
            char, code = cols[0], cols[1].strip()
            char2code.setdefault(char, code)
    return char2code


def code_to_char_map(char2code: dict[str, str]) -> dict[str, str]:
    return {code: char for char, code in char2code.items()}


def base_number(code: str) -> str:
    """Strip graphic-variant suffixes and ligature partners down to the
    primary M/N catalogue number, e.g. 'M264~a' -> 'M264', 'M001+M379~c' ->
    'M001+M379' (kept whole - a ligature is its own sign, not a variant)."""
    return re.sub(r"[~@][A-Za-z0-9]+", "", code)


def is_numeral_code(code: str) -> bool:
    return code.startswith("N")


def is_capacity_ligature(code: str) -> bool:
    """Dahl's 'complex capacity sign' (CCS): a word-sign fused with a
    numeral count, e.g. M036+1(N30D). These encode a counted quantity, not
    a stable lexical/phonetic value, so they don't belong in a syllabary."""
    return "+" in code and re.search(r"\(?N[0-9]", code) is not None


def extract_header_tokens() -> list[str]:
    text = UNICODE_PATH.read_text(encoding="utf-8")
    lines = text.split("\n")
    tokens: list[str] = []
    for i, line in enumerate(lines):
        m = LINE_RE.match(line)
        if not m:
            continue
        content = m.group(2)
        nxt = lines[i + 1].strip() if i + 1 < len(lines) else ""
        if not HEADER_MARK_RE.match(nxt):
            continue
        tokens.extend(content.split())
    return tokens


def classify(token: str, char2code: dict[str, str]) -> tuple[str, str] | None:
    """Return (kind, code) for a header token, or None to drop it.
    kind is 'sign', 'numeral', or 'capacity'."""
    if token in NOISE_TOKENS or "[" in token or "]" in token:
        return None
    chars = list(token)
    codes = [char2code.get(c) for c in chars]
    if any(c is None for c in codes):
        return None  # shouldn't happen for successfully-converted signs

    if len(set(chars)) == 1:
        code = codes[0]
        if is_numeral_code(code):
            return ("numeral", code)
        return ("sign", code)

    # Multi-char token: signs written back-to-back with no ATF space
    # between them, almost always because no single ligature codepoint was
    # catalogued for the combination - reconstruct the compound code.
    combined = "+".join(codes)
    if any(is_numeral_code(c) for c in codes):
        return ("capacity", combined)
    return ("sign", combined)


def glyph_for(code: str, code2char: dict[str, str]) -> str:
    """Render a code's glyph, falling back to concatenating each '+'-part's
    own glyph for synthesized compounds with no single catalogued codepoint."""
    if code in code2char:
        return code2char[code]
    return "".join(code2char.get(part, f"[{part}]") for part in code.split("+"))


SYLLABARY_TSV = ROOT / "texts" / "proto-elamite" / "header-syllabary.tsv"
SYLLABARY_GROUPED_TSV = ROOT / "texts" / "proto-elamite" / "header-syllabary-grouped.tsv"


def main() -> None:
    char2code = load_char_to_code()
    code2char = code_to_char_map(char2code)
    tokens = extract_header_tokens()

    kind_counts = collections.Counter()
    sign_freq = collections.Counter()   # exact code -> count
    base_freq = collections.Counter()   # base M-number -> count
    base_variants = collections.defaultdict(collections.Counter)  # base -> {code: count}

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

    print(f"header lines processed, tokens: {len(tokens)}")
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
