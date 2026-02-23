#!/usr/bin/env python3
"""Convert Voynich EVA text to phonetic reading based on number_abc_mapping.txt

Mapping: glyph shape → number → Greek alphabet position → sound value
  1→α:a  2→β:b  3→γ:c  4→δ:d  5→ε:e  6→ζ:f  7→η:g  8→ι:h
  9→θ:i  11→λ:l  15→ω:o  16→χ:ch
"""
import re
import sys

# Single-char EVA → sound (order matters for digraph pre-processing)
EVA_SOUND = {
    'o': 'a',   # 1 α
    'a': 'a',   # 1 α variant
    'l': 'b',   # 2 β
    'd': 'c',   # 3 γ
    'r': 'd',   # 4 δ
    'v': 'e',   # 5 ε
    'x': 'f',   # 6 ζ
    'k': 'g',   # 7 η
    'm': 'h',   # 8 ι
    'p': 'i',   # 9 θ (p-form)
    'f': 'i',   # 9 θ (f-form)
    't': 'l',   # 11 λ
    'y': 'o',   # 15 ω
    'g': 'c',   # 3 γ suffix form
    's': 's',   # uncertain (maybe μ=m)
    'e': 'e',   # connector/vowel
    'i': 'i',   # stroke
    'n': 'n',   # stroke
    'j': 'j',   # sub-component
    'q': 'q',   # prefix
    'c': 'c',   # 16 χ (left bench)
    'h': 'h',   # right bench
    'b': 'b',
    'u': 'u',
    'z': 'z',
}

# Digraph/trigraph replacements applied BEFORE single-char mapping
# These handle EVA bench+gallows combinations as unit sounds
DIGRAPHS = [
    # gallows-in-bench: cXh → X with aspiration
    ('cfh', 'çi'),   # bench + f-gallows → aspirated i
    ('cph', 'çi'),   # bench + p-gallows → aspirated i
    ('cth', 'çl'),   # bench + t-gallows → aspirated l
    ('ckh', 'çg'),   # bench + k-gallows → aspirated g
    # bench digraphs
    ('ch',  'ç'),    # chi → ç (voiceless velar/palatal)
    ('sh',  'š'),    # shin → š (voiceless sibilant)
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

if __name__ == '__main__':
    main()
