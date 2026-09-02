"""Shared loading/normalisation helpers for the Egyptian dictionary comparison scripts."""
import json
import os
import re
import unicodedata

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MY_DICT = os.path.join(PROJECT_ROOT, 'my_egyptian_dictionary.csv')
GARDINER_CSV = os.path.join(PROJECT_ROOT, 'app', 'gardiner.csv')
GARDINER_UNICODE_TSV = os.path.join(PROJECT_ROOT, 'probes', 'egy_dict_sources', 'gardiner_unicode.tsv')
MDC_ALIAS_JS = os.path.join(PROJECT_ROOT, 'scripts', 'gardiner_map_manuel_de_codage.js')
SOURCE_DIR = os.path.join(PROJECT_ROOT, 'probes', 'egy_dict_sources')

HIEROGLYPH_RANGE = (0x13000, 0x1342F)
FORMAT_CONTROL_RANGE = (0x13430, 0x1345F)

# Manuel de Codage phonetic alphabet -> Gardiner code (standard uniliterals).
MDC_UNILITERALS = {
    'A': 'G1', 'i': 'M17', 'j': 'M17', 'y': 'Z4', 'ii': 'M17M17', 'a': 'D36',
    'w': 'G43', 'W': 'G43', 'b': 'D58', 'p': 'Q3', 'f': 'I9', 'm': 'G17',
    'n': 'N35', 'r': 'D21', 'l': 'E23', 'h': 'O4', 'H': 'V28', 'x': 'Aa1',
    'X': 'F32', 's': 'S29', 'z': 'O34', 'S': 'N37', 'q': 'N29', 'K': 'N29',
    'k': 'V31', 'g': 'W11', 't': 'X1', 'T': 'V13', 'd': 'D46', 'D': 'I10',
}

# Transliteration letters -> the phonetic value used in Manuel de Codage.
TRANSLITERATION_TO_MDC = {
    'ꜣ': 'A', 'ʾ': 'A', 'ỉ': 'i', 'j': 'i', 'ï': 'i', 'ꜥ': 'a', 'ˁ': 'a',
    'ḥ': 'H', 'ḫ': 'x', 'ẖ': 'X', 'š': 'S', 'ḳ': 'q', 'ṯ': 'T', 'ḏ': 'D',
    'ṱ': 'T', 'ś': 's', 'ṣ': 's', 'ç': 'S', 'ꞽ': 'i', 'ı': 'i', 'ᵢ': 'i',
}
TRANSLITERATION_STRIP = '=?[]()·,~<>|\u0323\u0331'
TRANSLITERATION_SEPARATORS = '.-  '

GARDINER_CODE = re.compile(r'^(Aa|NL|NU|[A-Z])(\d+)([A-Za-z]*)$')
MDC_SEPARATORS = re.compile(r'[-:*()\s!+,]+')
TRAILING_STAR_NUMBER = re.compile(r'\\\d+$')

GLOSS_NOISE = re.compile(r'\([^)]*\)|\[[^\]]*\]')
GLOSS_STOPWORDS = {'a', 'an', 'the', 'to', 'of', 'be', 'is', 'or', 'and', 'in', 'on'}


def is_hieroglyph(character):
    return HIEROGLYPH_RANGE[0] <= ord(character) <= HIEROGLYPH_RANGE[1]


def is_hieroglyph_or_control(character):
    return is_hieroglyph(character) or FORMAT_CONTROL_RANGE[0] <= ord(character) <= FORMAT_CONTROL_RANGE[1]


def gardiner_to_glyph():
    """Complete Gardiner code -> Unicode glyph map, from the Unicode block plus the project table."""
    mapping = {}
    for codepoint in range(HIEROGLYPH_RANGE[0], HIEROGLYPH_RANGE[1] + 1):
        glyph = chr(codepoint)
        try:
            name = unicodedata.name(glyph)
        except ValueError:
            continue
        code = name.rsplit(' ', 1)[-1]
        mapping[normalise_gardiner(code)] = glyph
    if os.path.exists(GARDINER_UNICODE_TSV):
        with open(GARDINER_UNICODE_TSV, encoding='utf-8') as handle:
            handle.readline()
            for line in handle:
                fields = line.rstrip('\n').split('\t')
                if len(fields) > 5 and fields[0].strip() and fields[5].strip():
                    mapping.setdefault(normalise_gardiner(fields[0].strip()), fields[5].strip())
    if os.path.exists(GARDINER_CSV):
        for line in open(GARDINER_CSV, encoding='utf-8'):
            fields = line.rstrip('\n').split('\t')
            if len(fields) < 2 or not fields[1].strip():
                continue
            mapping.setdefault(normalise_gardiner(fields[0].strip()), fields[1].strip())
    return mapping


