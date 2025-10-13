#!/usr/bin/env python3
import re

eva_to_unicode={'a':'󿐐', 'b':'󿐈', 'c':'󿐌', 'd':'󿐉', 'e':'󿐆', 'f':'󿐠', 'g':'󿐋', 'h':'󿐏', 'i':'󿐀', 'j':'󿐂', 'k':'󿐢', 'l':'󿐚', 'm':'󿐄', 'n':'󿐁', 'o':'󿐔', 'p':'󿐡', 'q':'󿐝', 'r':'󿐃', 's':'󿐊', 't':'󿐣', 'u':'󿐑', 'v':'󿐛', 'x':'󿐜', 'y':'󿐗', 'z':'󿐞', 'A':'󿐒', 'E':'󿐇', 'F':'󿐨', 'H':'󿐎', 'I':'󿐅', 'K':'󿐪', 'O':'󿐕', 'P':'󿐩', 'S':'󿐍', 'T':'󿐫', 'Y':'󿐘', '"':'󿐱', '\'':'󿐰', '*':'󿐿', '+':'󿐲','.':' '}

# lines=open("voynich_v101.txt").readlines()
lines=open("voynich_eva.txt").readlines()
for line in lines:
	if line[0]=="#":
		print(line, end='')
		continue
	head=re.sub(">.*","",line)
	l=re.sub("<.*?>\s*","",line)

	print(head.strip(), end='> ')

	for char in l:
		if char in eva_to_unicode:
			print(eva_to_unicode[char], end='')
		else: 
			print(char, end='')


