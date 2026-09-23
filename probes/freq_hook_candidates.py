#!/usr/bin/env python3
"""List hooks the user wrote in dicts/chinese.freq.tsv column 5 that are not yet in the
curated mnemonics table. Output (TSV, stdout): rank, hanzi, pinyin, raw hook note, meaning,
existing hooks. Rows whose Latin hook words already appear in an existing hook are dropped.
Run from the repo root: python3 probes/freq_hook_candidates.py > probes/freq_hook_candidates.tsv
"""
import re
from collections import defaultdict

FREQ = "dicts/chinese.freq.tsv"
TABLE = "chinese/chinese.mnemonics.tsv"
UNIHAN_READINGS = "chinese/Unihan_Readings.txt"
HOOK_NOTE_COLUMN, MEANING_COLUMN = 4, 5
MEANING_WIDTH = 60
MIN_WORD_LENGTH = 3
LATIN_WORD = re.compile(r"[A-Za-zÀ-ÿ]{%d,}" % MIN_WORD_LENGTH)


def mandarin_readings():
    readings = {}
    for line in open(UNIHAN_READINGS, encoding="utf-8"):
        if "\tkMandarin\t" in line:
            code, _, value = line.rstrip("\n").split("\t")
            readings[chr(int(code[2:].split()[0], 16))] = value.split()[0]
    return readings


def existing_hooks():
    hooks = defaultdict(list)
    for line in open(TABLE, encoding="utf-8").read().splitlines()[3:]:
        cells = [c.strip() for c in line.split("\t")]
        if len(cells) >= 5:
            hooks[cells[1]].append(cells[3])
    return hooks


def already_covered(note, hooks):
    known = " ".join(hooks).lower()
    words = [w.lower() for w in LATIN_WORD.findall(note)]
    return bool(words) and all(w in known for w in words)


def main():
    readings, hooks = mandarin_readings(), existing_hooks()
    for line in open(FREQ, encoding="utf-8"):
        cells = line.rstrip("\n").split("\t")
        if len(cells) <= MEANING_COLUMN:
            continue
        hanzi, note = cells[1].strip(), cells[HOOK_NOTE_COLUMN].strip()
        if not LATIN_WORD.search(note) or already_covered(note, hooks[hanzi]):
            continue
        pinyin = readings.get(hanzi, cells[2].strip())
        meaning = cells[MEANING_COLUMN].strip()[:MEANING_WIDTH]
        print("\t".join([cells[0], hanzi, pinyin, note, meaning, " | ".join(hooks[hanzi])]))


if __name__ == "__main__":
    main()