def normalise_gardiner(code):
    """A001 / A1 / a1a -> A1 / A1A so both the Unicode and Gardiner spellings collide."""
    match = GARDINER_CODE.match(code.strip())
    if not match:
        return code.strip().upper()
    family, number, variant = match.groups()
    family = 'Aa' if family.upper() == 'AA' else family.upper()
    return f'{family}{int(number)}{variant.upper()}'


def mdc_aliases():
    """Phonetic alias -> Gardiner code, from the standard uniliterals plus the project's alias file."""
    aliases = dict(MDC_UNILITERALS)
    if os.path.exists(MDC_ALIAS_JS):
        for alias, code in re.findall(r"^\s*(\w+)\s*:\s*'([^']+)'", open(MDC_ALIAS_JS, encoding='utf-8').read(), re.M):
            aliases.setdefault(alias, code)
    return aliases


class Transcriber:
    """Turns Manuel de Codage / Gardiner strings and bare transliterations into Unicode hieroglyphs."""

    def __init__(self):
        self.glyphs = gardiner_to_glyph()
        self.aliases = {alias: code for alias, code in mdc_aliases().items()
                        if all(normalise_gardiner(part) in self.glyphs for part in self.split_codes(code))}

    @staticmethod
    def split_codes(code):
        return re.findall(r'(?:Aa|NL|NU|[A-Z])\d+[A-Za-z]*', code) or [code]

    def token_to_glyphs(self, token):
        token = TRAILING_STAR_NUMBER.sub('', token).strip()
        if not token:
            return '', True
        if all(is_hieroglyph_or_control(c) for c in token):
            return token, True
        code = normalise_gardiner(token)
        if code in self.glyphs:
            return self.glyphs[code], True
        if token in self.aliases:
            return ''.join(self.glyphs[normalise_gardiner(part)] for part in self.split_codes(self.aliases[token])), True
        for length in (4, 3, 2):
            if len(token) > length and token[:length] in self.aliases:
                head, head_ok = self.token_to_glyphs(token[:length])
                tail, tail_ok = self.token_to_glyphs(token[length:])
                if head_ok and tail_ok:
                    return head + tail, True
        letters = [MDC_UNILITERALS.get(c) for c in token]
        if all(letters):
            return ''.join(self.glyphs[normalise_gardiner(c)] for c in letters), True
        return '', False

    def transcribe(self, text):
        """Returns (glyph_string, fully_resolved)."""
        if not text:
            return '', False
        pieces, resolved = [], True
        for token in MDC_SEPARATORS.split(text):
            if not token:
                continue
            glyphs, ok = self.token_to_glyphs(token)
            pieces.append(glyphs)
            resolved = resolved and ok
        return ''.join(pieces), resolved and bool(pieces)

    def transcribe_transliteration(self, word):
        """Fallback: spell a transliterated word out with uniliteral signs."""
        letters = []
        for character in unicodedata.normalize('NFC', word):
            if unicodedata.category(character) == 'Mn' or character in TRANSLITERATION_STRIP:
                continue
            if character in TRANSLITERATION_SEPARATORS:
                letters.append('-')
                continue
            mapped = TRANSLITERATION_TO_MDC.get(character) or TRANSLITERATION_TO_MDC.get(character.lower())
            if not mapped:
                mapped = character if character in MDC_UNILITERALS else character.lower()
            letters.append(mapped)
        return self.transcribe(''.join(letters))


def glyph_key(text):
    """Comparison key for a hieroglyph sequence: bare hieroglyphs, order preserved."""
    return ''.join(c for c in text if is_hieroglyph(c))


