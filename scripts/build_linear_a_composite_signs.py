#!/usr/bin/env python3
"""Build a continuous GORILA A501-A664 composite-sign reference.

Unicode-encoded composites are emitted as their single assigned character.
Catalogue entries omitted from Unicode are emitted as a plain sequence of
their components, following the decomposition in Unicode proposal N3755.
"""

from pathlib import Path
import re


ROOT = Path(__file__).resolve().parents[1]
NAMES_LIST = ROOT / "abc/UCD/NamesList.txt"
OUTPUT = ROOT / "texts/Linear_A_composite_signs.txt"

# Entries present in GORILA and proposal N3755 but intentionally absent as
# independent characters from the final Unicode repertoire. Component order
# follows N3755; it is an encoding fallback, not a typographic reconstruction.
MISSING = {
    507: ("AB013", "AB131A"),
    514: ("AB024", "AB067"),
    517: ("AB028", "AB120", "AB003"),
    518: ("AB028", "AB122"),
    519: ("AB028", "A301"),
    522: ("AB031", "AB131A"),
    533: ("AB041", "A303"),
    543: ("AB066", "A303"),
    544: ("AB067",),
    546: ("AB067", "AB080", "AB026"),
    558: ("AB080", "AB026"),
    560: ("AB080", "AB026"),
    561: ("AB080", "AB026", "AB013"),
    562: ("AB080", "AB026", "AB013"),
    567: ("A100-102",),
    590: ("AB131A", "AB041"),
    593: ("AB131A", "AB058"),
    597: ("AB131A", "AB120"),
    599: ("AB180",),
    605: ("A301", "AB076"),
    607: ("A301", "A351"),
    625: ("A303", "A703", "A304", "AB003"),
    630: ("A304", "AB003", "A303", "A703"),
    631: ("A304", "AB003", "A316", "A703"),
    632: ("A304", "A303"),
    633: ("A304", "A303"),
    635: ("A306", "A100-102", "A307"),
    636: ("A306", "A303", "A704"),
    639: ("A316", "AB002", "AB131A"),
    641: ("A317", "AB067", "A334"),
    647: ("A348", "A303"),
    650: ("A401-VAS", "AB008"),
}


def unicode_signs() -> dict[str, str]:
    signs: dict[str, str] = {}
    entry = re.compile(r"^([0-9A-F]+)\tLINEAR A SIGN (\S+)(?: .*)?$")
    for line in NAMES_LIST.read_text(encoding="utf-8").splitlines():
        match = entry.match(line)
        if match:
            codepoint, identifier = match.groups()
            signs[identifier] = chr(int(codepoint, 16))
    return signs


def main() -> None:
    signs = unicode_signs()
    lines = [
        "Linear A composite signs (GORILA A501-A664)",
        "==============================================",
        "",
        "The first field is a single Unicode character where one exists.",
        "For catalogue signs omitted from Unicode, it is a sequence of the",
        "encoded components in N3755 order; this is not a claim about final",
        "ligature positioning or rendering.",
        "",
        "Sources:",
        "- Unicode NamesList 17.0: abc/UCD/NamesList.txt",
        "- Preliminary Linear A proposal N3755:",
        "  https://www.unicode.org/L2/L2010/10004-n3755-lineara.pdf",
        "",
        "FORMAT",
        "glyph_or_sequence<TAB>LA_number<TAB>components_if_decomposed",
        "",
    ]

    for number in range(501, 665):
        identifier = f"A{number}"
        if identifier in signs:
            lines.append(f"{signs[identifier]}\tLA{number:03d}")
            continue
        components = MISSING[number]
        glyphs = "".join(signs[component] for component in components)
        lines.append(f"{glyphs}\tLA{number:03d}\t" + "+".join(components))

    OUTPUT.write_text("\n".join(lines) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
