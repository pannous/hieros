#!/usr/bin/env python3
"""Shared helpers for analyzing the Proto-Elamite Unicode text: mapping
Unicode glyphs back to their CDLI sign codes via abc/proto-elamite.tsv."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TSV_PATH = ROOT / "abc" / "proto-elamite.tsv"
UNICODE_PATH = ROOT / "texts" / "proto-elamite" / "cdli-proto-elamite-unicode.txt"

NOISE_TOKENS = {"x", "…", "n"}


def load_char_to_code() -> dict[str, str]:
    """Reverse of the tsv: each row's char -> its primary code."""
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


ALLOGRAPH_PATH = ROOT / "abc" / "proto-elamite-allographs.tsv"


def load_allograph_clusters() -> dict[str, str]:
    """Manually curated cross-number allograph clusters (exact code ->
    cluster name). The ~x/@x numbering assumes variants cluster within one
    M-number, but real graphic similarity can cut across numbers (e.g.
    M005~a + M006 + M007~a forming one shape family while other M005/M007
    variants belong elsewhere). Codes listed in the tsv override
    base_number(); unlisted codes keep their default grouping."""
    clusters: dict[str, str] = {}
    if not ALLOGRAPH_PATH.exists():
        return clusters
    with ALLOGRAPH_PATH.open(encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            name, _, codes = line.partition("\t")
            for code in codes.split():
                clusters[code] = name
    return clusters


ALLOGRAPH_CLUSTERS = load_allograph_clusters()


def base_number(code: str) -> str:
    """Group a code for frequency counting: a manually curated allograph
    cluster if the exact code is listed in abc/proto-elamite-allographs.tsv,
    else strip graphic-variant suffixes down to the primary M/N catalogue
    number, e.g. 'M264~a' -> 'M264', 'M001+M379~c' -> 'M001+M379' (kept
    whole - a ligature is its own sign, not a variant)."""
    if code in ALLOGRAPH_CLUSTERS:
        return ALLOGRAPH_CLUSTERS[code]
    return re.sub(r"[~@][A-Za-z0-9]+", "", code)


def is_numeral_code(code: str) -> bool:
    return code.startswith("N")


def is_capacity_ligature(code: str) -> bool:
    """Dahl's 'complex capacity sign' (CCS): a word-sign fused with a
    numeral count, e.g. M036+1(N30D). These encode a counted quantity, not
    a stable lexical/phonetic value, so they don't belong in a syllabary."""
    return "+" in code and re.search(r"\(?N[0-9]", code) is not None


def token_codes(token: str, char2code: dict[str, str]) -> list[str] | None:
    """Every char's code for a whitespace-delimited token, or None if it's
    noise (uncertain reading, bracketed unresolved code, etc.)."""
    if token in NOISE_TOKENS or "[" in token or "]" in token:
        return None
    codes = [char2code.get(c) for c in token]
    if any(c is None for c in codes):
        return None
    return codes


def classify(token: str, char2code: dict[str, str]) -> tuple[str, str] | None:
    """Return (kind, code) for a token, or None to drop it.
    kind is 'sign', 'numeral', or 'capacity'."""
    codes = token_codes(token, char2code)
    if codes is None:
        return None

    if len(codes) == 1 or len(set(token)) == 1:
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


LINE_RE = re.compile(r"^(\d+[A-Za-z']*)\.\s*(.*?)\s*$")
HEADER_MARK_RE = re.compile(r"^#\s*\(?header\)?\s*$")


def read_lines() -> list[str]:
    return UNICODE_PATH.read_text(encoding="utf-8").split("\n")
