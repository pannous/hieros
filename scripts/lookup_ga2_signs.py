#!/usr/bin/env python3
"""
Look up meanings for Sumerian GÁ (GA2) signs and related variants
from local files and Wikipedia/EPSD.
"""
import re, os, sys, unicodedata, json, urllib.request, urllib.parse

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
UNICODE_BLOCK = os.path.join(BASE, "abc", "Cuneiform_Unicode_block.txt")
CUNEIFORM_LIST = os.path.join(BASE, "abc", "cuneiform.list")
PROTO_CUN_LIST = os.path.join(BASE, "abc", "proto-cuneiform.list")
EPSD_DIR = os.path.join(BASE, "dicts", "cuneiform", "epsd")

# ── Signs to look up ──────────────────────────────────────────────────────────
SIGNS = list("𒂷𒞮𒞰𒞱𒞲𒞳𒞴𒞵𒞶𒞷𒞸𒞹𒞺𒞻𒞼𒞾𒞿𒟂𒟃𒟄𒟍𒟎𒟏𒟓𒟔𒟕𒟖"
             "𒂸𒂹𒂺𒂻𒂼𒂽𒂾𒂿𒃀𒃁𒃂𒃃𒃄𒃅𒃆𒃇𒃈𒃉𒃊𒃋𒃌𒃍𒃎𒃏𒃐"
             "𒃑𒃒𒃓𒃔𒃕𒃖𒃗𒃘𒃙𒃚𒃛𒃜𒃝𒃞𒃟𒃠𒃡𒃢𒃣𒃤𒃥𒃦𒃧𒃨𒃩𒃪𒃫𒃬𒃭")
SIGNS = list(dict.fromkeys(SIGNS))  # deduplicate, preserve order


def load_unicode_block():
    """Return dict: char → (unicode_name, sign_list_no, meaning)"""
    result = {}
    with open(UNICODE_BLOCK, encoding="utf-8") as f:
        for line in f:
            parts = line.rstrip().split("\t")
            if len(parts) < 3:
                continue
            char = parts[0].strip()
            if not char or len(char) != 1:
                continue
            codepoint = parts[1].strip() if len(parts) > 1 else ""
            sign_name = parts[2].strip() if len(parts) > 2 else ""
            meaning = parts[5].strip() if len(parts) > 5 else ""
            result[char] = {"codepoint": codepoint, "sign_name": sign_name, "meaning": meaning}
    return result


