#!/usr/bin/env python3
#!/opt/homebrew/bin/python3

import sys

for line in open("chinese.freq.tsv").readlines():
    cols = line.rstrip("\n").split("\t")
    if len(cols) < 6:
        print(line, end="")
        continue

    sixth = cols[5]
    part = sixth.split(":", 1)[0]

    # insert as new column #3 (index 2)
    cols.insert(2, part)

    sys.stdout.write("\t".join(cols) + "\n")