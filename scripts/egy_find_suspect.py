#!/usr/bin/env python3
"""Cross-check my_egyptian_dictionary.csv against every available Egyptian reference
dictionary and report entries whose GLYPHS<->MEANING pairing looks unsupported.

Speculative etymology in the note column is expected and never a flag by itself.
"""

import json
import re
import sys
import unicodedata
from collections import Counter, defaultdict
from difflib import SequenceMatcher
from pathlib import Path

PROJECT = Path(__file__).resolve().parent.parent
USER_DICT = PROJECT / "my_egyptian_dictionary.csv"
SOURCES = PROJECT / "probes" / "egy_dict_sources"
PHARALEX = SOURCES / "repos" / "pharalex"
REPORT_MD = PROJECT / "probes" / "egyptian_suspect_entries.md"
REPORT_TSV = PROJECT / "probes" / "egyptian_suspect_entries.tsv"
GARDINER_UNICODE_TSV = SOURCES / "gardiner_unicode.tsv"
GARDINER_APP_CSV = PROJECT / "app" / "gardiner.csv"
MDC_ALIAS_JS = PROJECT / "scripts" / "gardiner_map_manuel_de_codage.js"
MDC_ALIAS_TXT = SOURCES / "mdc_to_gardiner.txt"
SYSTEM_WORDLIST = Path("/usr/share/dict/words")

HIEROGLYPH_RANGES = ((0x13000, 0x1342F), (0x13430, 0x1345F), (0x13460, 0x143FA))
PRIVATE_USE_RANGES = ((0xE000, 0xF8FF), (0xF0000, 0xFFFFD), (0x100000, 0x10FFFD))

MAX_ROWS_PER_SECTION = 300
NOTE_TRUNCATE = 90
NEAR_MISS_CANDIDATE_CAP = 600
NEAR_MISS_MIN_RATIO = 0.6
CONTRADICTION_MIN_ENTRIES = 2

GLOSS_SPLIT = re.compile(r"[,;/]|\bor\b")
PARENTHETICAL = re.compile(r"\([^)]*\)|\[[^\]]*\]")
WORD_RE = re.compile(r"[a-z][a-z']*")  # hyphens split, so "cattle-tax" matches "cattle tax"
MDC_TOKEN_SPLIT = re.compile(r"[-:*()\[\]{}<>#!.\s]+")
NOTE_LIKE_MARKERS = ("➙", "←", "→", "<>", "≈", "≟", "=", "+++", "??")

STOPWORDS = {
    "a", "an", "the", "of", "to", "in", "on", "at", "for", "with", "and", "or",
    "be", "is", "are", "was", "were", "as", "by", "it", "its", "that", "this",
    "from", "into", "up", "out", "not", "no", "one", "two", "some", "any",
    "someone", "something", "s", "sth", "sb", "etc", "v", "n", "adj", "adv",
    "prn", "pron", "vb", "noun", "verb", "pl", "sing", "masc", "fem", "du",
    "vs", "eg", "ie", "cf", "vgl", "de", "en",
}
INFLECTION_SUFFIXES = ("s", "es", "ed", "d", "ing", "er", "est", "ly", "'s")
GRAMMAR_ONLY_GLOSSES = {"unknown", "uncertain", "meaning unknown", ""}

SPECULATION_MARKERS = ("?", "??", "lol", "maybe", "perhaps", "guess", "≟", "!?")


def is_hieroglyph(char):
    return any(lo <= ord(char) <= hi for lo, hi in HIEROGLYPH_RANGES)


def is_private_use(char):
    return any(lo <= ord(char) <= hi for lo, hi in PRIVATE_USE_RANGES)


def warn(message):
    print(f"warning: {message}", file=sys.stderr)


# ---------------------------------------------------------------- gardiner map