def gloss_keys(meaning):
    """Normalised comparison keys for an English gloss, one per comma/slash-separated alternative."""
    cleaned = GLOSS_NOISE.sub(' ', meaning.lower())
    keys = set()
    for part in re.split(r'[,;/]| - ', cleaned):
        words = [w for w in re.findall(r"[a-z']+", part) if w not in GLOSS_STOPWORDS]
        if words:
            keys.add(' '.join(words))
    return keys


class Entry:
    __slots__ = ('glyphs', 'meaning', 'note', 'line_number', 'raw')

    def __init__(self, glyphs, meaning, note, line_number, raw):
        self.glyphs, self.meaning, self.note = glyphs, meaning, note
        self.line_number, self.raw = line_number, raw

    def __repr__(self):
        return f'Entry({self.glyphs!r}, {self.meaning!r})'


def load_my_dictionary(path=MY_DICT):
    """Parses the pipe table; returns (entries, skipped_line_count)."""
    entries, skipped = [], 0
    for line_number, line in enumerate(open(path, encoding='utf-8'), 1):
        stripped = line.strip()
        if not stripped:
            continue
        if '|' not in stripped:
            skipped += 1
            continue
        fields = [f.strip() for f in stripped.strip('|').split('|')]
        if len(fields) < 2 or not glyph_key(fields[0]):
            skipped += 1
            continue
        entries.append(Entry(fields[0], fields[1], ' | '.join(fields[2:]), line_number, stripped))
    return entries, skipped


def load_kaikki(path=None):
    """Wiktionary/kaikki Egyptian entries -> dicts with transliteration, mdc, glosses, pos."""
    path = path or os.path.join(SOURCE_DIR, 'kaikki-egyptian.jsonl')
    if not os.path.exists(path):
        return []
    merged = {}
    for line in open(path, encoding='utf-8'):
        raw = json.loads(line)
        if raw.get('pos') in ('romanization', 'character'):
            continue
        glosses = []
        for sense in raw.get('senses', []):
            for gloss in sense.get('glosses', []) or []:
                if gloss and not gloss.lower().startswith(('the meaning of this term', 'alternative', 'manuel de codage')):
                    glosses.append(gloss)
        mdc = ''
        for template in raw.get('head_templates', []):
            head = (template.get('args') or {}).get('head', '')
            head = re.sub(r'</?hiero>', '', head).strip()
            if head and len(head) > len(mdc):
                mdc = head
        if not glosses:
            continue
        record = merged.setdefault((raw['word'], raw.get('pos', ''), mdc), {
            'source': 'wiktionary',
            'translit': raw['word'],
            'mdc': mdc,
            'pos': raw.get('pos', ''),
            'glosses': [],
        })
        for gloss in glosses:
            if gloss not in record['glosses']:
                record['glosses'].append(gloss)
    return list(merged.values())


def load_aed(path=None):
    """Berlin AED / TLA lemma list: transliteration + English gloss, no hieroglyphic writing."""
    path = path or os.path.join(SOURCE_DIR, 'aed_lemmas.tsv')
    if not os.path.exists(path):
        return []
    records = []
    with open(path, encoding='utf-8') as handle:
        header = handle.readline().rstrip('\n').split('\t')
        column = {name: i for i, name in enumerate(header)}
        for line in handle:
            fields = line.rstrip('\n').split('\t')
            if len(fields) < len(header):
                continue
            english = fields[column['en']].strip()
            lemma = fields[column['lemma']].strip()
            if not english or not lemma:
                continue
            records.append({
                'source': 'aed',
                'translit': lemma,
                'mdc': fields[column['hiero']].strip() if 'hiero' in column else '',
                'pos': fields[column['pos']].strip().split(':')[0],
                'glosses': [g.strip() for g in english.split(';') if g.strip()],
            })
    return records


