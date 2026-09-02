#!/usr/bin/env python3
"""Compares reference Egyptian dictionaries against my_egyptian_dictionary.csv and proposes the
entries that are missing, already transcribed into Unicode (Gardiner) hieroglyphs.

Usage: python3 scripts/egy_find_missing.py [--limit N]
"""
import os
import sys
import unicodedata
from collections import defaultdict

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from egy_dict_common import (PROJECT_ROOT, REFERENCE_LOADERS, Transcriber, glyph_key,
                             gloss_keys, load_my_dictionary)

OUTPUT_MD = os.path.join(PROJECT_ROOT, 'probes', 'egyptian_missing_entries.md')
OUTPUT_TSV = os.path.join(PROJECT_ROOT, 'probes', 'egyptian_missing_entries.tsv')
MAX_GLOSSES_PER_ENTRY = 4
MAX_GLOSS_LENGTH = 160
MIN_PREFIX_OVERLAP = 2
MAX_SIMILAR_SHOWN = 2
SECTIONS = [
    ('missing_attested', 'Missing entries with an attested hieroglyphic writing',
     'Neither the glyph sequence nor the meaning is in my dictionary, and the source supplied the writing.'),
    ('missing_reconstructed', 'Missing entries whose writing had to be reconstructed',
     'The source gave only a transliteration, so the hieroglyphs were spelled out uniliterally. Verify before use.'),
    ('new_writing', 'Alternative writings for meanings already in the dictionary',
     'The meaning is present but under a different glyph sequence.'),
    ('no_english_gloss', 'Unknown glyph sequences whose source gloss is not English',
     'Glyph sequence absent from my dictionary; the gloss could not be compared because it is not in English.'),
]
GLYPH_SPACING = ' '


def load_reference_records():
    """All reference dictionaries, each as {source, translit, mdc, pos, glosses}."""
    records = []
    for name, loader in REFERENCE_LOADERS:
        loaded = loader()
        print(f'  {name}: {len(loaded)} records', file=sys.stderr)
        records.extend(loaded)
    return records


def index_my_dictionary(entries):
    by_glyphs = defaultdict(list)
    by_gloss = defaultdict(list)
    for entry in entries:
        by_glyphs[glyph_key(entry.glyphs)].append(entry)
        for key in gloss_keys(entry.meaning):
            by_gloss[key].append(entry)
    return by_glyphs, by_gloss


def similar_existing(key, by_glyphs):
    """Entries of mine whose glyph sequence starts with the same signs - likely the same word."""
    for length in range(len(key), MIN_PREFIX_OVERLAP - 1, -1):
        prefix = key[:length]
        hits = [entry for existing, entries in by_glyphs.items() if existing.startswith(prefix)
                for entry in entries]
        if hits:
            return ['{} = {} (line {})'.format(spaced(glyph_key(e.glyphs)), e.meaning, e.line_number)
                    for e in hits[:MAX_SIMILAR_SHOWN]]
    return []


def spaced(glyphs):
    return GLYPH_SPACING.join(glyphs)


def has_latin_gloss(record):
    """True when at least one gloss is written in the Latin script only."""
    for gloss in record['glosses']:
        letters = [c for c in gloss if c.isalpha()]
        if letters and all('LATIN' in unicodedata.name(c, '') for c in letters):
            return True
    return False


def classify(records, by_glyphs, by_gloss, transcriber):
    """Buckets reference records by how confidently they are missing from my dictionary."""
    buckets = {name: [] for name, _, _ in SECTIONS}
    covered, untranscribable = [], []
    for record in records:
        glyphs, resolved = transcriber.transcribe(record['mdc'])
        if not glyphs:
            glyphs, resolved = transcriber.transcribe_transliteration(record['translit'])
            record['from_transliteration'] = True
        key = glyph_key(glyphs)
        record['glyphs'] = key
        record['resolved'] = resolved
        if not key:
            untranscribable.append(record)
            continue
        known_glyphs = key in by_glyphs
        matched_glosses = {g for gloss in record['glosses'] for g in gloss_keys(gloss) if g in by_gloss}
        record['gloss_match'] = sorted(matched_glosses)
        record['similar'] = [] if known_glyphs else similar_existing(key, by_glyphs)
        if known_glyphs:
            covered.append(record)
        elif not has_latin_gloss(record):
            buckets['no_english_gloss'].append(record)
        elif matched_glosses:
            buckets['new_writing'].append(record)
        elif record.get('from_transliteration'):
            buckets['missing_reconstructed'].append(record)
        else:
            buckets['missing_attested'].append(record)
    return buckets, covered, untranscribable