def load_gardiner_to_glyph():
    code_to_glyph = {}
    for codepoint in range(HIEROGLYPH_RANGES[0][0], HIEROGLYPH_RANGES[0][1] + 1):
        char = chr(codepoint)
        try:
            name = unicodedata.name(char)
        except ValueError:
            continue
        match = re.fullmatch(r"EGYPTIAN HIEROGLYPH ([A-Z]{1,2})0*(\d+)([A-Z]?)", name)
        if match:
            code_to_glyph.setdefault(f"{match[1]}{match[2]}{match[3]}", char)

    def absorb(path, code_col, glyph_col, sep="\t", skip_header=True):
        if not path.exists():
            warn(f"missing gardiner source {path}")
            return
        with path.open(encoding="utf-8", errors="replace") as handle:
            for index, line in enumerate(handle):
                if skip_header and index == 0:
                    continue
                cells = line.rstrip("\n").split(sep)
                if len(cells) <= max(code_col, glyph_col):
                    continue
                code, glyph = cells[code_col].strip(), cells[glyph_col].strip()
                if code and glyph and all(is_hieroglyph(c) for c in glyph):
                    code_to_glyph.setdefault(code, glyph)

    absorb(GARDINER_UNICODE_TSV, 0, 3)
    absorb(GARDINER_APP_CSV, 0, 1, skip_header=False)

    glyphs_json = PHARALEX / "public" / "data" / "glyphs.json"
    if glyphs_json.exists():
        for glyph in json.loads(glyphs_json.read_text(encoding="utf-8")):
            unicode_glyph = glyph.get("unicode") or ""
            if glyph.get("code") and unicode_glyph:
                code_to_glyph.setdefault(glyph["code"], unicode_glyph)
    else:
        warn(f"missing {glyphs_json}")
    return code_to_glyph


def load_mdc_aliases():
    if not MDC_ALIAS_JS.exists():
        warn(f"missing {MDC_ALIAS_JS}")
        return {}
    text = MDC_ALIAS_JS.read_text(encoding="utf-8", errors="replace")
    aliases = {alias: code for alias, code in re.findall(r"^\s*([A-Za-z0-9_]+)\s*:\s*'([^']+)'", text, re.M)}
    if MDC_ALIAS_TXT.exists():
        for line in MDC_ALIAS_TXT.read_text(encoding="utf-8", errors="replace").splitlines():
            code, _, alias = line.strip().partition(",")
            if code and alias:
                aliases.setdefault(alias, code)
    else:
        warn(f"missing {MDC_ALIAS_TXT}")
    return aliases


class GlyphResolver:
    """Turns Gardiner codes and Manuel-de-Codage strings into Unicode glyph strings."""

    def __init__(self):
        self.code_to_glyph = load_gardiner_to_glyph()
        self.mdc_aliases = load_mdc_aliases()

    def code(self, token):
        glyph = self.code_to_glyph.get(token)
        if glyph:
            return glyph
        canonical = self.mdc_aliases.get(token)
        return self.code_to_glyph.get(canonical) if canonical else None

    def codes(self, tokens):
        glyphs = [self.code(t) for t in tokens]
        return "".join(g for g in glyphs if g) if any(glyphs) else ""

    def mdc(self, string):
        string = re.sub(r"</?hiero>", "", string)
        tokens = [t for t in MDC_TOKEN_SPLIT.split(string) if t]
        return self.codes(tokens)


# ------------------------------------------------------------------ gloss text

def normalize_gloss(text):
    text = PARENTHETICAL.sub(" ", text.lower())
    text = text.replace("’", "'")
    return " ".join(WORD_RE.findall(text))


def gloss_variants(text):
    variants = set()
    for chunk in GLOSS_SPLIT.split(text.lower()):
        normalized = normalize_gloss(chunk)
        if normalized:
            variants.add(normalized)
    whole = normalize_gloss(text)
    if whole:
        variants.add(whole)
    return variants


def unknown_words(words, vocabulary):
    """Words absent from the vocabulary even after stripping common English inflections."""
    missing = set()
    for word in words:
        stems = {word} | {word[: -len(suffix)] for suffix in INFLECTION_SUFFIXES if word.endswith(suffix)}
        stems |= {stem + "e" for stem in list(stems)} | {stem + "y" for stem in list(stems)}
        if not (stems & vocabulary):
            missing.add(word)
    return sorted(missing)


def content_words(text):
    return {w for w in WORD_RE.findall(text.lower()) if w not in STOPWORDS and len(w) > 1}


# ------------------------------------------------------------ source adapters
# Every adapter yields (glyph_string, gloss_text). Failures are warnings, never fatal.

