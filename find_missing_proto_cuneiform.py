#!/usr/bin/env python3
#!/usr/bin/env python3
"""
find_missing_proto_cuneiform.py

Parses docs/proto-cuneiform.list and compares the codepoints found there
against the full Unicode Proto-Cuneiform block range(s), then:
  1. Reports every code point in the block that does NOT appear in the file.
  2. Extracts sign names from PDFs/25211-proto-cuneiform.pdf (if present).
  3. Looks up Sumero-Akkadian cuneiform cross-references via abc/cuneiform.list.
  4. Writes a completed copy (docs/proto-cuneiform-full.list) with the
     missing code points inserted at the correct position with proper names
     and cuneiform cross-references.

Unicode Proto-Cuneiform allocation (proposed / pipeline for Unicode 18.0):
  • Proto-Cuneiform Numerals : U+12550 – U+1268F  (320 code points)
  • Proto-Cuneiform Signs    : U+12690 – U+12BFF  (1,392 code points)
  Combined span              : U+12550 – U+12BFF  (1,712 code points)

Usage:
    python3 find_missing_proto_cuneiform.py [path/to/proto-cuneiform.list]

If no path is given the script looks for docs/proto-cuneiform.list
relative to its own directory.

Requirements:
    pip install pymupdf   (for PDF extraction)
"""

import os
import re
import sys
import unicodedata

# ── Block definitions ────────────────────────────────────────────────
BLOCKS = {
    "Proto-Cuneiform Numerals": (0x12550, 0x1268F),
    "Proto-Cuneiform Signs":    (0x12690, 0x12BFF),
}

FULL_RANGE_START = min(s for s, _ in BLOCKS.values())
FULL_RANGE_END   = max(e for _, e in BLOCKS.values())

SIGNS_START = 0x12690
SIGNS_END   = 0x12BFF
NUMS_START  = 0x12550
NUMS_END    = 0x1268F


# ── Utility functions ────────────────────────────────────────────────

def extract_codepoints(filepath: str) -> set[int]:
    """Return every unique code point in the proto-cuneiform range
    that appears anywhere in *filepath*."""
    with open(filepath, "r", encoding="utf-8") as fh:
        text = fh.read()
    return {
        ord(ch) for ch in text
        if FULL_RANGE_START <= ord(ch) <= FULL_RANGE_END
    }


def char_name(cp: int) -> str:
    """Best-effort Unicode name for a code point."""
    try:
        return unicodedata.name(chr(cp))
    except ValueError:
        return "(unassigned / unnamed)"


# ── PDF extraction ───────────────────────────────────────────────────

def extract_pdf_sign_names(pdf_path: str) -> dict[int, str]:
    """Extract sign names from L2/25-211 proto-cuneiform proposal PDF.
    Returns {codepoint: short_sign_name}."""
    try:
        import fitz
    except ImportError:
        print("  (pymupdf not installed — skipping PDF extraction)", file=sys.stderr)
        return {}

    if not os.path.isfile(pdf_path):
        print(f"  (PDF not found: {pdf_path} — skipping)", file=sys.stderr)
        return {}

    doc = fitz.open(pdf_path)
    entries: dict[int, str] = {}

    for page_num in range(28, len(doc)):
        text = doc[page_num].get_text()
        lines = text.split('\n')
        i = 0
        while i < len(lines):
            line = lines[i].strip()
            m = re.match(r'^([0-9A-Fa-f]{4,5})\s+(.)', line)
            if m:
                cp = int(m.group(1), 16)
                if FULL_RANGE_START <= cp <= FULL_RANGE_END:
                    # Collect name from subsequent PROTO-CUNEIFORM line(s)
                    name_parts: list[str] = []
                    j = i + 1
                    while j < len(lines):
                        nline = lines[j].strip()
                        if nline.startswith('PROTO-CUNEIFORM'):
                            name_parts.append(nline)
                            k = j + 1
                            while k < len(lines):
                                cont = lines[k].strip()
                                if (cont
                                        and not re.match(r'^[0-9A-Fa-f]{4,5}\s', cont)
                                        and not cont.startswith('PROTO-CUNEIFORM')
                                        and not cont.startswith('L2/')
                                        and not re.match(r'^\d+$', cont)):
                                    name_parts.append(cont)
                                    k += 1
                                else:
                                    break
                            i = k - 1
                            break
                        elif (nline == ''
                              or re.match(r'^[0-9A-Fa-f]{4,5}\s', nline)
                              or nline.startswith('L2/')):
                            break
                        j += 1
                    full_name = ' '.join(name_parts)
                    short = full_name.replace('PROTO-CUNEIFORM SIGN ', '') \
                                     .replace('PROTO-CUNEIFORM NUMERIC SIGN ', 'NUM:')
                    if short:
                        entries[cp] = short
            i += 1
    doc.close()
    return entries


