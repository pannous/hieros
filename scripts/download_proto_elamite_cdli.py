#!/usr/bin/env python3
"""Download all Proto-Elamite texts from CDLI and convert sign names to Unicode.

Fetches the bulk ATF export for the CDLI "Proto-Elamite" period search
(https://cdli.earth/search?...&period=Proto-Elamite) and renders every
CDLI sign name (M001, N14, |M002+M379|, 3(N01), ...) to its Unicode
Proto-Elamite character using abc/proto-elamite.tsv.

Usage:
    scripts/download_proto_elamite_cdli.py [--refresh]

Writes:
    texts/proto-elamite/cdli-proto-elamite.atf       raw ATF as downloaded
    texts/proto-elamite/cdli-proto-elamite-unicode.txt  Unicode-converted
"""
from __future__ import annotations

import argparse
import re
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TSV_PATH = ROOT / "abc" / "proto-elamite.tsv"
OUT_DIR = ROOT / "texts" / "proto-elamite"
RAW_PATH = OUT_DIR / "cdli-proto-elamite.atf"
UNICODE_PATH = OUT_DIR / "cdli-proto-elamite-unicode.txt"

SEARCH_URL = (
    "https://cdli.earth/search"
    "?layout=full&limit=5000&period=Proto-Elamite&format=atf&aspect=inscriptions"
)

# A ligature can itself contain a count-prefixed sub-sign, e.g.
# |M260+1(N14)| or |M351+3(N01)| - so the character class has to allow
# nested parens throughout, not just as a single optional pair at the ends.
TOKEN_RE = re.compile(r"\|?[0-9A-Za-z@~+#?!*()]*[MN][0-9][0-9A-Za-z@~+#?!*()]*\|?")
TRAILING_MARKS_RE = re.compile(r"[#?!*]+$")
LEADING_MARKS_RE = re.compile(r"^[#?!*]+")
SEGMENT_RE = re.compile(r"^([0-9]*)\(?([MN][0-9A-Za-z@~]*?)\)?$")

# Confirmed CDLI data-entry typos (wrong sign-class letter): the source ATF
# has no catalogued M-sign matching these, but the equivalent N-sign is
# well attested and fits a numeral-tally context.
CODE_ALIASES = {
    "M39B": "N39B",
}


def load_code_map() -> dict[str, str]:
    code2char: dict[str, str] = {}
    with TSV_PATH.open(encoding="utf-8") as f:
        for line in f:
            line = line.rstrip("\n")
            if not line.strip():
                continue
            cols = line.split("\t")
            char = cols[0]
            for code in cols[1:]:
                code = code.strip()
                if code:
                    code2char.setdefault(code, char)
    return code2char


def clean_code(code: str) -> str:
    code = code.strip("|")
    code = LEADING_MARKS_RE.sub("", code)
    return TRAILING_MARKS_RE.sub("", code)


VARIANT_SUFFIX_RE = re.compile(r"(~[A-Za-z0-9]+|@[A-Za-z0-9]+)$")
N_CODE_RE = re.compile(r"^N([0-9]{1,2})(?:([A-Z])|~([a-z])|@([a-z]))?$")


def spelling_variants(code: str) -> list[str]:
    """Alternate spellings CDLI uses for the same sign, tried in order of
    likeliness before we resort to stripping graphic-variant suffixes.
    N-numeral digits are sometimes given unpadded (N1 for N01), and their
    trailing lettered sub-variant is spelled three ways in the wild for
    what the tsv catalogues under a single, inconsistent convention per
    sign: appended directly (N08A), tilde-lowercase (N39~b), or as an
    '@' variant (N14@b)."""
    variants = [code]
    if code in CODE_ALIASES:
        variants.append(CODE_ALIASES[code])
    m = N_CODE_RE.match(code)
    if m:
        digits, upper, tilde, at = m.groups()
        letter = (upper or tilde or at or "").lower()
        padded = f"N{int(digits):02d}"
        if letter:
            variants += [f"{padded}{letter.upper()}", f"{padded}~{letter}", f"{padded}@{letter}"]
        else:
            variants.append(padded)
    return variants


def resolve(code: str, code2char: dict[str, str]) -> str | None:
    """Look up a sign code, falling back to its base sign if the exact
    graphic variant (trailing ~x / @x) isn't separately catalogued."""
    while code:
        for variant in spelling_variants(code):
            if variant in code2char:
                return code2char[variant]
        stripped = VARIANT_SUFFIX_RE.sub("", code)
        if stripped == code:
            return None
        code = stripped
    return None


def render_segment(segment: str, code2char: dict[str, str]) -> str | None:
    """Render one '+'-joined ligature component, e.g. 'M260' or '1(N14)'."""
    m = SEGMENT_RE.match(segment)
    if not m:
        return None
    count, code = m.groups()
    glyph = resolve(clean_code(code), code2char)
    if glyph is None:
        return None
    return glyph * int(count) if count else glyph


def convert_token(token: str, code2char: dict[str, str], unresolved: set[str]) -> str:
    """Convert one CDLI sign token (e.g. '3(N01)', 'M157#', '|M260+1(N14)|') to Unicode."""
    code = clean_code(token)
    if not code:
        return token

    if "+" in code:
        # Try the whole ligature as a single catalogued sign first (e.g.
        # 'M001+M379~c' falling back to the base ligature 'M001+M379').
        rendered = resolve(code, code2char)
        if rendered is None:
            # No catalogued ligature for this combination - render (and
            # concatenate) each '+'-joined component on its own. Components
            # can carry their own count, e.g. |M260+1(N14)|.
            parts = [render_segment(p, code2char) for p in code.split("+")]
            rendered = "".join(parts) if all(parts) else None
    else:
        rendered = render_segment(code, code2char)

    if rendered is None:
        unresolved.add(code)
        rendered = f"[{code}]"

    return rendered


def convert_atf(atf_text: str, code2char: dict[str, str]) -> tuple[str, set[str]]:
    unresolved: set[str] = set()

    def repl(m: re.Match) -> str:
        token = m.group(0)
        if not re.search(r"[MN][0-9]", token):
            return token
        return convert_token(token, code2char, unresolved)

    return TOKEN_RE.sub(repl, atf_text), unresolved


def fetch_atf() -> str:
    req = urllib.request.Request(SEARCH_URL, headers={"User-Agent": "Mozilla/5.0"})
    with urllib.request.urlopen(req, timeout=120) as resp:
        return resp.read().decode("utf-8")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--refresh", action="store_true", help="re-download even if raw ATF exists")
    args = parser.parse_args()

    OUT_DIR.mkdir(parents=True, exist_ok=True)

    if args.refresh or not RAW_PATH.exists():
        print(f"downloading {SEARCH_URL}")
        atf_text = fetch_atf()
        RAW_PATH.write_text(atf_text, encoding="utf-8")
    else:
        atf_text = RAW_PATH.read_text(encoding="utf-8")

    n_texts = atf_text.count("\n&P")
    print(f"raw ATF: {len(atf_text):,} chars, {n_texts:,} texts -> {RAW_PATH}")

    code2char = load_code_map()
    print(f"loaded {len(code2char):,} sign codes from {TSV_PATH}")

    unicode_text, unresolved = convert_atf(atf_text, code2char)
    UNICODE_PATH.write_text(unicode_text, encoding="utf-8")
    print(f"wrote {UNICODE_PATH}")

    if unresolved:
        codes = sorted(unresolved)
        print(f"{len(codes)} unresolved sign codes (kept as [CODE]), e.g. {codes[:15]}")


if __name__ == "__main__":
    main()