def adapter_pharalex_words(resolver):
    path = PHARALEX / "public" / "data" / "words.json"
    for word in json.loads(path.read_text(encoding="utf-8")):
        glyphs = resolver.codes(word.get("gardinerCodes") or []) or resolver.mdc(word.get("mdc") or "")
        if glyphs:
            yield glyphs, word.get("translation") or ""


def adapter_pharalex_relations(resolver):
    path = PHARALEX / "public" / "data" / "word-relations.json"
    for entries in json.loads(path.read_text(encoding="utf-8")).values():
        for entry in entries:
            glyphs = resolver.codes(entry.get("gardinerCodes") or []) or resolver.mdc(entry.get("mdc") or "")
            if glyphs:
                yield glyphs, entry.get("translation") or ""


def adapter_pharalex_glyph_meanings(resolver):
    path = PHARALEX / "public" / "data" / "glyphs.json"
    for glyph in json.loads(path.read_text(encoding="utf-8")):
        unicode_glyph = glyph.get("unicode")
        if not unicode_glyph:
            continue
        yield unicode_glyph, " ; ".join(m.get("text") or "" for m in glyph.get("meanings") or [])


def _kaikki_lines(path, resolver):
    with path.open(encoding="utf-8") as handle:
        for line in handle:
            entry = json.loads(line)
            glosses = " ; ".join(
                gloss for sense in entry.get("senses") or [] for gloss in sense.get("glosses") or []
            )
            for template in entry.get("head_templates") or []:
                head = (template.get("args") or {}).get("head")
                if head:
                    glyphs = resolver.mdc(head)
                    if glyphs:
                        yield glyphs, glosses


def adapter_kaikki(resolver):
    yield from _kaikki_lines(SOURCES / "kaikki-egyptian.jsonl", resolver)


def adapter_pharalex_wiktionary(resolver):
    yield from _kaikki_lines(PHARALEX / "lib" / "data" / "wiktionary-egyptian.jsonl", resolver)


def adapter_zh_dictionary(resolver):
    path = SOURCES / "repos" / "AncientEgyptianHieroglyph-ZH" / "dictionary.json"
    for entry in json.loads(path.read_text(encoding="utf-8")).get("entries", []):
        glyphs = "".join(c for c in entry.get("egyptian", "") if is_hieroglyph(c))
        if glyphs:
            yield glyphs, entry.get("id", "").replace("_", " ")


def _tla_corpus(path):
    with path.open(encoding="utf-8") as handle:
        for line in handle:
            hieroglyphs = json.loads(line).get("hieroglyphs") or ""
            for run in hieroglyphs.split():
                glyphs = "".join(c for c in run if is_hieroglyph(c))
                if glyphs:
                    yield glyphs, ""


def adapter_tla_earlier(resolver):
    yield from _tla_corpus(PHARALEX / "lib" / "data" / "tla-earlier-egyptian.jsonl")


def adapter_tla_late(resolver):
    yield from _tla_corpus(PHARALEX / "lib" / "data" / "tla-late-egyptian.jsonl")


def adapter_unikemet(resolver):
    path = PHARALEX / "lib" / "data" / "unikemet.txt"
    with path.open(encoding="utf-8", errors="replace") as handle:
        for line in handle:
            if line.startswith("#"):
                continue
            cells = line.rstrip("\n").split("\t")
            if len(cells) < 3 or not cells[0].startswith("U+"):
                continue
            try:
                char = chr(int(cells[0][2:], 16))
            except ValueError:
                continue
            if is_hieroglyph(char):
                yield char, ""


# Sources whose glosses are real lexical translations; the rest only attest that a
# glyph sequence exists (corpus runs, sign catalogues) and must not veto a meaning.
LEXICAL_SOURCES = {
    "pharalex_words(TLA+Vygus)", "pharalex_word_relations", "pharalex_sign_meanings",
    "kaikki_wiktionary", "pharalex_wiktionary", "chinese_hieroglyph_dict",
    "middle_egyptian_lexicon", "wikidata_lexemes", "aed_tla_lemma_glosses",
}

MIN_GLYPHS_FOR_CONTRADICTION = 2

