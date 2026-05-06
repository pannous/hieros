#!/usr/bin/env python3
"""cuneiformize: look up classical cuneiform signs by name from cuneiform.list"""
import re, sys

CUNEIFORM_LIST = "cuneiform.list"

def build_lookup(path=CUNEIFORM_LIST):
    lookup = {}
    with open(path, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith('#'):
                continue
            parts = line.split('\t')
            if len(parts) < 2:
                continue
            name = parts[0].strip().upper()
            chars = parts[1].strip()
            if not name or not chars:
                continue
            # Take only the first cuneiform codepoint
            for c in chars:
                if '\U00012000' <= c <= '\U000133FF':
                    lookup[name] = c
                    break
    return lookup

def cuneiformize(name, lookup=None):
    if lookup is None:
        lookup = build_lookup()
    name = name.strip().upper()
    if name in lookup:
        return lookup[name]
    # Try base name before · or × modifier
    base = re.split(r'[·×]', name)[0]
    return lookup.get(base)

def enrich_file(filepath, lookup):
    with open(filepath, encoding="utf-8") as f:
        lines = f.readlines()

    out = []
    changed = 0
    for line in lines:
        stripped = line.rstrip('\n')
        rest = stripped.strip()
        # Skip blank or comment lines
        if not rest or rest.startswith('#') or rest.startswith('---'):
            out.append(stripped)
            continue

        # First char must be cuneiform (proto-cuneiform or regular)
        first = rest[0]
        if not ('\U00012000' <= first <= '\U000133FF'):
            out.append(stripped)
            continue

        # Extract sign name: first run of uppercase letters/digits/·×._-
        after_glyph = rest[1:].lstrip()
        m = re.match(r'([A-Z][A-Z0-9·×._\-]*)', after_glyph)
        if not m:
            out.append(stripped)
            continue

        sign_name = m.group(1)
        classical = cuneiformize(sign_name, lookup)
        if classical and classical not in stripped:
            stripped = stripped.rstrip() + ' ' + classical
            changed += 1

        out.append(stripped)

    with open(filepath, 'w', encoding="utf-8") as f:
        f.write('\n'.join(out) + '\n')

    print(f"Enriched {changed} lines in {filepath}")

if __name__ == "__main__":
    if len(sys.argv) > 1:
        # CLI: cuneiformize SIGNNAME
        lookup = build_lookup()
        for name in sys.argv[1:]:
            result = cuneiformize(name, lookup)
            print(result or f"(not found: {name})")
    else:
        lookup = build_lookup()
        enrich_file("proto-cuneiform.list", lookup)
