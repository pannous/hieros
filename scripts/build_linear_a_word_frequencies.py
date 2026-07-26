#!/usr/bin/env python3
"""Build a frequency-ranked list of phonetic Linear A word forms."""

from collections import Counter, defaultdict
from pathlib import Path
import re


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "Linear-A-texts-v3.md"
OUTPUT = ROOT / "texts/Linear_A_phonetic_word_frequencies.tsv"

# Lower-case logogram labels occurring without their catalogue prefixes.
LOGOGRAMS = {"arom", "fic", "gra", "ole", "oliv", "vir"}

# A phonetic form consists of one or more lower-case syllabic readings. The
# bullet marks a doubtful/unique sign reading and subscripts distinguish sign
# values; both are retained rather than silently normalized away.
PHONETIC = re.compile(
    r"^[•]?[a-zü]+(?:_[0-9]+)?(?:-[•]?[a-zü]+(?:_[0-9]+)?)*$"
)


def records():
    for block in SOURCE.read_text(encoding="utf-8").split("\n\n"):
        lines = [line.rstrip() for line in block.splitlines() if line.strip()]
        if len(lines) != 2 or "   " not in lines[0]:
            continue
        document, glyph_text = lines[0].split("   ", 1)
        yield document, glyph_text.split(), lines[1].split()


def is_phonetic_word(token: str) -> bool:
    return token not in LOGOGRAMS and PHONETIC.fullmatch(token) is not None


def main() -> None:
    counts: Counter[str] = Counter()
    documents: dict[str, set[str]] = defaultdict(set)
    glyphs: dict[str, Counter[str]] = defaultdict(Counter)

    for document, glyph_tokens, transcription_tokens in records():
        aligned = len(glyph_tokens) == len(transcription_tokens)
        for index, token in enumerate(transcription_tokens):
            if not is_phonetic_word(token):
                continue
            counts[token] += 1
            documents[token].add(document)
            if aligned:
                glyph = glyph_tokens[index]
                if glyph not in {"|", "||", "𐝫", "—"}:
                    glyphs[token][glyph] += 1

    ordered = sorted(counts, key=lambda word: (-counts[word], word))
    lines = [
        "rank\tcount\tdocuments\tword\tmost_common_signs\tsign_spellings",
    ]
    for rank, word in enumerate(ordered, 1):
        spellings = glyphs[word]
        common = spellings.most_common(1)[0][0] if spellings else ""
        alternatives = " ".join(
            f"{glyph}:{count}" for glyph, count in spellings.most_common()
        )
        lines.append(
            f"{rank}\t{counts[word]}\t{len(documents[word])}\t{word}\t{common}\t{alternatives}"
        )

    OUTPUT.write_text("\n".join(lines) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