def adapter_middle_egyptian_lexicon(resolver):
    path = SOURCES / "middle_egyptian_lexicon.csv"
    with path.open(encoding="utf-8", errors="replace") as handle:
        for line in handle:
            cells = line.rstrip("\n").split(";")
            if len(cells) < 3:
                continue
            glyphs = resolver.codes([c for c in cells[0].split(",") if c])
            if glyphs:
                yield glyphs, cells[2]


def adapter_wikidata_lexemes(resolver):
    path = SOURCES / "wikidata_egyptian_lexemes.tsv"
    with path.open(encoding="utf-8", errors="replace") as handle:
        next(handle, None)
        for line in handle:
            cells = line.rstrip("\n").split("\t")
            if len(cells) < 4:
                continue
            glyphs = "".join(c for c in cells[1] if is_hieroglyph(c))
            if glyphs:
                yield glyphs, cells[3].strip('"').split('"@')[0]


def adapter_aed_lemma_glosses(resolver):
    """No hieroglyphs, so this only widens the vocabulary of attested English glosses."""
    path = SOURCES / "aed_lemmas.tsv"
    with path.open(encoding="utf-8", errors="replace") as handle:
        next(handle, None)
        for line in handle:
            cells = line.rstrip("\n").split("\t")
            if len(cells) > 4 and cells[4]:
                yield "", cells[4]


ADAPTERS = {
    "pharalex_words(TLA+Vygus)": adapter_pharalex_words,
    "pharalex_word_relations": adapter_pharalex_relations,
    "pharalex_sign_meanings": adapter_pharalex_glyph_meanings,
    "kaikki_wiktionary": adapter_kaikki,
    "pharalex_wiktionary": adapter_pharalex_wiktionary,
    "chinese_hieroglyph_dict": adapter_zh_dictionary,
    "tla_corpus_earlier": adapter_tla_earlier,
    "tla_corpus_late": adapter_tla_late,
    "unikemet_sign_descriptions": adapter_unikemet,
    "middle_egyptian_lexicon": adapter_middle_egyptian_lexicon,
    "wikidata_lexemes": adapter_wikidata_lexemes,
    "aed_tla_lemma_glosses": adapter_aed_lemma_glosses,
}


class ReferenceIndex:
    def __init__(self, resolver):
        self.resolver = resolver
        self.sequence_glosses = defaultdict(set)
        self.lexical_glosses = defaultdict(set)
        self.sequence_sources = defaultdict(set)
        self.gloss_phrases = set()
        self.vocabulary = set()
        self.glyph_to_sequences = defaultdict(set)
        self.loaded_sources = {}

    def build(self):
        for name, adapter in ADAPTERS.items():
            count = 0
            try:
                for glyphs, gloss in adapter(self.resolver):
                    if glyphs:
                        self.sequence_sources[glyphs].add(name)
                    for variant in gloss_variants(gloss):
                        if variant not in GRAMMAR_ONLY_GLOSSES:
                            self.sequence_glosses[glyphs].add(variant)
                            self.gloss_phrases.add(variant)
                            if name in LEXICAL_SOURCES:
                                self.lexical_glosses[glyphs].add(variant)
                    self.vocabulary |= content_words(gloss)
                    count += 1
            except FileNotFoundError as error:
                warn(f"source {name} unavailable: {error}")
                continue
            except Exception as error:
                warn(f"source {name} failed to parse: {error}")
                continue
            self.loaded_sources[name] = count
        for sequence in self.sequence_sources:
            for glyph in set(sequence):
                self.glyph_to_sequences[glyph].add(sequence)
        if SYSTEM_WORDLIST.exists():
            self.vocabulary |= {
                w.strip().lower() for w in SYSTEM_WORDLIST.read_text(errors="replace").splitlines()
            }
        else:
            warn(f"missing {SYSTEM_WORDLIST}; unknown-word detection will be noisier")

    def near_miss(self, sequence):
        rarest = min(sequence, key=lambda g: len(self.glyph_to_sequences.get(g, ())) or 10**9, default=None)
        candidates = sorted(self.glyph_to_sequences.get(rarest, ()), key=len)[:NEAR_MISS_CANDIDATE_CAP]
        best, best_ratio = None, NEAR_MISS_MIN_RATIO
        for candidate in candidates:
            ratio = SequenceMatcher(None, sequence, candidate).ratio()
            if ratio > best_ratio:
                best, best_ratio = candidate, ratio
        if not best:
            return ""
        glosses = sorted(self.sequence_glosses.get(best, ()))[:3]
        return f"{best} = {'; '.join(glosses) or '(no gloss)'} ({best_ratio:.2f})"


