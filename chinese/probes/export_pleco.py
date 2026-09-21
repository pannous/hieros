#!/usr/bin/env python3
"""Export all Pleco flashcard/bookmark entries from the iCloud .pqb backups to TSV.

Pleco stores only headword + pronunciation in the backup (definitions stay in the
dictionaries), so the export is simple: simplified, traditional, pinyin, date added, categories.
"""
import glob, os, re, sqlite3, sys
from datetime import datetime

BACKUP_DIR = os.path.expanduser(
    "~/Library/Mobile Documents/TY59B5X35J~com~pleco~chinesesystem/Documents/Flashcard Backups")
OUTPUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "pleco_vocabulary.tsv")

VOWELS = "aoeiuvü"
TONE_MARKS = {
    "a": "aāáǎà", "o": "oōóǒò", "e": "eēéěè",
    "i": "iīíǐì", "u": "uūúǔù",
    "v": "üǖǘǚǜ", "ü": "üǖǘǚǜ",
}

def toned(syllable):
    """Convert one Pleco numbered syllable (e.g. 'tong2-') to tone-marked pinyin."""
    core = syllable.rstrip("-").strip()
    match = re.match(r"^([a-zA-Zü:]+)([1-5])$", core)
    if not match:
        return core
    letters, tone = match.group(1).lower().replace("u:", "v"), int(match.group(2))
    if tone == 5:
        return letters.replace("v", "ü")
    for vowel in ("a", "o", "e"):
        if vowel in letters:
            return letters.replace(vowel, TONE_MARKS[vowel][tone], 1).replace("v", "ü")
    for index, char in reversed(list(enumerate(letters))):
        if char in VOWELS:
            return (letters[:index] + TONE_MARKS[char][tone] + letters[index + 1:]).replace("v", "ü")
    return letters

def pinyin(pron):
    return " ".join(toned(s) for s in (pron or "").split("@") if s.strip())

def read_backup(path, entries):
    db = sqlite3.connect(f"file:{path}?mode=ro", uri=True)
    categories = dict(db.execute("select id, name from pleco_flash_categories"))
    assigns = {}
    for card, category in db.execute("select card, cat from pleco_flash_categoryassigns"):
        assigns.setdefault(card, set()).add(categories.get(category, str(category)))
    for card_id, hw, althw, pron, created in db.execute(
            "select id, hw, althw, pron, created from pleco_flash_cards"):
        simplified = (hw or "").replace("@", "")
        key = (simplified, pinyin(pron))
        entry = entries.setdefault(key, {
            "traditional": (althw or "").replace("@", ""),
            "categories": set(), "created": created or 0, "sources": set()})
        entry["categories"] |= assigns.get(card_id, set())
        entry["sources"].add(os.path.basename(path))
        entry["created"] = min(entry["created"] or created, created) if created else entry["created"]
    db.close()

def main():
    backups = sorted(glob.glob(os.path.join(BACKUP_DIR, "*.pqb")))
    if not backups:
        sys.exit(f"no .pqb backups found in {BACKUP_DIR}")
    entries = {}
    for path in backups:
        read_backup(path, entries)
    with open(OUTPUT, "w", encoding="utf-8") as out:
        out.write("simplified\ttraditional\tpinyin\tadded\tcategories\n")
        for (simplified, pron), entry in sorted(entries.items(), key=lambda kv: -kv[1]["created"]):
            added = datetime.fromtimestamp(entry["created"]).strftime("%Y-%m-%d") if entry["created"] else ""
            out.write(f"{simplified}\t{entry['traditional']}\t{pron}\t{added}\t{','.join(sorted(entry['categories']))}\n")
    print(f"{len(entries)} unique entries from {len(backups)} backups -> {os.path.normpath(OUTPUT)}")

main()
