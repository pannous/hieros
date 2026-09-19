#!/usr/bin/env python3
"""One-shot merge of the two Chinese mnemonic tables into chinese/chinese.mnemonics.tsv.

dicts/chinese-mnemonics.tsv is the larger, better-copyedited corpus (full German words,
fixed typos, corrected Pinyin), so it wins on conflict. chinese/ wins only where it
carries strictly more information — listed in CHINESE_WINS.
Run from the repo root: python3 probes/merge_mnemonics.py
"""
CANONICAL = "chinese/chinese.mnemonics.tsv"
MINED = "dicts/chinese-mnemonics.tsv"
MIN_TABS = 4
CHINESE_WINS = {"鿖"}  # dicts/ has Pinyin "—" and drops the 合利斯托斯 gloss


def lines(path):
    return open(path, encoding="utf-8").read().splitlines()


def is_entry(line):
    return line.count("\t") >= MIN_TABS


def word(line):
    return line.split("\t")[1].strip()


def main():
    canonical, mined = lines(CANONICAL), lines(MINED)

    header = canonical[:3]
    body = canonical[3:]
    # Entry rows run until the first blank line; everything from there to EOF is the free-form
    # cluster block, which mixes untabbed notes with stray tabbed rows (谷 gǔ) and must survive.
    tail_start = next(i for i, l in enumerate(body) if not l.strip())
    entries = {word(l): l for l in body[:tail_start] if is_entry(l)}
    clusters = body[tail_start:]

    mined_entries = [l for l in mined[1:] if is_entry(l)]
    mined_words = {word(l) for l in mined_entries}

    merged = [l for w, l in entries.items() if w not in mined_words]  # hand-added, newest first
    for line in mined_entries:
        merged.append(entries[word(line)] if word(line) in CHINESE_WINS else line)

    out = header + merged + clusters
    open(CANONICAL, "w", encoding="utf-8").write("\n".join(out) + "\n")

    kept = len(merged) - len(mined_entries)
    print(f"{len(merged)} entries ({len(mined_entries)} from dicts/, {kept} only in chinese/)")
    print(f"{len(clusters)} free-form cluster lines preserved")
    lost = {w for w in entries if w not in mined_words} - {word(l) for l in merged if is_entry(l)}
    print("lost entries:", lost or "none")


if __name__ == "__main__":
    main()
