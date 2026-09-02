#!/usr/bin/env python3
"""Compares reference Egyptian dictionaries against my_egyptian_dictionary.csv and proposes the
entries that are missing, already transcribed into Unicode (Gardiner) hieroglyphs.

Usage: python3 scripts/egy_find_missing.py [--limit N]
"""
import os
import sys
from collections import defaultdict

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from egy_dict_common import (PROJECT_ROOT, Transcriber, glyph_key, gloss_keys,
                             load_kaikki, load_my_dictionary)

OUTPUT_MD = os.path.join(PROJECT_ROOT, 'probes', 'egyptian_missing_entries.md')
OUTPUT_TSV = os.path.join(PROJECT_ROOT, 'probes', 'egyptian_missing_entries.tsv')
MAX_GLOSSES_PER_ENTRY = 4
MAX_GLOSS_LENGTH = 160
MIN_PREFIX_OVERLAP = 2
MAX_SIMILAR_SHOWN = 2
GLYPH_SPACING = ' '


def load_reference_records():
    """All reference dictionaries, each as {source, translit, mdc, pos, glosses}."""
    loaders = [('wiktionary', load_kaikki)]
    records = []
    for name, loader in loaders:
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


def classify(records, by_glyphs, by_gloss, transcriber):
    """Splits reference records into fully missing, new-writing-only, and already-covered."""
    missing, new_writing, covered, untranscribable = [], [], [], []
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
        elif matched_glosses:
            new_writing.append(record)
        else:
            missing.append(record)
    return missing, new_writing, covered, untranscribable


def format_row(record):
    glosses = '; '.join(g[:MAX_GLOSS_LENGTH].rstrip() for g in record['glosses'][:MAX_GLOSSES_PER_ENTRY])
    note = f"{record['translit']} ({record['pos']}) [{record['source']}]"
    if not record.get('resolved'):
        note += ' ~approx'
    if record.get('from_transliteration'):
        note += ' ~spelled-out'
    if record.get('similar'):
        note += ' | mine: ' + ' ; '.join(record['similar'])
    return spaced(record['glyphs']), glosses, note


def write_outputs(missing, new_writing, covered, untranscribable, my_count, skipped):
    with open(OUTPUT_TSV, 'w', encoding='utf-8') as tsv:
        tsv.write('status\tglyphs\tmeaning\tnote\tsimilar_in_my_dictionary\n')
        for status, group in (('missing', missing), ('new_writing', new_writing)):
            for record in group:
                glyphs, meaning, note = format_row(record)
                tsv.write('\t'.join((status, glyphs, meaning, note.split(' | mine: ')[0],
                                     ' ; '.join(record.get('similar', [])))) + '\n')

    with open(OUTPUT_MD, 'w', encoding='utf-8') as md:
        md.write('# Proposed additions to my_egyptian_dictionary.csv\n\n')
        md.write(f'- my dictionary: **{my_count}** parsed entries ({skipped} unparseable lines skipped)\n')
        md.write(f'- reference entries checked: **{len(missing) + len(new_writing) + len(covered) + len(untranscribable)}**\n')
        md.write(f'- already covered (same glyph sequence present): **{len(covered)}**\n')
        md.write(f'- **missing** (neither glyph sequence nor meaning present): **{len(missing)}**\n')
        md.write(f'- new writing of a meaning I already have: **{len(new_writing)}**\n')
        md.write(f'- could not be transcribed to Unicode: **{len(untranscribable)}**\n\n')
        md.write('Notes marked `~approx` contain at least one Manuel-de-Codage token that could not be '
                 'resolved; `~spelled-out` means the hieroglyphs were reconstructed uniliterally from the '
                 'transliteration because the source gave no writing. Review both before pasting.\n\n')
        for title, group in (('Missing entries', missing),
                             ('Alternative writings for meanings already in the dictionary', new_writing)):
            md.write(f'\n## {title} ({len(group)})\n\n | glyph | meaning | note | \n | ----- | ------- | ---- | \n')
            for record in sorted(group, key=lambda r: (not r['resolved'], r['translit'])):
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
    missing, new_writing, covered, untranscribable = classify(records, by_glyphs, by_gloss, transcriber)
    write_outputs(missing, new_writing, covered, untranscribable, len(entries), skipped)
    print(f'missing={len(missing)} new_writing={len(new_writing)} covered={len(covered)} '
          f'untranscribable={len(untranscribable)}', file=sys.stderr)
    print(f'wrote {OUTPUT_MD} and {OUTPUT_TSV}', file=sys.stderr)


if __name__ == '__main__':
    main()
