import os
import re

pattern_named = re.compile(r'^([0-9A-Fa-f]{4,6})\s*;')

for fname in os.listdir('.'):
    if not os.path.isfile(fname):
        continue
    if not ".txt" in fname:
        print("NO .txt in ",fname)
        continue

    with open(fname, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    modified = False
    new_lines = []

    for line in lines:
        match = pattern_named.match(line)
        if match:
            hex_code = match.group(1)
            try:
                char = chr(int(hex_code, 16))
                line = f"{char}\t{line}"
                modified = True
            except ValueError:
                pass
        new_lines.append(line)

    if modified:
        with open(fname, 'w', encoding='utf-8') as f:
            f.writelines(new_lines)