# ── Proto-cuneiform backwards reconstruction ─────────────────────────

def load_pc_name_lookup(src: str) -> dict[str, str]:
    """Build {simple_sign_name: first_proto-cuneiform_char} from the list file.

    Only simple (non-compound) names are indexed so they can serve as
    component lookups when reconstructing compound signs."""
    lookup: dict[str, str] = {}
    with open(src, 'r', encoding='utf-8') as fh:
        for line in fh:
            if not line.strip():
                continue
            parts = line.split()
            if len(parts) < 2:
                continue
            char, name = parts[0], parts[1]
            if '·' in name or '.' in name:
                continue  # skip compound entries
            lookup.setdefault(name.upper(), char)
    return lookup


def find_pc_backwards(compound_name: str, pc_dict: dict[str, str]) -> str:
    """Return the proto-cuneiform backwards reconstruction for a compound name.

    For LAGAB·U4  → 'pc_LAGAB·pc_U4'
    For GAN·KUR·A → 'pc_GAN·pc_KURpc_A'  (container·rest concatenated)
    Returns '' when no component can be resolved."""
    sep = '·' if '·' in compound_name else ('.' if '.' in compound_name else None)
    if not sep:
        return ''
    parts = compound_name.split(sep)

    def _lookup_pc(name: str) -> str:
        for n in (name, _strip_variant(name),
                  name.replace('SH', 'Š'), _strip_variant(name).replace('SH', 'Š')):
            found = pc_dict.get(n.upper(), '')
            if found:
                return found
        return ''

    resolved = [_lookup_pc(p.strip()) for p in parts]
    if not any(resolved):
        return ''
    # Format: container·(rest concatenated), matching user convention
    container = resolved[0] or parts[0]
    rest = ''.join(r or p for r, p in zip(resolved[1:], parts[1:]))
    return f'{container}·{rest}'


# ── Cuneiform cross-reference lookup ─────────────────────────────────

def load_cuneiform_dict(root: str) -> dict[str, str]:
    """Load sign-name → cuneiform-char mappings from cuneiform.list
    and cuneiform.csv.full."""
    name_to_cun: dict[str, str] = {}
    for relpath in ("abc/cuneiform.list", "abc/cuneiform.csv.full"):
        path = os.path.join(root, relpath)
        if not os.path.isfile(path):
            continue
        with open(path, "r", encoding="utf-8") as fh:
            for line in fh:
                line = line.strip()
                if not line or line.startswith('#'):
                    continue
                parts = line.split('\t')
                if len(parts) >= 2:
                    key = parts[0].strip()
                    val = parts[1].strip()
                    if val and ord(val[0]) >= 0x12000:
                        name_to_cun[key.upper()] = val
                        name_to_cun[key.lower()] = val
    return name_to_cun


def _strip_variant(name: str) -> str:
    """Remove trailing variant suffixes like -A, -B2, -C etc."""
    return re.sub(r'-[A-Z]\d*$', '', name)


def _lookup_simple(name: str, cun_dict: dict[str, str]) -> str:
    """Look up a single (non-compound) sign name in the cuneiform dict."""
    if name.startswith('NUM:'):
        return ''
    for try_name in (name, _strip_variant(name)):
        for n in (try_name, try_name.replace('SH', 'Š').replace('sh', 'š')):
            if n.upper() in cun_dict:
                return cun_dict[n.upper()]
            if n.lower() in cun_dict:
                return cun_dict[n.lower()]
    for sep in (' TIMES ', ' OVER ', ' JOINING '):
        if sep in name:
            return _lookup_simple(_strip_variant(name.split(sep)[0].strip()), cun_dict)
    return ''


def build_unicode_compound_lookup() -> dict[tuple[str, str], str]:
    """Return {(base_sign_name, modifier_sign_name): char} for signs named
    'CUNEIFORM SIGN X TIMES Y'."""
    lookup: dict[tuple[str, str], str] = {}
    for cp in range(0x12000, 0x12500):
        try:
            name = unicodedata.name(chr(cp))
        except ValueError:
            continue
        if not name.startswith('CUNEIFORM SIGN '):
            continue
        core = name[len('CUNEIFORM SIGN '):]
        m = re.match(r'^(.+?) TIMES (.+)$', core)
        if m:
            lookup[(m.group(1), m.group(2))] = chr(cp)
    return lookup


def _sign_unicode_component(char: str) -> str:
    """Strip 'CUNEIFORM SIGN ' prefix from the Unicode name of a cuneiform char."""
    try:
        name = unicodedata.name(char)
        if name.startswith('CUNEIFORM SIGN '):
            return name[len('CUNEIFORM SIGN '):]
    except ValueError:
        pass
    return ''


