#!/usr/bin/env python3
import re

eva_to_unicode={'a':'󿐐', 'b':'󿐈', 'c':'󿐌', 'd':'󿐉', 'e':'󿐆', 'f':'󿐠', 'g':'󿐋', 'h':'󿐏', 'i':'󿐀', 'j':'󿐂', 'k':'󿐢', 'l':'󿐚', 'm':'󿐄', 'n':'󿐁', 'o':'󿐔', 'p':'󿐡', 'q':'󿐝', 'r':'󿐃', 's':'󿐊', 't':'󿐣', 'u':'󿐑', 'v':'󿐛', 'x':'󿐜', 'y':'󿐗', 'z':'󿐞', 'A':'󿐒', 'E':'󿐇', 'F':'󿐨', 'H':'󿐎', 'I':'󿐅', 'K':'󿐪', 'O':'󿐕', 'P':'󿐩', 'S':'󿐍', 'T':'󿐫', 'Y':'󿐘', '"':'󿐱', '\'':'󿐰', '*':'󿐿', '+':'󿐲','.':' '}
# 󿐌󿐏 󿐌 󿐏 ch ligature
# lines=open("voynich_v101.txt").readlines()

import sys
args=" ".join(sys.argv[1:])
line=args
result=""
for char in line:
	if char in eva_to_unicode:
		print(eva_to_unicode[char], end='')
		result+=eva_to_unicode[char]
	else: 
		print(char, end='')
		result+=char

# copy to clipboard
import pyperclip
pyperclip.copy(result)