# ------------------------------------------------------------ user dictionary

class Entry:
    __slots__ = ("line_no", "raw", "glyphs", "invalid_chars", "phon", "meaning", "note")

    def __init__(self, line_no, raw, glyphs, invalid_chars, phon, meaning, note):
        self.line_no = line_no
        self.raw = raw
        self.glyphs = glyphs
        self.invalid_chars = invalid_chars
        self.phon = phon
        self.meaning = meaning
        self.note = note


def parse_user_dictionary(path):
    entries, skipped = [], Counter()
    for line_no, raw in enumerate(path.read_text(encoding="utf-8").splitlines(), start=1):
        line = raw.strip()
        if not line:
            skipped["blank"] += 1
            continue
        if "|" not in line:
            skipped["no pipe (prose/comment line)"] += 1
            continue
        cells = [c.strip() for c in line.split("|")]
        if len(cells) < 2:
            skipped["fewer than two columns"] += 1
            continue
        if set(cells[0]) <= set("- "):
            skipped["table separator/header"] += 1
            continue
        glyph_cell = cells[0]
        if not any(is_hieroglyph(c) or is_private_use(c) for c in glyph_cell):
            skipped["first column holds no glyphs"] += 1
            continue
        # Header column order is glyph | phon | meaning | note; the phon column is
        # omitted in the common three-column form.
        phon = cells[1] if len(cells) >= 4 else ""
        meaning = cells[2] if len(cells) >= 4 else cells[1]
        note = " | ".join(cells[3:]) if len(cells) >= 4 else (cells[2] if len(cells) == 3 else "")
        glyphs = "".join(c for c in glyph_cell if is_hieroglyph(c))
        invalid = [c for c in glyph_cell if not is_hieroglyph(c) and not c.isspace()]
        entries.append(Entry(line_no, line, glyphs, invalid, phon, meaning, note))
    return entries, skipped


def looks_non_english(text, vocabulary):
    if not text:
        return True
    if any(marker in text for marker in NOTE_LIKE_MARKERS):
        return True
    letters = [c for c in text if c.isalpha()]
    if not letters:
        return True
    if sum(c.isascii() for c in letters) / len(letters) < 0.6:
        return True
    words = content_words(text)
    return bool(words) and not (words & vocabulary)


