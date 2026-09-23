#!/usr/bin/env python3
"""Copy curated hooks from the mnemonics table into column 5 of the frequency table.

A hook is appended only when the character's column-5 notes don't already mention it.
Usage (from repo root): python3 probes/sync_hooks_to_freq.py [--write]
"""
import re
import sys

MNEMONICS = "chinese/chinese.mnemonics.tsv"
FREQUENCY = "dicts/chinese.freq.tsv"
MNEMONIC_COLUMNS = 5
HANZI_COLUMN = 1
HOOK_NOTES_COLUMN = 4
SEPARATOR = " "


def curated_hooks():
    hooks = {}
    for line in open(MNEMONICS, encoding="utf-8"):
        cells = [c.strip() for c in line.rstrip("\n").split("\t")]
        if len(cells) < MNEMONIC_COLUMNS or not cells[0].startswith("★"):
            continue
        hooks.setdefault(cells[1], []).append(cells[3])
    return hooks


def hook_parts(hook):
    for part in re.split(r"[/|,;]", hook):
        word = part.strip(" -!?.()").lower()
        if word:
            yield word


def already_noted(hook, notes):
    return any(part in notes.lower() for part in hook_parts(hook))


def main():
    write = "--write" in sys.argv
    hooks = curated_hooks()
    lines = open(FREQUENCY, encoding="utf-8").read().split("\n")
    changed = []
    for index, line in enumerate(lines):
        cells = line.split("\t")
        if len(cells) <= HOOK_NOTES_COLUMN:
            continue
        hanzi = cells[HANZI_COLUMN].strip()
        notes = cells[HOOK_NOTES_COLUMN]
        missing = [h.strip(" |") for h in hooks.get(hanzi, []) if h.strip(" |") and not already_noted(h, notes)]
        if not missing:
            continue
        cells[HOOK_NOTES_COLUMN] = SEPARATOR.join(filter(None, [notes.strip(), *missing]))
        lines[index] = "\t".join(cells)
        changed.append(f"{hanzi}\t{notes.strip()!r} + {missing}")
    print("\n".join(changed))
    print(f"{len(changed)} rows {'updated' if write else 'would change'}")
    if write:
        open(FREQUENCY, "w", encoding="utf-8").write("\n".join(lines))


if __name__ == "__main__":
    main()
