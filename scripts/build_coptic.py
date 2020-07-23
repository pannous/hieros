#!/usr/local/bin/python3

import sys
sys.path.append('/me/dev/script/python/')

from WiktionaryParser.wiktionaryimporter import search, query, all, titles
from WiktionaryParser.wiktionet import describe, word
import os
from extensions import * 

file='coptic.lemmas'
old=read_text('coptic.tsv')


coptic={"Ⲁ":"A", "Ⲃ":"B", "Ⲥ":"C", "Ⲇ":"D", "Ⲉ":"E", "Ⲋ":"F", "Ⲅ":"G","Ⲏ":"H", "Ϩ":"CH", "Ϫ":"J", "Ⲓ":"I", "Ϧ":"K","Ⲕ":"K","ⳤ":"Kr", "Ⲗ":"L", "Ⲙ":"M", "Ⲛ":"N", "Ⲟ":"O", "Ⲡ":"P", "Ϥ":"Q","Ⲣ":"R", "ⳁ":"PR", "Ϭ":"S", "Ϯ":"T","Ⲧ":"T", "Ⲩ":"U", "Ⲫ":"V", "Ⲱ":"W", "Ⲭ":"X","Ⲝ":"Xr", "Ⲯ":"Y", "Ⳉ":"Z", "Ⲍ":"Z","Ⲑ":"TH", "Ϣ":"SH", "Ⲋ":"ß", "ⲁ":"a", "ⲃ":"b", "ⲥ":"c", "ⲇ":"d", "ⲉ":"e", "ⲋ":"f", "ⲅ":"g", "ϩ":"ch","ⲏ":"h", "ϫ":"j", "ⲓ":"i", "ϧ":"k", "ⲕ":"k", "ⲗ":"l", "ⲙ":"m", "ⲛ":"n", "ⲟ":"o", "ⲡ":"p", "ϥ":"q","ⲣ":"r", "ⳁ":"pr", "ϭ":"s", "ϯ":"t","ⲧ":"t", "ⲩ":"u", "ⲫ":"v", "ⲱ":"w","ⲭ":"x", "ⲝ":"xr", "ⲯ":"y", "ⳉ":"z","ⲍ":"z", "ⲑ":"th", "ϣ":"sh", "ⲋ":"ß"," ":" ","-":"-","⸗":"⸗","ⳋ":"ⳋ","ⳃ":"SH","ç":"Ch"}

coptic2={"Ⲁ":"A", "Ⲃ":"B", "Ⲥ":"C", "Ⲇ":"D", "Ⲉ":"E", "Ⲋ":"F", "Ⲅ":"G","Ⲏ":"H", "Ϩ":"CH", "Ϫ":"J", "Ⲓ":"I", "Ϧ":"K","Ⲕ":"K","ⳤ":"Kr", "Ⲗ":"L", "Ⲙ":"M", "Ⲛ":"N", "Ⲟ":"O", "Ⲡ":"P", "Ϥ":"Q","Ⲣ":"R", "ⳁ":"PR", "Ϭ":"S", "Ϯ":"T","Ⲧ":"T", "Ⲩ":"U", "Ⲫ":"V", "Ⲱ":"W", "Ⲭ":"X","Ⲝ":"Xr", "Ⲯ":"Y", "Ⳉ":"Z", "Ⲍ":"Z","Ⲑ":"TH", "Ϣ":"SH", "Ⲋ":"ß",
 "ⲁ":"a", "ⲃ":"b", "ⲥ":"cᵍ", "ⲇ":"d", "ⲉ":"e", "ⲋ":"f", "ⲅ":"g", "ϩ":"ˤs","ⲏ":"hʳ", "ϫ":"ɗ", "ⲓ":"i", "ϧ":"k", "ⲕ":"ᵍk", "ⲗ":"ʳl", "ⲙ":"m", "ⲛ":"n", "ⲟ":"o", "ⲡ":"p", "ϥ":"q","ⲣ":"r", "ⳁ":"pr", "ϭ":"ˢt", "ϯ":"ƫ","ⲧ":"z", "ⲩ":"v", "ⲫ":"pʰ", "ⲱ":"uᵑ","ⲭ":"x", "ⲝ":"xr", "ⲯ":"y", "ⳉ":"z","ⲍ":"z", "ⲑ":"th", "ϣ":"sh", "ⲋ":"ß"," ":" ","-":"-","⸗":"⸗","ⳋ":"ⳋ","ⳃ":"SH","ç":"Ch"}


# Superscript upper upper ᴬ ᴭ ᴮᴰ ᴱᴲ ᴳ ᴴ ᴵ ᴶ ᴷ ᴸ ᴹ ᴺ ᴻ ᴼ ᴽ ᴾ ᴿ ˢ ᵀ ᵁ ᵂ ᴷᴬᴿˢᵀᴱᴺ
# Superscript upper ᵃ ᵄ ᵅ ᵆ ᵇ ᵝ ᵓᵈᵟ ᵉᵊᵌˤ ᵍ ᵞˠ ʰ ʱ ᵎᶦ ʲ ᵏ ˡ ᵐ ⁿᵑ ᵒ ᵓᵔᵕ ᵖ ʳ ʴ ʵ ʶ ˢᵋ ᵗ ᵘ ᵙ ᵚ ᵛ ʷ ˣ ʸ ˠ

def english_reading(ok):
	# ok=ok.replace("h","hʳ")
	ok=ok.replace("e","ⲉ")
	ok=ok.replace("o","ø")
	ok=ok.replace("i","í")
	ok=ok.replace("$","")
	ok=ok.replace("^","")
	return ok

def german_reading(ok):
	# ok=ok.replace("a","ᵖa")
	# ok=ok.replace("h","ᵖh")
	# ok=ok.replace("s","ˢc")
	ok=ok.replace("$","")
	ok=ok.replace("^","")
	return ok

for line in read_lines(file):
	l=line.strip()
	if l in old:
		continue 
	w=word(l)
	p="^"+"".join(list(map(lambda it: coptic[it],l)))+"$"
	p2="^"+"".join(list(map(lambda it: coptic2[it],l)))+"$"
	p=german_reading(p)
	p2=english_reading(p2)
	trans=w.translations()
	if len(trans)>0 and isinstance(trans[0],tuple):
		trans=map(lambda it: it[1], trans)
	trans=",".join(trans).strip()
	print(l,"\t",p,"\t",p2,"\t", trans)