def classify(entries, index):
    findings = defaultdict(list)
    by_sequence = defaultdict(list)
    seen_rows = {}
    corroborated_by_source = Counter()
    corroborated_exact = 0

    for entry in entries:
        key = (entry.glyphs, normalize_gloss(entry.meaning), normalize_gloss(entry.note))
        if key in seen_rows and entry.glyphs:
            findings["duplicate_row"].append((entry, f"identical to line {seen_rows[key]}", ""))
        else:
            seen_rows[key] = entry.line_no
        if entry.glyphs:
            by_sequence[entry.glyphs].append(entry)

    for entry in entries:
        variants = gloss_variants(entry.meaning)
        words = content_words(entry.meaning)
        sources = index.sequence_sources.get(entry.glyphs, set())
        reference_glosses = index.lexical_glosses.get(entry.glyphs, set())
        reference_words = set()
        for gloss in reference_glosses:
            reference_words |= content_words(gloss)
        gloss_matches = bool(words & reference_words) or bool(variants & reference_glosses)
        missing_words = unknown_words(words, index.vocabulary)
        phrase_attested = bool(variants & index.gloss_phrases)

        if sources:
            corroborated_exact += 1
            for source in sources:
                corroborated_by_source[source] += 1

        if not entry.glyphs:
            findings["empty_or_malformed"].append((entry, "glyph column contains no valid hieroglyph", ""))
            continue
        if not entry.meaning.strip():
            findings["empty_or_malformed"].append((entry, "empty meaning column", ""))
            continue

        if entry.invalid_chars:
            kind = "private-use codepoints (custom font)" if all(
                is_private_use(c) for c in entry.invalid_chars
            ) else "non-hieroglyph codepoints"
            findings["invalid_glyphs"].append(
                (entry, f"{kind}: {' '.join(f'U+{ord(c):04X} {c}' for c in entry.invalid_chars[:6])}", "")
            )

        if not sources and not phrase_attested and missing_words:
            findings["unattested_glyphs_and_gloss"].append(
                (entry,
                 f"glyph sequence in no reference dictionary; gloss words unattested: {', '.join(missing_words[:5])}",
                 index.near_miss(entry.glyphs)))
        elif not sources:
            findings["unattested_glyph_sequence"].append(
                (entry, "glyph sequence appears in no reference dictionary", index.near_miss(entry.glyphs)))
        elif not gloss_matches and reference_glosses and len(entry.glyphs) >= MIN_GLYPHS_FOR_CONTRADICTION:
            findings["gloss_contradicts_sources"].append(
                (entry,
                 f"sequence attested but references gloss it as: {'; '.join(sorted(reference_glosses)[:3]) or '(no gloss)'}",
                 ""))

        if missing_words and not phrase_attested:
            findings["gloss_unattested_anywhere"].append(
                (entry, f"gloss words found in no reference gloss nor English wordlist: {', '.join(missing_words[:5])}", ""))

        if looks_non_english(entry.meaning, index.vocabulary):
            findings["meaning_column_not_english"].append(
                (entry, "meaning column looks like a note, not an English gloss", ""))

        note_speculative = any(marker in entry.note.lower() for marker in SPECULATION_MARKERS)
        if note_speculative and not sources:
            findings["speculative_note_unsupported"].append(
                (entry, "note is explicitly tentative and no source attests the reading", index.near_miss(entry.glyphs)))

    for sequence, group in by_sequence.items():
        if len(group) < CONTRADICTION_MIN_ENTRIES:
            continue
        attested = set()
        for gloss in index.lexical_glosses.get(sequence, ()):
            attested |= content_words(gloss)
        word_sets = [content_words(e.meaning) for e in group]
        if all(words & attested for words in word_sets):
            continue
        if all(not (a & b) for i, a in enumerate(word_sets) for b in word_sets[i + 1:]):
            others = "; ".join(f"L{e.line_no}:{e.meaning[:40]}" for e in group[1:4])
            findings["contradictory_duplicates"].append(
                (group[0], f"same glyphs glossed with unrelated meanings -> {others}", ""))

    return findings, corroborated_exact, corroborated_by_source


CATEGORY_ORDER = [
    ("unattested_glyphs_and_gloss", "1. Glyph sequence unknown AND gloss words unattested anywhere"),
    ("invalid_glyphs", "3. Invalid / non-hieroglyph codepoints in the glyph column"),
    ("gloss_contradicts_sources", "1b. Glyph sequence IS attested but references gloss it as something unrelated"),
    ("contradictory_duplicates", "4a. Same glyph sequence, mutually unrelated meanings"),
    ("duplicate_row", "4b. Exact duplicate rows"),
    ("empty_or_malformed", "4c. Empty glyph or empty meaning"),
    ("meaning_column_not_english", "4d. Meaning column does not look like an English gloss"),
    ("gloss_unattested_anywhere", "2. Gloss found in no reference dictionary at all"),
    ("unattested_glyph_sequence", "1c. Glyph sequence in no reference dictionary (gloss itself is normal English)"),
    ("speculative_note_unsupported", "5. Tentative note with no external support"),
]


CATEGORY_NOTES = {
    "invalid_glyphs": "Private-use codepoints most likely come from the project's own hieratic font "
                      "and are only listed so the sequence can be checked, not as errors.",
    "unattested_glyph_sequence": "Weakest signal: the reference dictionaries are far from complete, "
                                 "so absence here mostly means 'not in Vygus/TLA/Wiktionary'.",
    "speculative_note_unsupported": "Listed for convenience only; a tentative note is not itself wrong.",
    "gloss_contradicts_sources": "Semantic overlap is judged by shared content words, so paraphrases "
                                 "('remember' vs 'memory') can appear here.",
}


def truncate(text, limit=NOTE_TRUNCATE):
    text = text.replace("|", "/").replace("\t", " ").strip()
    return text if len(text) <= limit else text[: limit - 1] + "…"