def merge_by_writing(records):
    """One proposal per glyph sequence, pooling the glosses and the sources that attest it."""
    merged = {}
    for record in sorted(records, key=lambda r: r['source']):
        pooled = merged.get(record['glyphs'])
        if pooled is None:
            merged[record['glyphs']] = dict(record, sources=[record['source']],
                                            translits=[record['translit']] if record['translit'] else [])
            continue
        for gloss in record['glosses']:
            if gloss not in pooled['glosses']:
                pooled['glosses'].append(gloss)
        if record['source'] not in pooled['sources']:
            pooled['sources'].append(record['source'])
        if record['translit'] and record['translit'] not in pooled['translits']:
            pooled['translits'].append(record['translit'])
        pooled['resolved'] = pooled['resolved'] or record['resolved']
    return sorted(merged.values(), key=lambda r: (-len(r['sources']), r['translits'][:1], r['glyphs']))


def format_row(record):
    glosses = '; '.join(g[:MAX_GLOSS_LENGTH].rstrip() for g in record['glosses'][:MAX_GLOSSES_PER_ENTRY])
    note = '{} ({}) [{}]'.format(', '.join(record.get('translits') or [record['translit']]),
                                 record['pos'], ', '.join(record.get('sources') or [record['source']]))
    if not record.get('resolved'):
        note += ' ~approx'
    if record.get('from_transliteration'):
        note += ' ~spelled-out'
    if record.get('similar'):
        note += ' | mine: ' + ' ; '.join(record['similar'])
    return spaced(record['glyphs']), glosses, note


def write_outputs(buckets, covered, untranscribable, my_count, skipped):
    with open(OUTPUT_TSV, 'w', encoding='utf-8') as tsv:
        tsv.write('status\tglyphs\tmeaning\tnote\tsimilar_in_my_dictionary\n')
        for status, _, _ in SECTIONS:
            for record in merge_by_writing(buckets[status]):
                glyphs, meaning, note = format_row(record)
                tsv.write('\t'.join((status, glyphs, meaning, note.split(' | mine: ')[0],
                                     ' ; '.join(record.get('similar', [])))) + '\n')

    with open(OUTPUT_MD, 'w', encoding='utf-8') as md:
        md.write('# Proposed additions to my_egyptian_dictionary.csv\n\n')
        md.write(f'- my dictionary: **{my_count}** parsed entries ({skipped} unparseable lines skipped)\n')
        total = sum(len(g) for g in buckets.values()) + len(covered) + len(untranscribable)
        md.write(f'- reference entries checked: **{total}**\n')
        md.write(f'- already covered (same glyph sequence present): **{len(covered)}**\n')
        for name, title, _ in SECTIONS:
            md.write(f'- {title.lower()}: **{len(merge_by_writing(buckets[name]))}** distinct writings '
                     f'({len(buckets[name])} source records)\n')
        md.write(f'- could not be transcribed to Unicode: **{len(untranscribable)}**\n\n')
        md.write('Notes marked `~approx` contain at least one Manuel-de-Codage token that could not be '
                 'resolved; `~spelled-out` means the hieroglyphs were reconstructed uniliterally from the '
                 'transliteration because the source gave no writing. Review both before pasting.\n\n')
        for name, title, explanation in SECTIONS:
            group = merge_by_writing(buckets[name])
            md.write(f'\n## {title} ({len(group)} distinct writings)\n\n{explanation}\n'
                     'Rows are ordered by how many independent sources attest the writing.\n\n')
            md.write(' | glyph | meaning | note | \n | ----- | ------- | ---- | \n')
            for record in group:
                md.write(' | {} | {} | {} | \n'.format(*format_row(record)))


def main():
    print('loading my dictionary…', file=sys.stderr)
    entries, skipped = load_my_dictionary()
    by_glyphs, by_gloss = index_my_dictionary(entries)
    print(f'  {len(entries)} entries, {len(by_glyphs)} distinct glyph sequences, {len(by_gloss)} gloss keys',
          file=sys.stderr)
    print('loading reference dictionaries…', file=sys.stderr)
    records = load_reference_records()
    transcriber = Transcriber()
    buckets, covered, untranscribable = classify(records, by_glyphs, by_gloss, transcriber)
    write_outputs(buckets, covered, untranscribable, len(entries), skipped)
    for name, _, _ in SECTIONS:
        print(f'  {name}={len(buckets[name])}', file=sys.stderr)
    print(f'  covered={len(covered)} untranscribable={len(untranscribable)}', file=sys.stderr)
    print(f'wrote {OUTPUT_MD} and {OUTPUT_TSV}', file=sys.stderr)


if __name__ == '__main__':
    main()
