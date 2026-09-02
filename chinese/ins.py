import os
import re

def insert_unicode_char_in_files():
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