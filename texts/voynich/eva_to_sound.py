#!/usr/bin/env python3
"""Convert Voynich EVA text to phonetic reading based on eva_sound_mapping.tsv

Mapping: glyph shape → number → Greek alphabet position → sound value
TSV columns: number  unicode  greek  sound  EVA  notes
"""
import os
import re
import sys


message="Most frequent words by topic (using standard EVA transcription, NOT to be read phonetically): "
topics="""
Topic 0: chedy daiin shedy ol aiin chol or ar gokeedy gokedy gokain chey gokeey gokaiin shey al dar chor dal okaiin  
Topic 1: daiin chol chor thy chy shol sho cthol cthor shor shy cho dy chaiin dain gotchy otchy cheor dor they  
Topic 2: aiin ar al or okar air otaiin oteos oteey okaiin otar oteody okal cheody chdy otal am dar ykar okey  
Topic 3: okeol cheol gokeey gokeol okey cheor cheey shey sheol ckhey cheody ol cheo oteey okey gokeody okol gokeedy chey dol  
Topic 4: gokaiin al gokain gokeedy otaiin ar gokeey lkaiin chey gotaiin oteey lchedy chol chy raiin lkeey gotain chaiin otain  
Topic 5: okaiin or gokain ol gokar chol gokaiin okar okaiin goal godaiin gokol otain okain okal chdy kaiin olkain gotaiin okol gokeo  
"""

def load_mapping(tsv_path='eva_sound_mapping.tsv'):
    """Load EVA→sound mapping from TSV (columns: number unicode greek sound EVA notes)."""
    if not os.path.isabs(tsv_path):
        tsv_path = os.path.join(os.path.dirname(__file__) or '.', tsv_path)
    eva_sound = {}
    with open(tsv_path) as f:
        for line in f:
            line = line.rstrip('\n')
            if not line or line.startswith('#'):
                continue
            cols = line.split('\t')
            if len(cols) < 5:
                continue
            unicod, sound, eva = cols[1].strip(),cols[3].strip(), cols[4].strip()
            if not eva or not sound or sound == 'sound' or sound == '?':
                continue
            eva_sound[eva] = sound
    return eva_sound

EVA_SOUND = load_mapping()

# Digraph/trigraph replacements applied BEFORE single-char mapping
# These handle EVA bench+gallows combinations as unit sounds
DIGRAPHS = [
    # gallows-in-bench: cXh → X with aspiration
    ('cfh', 'th'),   # bench + f-gallows
    ('cph', 'tẖ '),   # bench + p-gallows
    ('cth', 'ɽ'),   # bench + t-gallows 󿐣 'l' hr > l
    ('ckh', 'ch'),   # bench + k-gallows
    # bench digraphs
    ('ch',  'ç'),    # chi → ç
    ('sh',  'š'),    # shin → š
    # common prefix
    ('qo',  'qa'),   # q + o(=a)
]

def convert_eva_word(word):
    """Convert a single EVA word to sound representation."""
    # First apply digraphs (longest match first, already sorted)
    for eva, sound in DIGRAPHS:
        word = word.replace(eva, sound)
    # Then map remaining single characters
    result = []
    for ch in word:
        result.append(EVA_SOUND.get(ch, ch))
    return ''.join(result)

def convert_line(line):
    """Convert EVA text line, preserving metadata prefix."""
    # Strip EVA annotations: [X:Y]→X, {X}→X, <!...>→'', <$>→'', <->→' '
    text = re.sub(r'<!.*?>', '', line)
    text = text.replace('<$>', '')
    text = text.replace('<->', ' ')
    text = re.sub(r'\[([^:\]]+):[^\]]+\]', r'\1', text)  # [X:Y] → X
    text = re.sub(r'\{([^}]*)\}', r'\1', text)            # {X} → X
    text = re.sub(r'<[^>]*>', '', text)                     # remaining tags
    text = re.sub(r'@\d+;', '', text)                       # @NNN; codes

    # Split on tab: prefix\teva_text
    parts = text.split('\t', 1)
    if len(parts) < 2:
        return line.rstrip()

    prefix, eva_text = parts[0], parts[1]
    # Words separated by dots, commas are minor breaks
    eva_text = eva_text.replace(',', '.')
    words = eva_text.split('.')
    return convert_words(prefix,words)

def convert_words(prefix,words):
    converted = ' '.join(convert_eva_word(w) for w in words if w.strip())
    return f"{prefix}\t{converted}"

def main():
    infile = sys.argv[1] if len(sys.argv) > 1 else 'voynich.eva.pure.txt'
    outfile = sys.argv[2] if len(sys.argv) > 2 else 'voynich.sound.txt'

    with open(infile) as f:
        lines = f.readlines()

    results = []
    for line in lines:
        line = line.rstrip('\n')
        if not line or line.startswith('#'):
            continue
        results.append(convert_line(line))

    with open(outfile, 'w') as f:
        f.write('\n'.join(results) + '\n')

    # Show first 30 lines as preview
    for r in results[:30]:
        print(r)
    print(f"\n--- wrote {len(results)} lines to {outfile} ---")

    print(convert_words(message, topics.split(" ")))

if __name__ == '__main__':
    main()