def find_cuneiform_xref(pdf_name: str, cun_dict: dict[str, str],
                        compound_lookup: dict[tuple[str, str], str] | None = None) -> str:
    """Resolve a proto-cuneiform sign name to Sumero-Akkadian cuneiform.

    For compound names (LAGAB·U4) returns base+modifier chars and, when a
    unified Unicode sign exists, appends ' ≈ <combined>'."""
    if pdf_name.startswith('NUM:'):
        return ''

    sep = '·' if '·' in pdf_name else ('.' if '.' in pdf_name else None)
    if sep and compound_lookup is not None:
        head, tail = pdf_name.split(sep, 1)
        base_char = _lookup_simple(head.strip(), cun_dict)
        mod_char  = _lookup_simple(tail.strip(), cun_dict)
        if base_char or mod_char:
            result = (base_char or '') + (mod_char or '')
            if base_char and mod_char:
                b_name = _sign_unicode_component(base_char[0])
                m_name = _sign_unicode_component(mod_char[0])
                combined = compound_lookup.get((b_name, m_name), '')
                if combined:
                    result += f' ≈ {combined}'
            return result

    return _lookup_simple(pdf_name, cun_dict)


# ── Build the completed file ─────────────────────────────────────────

def _enhance_compound_line(raw_line: str, cun_dict: dict[str, str],
                           compound_lookup: dict[tuple[str, str], str],
                           pc_dict: dict[str, str]) -> str:
    """Enrich a compound (·) line with the full cuneiform xref and
    proto-cuneiform backwards reconstruction."""
    if '·' not in raw_line and '.' not in raw_line:
        return raw_line
    parts = raw_line.split()
    if len(parts) < 2:
        return raw_line
    sign_char, name = parts[0], parts[1]
    sep = '·' if '·' in name else ('.' if '.' in name else None)
    if not sep:
        return raw_line
    new_xref = find_cuneiform_xref(name, cun_dict, compound_lookup)
    pc_back  = find_pc_backwards(name, pc_dict)
    if not new_xref and not pc_back:
        return raw_line
    line = f"{sign_char} {name}"
    if new_xref:
        line += f" {new_xref}"
    if pc_back:
        line += f"  {pc_back}"
    return line


def build_completed_copy(src: str, dst: str, present: set[int],
                         pdf_names: dict[int, str],
                         cun_dict: dict[str, str],
                         compound_lookup: dict[tuple[str, str], str] | None = None,
                         pc_dict: dict[str, str] | None = None,
                         ) -> dict[str, int]:
    """Write *dst* — a copy of *src* with missing codepoints inserted.

    Returns a stats dict with counts.
    """
    if compound_lookup is None:
        compound_lookup = {}
    if pc_dict is None:
        pc_dict = {}

    # Parse existing sign lines
    existing_lines: dict[int, str] = {}
    with open(src, "r", encoding="utf-8") as fh:
        original_text = fh.read()
    for raw_line in original_text.split('\n'):
        if not raw_line:
            continue
        cp = ord(raw_line[0])
        if SIGNS_START <= cp <= SIGNS_END:
            existing_lines[cp] = raw_line

    # Build merged sign lines
    merged_signs: dict[int, str] = {}
    new_with_xref = 0
    new_without_xref = 0
    enhanced = 0
    for cp in range(SIGNS_START, SIGNS_END + 1):
        if cp in existing_lines:
            line = existing_lines[cp]
            enriched = _enhance_compound_line(line, cun_dict, compound_lookup, pc_dict)
            if enriched != line:
                enhanced += 1
            merged_signs[cp] = enriched
        elif cp in pdf_names:
            name = pdf_names[cp]
            xref    = find_cuneiform_xref(name, cun_dict, compound_lookup)
            pc_back = find_pc_backwards(name, pc_dict)
            line = f"{chr(cp)} {name}"
            if xref:
                line += f" {xref}"
                new_with_xref += 1
            else:
                new_without_xref += 1
            if pc_back:
                line += f"  {pc_back}"
            merged_signs[cp] = line

    # Split original into pre-body / body / post-body
    all_lines = original_text.split('\n')
    pre_lines: list[str] = []
    post_lines: list[str] = []
    in_body = False
    for raw_line in all_lines:
        if not raw_line:
            (post_lines if in_body else pre_lines).append(raw_line)
            continue
        cp = ord(raw_line[0])
        if SIGNS_START <= cp <= SIGNS_END:
            in_body = True
        else:
            (post_lines if in_body else pre_lines).append(raw_line)

    # Write
    with open(dst, "w", encoding="utf-8") as fh:
        for line in pre_lines:
            fh.write(line + '\n')
        for cp in sorted(merged_signs.keys()):
            fh.write(merged_signs[cp] + '\n')
        for line in post_lines:
            fh.write(line + '\n')

    return {
        "original": len(existing_lines),
        "total": len(merged_signs),
        "new": len(merged_signs) - len(existing_lines),
        "with_xref": new_with_xref,
        "without_xref": new_without_xref,
        "enhanced": enhanced,
    }


