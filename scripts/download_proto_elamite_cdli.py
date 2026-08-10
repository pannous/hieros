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

TOKEN_RE = re.compile(r"\|?[0-9]*\(?[MN][0-9A-Za-z@~+#?!*]*\)?\|?")
TRAILING_MARKS_RE = re.compile(r"[#?!*]+$")


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
    return TRAILING_MARKS_RE.sub("", code)


def convert_token(token: str, code2char: dict[str, str], unresolved: set[str]) -> str:
    """Convert one CDLI sign token (e.g. '3(N01)', 'M157#', '|M002+M379|') to Unicode."""
    m = re.match(r"\|?([0-9]*)\(?([MN][0-9A-Za-z@~+#?!*]*?)\)?\|?$", token)
    if not m:
        return token
    count, code = m.groups()
    code = clean_code(code)

    if "+" in code:
        parts = [code2char.get(p) for p in code.split("+")]
        if all(parts):
            rendered = "".join(parts)
        else:
            unresolved.add(code)
            rendered = f"[{code}]"
    elif code in code2char:
        rendered = code2char[code]
    else:
        unresolved.add(code)
        rendered = f"[{code}]"

    # Proto-Elamite numerals are additive tallies: N(count) means the sign
    # is impressed `count` times, so roll that out as repeated glyphs
    # instead of a decimal digit prefix.
    return rendered * int(count) if count else rendered


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