def load_cuneiform_list():
    """Return dict: char → list of (transliteration, meaning) from cuneiform.list"""
    result = {}
    with open(CUNEIFORM_LIST, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            # Find any cuneiform characters on this line
            chars_on_line = [c for c in line if unicodedata.category(c) == "Lo"
                             and 0x12000 <= ord(c) <= 0x1254F or 0x12700 <= ord(c) <= 0x127FF]
            if not chars_on_line:
                continue
            # Try to extract transliteration and meaning
            parts = re.split(r"\t+", line)
            translit = parts[0].strip() if parts else ""
            meaning = parts[2].strip() if len(parts) > 2 else (parts[1].strip() if len(parts) > 1 else "")
            for ch in chars_on_line:
                if ch not in result:
                    result[ch] = []
                result[ch].append((translit, meaning))
    return result


def load_proto_cuneiform():
    """Return dict: char → {sign_name, ur3_equiv, raw} from proto-cuneiform.list"""
    result = {}
    def is_proto(c): return 0x12500 <= ord(c) <= 0x127FF
    def is_ur3(c): return 0x12000 <= ord(c) <= 0x1254F
    with open(PROTO_CUN_LIST, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            # Split on double-space or tab; first field = "CHAR signname [ur3char]"
            fields = re.split(r"\s{2,}|\t", line, maxsplit=3)
            first_field = fields[0].strip()
            if not first_field or not is_proto(first_field[0]):
                continue
            proto_char = first_field[0]
            # Sign name is alphanumeric tail of first_field (before any cuneiform char)
            tail = first_field[1:].strip()
            sign_name_m = re.match(r'([A-ZḪŠṬ₀-ₜ\xB2\xB3\xB9²·\xD7/\-\.]+(?:[\s·][A-ZḪŠṬ₀-ₜ·\xD7/\-\.]+)*)', tail)
            sign_name = sign_name_m.group(1).strip() if sign_name_m else tail.split()[0] if tail else ""
            # UR3 equiv: first standard cuneiform char anywhere in the line
            ur3_equiv = next((c for c in line if is_ur3(c)), "")
            raw = " ".join(fields[1:]).strip()[:80]
            result[proto_char] = {"sign_name": sign_name, "ur3": ur3_equiv, "raw": raw}
    return result


def load_epsd():
    """Return dict: GA2_sign_name → (word, meaning) from EPSD files"""
    result = {}
    for fname in sorted(os.listdir(EPSD_DIR)):
        if not fname.endswith(".txt"):
            continue
        with open(os.path.join(EPSD_DIR, fname), encoding="utf-8", errors="ignore") as f:
            content = f.read()
        # Pattern: WORD [MEANING] wr. |GA2×XXX| "gloss"
        for m in re.finditer(
            r'(\w[\w ]+?)\s+\[([A-Z][^\]]+)\][^\n]*?wr\.\s+(\|?GA2[^|"\n]+\|?)',
            content
        ):
            word, meaning, sign_pattern = m.group(1).strip(), m.group(2).strip(), m.group(3).strip()
            sign_pattern = re.sub(r'\s+', '', sign_pattern).upper()
            if sign_pattern not in result:
                result[sign_pattern] = (word, meaning)
    return result


def wikipedia_lookup(sign_name):
    """Fetch meaning from Wikipedia's cuneiform sign article."""
    # Normalize: GA_2 x AN → "Cuneiform sign GA2 times AN"
    query = f"Cuneiform sign {sign_name}"
    url = ("https://en.wikipedia.org/w/api.php?action=query&list=search"
           f"&srsearch={urllib.parse.quote(query)}&format=json&srlimit=1")
    try:
        with urllib.request.urlopen(url, timeout=5) as r:
            data = json.loads(r.read())
        hits = data.get("query", {}).get("search", [])
        if hits:
            snippet = re.sub(r"<[^>]+>", "", hits[0].get("snippet", ""))
            return snippet.strip()[:200]
    except Exception:
        pass
    return ""


def sign_name_to_epsd_key(sign_name):
    """Convert 'GA_2 x AN' → 'GA2×AN' for EPSD lookup."""
    key = sign_name.replace("_2", "2").replace(" x ", "×").replace(" / ", "/")
    key = re.sub(r"\s+", "", key).upper()
    return key


def main():
    print("Loading local data sources…")
    ub = load_unicode_block()
    cl = load_cuneiform_list()
    pc = load_proto_cuneiform()
    epsd = load_epsd()

    fetch_wiki = "--wiki" in sys.argv

    rows = []
    for sign in SIGNS:
        cp = f"U+{ord(sign):05X}"
        info = ub.get(sign, {})
        sign_name = info.get("sign_name", "")
        local_meaning = info.get("meaning", "")

        # Cuneiform.list lookup
        cl_entries = cl.get(sign, [])
        translit = ", ".join(t for t, _ in cl_entries if t and not any(c in t for c in "𒀀-𒎙")) or ""
        cl_meaning = "; ".join(m for _, m in cl_entries if m)[:100] or ""

        # EPSD lookup via sign name
        epsd_key = sign_name_to_epsd_key(sign_name) if sign_name else ""
        epsd_entry = epsd.get(epsd_key, ("", ""))
        epsd_word = epsd_entry[0] if epsd_entry else ""
        epsd_meaning = epsd_entry[1] if epsd_entry else ""

        # Proto-cuneiform lookup for 127xx signs
        pc_entry = pc.get(sign, {})
        if not sign_name and pc_entry:
            sign_name = pc_entry.get("sign_name", "")
        ur3_equiv = pc_entry.get("ur3", "")
        pc_raw = pc_entry.get("raw", "")

        # Unicode name fallback
        try:
            uname = unicodedata.name(sign)
        except ValueError:
            uname = ""

        # Wikipedia (only if requested and nothing found locally)
        wiki = ""
        if fetch_wiki and sign_name and not local_meaning and not cl_meaning and not epsd_meaning and not pc_raw:
            wiki = wikipedia_lookup(sign_name)

        rows.append({
            "sign": sign, "cp": cp, "sign_name": sign_name or uname,
            "translit": translit, "local": local_meaning,
            "cl": cl_meaning, "epsd_word": epsd_word, "epsd": epsd_meaning,
            "ur3": ur3_equiv, "pc_raw": pc_raw, "wiki": wiki,
        })

    # ── Output ────────────────────────────────────────────────────────────────
    print(f"\n{'Sign':<4} {'Codepoint':<10} {'Unicode Name':<40} {'UR3':<4} {'Meaning'}")
    print("─" * 120)
    for r in rows:
        meaning = r["local"] or r["cl"] or r["epsd"] or r["pc_raw"] or r["wiki"]
        if r["epsd_word"] and r["epsd"]:
            meaning = f"{r['epsd_word']} [{r['epsd']}]{' — ' + r['local'] if r['local'] else ''}"
        elif r["pc_raw"] and not meaning:
            meaning = r["pc_raw"]
        print(f"{r['sign']:<4} {r['cp']:<10} {r['sign_name']:<40} {r['ur3']:<4} {meaning[:80]}")

    # Also save TSV
    out_path = os.path.join(BASE, "dicts", "cuneiform", "ga2_signs_meanings.tsv")
    with open(out_path, "w", encoding="utf-8") as f:
        f.write("sign\tcodepoint\tsign_name\tur3_equiv\tmeaning_local\tmeaning_cl\tepsd_word\tepsd_meaning\tproto_raw\twiki\n")
        for r in rows:
            f.write("\t".join([r["sign"], r["cp"], r["sign_name"], r["ur3"],
                               r["local"], r["cl"], r["epsd_word"], r["epsd"],
                               r["pc_raw"], r["wiki"]]) + "\n")
    print(f"\n→ Saved to {out_path}")


if __name__ == "__main__":
    main()