# ── Main ─────────────────────────────────────────────────────────────

def main() -> None:
    # Resolve paths
    if len(sys.argv) > 1:
        src = sys.argv[1]
    else:
        here = os.path.dirname(os.path.abspath(__file__))
        src = os.path.join(here, "docs", "proto-cuneiform.list")
    if not os.path.isfile(src):
        sys.exit(f"Error: file not found: {src}")

    root = os.path.dirname(os.path.abspath(src)).rstrip('/docs')
    # heuristic: go up from docs/ to project root
    if root.endswith('/docs'):
        root = root[:-5]
    else:
        root = os.path.dirname(os.path.abspath(src))
        # try parent
        if os.path.isdir(os.path.join(root, '..', 'abc')):
            root = os.path.join(root, '..')
        root = os.path.abspath(root)

    pdf_path = os.path.join(root, "PDFs", "25211-proto-cuneiform.pdf")

    # Parse existing codepoints
    present = extract_codepoints(src)

    # ── Analysis report ──────────────────────────────────────────────
    total_missing = 0
    all_missing: list[tuple[int, str]] = []

    for block_name, (start, end) in sorted(BLOCKS.items(), key=lambda x: x[1][0]):
        block_size = end - start + 1
        block_present = {cp for cp in present if start <= cp <= end}
        block_missing = []
        for cp in range(start, end + 1):
            if cp not in present:
                block_missing.append((cp, char_name(cp)))
        total_missing += len(block_missing)
        all_missing.extend(block_missing)

        print(f"{'─' * 60}")
        print(f"Block: {block_name}  (U+{start:05X} – U+{end:05X})")
        print(f"  Block size : {block_size}")
        print(f"  In file    : {len(block_present)}")
        print(f"  Missing    : {len(block_missing)}")
        print()

    full_size = FULL_RANGE_END - FULL_RANGE_START + 1
    print(f"{'═' * 60}")
    print(f"TOTAL  range U+{FULL_RANGE_START:05X} – U+{FULL_RANGE_END:05X}  "
          f"({full_size} code points)")
    print(f"  Present in file : {len(present)}")
    print(f"  Missing         : {total_missing}")
    print(f"{'═' * 60}\n")

    if all_missing:
        print("Missing code points:\n")
        print(f"{'CP':>8}  {'Hex':>8}  {'Char':>4}  Name")
        print(f"{'─'*8}  {'─'*8}  {'─'*4}  {'─'*40}")
        for cp, name in all_missing:
            print(f"{cp:>8d}  U+{cp:05X}  {chr(cp):>4}  {name}")
    else:
        print("No missing code points — the file covers the entire block.")

    # ── Extract sign names from PDF ──────────────────────────────────
    print(f"\nExtracting sign names from PDF ...")
    pdf_names = extract_pdf_sign_names(pdf_path)
    print(f"  {len(pdf_names)} sign names extracted from PDF.")

    # ── Load cuneiform cross-reference dictionary ────────────────────
    cun_dict = load_cuneiform_dict(root)
    print(f"  {len(cun_dict)} cuneiform name-to-char mappings loaded.")

    # ── Build Unicode compound sign lookup (LAGAB TIMES UD → 𒇩) ────
    compound_lookup = build_unicode_compound_lookup()
    print(f"  {len(compound_lookup)} Unicode compound sign mappings indexed.")

    # ── Build proto-cuneiform component name lookup (backwards recon) ─
    pc_dict = load_pc_name_lookup(src)
    print(f"  {len(pc_dict)} proto-cuneiform simple-name mappings loaded.")

    # ── Build completed copy ─────────────────────────────────────────
    dst = os.path.join(os.path.dirname(src), "proto-cuneiform-full.list")
    stats = build_completed_copy(src, dst, present, pdf_names, cun_dict,
                                 compound_lookup, pc_dict)

    print(f"\nCompleted copy written to:\n  {dst}")
    print(f"  Original entries : {stats['original']}")
    print(f"  Enhanced entries : {stats['enhanced']}  (compound xref enriched)")
    print(f"  New entries      : {stats['new']}")
    print(f"    with xref      : {stats['with_xref']}")
    print(f"    without xref   : {stats['without_xref']}")
    print(f"  Total entries    : {stats['total']}")


if __name__ == "__main__":
    main()