def write_reports(findings, entries, skipped, index, corroborated_exact, corroborated_by_source):
    with REPORT_TSV.open("w", encoding="utf-8") as tsv:
        tsv.write("category\tline\tglyphs\tmeaning\tnote\treason\tnear_miss\n")
        for key, title in CATEGORY_ORDER:
            for entry, reason, near_miss in findings.get(key, []):
                tsv.write("\t".join((key, str(entry.line_no), entry.glyphs, truncate(entry.meaning, 200),
                                     truncate(entry.note, 200), truncate(reason, 300), near_miss)) + "\n")

    flagged_lines = {e.line_no for rows in findings.values() for e, _, _ in rows}
    lines = [
        "# Suspect entries in my_egyptian_dictionary.csv",
        "",
        "Automatically generated by `scripts/egy_find_suspect.py`. Speculation in the *note*",
        "column is never a reason to flag; only the glyphs↔meaning pairing is judged.",
        "",
        "## Totals",
        "",
        f"- rows parsed as entries: **{len(entries)}**",
        f"- distinct lines flagged at least once: **{len(flagged_lines)}**",
        f"- entries whose exact glyph sequence is corroborated by ≥1 source: **{corroborated_exact}**"
        f" ({corroborated_exact * 100 // max(len(entries), 1)}%)",
        "",
        "### Skipped input lines",
        "",
    ]
    lines += [f"- {reason}: {count}" for reason, count in skipped.most_common()]
    lines += ["", "### Reference sources loaded", ""]
    lines += [f"- `{name}`: {count} records ingested" for name, count in index.loaded_sources.items()]
    lines += [f"- distinct glyph sequences indexed: {len(index.sequence_sources)}",
              f"- distinct reference gloss phrases: {len(index.gloss_phrases)}", ""]
    lines += ["### Corroboration per source", ""]
    lines += [f"- `{name}`: corroborates {count} user entries" for name, count in corroborated_by_source.most_common()]
    lines += ["", "### Suspect counts per category", ""]
    lines += [f"- {title}: **{len(findings.get(key, []))}**" for key, title in CATEGORY_ORDER]
    lines += ["", f"Full machine-readable list: `{REPORT_TSV.relative_to(PROJECT)}`", ""]

    for key, title in CATEGORY_ORDER:
        rows = findings.get(key, [])
        if not rows:
            continue
        lines += ["", f"## {title}", "", f"{len(rows)} hits" +
                  (f", showing worst {MAX_ROWS_PER_SECTION}" if len(rows) > MAX_ROWS_PER_SECTION else ""), "",
                  *( [CATEGORY_NOTES[key], ""] if key in CATEGORY_NOTES else [] ),
                  "| line | glyphs | meaning | note | reason | nearest reference match |",
                  "| ---- | ------ | ------- | ---- | ------ | ----------------------- |"]
        for entry, reason, near_miss in rows[:MAX_ROWS_PER_SECTION]:
            lines.append("| {} | {} | {} | {} | {} | {} |".format(
                entry.line_no, entry.glyphs, truncate(entry.meaning, 60),
                truncate(entry.note), truncate(reason, 120), truncate(near_miss, 70)))
    REPORT_MD.write_text("\n".join(lines) + "\n", encoding="utf-8")


def main():
    resolver = GlyphResolver()
    print(f"gardiner codes mapped: {len(resolver.code_to_glyph)}, mdc aliases: {len(resolver.mdc_aliases)}")
    index = ReferenceIndex(resolver)
    index.build()
    print(f"indexed {len(index.sequence_sources)} glyph sequences from {len(index.loaded_sources)} sources")
    entries, skipped = parse_user_dictionary(USER_DICT)
    print(f"parsed {len(entries)} entries, skipped {sum(skipped.values())} lines")
    findings, corroborated_exact, corroborated_by_source = classify(entries, index)
    write_reports(findings, entries, skipped, index, corroborated_exact, corroborated_by_source)
    for key, title in CATEGORY_ORDER:
        print(f"{len(findings.get(key, [])):6d}  {title}")
    print(f"wrote {REPORT_MD} and {REPORT_TSV}")


if __name__ == "__main__":
    main()
