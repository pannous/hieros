import os
import re

def insert_unicode_char_in_files():
    import os
    import re

    pattern_uplus = re.compile(r'^(U\+([0-9A-Fa-f]{4,6}))\b')
    pattern_semicolon = re.compile(r'^([0-9A-Fa-f]{4,6});\s*([0-9A-Fa-f]{4,6})\b')

    for fname in os.listdir('.'):
        if not ".txt" in fname:
            print("NO .txt in ",fname)
            continue
        if not os.path.isfile(fname):
            continue

        with open(fname, 'r', encoding='utf-8') as f:
            lines = f.readlines()

        modified = False
        new_lines = []

        for line in lines:
            match_uplus = pattern_uplus.match(line)
            match_semi = pattern_semicolon.match(line)
            if match_uplus:
                full_code, hex_part = match_uplus.groups()
                try:
                    char = chr(int(hex_part, 16))
                    line = line.replace(full_code, f"{full_code} {char}", 1)
                    modified = True
                except ValueError:
                    pass
            elif match_semi:
                h1, h2 = match_semi.groups()
                try:
                    c1 = chr(int(h1, 16))
                    c2 = chr(int(h2, 16))
                    line = line.replace(f"{h1}; {h2}", f"{c1}\t{c2}", 1)
                    modified = True
                except ValueError:
                    pass
            new_lines.append(line)

        if modified:
            with open(fname, 'w', encoding='utf-8') as f:
                f.writelines(new_lines)

def insert_unicode_char_in_files2():
    pattern = re.compile(r'^(U\+([0-9A-Fa-f]{4,6}))\b')
    
    for fname in os.listdir('.'):
        if not os.path.isfile(fname):
            continue

        with open(fname, 'r', encoding='utf-8') as f:
            lines = f.readlines()

        modified = False
        new_lines = []

        for line in lines:
            match = pattern.match(line)
            if match:
                full_code, hex_part = match.groups()
                try:
                    char = chr(int(hex_part, 16))
                    line = line.replace(full_code, f"{full_code} {char}", 1)
                    modified = True
                except ValueError:
                    pass
            new_lines.append(line)

        if modified:
            with open(fname, 'w', encoding='utf-8') as f:
                f.writelines(new_lines)

if __name__ == "__main__":
    insert_unicode_char_in_files()