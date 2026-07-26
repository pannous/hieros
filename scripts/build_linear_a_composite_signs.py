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

# Conservative conventional labels used in Linear A transliterations. An
# unlisted component remains in catalogue notation rather than receiving a
# speculative phonetic or semantic value.
VALUES = {
    "AB001": "DA", "AB002": "RO", "AB003": "PA", "AB004": "TE",
    "AB007": "DI", "AB008": "A", "AB009": "SE", "AB010": "U",
    "AB013": "ME", "AB016": "QA", "AB021": "OVIS",
    "AB021F": "OVISf", "AB022M": "CAPm", "AB023": "MU",
    "AB024": "NE", "AB026": "RU", "AB027": "RE", "AB028": "I",
    "AB031": "SA", "AB037": "TI", "AB038": "E", "AB039": "PI",
    "AB040": "WI", "AB041": "SI", "AB050": "PU", "AB051": "DU",
    "AB053": "RI", "AB054": "WA", "AB056": "PA3", "AB057": "JA",
    "AB058": "SU", "AB059": "TA", "AB060": "RA", "AB065": "JU",
    "AB066": "TA2", "AB067": "KI", "AB069": "TU", "AB073": "MI",
    "AB074": "ZE", "AB076": "RA2", "AB077": "KA", "AB078": "QE",
    "AB080": "MA", "AB081": "KU", "AB120": "GRA",
    "AB122": "OLIV", "AB131A": "VINa", "AB131B": "VINb",
    "A100-102": "VIR", "A302": "OLE", "A702": "1/3",
    "A703": "1/5", "A704": "1/4", "A705": "1/8",
    "A706": "fraction-H?", "A708": "1/16", "A709": "fraction-L",
    "A709-2": "fraction-L2", "A709-3": "fraction-L3",
    "A713": "OMEGA",
}


def unicode_data() -> tuple[dict[str, str], dict[str, tuple[str, ...]], set[str]]:
    signs: dict[str, str] = {}
    codepoints: dict[str, str] = {}
    annotations: dict[str, str] = {}
    current_identifier: str | None = None
    entry = re.compile(r"^([0-9A-F]+)\tLINEAR A SIGN (\S+)(?: .*)?$")
    for line in NAMES_LIST.read_text(encoding="utf-8").splitlines():
        match = entry.match(line)
        if match:
            codepoint, identifier = match.groups()
            signs[identifier] = chr(int(codepoint, 16))
            codepoints[codepoint] = identifier
            current_identifier = identifier
        elif current_identifier and (line.startswith("\t*") or line.startswith("\tx")):
            annotations[current_identifier] = line

    components: dict[str, tuple[str, ...]] = {}
    aliases: set[str] = set()
    for identifier, annotation in annotations.items():
        numbered = re.fullmatch(r"A(\d+)", identifier)
        if not numbered or not 501 <= int(numbered.group(1)) <= 664:
            continue
        references = re.findall(r"\b([0-9A-F]{5})\b", annotation)
        resolved = tuple(codepoints[reference] for reference in references)
        if resolved:
            components[identifier] = resolved
        if annotation.startswith("\tx"):
            aliases.add(identifier)
    return signs, components, aliases


def main() -> None:
    signs, unicode_components, aliases = unicode_data()
    lines = [
        "Linear A composite signs (GORILA A501-A664)",
        "==============================================",
        "",
        "The first field is a single Unicode character where one exists. For",
        "catalogue signs omitted from Unicode, it is a sequence of the encoded",
        "components in N3755 order; this is not a claim about final ligature",
        "positioning or rendering. Every genuine composite has its components",
        "listed. Entries marked alias are alternate forms, not ligatures.",
        "",
        "Sources:",
        "- Unicode NamesList 17.0: abc/UCD/NamesList.txt",
        "- Preliminary Linear A proposal N3755:",
        "  https://www.unicode.org/L2/L2010/10004-n3755-lineara.pdf",
        "",
        "FORMAT",
        "glyph_or_sequence<TAB>LA_number<TAB>components<TAB>conventional_description<TAB>component_signs<TAB>type",
        "",
    ]

    for number in range(501, 665):
        identifier = f"A{number}"
        if identifier in signs:
            glyphs = signs[identifier]
            components = unicode_components.get(identifier, ())
            kind = "alias" if identifier in aliases else "ligature"
        else:
            components = MISSING[number]
            glyphs = "".join(signs[component] for component in components)
            kind = "alias" if len(components) == 1 else "decomposed ligature"
        formal = "+".join(components)
        description = "+".join(VALUES.get(component, f"*{component[1:]}") for component in components)
        component_signs = "".join(signs[component] for component in components)
        lines.append(
            f"{glyphs}\tLA{number:03d}\t{formal}\t{description}\t{component_signs}\t{kind}"
        )

    OUTPUT.write_text("\n".join(lines) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