def load_wikidata(path=None):
    """Wikidata Egyptian lexemes: one row per representation, hieroglyphic and transliterated."""
    path = path or os.path.join(SOURCE_DIR, 'wikidata_egyptian_lexemes.tsv')
    if not os.path.exists(path):
        return []
    merged = {}
    with open(path, encoding='utf-8') as handle:
        handle.readline()
        for line in handle:
            fields = line.rstrip('\n').split('\t')
            if len(fields) < 4:
                continue
            lexeme, lemma, pos, gloss = (f.strip() for f in fields[:4])
            lemma = lemma.strip('"').split('"@')[0].strip('"')
            gloss = gloss.strip('"').split('"@')[0].strip('"')
            pos = pos.strip('"').split('"@')[0].strip('"')
            if not gloss:
                continue
            record = merged.setdefault(lexeme, {'source': 'wikidata', 'translit': '', 'mdc': '',
                                                'pos': pos, 'glosses': []})
            if glyph_key(lemma):
                record['mdc'] = lemma
            elif lemma and not record['translit']:
                record['translit'] = lemma
            if gloss not in record['glosses']:
                record['glosses'].append(gloss)
    return [r for r in merged.values() if r['mdc'] or r['translit']]


def load_zh_lexicon(path=None):
    """AncientEgyptianHieroglyph-ZH: Unicode writing + transliteration, glosses in Chinese only."""
    path = path or os.path.join(SOURCE_DIR, 'repos', 'AncientEgyptianHieroglyph-ZH', 'dictionary.json')
    if not os.path.exists(path):
        return []
    data = json.load(open(path, encoding='utf-8'))
    records = []
    for entry in data.get('entries', []):
        writing = entry.get('egyptian', '')
        if entry.get('pos') == 'sign' or not glyph_key(writing):
            continue
        records.append({
            'source': 'zh-lexicon',
            'translit': entry.get('transliteration', ''),
            'mdc': writing,
            'pos': entry.get('pos', ''),
            'glosses': [g for g in [entry.get('zh_Hans', '')] if g],
        })
    return records


def load_pharalex(path=None):
    """Vygus + TLA words from the pharalex project: Gardiner code sequence plus English gloss."""
    path = path or os.path.join(SOURCE_DIR, 'pharalex_words.tsv')
    if not os.path.exists(path):
        return []
    merged = {}
    with open(path, encoding='utf-8') as handle:
        header = handle.readline().rstrip('\n').split('\t')
        column = {name: i for i, name in enumerate(header)}
        for line in handle:
            fields = line.rstrip('\n').split('\t')
            if len(fields) < len(header):
                continue
            translation = fields[column['translation']].strip()
            writing = fields[column['gardinerCodes']].strip() or fields[column['mdc']].strip()
            if not translation or not writing:
                continue
            key = (writing, fields[column['transliteration']].strip())
            record = merged.setdefault(key, {
                'source': 'vygus' if fields[column['source']] == 'vygus' else 'tla',
                'translit': fields[column['transliteration']].strip(),
                'mdc': writing,
                'pos': fields[column['grammar']].strip().lower(),
                'glosses': [],
            })
            for gloss in (g.strip() for g in translation.split(',')):
                if gloss and gloss not in record['glosses']:
                    record['glosses'].append(gloss)
    return list(merged.values())


def load_middle_egyptian(path=None):
    """fayrose/MiddleEgyptianDataset lexicon: Gardiner codes; transliteration; translation; frequency."""
    path = path or os.path.join(SOURCE_DIR, 'middle_egyptian_lexicon.csv')
    if not os.path.exists(path):
        return []
    merged = {}
    for line in open(path, encoding='utf-8'):
        fields = line.rstrip('\n').split(';')
        if len(fields) < 3:
            continue
        writing, translit, translation = fields[0].strip(', '), fields[1].strip(), fields[2].strip()
        if not writing or not translation:
            continue
        record = merged.setdefault((writing, translit), {
            'source': 'middle-egyptian', 'translit': translit, 'mdc': writing,
            'pos': '', 'glosses': [],
        })
        for gloss in (g.strip() for g in translation.split(',')):
            if gloss and gloss not in record['glosses']:
                record['glosses'].append(gloss)
    return list(merged.values())


REFERENCE_LOADERS = [('wiktionary', load_kaikki), ('pharalex-vygus-tla', load_pharalex),
                     ('middle-egyptian', load_middle_egyptian), ('aed', load_aed),
                     ('wikidata', load_wikidata), ('zh-lexicon', load_zh_lexicon)]
