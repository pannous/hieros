#!/usr/bin/env python3
"""List frequent single hanzi with no raw hook note and no mnemonics-table entry."""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
FREQ_FILE = ROOT / "dicts/chinese.freq.tsv"
MNEMONICS_FILE = ROOT / "chinese/chinese.mnemonics.tsv"


def is_hanzi(char):
    return "㐀" <= char <= "鿿" or "\U00020000" <= char <= "\U0002ffff"


def covered_chars():
    covered = set()
    for line in MNEMONICS_FILE.read_text().splitlines():
        columns = line.split("\t")
        if len(columns) > 1:
            covered.update(c for c in columns[1] if is_hanzi(c))
    return covered


def hookless_rows():
    covered = covered_chars()
    for line in FREQ_FILE.read_text().splitlines():
        columns = line.split("\t") + [""] * 6
        rank, hanzi, pinyin, numbered, hook, meaning = (c.strip() for c in columns[:6])
        if len(hanzi) == 1 and is_hanzi(hanzi) and not hook and hanzi not in covered:
            yield rank, hanzi, pinyin, numbered, meaning


if __name__ == "__main__":
    rows = sorted(hookless_rows(), key=lambda row: int(row[0]) if row[0].isdigit() else 10**9)
    for row in rows:
        print("\t".join(row))
