#!/usr/local/bin/python3
dic={
'ʿ':'𐩲',
'(':'𐩲',
'ʾ':'𐩱',
')':'𐩱',
'B':'𐩨',
'G':'𐩴',
'D':'𐩵',
'Ḏ':'𐩹',
'Ḍ':'𐩳',
'F':'𐩰',
'G':'𐩴',
'Ġ':'𐩶',
'Ḥ':'𐩢',
'H':'𐩠',
'Y':'𐩺',
'Ḫ':'𐩭',
'K':'𐩫',
'L':'𐩡',
'M':'𐩣',
'N':'𐩬',
'ʿ':'𐩲',
'B':'𐩨',
'Q':'𐩤',
'R':'𐩧',
'S':'𐩦',
'S':'𐩪',
'Ṣ':'𐩮',
'S':'𐩯',
'Ṭ':'𐩷',
'Ḍ':'𐩳',
'Ṯ':'𐩻',
'Ẓ':'𐩼',
'W':'𐩥',
'T':'𐩩',
'Y':'𐩺',
'Z':'𐩸',
}


import sys
argc=len(sys.argv)
if argc<2:
	print('Usage: python3 Sabaean.py text')
	# exit()
	word="LBB" 
else:
	word=sys.argv[1]

for c in word:
	print(dic[c],end='')
print()